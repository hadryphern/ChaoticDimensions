package net.blue.chaoticd.gameplay;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.blue.chaoticd.content.ModEnchantments;
import net.blue.chaoticd.content.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Initializes special gear only after it actually enters a player inventory.
 *
 * <p>This keeps Creative Mode search results clean while preserving the
 * intended Sapphire kit and the controlled Emerald enchantment rolls.</p>
 */
public final class ChaoticGearInitialization {
    private static final String INITIALIZED_TAG =
        "chaoticdGearInitialized";

    private static final String EMERALD_VERSION_TAG =
        "chaoticdEmeraldGearVersion";

    /*
     * Version 2 removes the old level X/XV enchantment lottery and replaces it
     * with at most two ordinary vanilla-level enchantments.
     */
    private static final int CURRENT_EMERALD_VERSION = 2;

    private static final int CHECK_INTERVAL_TICKS = 5;
    private static final int MAX_SELECTION_ATTEMPTS = 32;

    private ChaoticGearInitialization() {
    }

    public static void initialize() {
        ServerTickEvents.END_WORLD_TICK.register(
            ChaoticGearInitialization::tickWorld
        );
    }

    private static void tickWorld(
        ServerLevel level
    ) {
        if (
            level.getGameTime()
                % CHECK_INTERVAL_TICKS
                != 0L
        ) {
            return;
        }

        for (
            ServerPlayer player :
                level.players()
        ) {
            boolean changed = false;

            for (
                int slot = 0;
                slot
                    < player.getInventory()
                        .getContainerSize();
                slot++
            ) {
                ItemStack stack =
                    player.getInventory()
                        .getItem(slot);

                changed |=
                    initializeStack(
                        stack,
                        level.getRandom()
                    );
            }

            ItemStack carried =
                player.containerMenu
                    .getCarried();

            changed |=
                initializeStack(
                    carried,
                    level.getRandom()
                );

            if (changed) {
                player.getInventory()
                    .setChanged();

                player.containerMenu
                    .broadcastChanges();
            }
        }
    }

    private static boolean initializeStack(
        ItemStack stack,
        RandomSource random
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        /*
         * Emerald gear uses a version tag so equipment created before this
         * rebalance is automatically cleaned and rolled again exactly once.
         */
        if (ModItems.isEmeraldGear(stack)) {
            CompoundTag tag =
                stack.getOrCreateTag();

            if (
                tag.getInt(
                    EMERALD_VERSION_TAG
                )
                    >= CURRENT_EMERALD_VERSION
            ) {
                return false;
            }

            initializeEmerald(
                stack,
                random
            );

            tag =
                stack.getOrCreateTag();

            tag.putInt(
                EMERALD_VERSION_TAG,
                CURRENT_EMERALD_VERSION
            );

            tag.putBoolean(
                INITIALIZED_TAG,
                true
            );

            return true;
        }

        if (!ModItems.isSapphireGear(stack)) {
            return false;
        }

        CompoundTag tag =
            stack.getOrCreateTag();

        if (
            tag.getBoolean(
                INITIALIZED_TAG
            )
        ) {
            return false;
        }

        initializeSapphire(stack);

        stack.getOrCreateTag()
            .putBoolean(
                INITIALIZED_TAG,
                true
            );

        return true;
    }

    private static void initializeSapphire(
        ItemStack stack
    ) {
        if (
            stack.is(
                ModItems.SAPPHIRE_SWORD
            )
        ) {
            stack.enchant(
                ModEnchantments.SAPPHIRIC,
                1
            );

            stack.enchant(
                ModEnchantments.DHEATHIC,
                1
            );

            return;
        }

        stack.enchant(
            Enchantments.BLOCK_EFFICIENCY,
            50
        );

        stack.enchant(
            Enchantments.UNBREAKING,
            50
        );

        if (
            stack.is(
                ModItems.SAPPHIRE_PICKAXE
            )
                || stack.is(
                    ModItems.SAPPHIRE_AXE
                )
        ) {
            stack.enchant(
                Enchantments.BLOCK_FORTUNE,
                50
            );
        }

        if (
            stack.is(
                ModItems.SAPPHIRE_AXE
            )
        ) {
            stack.enchant(
                Enchantments.SHARPNESS,
                50
            );
        }
    }

