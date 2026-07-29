package net.blue.chaoticd.worldgen;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.worldgen.AuroraSafeArrival;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative transitions, curse behavior and Death Totem rules.
 *
 * <p>Falling below Aurora enters Shadow. Dropping a Death Totem in Shadow
 * returns the player to Aurora while returning the item to the inventory.</p>
 */
public final class ShadowDimensionSystems {
    public static final ResourceKey<Level> AURORA_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        id("aurora_dimension")
    );

    public static final ResourceKey<Level> SHADOW_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        id("shadow_dimension")
    );

    private static final int AURORA_VOID_MARGIN = 8;
    private static final int CURSE_REFRESH_TICKS = 20;
    private static final int CURSE_DURATION_TICKS = 60;
    private static final int DAMAGE_INTERVAL_TICKS = 60;
    private static final int SHADOW_STORM_DURATION_TICKS = 1_000_000;
    private static final int SHADOW_STORM_REFRESH_TICKS = 200;
    private static final int DEATH_TOTEM_NIGHT_VISION_DURATION = 999_999;
    private static final float SHADOW_DAMAGE = 2.0F;
    private static final double DROPPED_TOTEM_SEARCH_RADIUS = 6.0D;
    private static final int DROPPED_TOTEM_CHECK_INTERVAL_TICKS = 5;
    private static final long VOID_ARRIVAL_RETRY_TICKS = 200L;

    private static final Set<UUID> CURSED_PLAYERS = new HashSet<>();
    private static final Map<UUID, Long> NEXT_VOID_ARRIVAL_ATTEMPT = new HashMap<>();

    private ShadowDimensionSystems() {
    }

    public static void initialize() {
        ServerTickEvents.END_WORLD_TICK.register(
            ShadowDimensionSystems::tickWorld
        );

        ServerPlayerEvents.COPY_FROM.register(
            ShadowDimensionSystems::copyDeathTotemsAfterRespawn
        );
    }

    private static void tickWorld(ServerLevel level) {
        if (level.dimension().equals(AURORA_DIMENSION)) {
            for (ServerPlayer player : List.copyOf(level.players())) {
                clearShadowCurse(player);
                handleAuroraVoidFall(level, player);
            }

            return;
        }

        if (level.dimension().equals(SHADOW_DIMENSION)) {
            enforceShadowStorm(level);

            for (ServerPlayer player : List.copyOf(level.players())) {
                NEXT_VOID_ARRIVAL_ATTEMPT.remove(player.getUUID());

                if (player.tickCount % DROPPED_TOTEM_CHECK_INTERVAL_TICKS == 0
                    && handleDroppedDeathTotem(level, player)) {
                    continue;
                }

                if (hasDeathTotem(player)) {
                    grantDeathTotemNightVision(player);
                }

                if (isProtected(player)) {
                    clearShadowCurse(player);
                } else {
                    applyShadowCurse(level, player);
                }
            }

            return;
        }

        for (ServerPlayer player : level.players()) {
            NEXT_VOID_ARRIVAL_ATTEMPT.remove(player.getUUID());
            clearShadowCurse(player);
        }
    }

    private static void handleAuroraVoidFall(ServerLevel aurora, ServerPlayer player) {
        if (player.getY() >= aurora.getMinBuildHeight() - AURORA_VOID_MARGIN) {
            NEXT_VOID_ARRIVAL_ATTEMPT.remove(player.getUUID());
            return;
        }

        long now = aurora.getGameTime();
        long nextAttempt = NEXT_VOID_ARRIVAL_ATTEMPT.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (now < nextAttempt) {
            return;
        }
        /* A safe-arrival search can inspect/generate many chunks.  Never run it
         * continuously while a player is falling through Aurora's void. */
        NEXT_VOID_ARRIVAL_ATTEMPT.put(player.getUUID(), now + VOID_ARRIVAL_RETRY_TICKS);

        ServerLevel shadow = player.server.getLevel(SHADOW_DIMENSION);

        if (shadow == null) {
            player.displayClientMessage(
                Component.translatable("message.chaoticd.shadow_unavailable"),
                false
            );
            return;
        }

        ShadowSafeArrival.find(shadow).ifPresentOrElse(
            arrival -> {
                NEXT_VOID_ARRIVAL_ATTEMPT.remove(player.getUUID());
                teleport(player, shadow, arrival);
            },
            () -> player.displayClientMessage(
                Component.translatable("message.chaoticd.shadow_no_safe_arrival"),
                false
            )
        );
    }

    private static boolean handleDroppedDeathTotem(
        ServerLevel shadow,
        ServerPlayer player
    ) {
        List<ItemEntity> drops = shadow.getEntitiesOfClass(
            ItemEntity.class,
            player.getBoundingBox().inflate(DROPPED_TOTEM_SEARCH_RADIUS),
            item -> item.isAlive() && item.getItem().is(ModItems.DEATH_TOTEM)
        );

        if (drops.isEmpty()) {
            return false;
        }

        ItemEntity dropped = drops.get(0);
        ItemStack returnedTotem = dropped.getItem().copy();
        dropped.discard();

        boolean dropAfterTeleport = !player.getInventory().add(returnedTotem);

        player.containerMenu.broadcastChanges();
        grantDeathTotemNightVision(player);

        ServerLevel aurora = player.server.getLevel(AURORA_DIMENSION);

        if (aurora == null) {
            if (dropAfterTeleport) {
                player.drop(returnedTotem, false);
            }
            player.displayClientMessage(
                Component.translatable("message.chaoticd.aurora_unavailable"),
                false
            );
            return true;
        }

        clearShadowCurse(player);

        AuroraSafeArrival.find(aurora).ifPresentOrElse(
            arrival -> {
                teleport(player, aurora, arrival);
                if (dropAfterTeleport) {
                    player.drop(returnedTotem, false);
                }
            },
            () -> {
                if (dropAfterTeleport) {
                    player.drop(returnedTotem, false);
                }
                player.displayClientMessage(
                    Component.translatable("message.chaoticd.aurora_no_safe_arrival"),
                    false
                );
            }
        );

        return true;
    }

    private static boolean isProtected(ServerPlayer player) {
        return player.getOffhandItem().is(ModItems.DEATH_TOTEM);
    }

    /**
     * Receiving a Death Totem in Shadow grants the requested near-infinite
     * Night Vision immediately, even before the player decides whether to put
     * it in the offhand for protection from the Shadow curse.
     */
    private static boolean hasDeathTotem(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(ModItems.DEATH_TOTEM)) {
                return true;
            }
        }
        return player.containerMenu.getCarried().is(ModItems.DEATH_TOTEM);
    }

    /** Keeps Shadow's storm active even after its natural weather timer expires. */
    private static void enforceShadowStorm(ServerLevel level) {
        if (!level.isRaining()
            || !level.isThundering()
            || level.getGameTime() % SHADOW_STORM_REFRESH_TICKS == 0L) {
            level.setWeatherParameters(
                0,
                SHADOW_STORM_DURATION_TICKS,
                true,
                true
            );
        }
    }

    /**
     * A collected Death Totem grants near-infinite Night Vision in Shadow.
     */
    private static void grantDeathTotemNightVision(ServerPlayer player) {
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);

        if (current != null
            && current.getDuration()
                > DEATH_TOTEM_NIGHT_VISION_DURATION - CURSE_REFRESH_TICKS) {
            return;
        }

        player.addEffect(
            new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                DEATH_TOTEM_NIGHT_VISION_DURATION,
                0,
                true,
                false,
                true
            )
        );
    }

    private static void applyShadowCurse(ServerLevel level, ServerPlayer player) {
        boolean newlyCursed = CURSED_PLAYERS.add(player.getUUID());

        if (newlyCursed || player.tickCount % CURSE_REFRESH_TICKS == 0) {
            applyEffect(player, MobEffects.BLINDNESS, 0);
            applyEffect(player, MobEffects.MOVEMENT_SLOWDOWN, 1);
            applyEffect(player, MobEffects.DARKNESS, 0);
            applyEffect(player, MobEffects.DIG_SLOWDOWN, 2);
        }

        if (!player.isCreative()
            && !player.isSpectator()
            && player.tickCount % DAMAGE_INTERVAL_TICKS == 0) {
            player.hurt(level.damageSources().magic(), SHADOW_DAMAGE);
        }
    }

    private static void applyEffect(
        ServerPlayer player,
        MobEffect effect,
        int amplifier
    ) {
        player.addEffect(
            new MobEffectInstance(
                effect,
                CURSE_DURATION_TICKS,
                amplifier,
                true,
                false,
                true
            )
        );
    }

    private static void clearShadowCurse(ServerPlayer player) {
        if (!CURSED_PLAYERS.remove(player.getUUID())) {
            return;
        }

        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    private static void teleport(
        ServerPlayer player,
        ServerLevel destination,
        BlockPos arrival
    ) {
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;

        player.teleportTo(
            destination,
            arrival.getX() + 0.5D,
            arrival.getY() + 0.1D,
            arrival.getZ() + 0.5D,
            player.getYRot(),
            player.getXRot()
        );
    }

    /**
     * Inventory.dropAll is mixed so Death Totems remain on the old player.
     * This callback copies any missing Totem stacks to the respawned player.
     */
    private static void copyDeathTotemsAfterRespawn(
        ServerPlayer oldPlayer,
        ServerPlayer newPlayer,
        boolean alive
    ) {
        if (alive) {
            return;
        }

        Inventory oldInventory = oldPlayer.getInventory();
        Inventory newInventory = newPlayer.getInventory();

        int oldCount = countDeathTotems(oldInventory);
        int newCount = countDeathTotems(newInventory);
        int missing = oldCount - newCount;

        if (missing <= 0) {
            return;
        }

        for (
            int slot = 0;
            slot < oldInventory.getContainerSize() && missing > 0;
            slot++
        ) {
            ItemStack stack = oldInventory.getItem(slot);

            if (!stack.is(ModItems.DEATH_TOTEM)) {
                continue;
            }

            ItemStack copy = stack.copy();

            if (newInventory.getItem(slot).isEmpty()) {
                newInventory.setItem(slot, copy);
            } else if (!newInventory.add(copy)) {
                // A full respawn inventory must not delete a retained Totem.
                newPlayer.drop(copy, false);
            }

            missing -= copy.getCount();
        }
    }

    private static int countDeathTotems(Inventory inventory) {
        int count = 0;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);

            if (stack.is(ModItems.DEATH_TOTEM)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, path);
    }
}
