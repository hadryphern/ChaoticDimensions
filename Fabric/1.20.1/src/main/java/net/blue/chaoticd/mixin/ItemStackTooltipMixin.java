package net.blue.chaoticd.mixin;

import java.util.ArrayList;
import java.util.List;
import net.blue.chaoticd.content.ModCombatEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
        if (multiplier <= 1.0F && reach <= 0.0F) {
            return;
        }

        List<Component> lines = new ArrayList<>(callback.getReturnValue());
        if (multiplier > 1.0F) {
            String attackDamageName = Component.translatable("attribute.name.generic.attack_damage").getString();
            for (int index = 0; index < lines.size(); index++) {
                if (lines.get(index).getString().contains(attackDamageName)) {
                    lines.set(index, Component.literal(format(finalAttackDamage(stack, player) * multiplier) + " ")
                        .append(Component.translatable("attribute.name.generic.attack_damage"))
                        .withStyle(ChatFormatting.DARK_PURPLE));
                    break;
                }
            }
        }
        if (reach > 0.0F) {
            lines.add(Component.translatable("tooltip.chaoticd.attack_reach", format(reach))
                .withStyle(ChatFormatting.DARK_PURPLE));
        }
        callback.setReturnValue(lines);
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