    private static void initializeEmerald(
        ItemStack stack,
        RandomSource random
    ) {
        /*
         * Remove the old lottery enchantments from equipment generated by the
         * previous system. RepairCost is removed as well so the cleaned item
         * does not retain an unfair anvil penalty.
         */
        CompoundTag tag =
            stack.getOrCreateTag();

        tag.remove("Enchantments");
        tag.remove("RepairCost");

        /*
         * Luck V remains the defining guaranteed enchantment of all Emerald
         * tools and armor pieces.
         */
        stack.enchant(
            ModEnchantments.LUCK,
            5
        );

        int additionalEnchantments =
            rollAdditionalEnchantmentCount(
                random
            );

        if (additionalEnchantments == 0) {
            return;
        }

        List<Enchantment> pool =
            emeraldEnchantmentPool(stack);

        if (pool.isEmpty()) {
            return;
        }

        Set<Enchantment> selected =
            new LinkedHashSet<>();

        int attempts = 0;

        while (
            selected.size()
                < additionalEnchantments
                && attempts
                < MAX_SELECTION_ATTEMPTS
        ) {
            attempts++;

            Enchantment candidate =
                pool.get(
                    random.nextInt(
                        pool.size()
                    )
                );

            if (
                !isCompatibleSelection(
                    candidate,
                    selected
                )
            ) {
                continue;
            }

            selected.add(candidate);
        }

        for (
            Enchantment enchantment :
                selected
        ) {
            int maximumLevel = vanillaMaximumLevel(enchantment);

            int level =
                1
                    + random.nextInt(
                        maximumLevel
                    );

            stack.enchant(
                enchantment,
                level
            );
        }
    }

    /**
     * Emerald equipment can receive:
     *
     * <ul>
     *     <li>50% chance of no additional enchantment.</li>
     *     <li>40% chance of one ordinary enchantment.</li>
     *     <li>10% chance of two ordinary enchantments.</li>
     * </ul>
     */
    private static int rollAdditionalEnchantmentCount(
        RandomSource random
    ) {
        int roll =
            random.nextInt(100);

        if (roll < 50) {
            return 0;
        }

        if (roll < 90) {
            return 1;
        }

        return 2;
    }

