package net.blue.chaoticd.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.blue.chaoticd.content.ModCombatEnchantments;
import net.blue.chaoticd.content.ModItemRarities;
import net.blue.chaoticd.content.RarityText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces the normal green attack-damage line with the real custom-enchantment result. */
@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void chaoticd$showFinalChaoticDamage(Player player, TooltipFlag flag,
                                                  CallbackInfoReturnable<List<Component>> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        float multiplier = ModCombatEnchantments.damageMultiplier(stack);
        float reach = ModCombatEnchantments.attackReach(stack);
        List<Component> lines = new ArrayList<>(callback.getReturnValue());
        if (multiplier > 1.0F) {
            String attackDamageName = Component.translatable("attribute.name.generic.attack_damage").getString();
            for (int index = 0; index < lines.size(); index++) {
                if (lines.get(index).getString().contains(attackDamageName)) {
                    lines.set(index, Component.translatable("attribute.modifier.plus.0",
                        format(finalAttackDamage(stack, player) * multiplier),
                        Component.translatable("attribute.name.generic.attack_damage"))
                        .withStyle(ChatFormatting.DARK_PURPLE));
                    break;
                }
            }
        }
        if (reach > 0.0F) {
            lines.add(Component.translatable("attribute.modifier.plus.0", format(reach),
                Component.translatable("tooltip.chaoticd.attack_reach"))
                .withStyle(ChatFormatting.DARK_PURPLE));
        }
        styleEnchantments(stack, lines);
        // Keep every vanilla/mod tooltip line exactly where it belongs; rarity is only the final line.
        lines.add(rankLine(ModItemRarities.rank(stack)));
        callback.setReturnValue(lines);
    }

    private static void styleEnchantments(ItemStack stack, List<Component> lines) {
        boolean sapphire = ModItemRarities.isSapphire(stack);
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            String label = entry.getKey().getFullname(entry.getValue()).getString();
            for (int index = 0; index < lines.size(); index++) {
                if (!lines.get(index).getString().equals(label)) continue;
                if (sapphire) {
                    lines.set(index, RarityText.rainbow(label));
                } else if (ModItemRarities.isChaoticEnchantment(entry.getKey())) {
                    lines.set(index, Component.literal(label).withStyle(ChatFormatting.DARK_PURPLE));
                } else if (ModItemRarities.isAboveVanillaCap(entry.getKey(), entry.getValue())) {
                    lines.set(index, Component.literal(label).withStyle(ChatFormatting.GOLD));
                }
                break;
            }
        }
    }

    private static Component rankLine(ModItemRarities.Rank rank) {
        String label = Component.translatable(rank.translationKey()).getString();
        return switch (rank) {
            case LEGENDARY -> RarityText.rainbow(label);
            case FORBIDDEN -> RarityText.gradient(label, 0xAAAAAA, 0xFFFFFF);
            case EXTRAVAGANT -> RarityText.gradient(label, 0xFFD700, 0x55DFFF);
            case GOD -> RarityText.gradient(label, 0xFFD700, 0xFFFFFF);
            case ENDGAME -> RarityText.gradient(label, 0xAA00FF, 0xFF55FF, 0x5555FF, 0x111111);
            default -> Component.literal(label).withStyle(Style.EMPTY.withColor(rank.color()));
        };
    }

    private static double finalAttackDamage(ItemStack stack, Player player) {
        double value = player == null ? 1.0D : player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
            .get(Attributes.ATTACK_DAMAGE)) {
            switch (modifier.getOperation()) {
                case ADDITION -> value += modifier.getAmount();
                case MULTIPLY_BASE -> value += value * modifier.getAmount();
                case MULTIPLY_TOTAL -> value *= 1.0D + modifier.getAmount();
            }
        }
        return value;
    }

    private static String format(double value) {
        return value == Math.rint(value) ? Long.toString(Math.round(value)) : String.format("%.2f", value);
    }
}
