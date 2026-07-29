package net.blue.chaoticd.gameplay;

import net.blue.chaoticd.content.ModTags;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/** Enforces the Silk Touch harvesting rule shared by every Crystal block. */
public final class CrystalHarvestRules {
    private CrystalHarvestRules() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.BEFORE.register(
            (level, player, pos, state, blockEntity) -> {
                if (!state.is(ModTags.CRYSTAL_SENSITIVE)
                    || player.isCreative()
                    || canHarvestCrystal(player.getMainHandItem())) {
                    return true;
                }

                player.displayClientMessage(
                    Component.translatable(
                        "message.chaoticd.crystal_requires_silk_touch"
                    ),
                    true
                );

                return false;
            }
        );
    }

    private static boolean canHarvestCrystal(ItemStack stack) {
        if (EnchantmentHelper.getItemEnchantmentLevel(
            Enchantments.SILK_TOUCH,
            stack
        ) <= 0) {
            return false;
        }

        Item item = stack.getItem();

        return item instanceof ShovelItem
            || item instanceof PickaxeItem
            || item instanceof AxeItem
            || item instanceof SwordItem;
    }
}
