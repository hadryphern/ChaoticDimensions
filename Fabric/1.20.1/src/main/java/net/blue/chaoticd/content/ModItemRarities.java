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
        COMMON("rarity.chaoticd.common", 0x55DFFF, 0),
        UNCOMMON("rarity.chaoticd.uncommon", 0x55FF55, 4),
        RARE("rarity.chaoticd.rare", 0x55FFFF, 10),
        VERY_RARE("rarity.chaoticd.very_rare", 0xFF55FF, 20),
        EXTREMELY_RARE("rarity.chaoticd.extremely_rare", 0x5555FF, 36),
        ULTRA_RARE("rarity.chaoticd.ultra_rare", 0xAA00FF, 60),
        IMPOSSIBLE("rarity.chaoticd.impossible", 0xFF5555, 100),
        FORBIDDEN("rarity.chaoticd.forbidden", 0xAAAAAA, 170),
        LEGENDARY("rarity.chaoticd.legendary", 0xFFD700, 280),
        EXTRAVAGANT("rarity.chaoticd.extravagant", 0xFFD700, 430),
        GOD("rarity.chaoticd.god", 0xFFD700, 620),
        ENDGAME("rarity.chaoticd.endgame", 0xAA00FF, 850);

        private final String translationKey;
        private final int color;
        private final int threshold;

        Rank(String translationKey, int color, int threshold) {
            this.translationKey = translationKey;
            this.color = color;
            this.threshold = threshold;
        }

        public String translationKey() { return translationKey; }
        public int color() { return color; }
        public int threshold() { return threshold; }
    }

    private ModItemRarities() {
    }

    public static boolean isSapphire(ItemStack stack) {
        return stack.is(ModItems.SAPPHIRE_GEM) || stack.is(ModItems.SAPPHIRE_SWORD)
            || stack.is(ModItems.SAPPHIRE_PICKAXE) || stack.is(ModItems.SAPPHIRE_AXE)
            || stack.is(ModItems.SAPPHIRE_SHOVEL) || stack.is(ModItems.SAPPHIRE_HOE);
    }

    public static Rank rank(ItemStack stack) {
        int totalScore = baseRank(stack).threshold();
        for (var entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Rank enchantmentRank = enchantmentRank(stack, entry.getKey(), entry.getValue());
            // Level L is already part of the certified Legendary Sapphire tool set, not a separate promotion.
            if (!isSapphireLegendaryLevel(stack, entry.getValue())) {
                totalScore += enchantmentScore(enchantmentRank);
            }
        }
        return rankForScore(totalScore);
    }

    private static Rank baseRank(ItemStack stack) {
        if (isSapphire(stack)) return Rank.LEGENDARY;
        if (PotionUtils.getPotion(stack) == ModPotions.SAPPHIRIC
            ) {
            return Rank.ULTRA_RARE;
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

    /** Determines one enchantment's visual rarity independently of the item it is on. */
    public static Rank enchantmentRank(ItemStack stack, Enchantment enchantment, int level) {
        if (isSapphireLegendaryLevel(stack, level)) return Rank.LEGENDARY;
        if (isChaoticEnchantment(enchantment)) return Rank.ULTRA_RARE;
        if (isExtendedVanillaLevel(enchantment, level)) return Rank.VERY_RARE;
        return switch (enchantment.getRarity()) {
            case COMMON -> Rank.COMMON;
            case UNCOMMON -> Rank.UNCOMMON;
            case RARE -> Rank.RARE;
            case VERY_RARE -> Rank.VERY_RARE;
        };
    }

    private static boolean isSapphireLegendaryLevel(ItemStack stack, int level) {
        return isSapphire(stack) && level >= 50;
    }

    /**
     * Enchantment score grows slower than the rank thresholds. This prevents a single powerful book
     * from instantly making a normal item Legendary, while combinations still matter.
     */
    private static int enchantmentScore(Rank rank) {
        return switch (rank) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 6;
            case VERY_RARE -> 16;
            case EXTREMELY_RARE -> 25;
            case ULTRA_RARE -> 40;
            case IMPOSSIBLE -> 65;
            case FORBIDDEN -> 100;
            case LEGENDARY -> 150;
            case EXTRAVAGANT -> 210;
            case GOD -> 300;
            case ENDGAME -> 420;
        };
    }

    private static Rank rankForScore(int score) {
        Rank result = Rank.COMMON;
        for (Rank candidate : Rank.values()) {
            if (score >= candidate.threshold()) result = candidate;
        }
        return result;
    }

    public static boolean isChaoticEnchantment(Enchantment enchantment) {
        return enchantment == ModEnchantments.SAPPHIRIC || enchantment == ModEnchantments.DHEATHIC
            || enchantment == ModEnchantments.BIG_BERTHA || enchantment == ModEnchantments.ROYAL
            || enchantment == ModEnchantments.DISPARADA;
    }

    /** Any level beyond the vanilla cap is Very Rare; it is still coloured by rarity, never by item material. */
    private static boolean isExtendedVanillaLevel(Enchantment enchantment, int level) {
        int vanillaCap = enchantment == Enchantments.SHARPNESS || enchantment == Enchantments.SMITE
            || enchantment == Enchantments.BANE_OF_ARTHROPODS || enchantment == Enchantments.BLOCK_EFFICIENCY ? 5
            : enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.MOB_LOOTING
            || enchantment == Enchantments.BLOCK_FORTUNE || enchantment == Enchantments.SWEEPING_EDGE
            || enchantment == Enchantments.THORNS ? 3
            : enchantment == Enchantments.ALL_DAMAGE_PROTECTION || enchantment == Enchantments.FIRE_PROTECTION
            || enchantment == Enchantments.BLAST_PROTECTION || enchantment == Enchantments.PROJECTILE_PROTECTION
            || enchantment == Enchantments.FALL_PROTECTION ? 4
            : enchantment == Enchantments.KNOCKBACK || enchantment == Enchantments.FIRE_ASPECT ? 2 : Integer.MAX_VALUE;
        return level > vanillaCap;
    }
}
