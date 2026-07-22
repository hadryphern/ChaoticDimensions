package net.blue.chaoticd.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.blue.chaoticd.client.rarity.TooltipEnchantmentStyler;
import net.blue.chaoticd.client.rarity.TooltipRarityRenderer;
import net.blue.chaoticd.content.ModCombatEnchantments;
import net.blue.chaoticd.rarity.RarityDefinition;
import net.blue.chaoticd.rarity.RarityResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Surgical client tooltip integration: every enchantment is styled as its own line when appended. */
@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    /**
     * Intercepts the vanilla append operation instead of searching localized strings afterwards.
     * Unknown command/NBT enchantments remain untouched and cannot shift subsequent known entries.
     */
    @Redirect(
        method = "getTooltipLines",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"
        )
    )
    private void chaoticd$appendIndividuallyStyledEnchantments(List<Component> lines, ListTag enchantmentTags) {
        ItemStack stack = (ItemStack) (Object) this;
        TooltipEnchantmentStyler.appendAndStyle(stack, lines, enchantmentTags);
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void chaoticd$appendIndependentCombatAndRarityLines(Player player, TooltipFlag flag,
                                                                 CallbackInfoReturnable<List<Component>> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        float multiplier = ModCombatEnchantments.damageMultiplier(stack);
        float reach = ModCombatEnchantments.attackReach(stack);
        List<Component> lines = new ArrayList<>(callback.getReturnValue());

        if (multiplier > 1.0F) {
            String attackDamageName = Component.translatable("attribute.name.generic.attack_damage").getString();
            for (int index = 0; index < lines.size(); index++) {
                if (!lines.get(index).getString().contains(attackDamageName)) continue;
                lines.set(index, Component.translatable("attribute.modifier.plus.0",
                    format(finalAttackDamage(stack, player) * multiplier),
                    Component.translatable("attribute.name.generic.attack_damage"))
                    .withStyle(ChatFormatting.DARK_PURPLE));
                break;
            }
        }

        if (reach > 0.0F) {
            lines.add(Component.translatable("attribute.modifier.plus.0", format(reach),
                Component.translatable("tooltip.chaoticd.attack_reach"))
                .withStyle(ChatFormatting.DARK_PURPLE));
        }

        // The name was styled by ItemStackNameMixin; lore and attributes retain their own styles.
        RarityDefinition stackRarity = RarityResolver.global().resolveItem(stack);
        Component rankLabel = Component.translatable(stackRarity.translationKey());
        lines.add(TooltipRarityRenderer.global().style(rankLabel, stackRarity));
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
        return value == Math.rint(value) ? Long.toString(Math.round(value))
            : String.format(Locale.ROOT, "%.2f", value);
    }
}
