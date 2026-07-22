package net.blue.chaoticd.client.rarity;

import java.util.List;
import net.blue.chaoticd.rarity.RarityDefinition;
import net.blue.chaoticd.rarity.RarityResolver;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Shared exact NBT-to-tooltip-line mapping for normal items and enchanted books. */
public final class TooltipEnchantmentStyler {
    private TooltipEnchantmentStyler() {
    }

    public static void appendAndStyle(ItemStack stack, List<Component> lines, ListTag enchantmentTags) {
        int firstAddedLine = lines.size();
        ItemStack.appendEnchantmentNames(lines, enchantmentTags);
        styleExisting(stack, lines, enchantmentTags, firstAddedLine,
            AnimatedColorProvider.global().nowNanos());
    }

    public static void styleExisting(ItemStack stack, List<Component> lines, ListTag enchantmentTags,
                                     int firstLine, long nowNanos) {
        if (firstLine >= lines.size()) return;
        RarityResolver resolver = RarityResolver.global();
        TooltipRarityRenderer renderer = TooltipRarityRenderer.global();
        int lineIndex = firstLine;

        for (int tagIndex = 0; tagIndex < enchantmentTags.size() && lineIndex < lines.size(); tagIndex++) {
            CompoundTag tag = enchantmentTags.getCompound(tagIndex);
            ResourceLocation enchantmentId = EnchantmentHelper.getEnchantmentId(tag);
            if (enchantmentId == null) continue;
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(enchantmentId).orElse(null);
            if (enchantment == null) continue;

            int level = EnchantmentHelper.getEnchantmentLevel(tag);
            RarityDefinition rarity = resolver.resolveEnchantment(stack, enchantment, level);
            lines.set(lineIndex, renderer.styleAt(lines.get(lineIndex), rarity, nowNanos));
            lineIndex++;
        }
    }
}
