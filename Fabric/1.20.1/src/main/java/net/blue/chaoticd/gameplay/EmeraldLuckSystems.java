package net.blue.chaoticd.gameplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.blue.chaoticd.content.ModEnchantments;
import net.blue.chaoticd.content.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Implements the world interactions granted by the hidden Luck enchantment.
 */
public final class EmeraldLuckSystems {
    private static final String OWNER_PREFIX =
        "chaoticd_emerald_owner_";

    private static final int EFFECT_REFRESH_TICKS = 20;
    private static final int VILLAGER_GIFT_INTERVAL_TICKS = 600;
    private static final double VILLAGER_GIFT_RADIUS = 18.0D;
    private static final double GOLEM_COMMAND_RADIUS = 28.0D;
    private static final double GOLEM_FOLLOW_RADIUS = 16.0D;
    private static final double GOLEM_STOP_FOLLOW_RADIUS = 20.0D;

    private static final Map<MerchantOffer, Integer>
        ORIGINAL_SPECIAL_PRICES = new WeakHashMap<>();

    private static final List<Item> VILLAGER_GIFTS = List.of(
        Items.BREAD,
        Items.COOKIE,
        Items.PUMPKIN_PIE,
        Items.COOKED_BEEF,
        Items.GOLDEN_CARROT,
        Items.EXPERIENCE_BOTTLE,
        Items.IRON_INGOT,
        Items.GOLD_INGOT,
        Items.EMERALD,
        Items.DIAMOND
    );

    private EmeraldLuckSystems() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register(
            EmeraldLuckSystems::afterBlockBreak
        );

        UseEntityCallback.EVENT.register(
            EmeraldLuckSystems::useEntity
        );

