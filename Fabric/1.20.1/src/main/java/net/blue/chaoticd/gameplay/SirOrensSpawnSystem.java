package net.blue.chaoticd.gameplay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.ModEntities;
import net.blue.chaoticd.content.entity.SirOrensEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Owns the one-time, server-authoritative arrival of Sir. Orens.
 *
 * <p>The entity is marked with persistent ownership tags, while player
 * progress and the selected home bed are stored in {@link SavedData}; both
 * therefore survive server restarts.</p>
 *
 * <p>A player first becomes eligible after entering Aurora.  If their respawn
 * bed is already inside a small enclosed house, Sir. Orens is placed beside it
 * immediately.  A later bed placement is observed through Fabric's common
 * use-block callback and rechecked on the following server tick, so building a
 * home after visiting Aurora works without relogging or sleeping first.</p>
 */
public final class SirOrensSpawnSystem {
    public static final ResourceKey<Level> AURORA_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "aurora_dimension")
    );

    /** Translation key intentionally lives in the language files, not here. */
    public static final String SPAWN_MESSAGE_KEY = "message.chaoticd.sir_orens_spawned";

    private static final String DATA_ID = "chaoticd_sir_orens";
    private static final String SIR_ORENS_TAG = "chaoticd:sir_orens";
    private static final String OWNER_TAG_PREFIX = "chaoticd:sir_orens_owner:";

    private static final int PLACEMENT_RECHECK_DELAY_TICKS = 1;
    private static final int PENDING_HOME_RECHECK_TICKS = 20;
    private static final int MAINTENANCE_INTERVAL_TICKS = 100;
    private static final int HOME_RADIUS = 5;
    private static final int BED_SEARCH_RADIUS = 3;

    /**
     * Temporary placement hints. The actual candidate is persisted in
     * {@link SirOrensData}; this map only lets the placement itself be tested
     * after vanilla has completed it.
     */
    private static final Map<UUID, BedPlacementHint> BED_PLACEMENT_HINTS = new HashMap<>();

    private SirOrensSpawnSystem() {
    }

    /** Registers all server-side arrival, placement and maintenance hooks. */
    public static void initialize() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
            SirOrensSpawnSystem::afterPlayerChangesWorld
        );
        UseBlockCallback.EVENT.register(SirOrensSpawnSystem::observePotentialBedPlacement);
        ServerTickEvents.END_SERVER_TICK.register(SirOrensSpawnSystem::tickServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> BED_PLACEMENT_HINTS.clear());
    }

    /**
     * Creates an owner-bound Sir. Orens beside a player for development and
     * balance testing. This intentionally does not mark the player's real
     * Aurora-home arrival as complete, so the normal one-time home spawn still
     * works independently when it is earned in-game.
     */
    public static boolean summonForTesting(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos spawnPos = findTestSpawnPosition(level, player.blockPosition());

        if (spawnPos == null) {
            return false;
        }

        SirOrensEntity villager = ModEntities.SIR_ORENS.create(level);

        if (villager == null) {
            return false;
        }

        villager.moveTo(
            spawnPos.getX() + 0.5D,
            spawnPos.getY(),
            spawnPos.getZ() + 0.5D,
            player.getYRot(),
            0.0F
        );
        configureSirOrens(villager, spawnPos, player.getUUID());
        villager.addTag("chaoticd:sir_orens_test");

        return level.addFreshEntity(villager);
    }

    private static void afterPlayerChangesWorld(
        ServerPlayer player,
        ServerLevel origin,
        ServerLevel destination
    ) {
        if (destination.dimension().equals(AURORA_DIMENSION)) {
            markAuroraVisit(player);
        }
    }

    /**
     * Observes a bed item before vanilla places it. Returning PASS leaves the
     * normal placement completely unchanged; its result is inspected next tick.
     */
    private static InteractionResult observePotentialBedPlacement(
        net.minecraft.world.entity.player.Player player,
        Level level,
        net.minecraft.world.InteractionHand hand,
        BlockHitResult hit
    ) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack held = serverPlayer.getItemInHand(hand);

        if (!(held.getItem() instanceof BlockItem blockItem)
            || !(blockItem.getBlock() instanceof BedBlock)) {
            return InteractionResult.PASS;
        }

        MinecraftServer server = serverPlayer.server;
        SirOrensData data = data(server);
        /*
         * Keep a bed placed before the Aurora visit as well. This makes an
         * already-built home usable on the very first arrival even when the
         * player has not slept in that bed yet.
         */
        OwnerRecord record = data.getOrCreate(serverPlayer.getUUID());

        if (record.spawned) {
            return InteractionResult.PASS;
        }

        BlockPos likelyPlacement = hit.getBlockPos().relative(hit.getDirection());
        BED_PLACEMENT_HINTS.put(
            serverPlayer.getUUID(),
            new BedPlacementHint(
                level.dimension(),
                likelyPlacement,
                server.overworld().getGameTime() + PLACEMENT_RECHECK_DELAY_TICKS
            )
        );

        return InteractionResult.PASS;
    }

    private static void tickServer(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        SirOrensData data = data(server);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.serverLevel().dimension().equals(AURORA_DIMENSION)) {
                OwnerRecord arrivalRecord = data.getOrCreate(player.getUUID());

                /*
                 * The world-change event is the normal immediate path. This
                 * fallback only covers a player already in Aurora while a
                 * server/mod is reloaded; it must not retry home inspection
                 * every tick while that player remains in the dimension.
                 */
                if (!arrivalRecord.auroraVisited) {
                    markAuroraVisit(player);
                }
            }

            OwnerRecord record = data.get(player.getUUID());

            if (record == null || record.spawned) {
                continue;
            }

            BedPlacementHint hint = BED_PLACEMENT_HINTS.get(player.getUUID());

            if (hint != null && gameTime >= hint.recheckAt()) {
                BED_PLACEMENT_HINTS.remove(player.getUUID());
                rememberBedNear(data, record, server, hint.dimension(), hint.position());
            }

            if (record.auroraVisited
                && (hint != null || gameTime % PENDING_HOME_RECHECK_TICKS == 0L)) {
                trySpawnAtHome(player, data, record, false);
            }
        }

        if (gameTime % MAINTENANCE_INTERVAL_TICKS == 0L) {
            maintainSpawnedVillagers(server, data);
        }
    }

    private static void markAuroraVisit(ServerPlayer player) {
        SirOrensData data = data(player.server);
        OwnerRecord record = data.getOrCreate(player.getUUID());

        boolean firstAuroraVisit = !record.auroraVisited;

        if (firstAuroraVisit) {
            record.auroraVisited = true;
            data.setDirty();
        }

        if (firstAuroraVisit && !record.spawned) {
            /* The first Aurora arrival is the one permitted eager house
             * inspection. Later periodic checks never force-load the home. */
            trySpawnAtHome(player, data, record, true);
        }
    }

    /**
     * Finds a viable home in priority order: a recent placed bed, then the
     * player's actual respawn bed.  A remembered in-progress house remains a
     * candidate so finishing its walls/roof later needs no new bed placement.
     */
    private static void trySpawnAtHome(
        ServerPlayer player,
        SirOrensData data,
        OwnerRecord record,
        boolean allowChunkLoad
    ) {
        if (record.spawned) {
            return;
        }

        HomeLocation home = findCandidateHome(
            player.server,
            player,
            data,
            record,
            allowChunkLoad
        );

        if (home == null) {
            return;
        }

        ServerLevel homeLevel = player.server.getLevel(home.dimension());

        if (homeLevel == null || !isSuitableHouseBed(homeLevel, home.bedPos())) {
            return;
        }

        BlockPos spawnPos = findSpawnPosition(homeLevel, home.bedPos());

        if (spawnPos == null) {
            return;
        }

        SirOrensEntity villager = createSirOrens(homeLevel, spawnPos, home.bedPos(), player.getUUID());

        if (villager == null) {
            return;
        }

        record.spawned = true;
        record.home = home;
        record.villagerUuid = villager.getUUID();
        data.setDirty();

        player.sendSystemMessage(
            Component.translatable(SPAWN_MESSAGE_KEY).withStyle(ChatFormatting.BLUE)
        );
    }

    @Nullable
    private static HomeLocation findCandidateHome(
        MinecraftServer server,
        ServerPlayer player,
        SirOrensData data,
        OwnerRecord record,
        boolean allowChunkLoad
    ) {
        if (record.home != null) {
            ServerLevel homeLevel = server.getLevel(record.home.dimension());

            if (homeLevel != null) {
                if (allowChunkLoad) {
                    loadHomeArea(homeLevel, record.home.bedPos());
                }

                if (!hasLoadedChunk(homeLevel, record.home.bedPos())) {
                    return null;
                }

                if (isBed(homeLevel, record.home.bedPos())) {
                    return record.home;
                }

                record.home = null;
                data.setDirty();
            }
        }

        BlockPos respawn = player.getRespawnPosition();

        if (respawn == null) {
            return null;
        }

        ServerLevel respawnLevel = server.getLevel(player.getRespawnDimension());

        if (respawnLevel == null) {
            return null;
        }

        if (allowChunkLoad) {
            loadHomeArea(respawnLevel, respawn);
        }

        if (!hasLoadedChunk(respawnLevel, respawn)) {
            return null;
        }

        if (!isBed(respawnLevel, respawn)) {
            return null;
        }

        HomeLocation home = new HomeLocation(respawnLevel.dimension(), respawn);
        record.home = home;
        data.setDirty();
        return home;
    }

    private static void rememberBedNear(
        SirOrensData data,
        OwnerRecord record,
        MinecraftServer server,
        ResourceKey<Level> dimension,
        BlockPos center
    ) {
        ServerLevel level = server.getLevel(dimension);

        if (level == null) {
            return;
        }

        BlockPos bed = findBedNear(level, center);

        if (bed != null) {
            record.home = new HomeLocation(dimension, bed);
            data.setDirty();
        }
    }

    @Nullable
    private static BlockPos findBedNear(ServerLevel level, BlockPos center) {
        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int xOffset = -BED_SEARCH_RADIUS; xOffset <= BED_SEARCH_RADIUS; xOffset++) {
                for (int zOffset = -BED_SEARCH_RADIUS; zOffset <= BED_SEARCH_RADIUS; zOffset++) {
                    BlockPos candidate = center.offset(xOffset, yOffset, zOffset);

                    if (hasLoadedChunk(level, candidate) && isBed(level, candidate)) {
                        return canonicalBedFoot(level, candidate);
                    }
                }
            }
        }

        return null;
    }

    private static boolean isSuitableHouseBed(ServerLevel level, BlockPos bedPos) {
        if (!hasLoadedChunk(level, bedPos) || !isBed(level, bedPos)) {
            return false;
        }

        BlockPos foot = canonicalBedFoot(level, bedPos);
        BlockState footState = level.getBlockState(foot);
        Direction facing = footState.getValue(BedBlock.FACING);
        BlockPos head = foot.relative(facing);

        if (!isBed(level, head)
            || !isSolid(level, foot.below())
            || !isSolid(level, head.below())) {
            return false;
        }

        /*
         * The roof and wall checks purposefully accept normal solid building
         * materials (including glass) while rejecting a bed merely dropped in
         * the open. They do not require one exact house shape.
         */
        if (!hasRoof(level, foot) || !hasRoof(level, head)) {
            return false;
        }

        int walls = 0;

        /*
         * Accept compact 3×3 homes as well as ordinary 4×4/5×5 rooms. The
         * nearby solid count is deliberately paired with the roof check
         * above, so an exposed bed beside a few blocks cannot qualify.
         */
        for (int xOffset = -2; xOffset <= 2; xOffset++) {
            for (int zOffset = -2; zOffset <= 2; zOffset++) {
                if (xOffset == 0 && zOffset == 0) {
                    continue;
                }

                for (int yOffset = 1; yOffset <= 2; yOffset++) {
                    if (isSolid(level, foot.offset(xOffset, yOffset, zOffset))) {
                        walls++;
                    }
                }
            }
        }

        return walls >= 4;
    }

    /**
     * This is only used for the initial Aurora-arrival check. Periodic checks
     * and ordinary ticks must never force-load a distant player home.
     */
    private static void loadHomeArea(ServerLevel level, BlockPos bedPos) {
        level.getChunkAt(bedPos.offset(-3, 0, -3));
        level.getChunkAt(bedPos.offset(3, 0, -3));
        level.getChunkAt(bedPos.offset(-3, 0, 3));
        level.getChunkAt(bedPos.offset(3, 0, 3));
    }

    private static boolean hasRoof(ServerLevel level, BlockPos bedPart) {
        for (int yOffset = 3; yOffset <= 4; yOffset++) {
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    if (isSolid(level, bedPart.offset(xOffset, yOffset, zOffset))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos bedPos) {
        BlockPos foot = canonicalBedFoot(level, bedPos);

        for (int radius = 1; radius <= 3; radius++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (Math.abs(xOffset) != radius && Math.abs(zOffset) != radius) {
                        continue;
                    }

                    BlockPos candidate = foot.offset(xOffset, 0, zOffset);

                    if (isSafeStandingSpace(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isSafeStandingSpace(ServerLevel level, BlockPos pos) {
        return hasLoadedChunk(level, pos)
            && isSolid(level, pos.below())
            && isEmptyCollision(level, pos)
            && isEmptyCollision(level, pos.above());
    }

    @Nullable
    private static BlockPos findTestSpawnPosition(ServerLevel level, BlockPos playerPos) {
        for (int radius = 2; radius <= 5; radius++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (Math.abs(xOffset) != radius && Math.abs(zOffset) != radius) {
                        continue;
                    }

                    for (int yOffset = 2; yOffset >= -3; yOffset--) {
                        BlockPos candidate = playerPos.offset(xOffset, yOffset, zOffset);

                        if (isSafeStandingSpace(level, candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isSolid(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    /**
     * Reads the server chunk cache directly, without the deprecated
     * {@code LevelReader.hasChunkAt} convenience methods and without loading
     * a chunk just to inspect a possible home.
     */
    private static boolean hasLoadedChunk(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static boolean isEmptyCollision(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isBed(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BedBlock;
    }

    private static BlockPos canonicalBedFoot(ServerLevel level, BlockPos bedPart) {
        BlockState state = level.getBlockState(bedPart);

        if (!(state.getBlock() instanceof BedBlock)) {
            return bedPart;
        }

        Direction facing = state.getValue(BedBlock.FACING);
        BedPart part = state.getValue(BedBlock.PART);
        return part == BedPart.HEAD ? bedPart.relative(facing.getOpposite()) : bedPart;
    }

    @Nullable
    private static SirOrensEntity createSirOrens(
        ServerLevel level,
        BlockPos spawnPos,
        BlockPos bedPos,
        UUID owner
    ) {
        SirOrensEntity villager = ModEntities.SIR_ORENS.create(level);

        if (villager == null) {
            return null;
        }

        villager.moveTo(
            spawnPos.getX() + 0.5D,
            spawnPos.getY(),
            spawnPos.getZ() + 0.5D,
            0.0F,
            0.0F
        );
        configureSirOrens(villager, bedPos, owner);

        if (!level.addFreshEntity(villager)) {
            return null;
        }

        return villager;
    }

    private static void configureSirOrens(SirOrensEntity villager, BlockPos bedPos, UUID owner) {
        villager.configureForHome(owner);
        villager.restrictTo(bedPos, HOME_RADIUS);
        villager.addTag(SIR_ORENS_TAG);
        villager.addTag(ownerTag(owner));
    }

    /** Keeps the unique villager anchored and repairs it if an external command removes it. */
    private static void maintainSpawnedVillagers(MinecraftServer server, SirOrensData data) {
        for (Map.Entry<UUID, OwnerRecord> entry : data.records().entrySet()) {
            UUID owner = entry.getKey();
            OwnerRecord record = entry.getValue();

            if (!record.spawned || record.home == null) {
                continue;
            }

            ServerLevel homeLevel = server.getLevel(record.home.dimension());

            if (homeLevel == null
                || !hasLoadedChunk(homeLevel, record.home.bedPos())
                || !isSuitableHouseBed(homeLevel, record.home.bedPos())) {
                continue;
            }

            SirOrensEntity villager = findRegisteredVillager(server, record.villagerUuid, owner);

            if (villager == null) {
                BlockPos replacementPos = findSpawnPosition(homeLevel, record.home.bedPos());

                if (replacementPos == null) {
                    continue;
                }

                villager = createSirOrens(homeLevel, replacementPos, record.home.bedPos(), owner);

                if (villager == null) {
                    continue;
                }

                record.villagerUuid = villager.getUUID();
                data.setDirty();
            } else {
                configureSirOrens(villager, record.home.bedPos(), owner);
            }
        }
    }

    @Nullable
    private static SirOrensEntity findRegisteredVillager(
        MinecraftServer server,
        @Nullable UUID villagerUuid,
        UUID owner
    ) {
        if (villagerUuid == null) {
            return null;
        }

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(villagerUuid);

            if (entity instanceof SirOrensEntity villager && isSirOrens(villager, owner)) {
                return villager;
            }
        }

        return null;
    }

    private static boolean isSirOrens(SirOrensEntity villager, UUID owner) {
        return villager.isOwnedBy(owner)
            && villager.getTags().contains(SIR_ORENS_TAG)
            && villager.getTags().contains(ownerTag(owner));
    }

    private static String ownerTag(UUID owner) {
        return OWNER_TAG_PREFIX + owner;
    }

    private static SirOrensData data(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            SirOrensData::load,
            SirOrensData::new,
            DATA_ID
        );
    }

    private record BedPlacementHint(
        ResourceKey<Level> dimension,
        BlockPos position,
        long recheckAt
    ) {
    }

    private record HomeLocation(ResourceKey<Level> dimension, BlockPos bedPos) {
    }

    private static final class OwnerRecord {
        private boolean auroraVisited;
        private boolean spawned;
        @Nullable
        private HomeLocation home;
        @Nullable
        private UUID villagerUuid;
    }

    /** Persistent per-player state; kept in the Overworld data store. */
    private static final class SirOrensData extends SavedData {
        private final Map<UUID, OwnerRecord> records = new HashMap<>();

        private static SirOrensData load(CompoundTag tag) {
            SirOrensData data = new SirOrensData();
            ListTag serializedRecords = tag.getList("Owners", Tag.TAG_COMPOUND);

            for (int index = 0; index < serializedRecords.size(); index++) {
                CompoundTag entry = serializedRecords.getCompound(index);

                if (!entry.hasUUID("Owner")) {
                    continue;
                }

                OwnerRecord record = new OwnerRecord();
                record.auroraVisited = entry.getBoolean("AuroraVisited");
                record.spawned = entry.getBoolean("Spawned");

                if (entry.contains("HomeDimension", Tag.TAG_STRING)
                    && entry.contains("HomeBed", Tag.TAG_LONG)) {
                    ResourceKey<Level> dimension = decodeDimension(entry.getString("HomeDimension"));

                    if (dimension != null) {
                        record.home = new HomeLocation(dimension, BlockPos.of(entry.getLong("HomeBed")));
                    }
                }

                if (entry.hasUUID("Villager")) {
                    record.villagerUuid = entry.getUUID("Villager");
                }

                data.records.put(entry.getUUID("Owner"), record);
            }

            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag serializedRecords = new ListTag();

            for (Map.Entry<UUID, OwnerRecord> entry : records.entrySet()) {
                OwnerRecord record = entry.getValue();
                CompoundTag serialized = new CompoundTag();
                serialized.putUUID("Owner", entry.getKey());
                serialized.putBoolean("AuroraVisited", record.auroraVisited);
                serialized.putBoolean("Spawned", record.spawned);

                if (record.home != null) {
                    serialized.putString(
                        "HomeDimension",
                        record.home.dimension().location().toString()
                    );
                    serialized.putLong("HomeBed", record.home.bedPos().asLong());
                }

                if (record.villagerUuid != null) {
                    serialized.putUUID("Villager", record.villagerUuid);
                }

                serializedRecords.add(serialized);
            }

            tag.put("Owners", serializedRecords);
            return tag;
        }

        @Nullable
        private OwnerRecord get(UUID owner) {
            return records.get(owner);
        }

        private OwnerRecord getOrCreate(UUID owner) {
            OwnerRecord existing = records.get(owner);

            if (existing != null) {
                return existing;
            }

            OwnerRecord created = new OwnerRecord();
            records.put(owner, created);
            setDirty();
            return created;
        }

        private Map<UUID, OwnerRecord> records() {
            return records;
        }
    }

    @Nullable
    private static ResourceKey<Level> decodeDimension(String serialized) {
        try {
            return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(serialized));
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
