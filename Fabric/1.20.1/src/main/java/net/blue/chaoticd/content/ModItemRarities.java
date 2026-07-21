package net.blue.chaoticd.content;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.alchemy.PotionUtils;

/** Central visual rarity rules. Extend this class whenever new Chaotic Dimensions item families are added. */
public final class ModItemRarities {
    public enum Rank {
        COMMON("rarity.chaoticd.common", 0x55DFFF),
        UNCOMMON("rarity.chaoticd.uncommon", 0x55FF55),
        RARE("rarity.chaoticd.rare", 0x55FFFF),
        VERY_RARE("rarity.chaoticd.very_rare", 0xFF55FF),
        EXTREMELY_RARE("rarity.chaoticd.extremely_rare", 0x5555FF),
        ULTRA_RARE("rarity.chaoticd.ultra_rare", 0xAA00FF),
        IMPOSSIBLE("rarity.chaoticd.impossible", 0xFF5555),
        FORBIDDEN("rarity.chaoticd.forbidden", 0xAAAAAA),
        LEGENDARY("rarity.chaoticd.legendary", 0xFFD700),
        EXTRAVAGANT("rarity.chaoticd.extravagant", 0xFFD700),
        GOD("rarity.chaoticd.god", 0xFFD700),
        ENDGAME("rarity.chaoticd.endgame", 0xAA00FF);

        private final String translationKey;
        private final int color;

        Rank(String translationKey, int color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        public String translationKey() { return translationKey; }
        public int color() { return color; }
    }

    private ModItemRarities() {
    }

    public static boolean isSapphire(ItemStack stack) {
        return stack.is(ModItems.SAPPHIRE_GEM) || stack.is(ModItems.SAPPHIRE_SWORD)
            || stack.is(ModItems.SAPPHIRE_PICKAXE) || stack.is(ModItems.SAPPHIRE_AXE)
            || stack.is(ModItems.SAPPHIRE_SHOVEL) || stack.is(ModItems.SAPPHIRE_HOE);
    }

    public static Rank rank(ItemStack stack) {
        if (isSapphire(stack)) return Rank.LEGENDARY;
        if (PotionUtils.getPotion(stack) == ModPotions.SAPPHIRIC
            || EnchantmentHelper.getEnchantments(stack).keySet().stream().anyMatch(ModItemRarities::isChaoticEnchantment)) {
            return Rank.ULTRA_RARE;
        }
        if (EnchantmentHelper.getEnchantments(stack).entrySet().stream()
            .anyMatch(entry -> isGoldExtendedLevel(entry.getKey(), entry.getValue()))) {
            return Rank.VERY_RARE;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (id.contains("netherite")) return Rank.EXTREMELY_RARE;
        if (id.contains("diamond")) return Rank.RARE;
        if (id.contains("iron") || id.contains("golden")) return Rank.UNCOMMON;
        return switch (stack.getRarity()) {
            case UNCOMMON -> Rank.UNCOMMON;
            case RARE -> Rank.RARE;
            case EPIC -> Rank.VERY_RARE;
            default -> Rank.COMMON;
        };
    }

    public static boolean isChaoticEnchantment(Enchantment enchantment) {
        return enchantment == ModEnchantments.SAPPHIRIC || enchantment == ModEnchantments.DHEATHIC
            || enchantment == ModEnchantments.BIG_BERTHA || enchantment == ModEnchantments.ROYAL
            || enchantment == ModEnchantments.DISPARADA;
    }

    /** Only the highest new vanilla level earns the gold treatment; intermediate levels stay vanilla-coloured. */
    public static boolean isGoldExtendedLevel(Enchantment enchantment, int level) {
        int goldLevel = enchantment == Enchantments.SHARPNESS || enchantment == Enchantments.ALL_DAMAGE_PROTECTION
            || enchantment == Enchantments.FIRE_PROTECTION || enchantment == Enchantments.BLAST_PROTECTION
            || enchantment == Enchantments.PROJECTILE_PROTECTION || enchantment == Enchantments.FALL_PROTECTION
            || enchantment == Enchantments.THORNS ? 15
            : enchantment == Enchantments.KNOCKBACK ? 20
            : enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.BLOCK_EFFICIENCY
            || enchantment == Enchantments.SMITE || enchantment == Enchantments.BANE_OF_ARTHROPODS
            || enchantment == Enchantments.SWEEPING_EDGE || enchantment == Enchantments.FIRE_ASPECT
            || enchantment == Enchantments.MOB_LOOTING || enchantment == Enchantments.BLOCK_FORTUNE ? 10
            : Integer.MAX_VALUE;
        return level >= goldLevel;
    }
}