        AttackEntityCallback.EVENT.register(
            EmeraldLuckSystems::attackEntity
        );

        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
            EmeraldLuckSystems::allowDamage
        );

        ServerTickEvents.END_WORLD_TICK.register(
            EmeraldLuckSystems::tickWorld
        );
    }

    private static void afterBlockBreak(
        Level level,
        net.minecraft.world.entity.player.Player player,
        BlockPos pos,
        BlockState state,
        net.minecraft.world.level.block.entity.BlockEntity blockEntity
    ) {
        if (level.isClientSide
            || !(player instanceof ServerPlayer serverPlayer)
            || !hasLuck(serverPlayer)
            || !player.hasCorrectToolForDrops(state)
            || !isOre(state)) {
            return;
        }

        RandomSource random =
            serverPlayer.getRandom();

        Block.popResource(
            level,
            pos,
            new ItemStack(
                Items.EMERALD,
                1 + random.nextInt(5)
            )
        );

        Block.popResource(
            level,
            pos,
            new ItemStack(
                Items.DIAMOND,
                1 + random.nextInt(5)
            )
        );
    }

    private static InteractionResult useEntity(
        net.minecraft.world.entity.player.Player player,
        Level level,
        net.minecraft.world.InteractionHand hand,
        Entity entity,
        net.minecraft.world.phys.EntityHitResult hitResult
    ) {
        if (entity instanceof Villager villager) {
            if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer) {
                updateVillagerPrices(
                    villager,
                    hasLuck(serverPlayer)
                );
            }

            return InteractionResult.PASS;
        }

        if (!(entity instanceof IronGolem golem)) {
            return InteractionResult.PASS;
        }

        ItemStack held =
            player.getItemInHand(hand);

        boolean canBind =
            hasFullEmeraldArmor(player)
                && held.is(ItemTags.FLOWERS);

        boolean canRide =
            held.isEmpty()
                && hasLuck(player);

        if (!canBind && !canRide) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (canBind) {
            setOwner(golem, player.getUUID());

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    golem.getX(),
                    golem.getY() + 1.8D,
                    golem.getZ(),
                    12,
                    0.45D,
                    0.55D,
                    0.45D,
                    0.03D
                );
            }

            return InteractionResult.CONSUME;
        }

        player.startRiding(golem, true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult attackEntity(
        net.minecraft.world.entity.player.Player player,
        Level level,
        net.minecraft.world.InteractionHand hand,
        Entity entity,
        net.minecraft.world.phys.EntityHitResult hitResult
    ) {
        if (level.isClientSide
            || !(player instanceof ServerPlayer serverPlayer)
            || !(entity instanceof LivingEntity target)
            || !hasLuck(serverPlayer)) {
            return InteractionResult.PASS;
        }

        AABB search =
            serverPlayer.getBoundingBox().inflate(
                GOLEM_COMMAND_RADIUS
            );

        for (
            IronGolem golem :
                level.getEntitiesOfClass(
                    IronGolem.class,
                    search,
                    candidate ->
                        isOwner(
                            candidate,
                            serverPlayer.getUUID()
                        )
                )
        ) {
            golem.setTarget(target);
        }

        return InteractionResult.PASS;
    }

    private static boolean allowDamage(
        LivingEntity entity,
        net.minecraft.world.damagesource.DamageSource source,
        float amount
    ) {
        if (!(entity instanceof ServerPlayer player)
            || !hasLuck(player)) {
            return true;
        }

        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();

        return !(attacker instanceof IronGolem)
            && !(direct instanceof IronGolem);
    }

    private static void tickWorld(ServerLevel level) {
        if (level.getGameTime() % EFFECT_REFRESH_TICKS == 0L) {
            for (ServerPlayer player : level.players()) {
                refreshEmeraldBootEffects(player);
            }

            tickOwnedGolems(level);
        }

        if (
            level.getGameTime()
                % VILLAGER_GIFT_INTERVAL_TICKS
                == 0L
        ) {
            giveVillagerGifts(level);
        }
    }

    private static void refreshEmeraldBootEffects(
        ServerPlayer player
    ) {
        ItemStack boots =
            player.getItemBySlot(EquipmentSlot.FEET);

        if (!boots.is(ModItems.EMERALD_BOOTS)
            || EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.LUCK,
                boots
            ) <= 0) {
            return;
        }

        player.addEffect(
            new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                40,
                0,
                true,
                false,
                true
            )
        );

        player.addEffect(
            new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.JUMP,
                40,
                0,
                true,
                false,
                true
            )
        );
    }

    private static void giveVillagerGifts(
        ServerLevel level
    ) {
        RandomSource random = level.getRandom();

        for (ServerPlayer player : level.players()) {
            if (!hasLuck(player)) {
                continue;
            }

            AABB area =
                player.getBoundingBox().inflate(
                    VILLAGER_GIFT_RADIUS
                );

            List<Villager> villagers =
                level.getEntitiesOfClass(
                    Villager.class,
                    area,
                    Villager::isAlive
                );

            for (Villager villager : villagers) {
                Item gift =
                    VILLAGER_GIFTS.get(
                        random.nextInt(
                            VILLAGER_GIFTS.size()
                        )
                    );

                int count =
                    gift == Items.DIAMOND
                        ? 1
                        : 1 + random.nextInt(3);

                villager.spawnAtLocation(
                    new ItemStack(gift, count)
                );

                level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    villager.getX(),
                    villager.getY() + 1.4D,
                    villager.getZ(),
                    5,
                    0.25D,
                    0.35D,
                    0.25D,
                    0.0D
                );
            }
        }
    }

    private static void updateVillagerPrices(
        Villager villager,
        boolean lucky
    ) {
        for (MerchantOffer offer : villager.getOffers()) {
            if (!offer.getBaseCostA().is(Items.EMERALD)) {
                continue;
            }

            if (lucky) {
                ORIGINAL_SPECIAL_PRICES.putIfAbsent(
                    offer,
                    offer.getSpecialPriceDiff()
                );

                int currentCount =
                    offer.getCostA().getCount();

                offer.setSpecialPriceDiff(
                    offer.getSpecialPriceDiff()
                        + 1
                        - currentCount
                );
            } else {
                Integer original =
                    ORIGINAL_SPECIAL_PRICES.remove(offer);

                if (original != null) {
                    offer.setSpecialPriceDiff(original);
                }
            }
        }
    }

    private static void tickOwnedGolems(
        ServerLevel level
    ) {
        Set<UUID> processed = new java.util.HashSet<>();

        for (ServerPlayer observer : level.players()) {
            AABB area =
                observer.getBoundingBox().inflate(
                    GOLEM_STOP_FOLLOW_RADIUS + 12.0D
                );

            for (
                IronGolem golem :
                    level.getEntitiesOfClass(
                        IronGolem.class,
                        area,
                        candidate ->
                            ownerUuid(candidate) != null
                    )
            ) {
                if (!processed.add(golem.getUUID())) {
                    continue;
                }

                UUID ownerUuid =
                    ownerUuid(golem);

                if (ownerUuid == null) {
                    continue;
                }

                ServerPlayer owner =
                    level.getServer()
                        .getPlayerList()
                        .getPlayer(ownerUuid);

                if (owner == null
                    || owner.level() != level) {
                    golem.getNavigation().stop();
                    continue;
                }

                if (
                    golem.getTarget()
                        instanceof ServerPlayer target
                        && hasLuck(target)
                ) {
                    golem.setTarget(null);
                }

                double distance =
                    golem.distanceTo(owner);

                if (distance <= 3.0D) {
                    golem.getNavigation().stop();
                } else if (
                    distance <= GOLEM_FOLLOW_RADIUS
                ) {
                    golem.getNavigation().moveTo(
                        owner,
                        1.10D
                    );
                } else if (
                    distance > GOLEM_STOP_FOLLOW_RADIUS
                ) {
                    golem.getNavigation().stop();
                }
            }
        }
    }

    public static boolean hasLuck(
        net.minecraft.world.entity.player.Player player
    ) {
        for (ItemStack stack : player.getAllSlots()) {
            if (
                EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.LUCK,
                    stack
                ) > 0
            ) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasFullEmeraldArmor(
        net.minecraft.world.entity.player.Player player
    ) {
        return player.getItemBySlot(EquipmentSlot.HEAD)
                .is(ModItems.EMERALD_HELMET)
            && player.getItemBySlot(EquipmentSlot.CHEST)
                .is(ModItems.EMERALD_CHESTPLATE)
            && player.getItemBySlot(EquipmentSlot.LEGS)
                .is(ModItems.EMERALD_LEGGINGS)
            && player.getItemBySlot(EquipmentSlot.FEET)
                .is(ModItems.EMERALD_BOOTS);
    }

    private static boolean isOre(
        BlockState state
    ) {
        String path =
            BuiltInRegistries.BLOCK.getKey(
                state.getBlock()
            ).getPath();

        return path.endsWith("_ore")
            || path.contains("_ore_");
    }

    private static void setOwner(
        IronGolem golem,
        UUID owner
    ) {
        List<String> oldOwnerTags =
            new ArrayList<>();

        for (String tag : golem.getTags()) {
            if (tag.startsWith(OWNER_PREFIX)) {
                oldOwnerTags.add(tag);
            }
        }

        for (String tag : oldOwnerTags) {
            golem.removeTag(tag);
        }

        golem.addTag(
            OWNER_PREFIX + owner
        );
    }

    private static boolean isOwner(
        IronGolem golem,
        UUID player
    ) {
        UUID owner =
            ownerUuid(golem);

        return owner != null
            && owner.equals(player);
    }

    private static UUID ownerUuid(
        IronGolem golem
    ) {
        for (String tag : golem.getTags()) {
            if (!tag.startsWith(OWNER_PREFIX)) {
                continue;
            }

            try {
                return UUID.fromString(
                    tag.substring(
                        OWNER_PREFIX.length()
                    )
                );
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }
}