    /**
     * Returns a small item-specific pool containing only ordinary enchantments.
     *
     * <p>There are no level X/XV rolls, curses, Mending, specialized damage
     * enchantments or combinations with five random effects.</p>
     */
    private static List<Enchantment> emeraldEnchantmentPool(
        ItemStack stack
    ) {
        List<Enchantment> pool =
            new ArrayList<>();

        if (
            stack.is(
                ModItems.EMERALD_SWORD
            )
        ) {
            pool.add(
                Enchantments.SHARPNESS
            );

            pool.add(
                Enchantments.UNBREAKING
            );

            pool.add(
                Enchantments.MOB_LOOTING
            );

            pool.add(
                Enchantments.KNOCKBACK
            );

            pool.add(
                Enchantments.FIRE_ASPECT
            );

            return pool;
        }

        if (
            stack.is(
                ModItems.EMERALD_PICKAXE
            )
                || stack.is(
                    ModItems.EMERALD_SHOVEL
                )
                || stack.is(
                    ModItems.EMERALD_HOE
                )
        ) {
            pool.add(
                Enchantments.BLOCK_EFFICIENCY
            );

            pool.add(
                Enchantments.UNBREAKING
            );

            pool.add(
                Enchantments.BLOCK_FORTUNE
            );

            pool.add(
                Enchantments.SILK_TOUCH
            );

            return pool;
        }

        if (
            stack.is(
                ModItems.EMERALD_AXE
            )
        ) {
            pool.add(
                Enchantments.BLOCK_EFFICIENCY
            );

            pool.add(
                Enchantments.UNBREAKING
            );

            pool.add(
                Enchantments.BLOCK_FORTUNE
            );

            pool.add(
                Enchantments.SILK_TOUCH
            );

            pool.add(
                Enchantments.SHARPNESS
            );

            return pool;
        }

        if (
            stack.is(
                ModItems.EMERALD_HELMET
            )
        ) {
            pool.add(
                Enchantments.ALL_DAMAGE_PROTECTION
            );

            pool.add(
                Enchantments.UNBREAKING
            );

            pool.add(
                Enchantments.RESPIRATION
            );

            pool.add(
                Enchantments.AQUA_AFFINITY
            );

            return pool;
        }

        if (
            stack.is(
                ModItems.EMERALD_CHESTPLATE
            )
        ) {
            pool.add(
                Enchantments.ALL_DAMAGE_PROTECTION
            );

            pool.add(
                Enchantments.UNBREAKING
            );

            pool.add(
                Enchantments.THORNS
            );

            return pool;
        }

        if (
            stack.is(
                ModItems.EMERALD_LEGGINGS
            )
        ) {
            pool.add(
                Enchantments.ALL_DAMAGE_PROTECTION
            );

            pool.add(
                Enchantments.UNBREAKING
            );

            return pool;
        }

        if (
            stack.is(
                ModItems.EMERALD_BOOTS
            )
        ) {
            pool.add(
                Enchantments.ALL_DAMAGE_PROTECTION
            );

            pool.add(
                Enchantments.UNBREAKING
            );

            pool.add(
                Enchantments.FALL_PROTECTION
            );

            pool.add(
                Enchantments.DEPTH_STRIDER
            );

            return pool;
        }

        return pool;
    }

    private static boolean isCompatibleSelection(
        Enchantment candidate,
        Set<Enchantment> selected
    ) {
        if (
            candidate
                == Enchantments.BLOCK_FORTUNE
                && selected.contains(
                    Enchantments.SILK_TOUCH
                )
        ) {
            return false;
        }

        if (
            candidate
                == Enchantments.SILK_TOUCH
                && selected.contains(
                    Enchantments.BLOCK_FORTUNE
                )
        ) {
            return false;
        }

        return true;
    }

    /**
     * The mod deliberately raises the global maximum levels for several
     * vanilla enchantments. Emerald gear is intended to roll only ordinary
     * vanilla levels, so it must not query the globally patched max level.
     */
    private static int vanillaMaximumLevel(Enchantment enchantment) {
        if (enchantment == Enchantments.SHARPNESS
            || enchantment == Enchantments.SMITE
            || enchantment == Enchantments.BANE_OF_ARTHROPODS
            || enchantment == Enchantments.BLOCK_EFFICIENCY) {
            return 5;
        }
        if (enchantment == Enchantments.ALL_DAMAGE_PROTECTION
            || enchantment == Enchantments.FIRE_PROTECTION
            || enchantment == Enchantments.BLAST_PROTECTION
            || enchantment == Enchantments.PROJECTILE_PROTECTION
            || enchantment == Enchantments.FALL_PROTECTION) {
            return 4;
        }
        if (enchantment == Enchantments.UNBREAKING
            || enchantment == Enchantments.MOB_LOOTING
            || enchantment == Enchantments.BLOCK_FORTUNE
            || enchantment == Enchantments.RESPIRATION
            || enchantment == Enchantments.THORNS
            || enchantment == Enchantments.DEPTH_STRIDER
            || enchantment == Enchantments.SWEEPING_EDGE) {
            return 3;
        }
        if (enchantment == Enchantments.KNOCKBACK
            || enchantment == Enchantments.FIRE_ASPECT) {
            return 2;
        }
        return 1;
    }
}
