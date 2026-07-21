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
        Rank result = baseRank(stack);
        int nonCommonEnchantments = 0;
        for (var entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Rank enchantmentRank = enchantmentRank(entry.getKey(), entry.getValue());
            result = higher(result, enchantmentRank);
            if (enchantmentRank != Rank.COMMON) nonCommonEnchantments++;
        }
        // A combination of three meaningful enchantments promotes the item one extra tier.
        return promote(result, nonCommonEnchantments / 3);
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
    public static Rank enchantmentRank(Enchantment enchantment, int level) {
        if (isChaoticEnchantment(enchantment)) return Rank.ULTRA_RARE;
        if (isExtendedVanillaLevel(enchantment, level)) return Rank.VERY_RARE;
        return switch (enchantment.getRarity()) {
            case COMMON -> Rank.COMMON;
            case UNCOMMON -> Rank.UNCOMMON;
            case RARE -> Rank.RARE;
            case VERY_RARE -> Rank.VERY_RARE;
        };
    }

    private static Rank higher(Rank first, Rank second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }

    private static Rank promote(Rank rank, int levels) {
        return Rank.values()[Math.min(rank.ordinal() + levels, Rank.ENDGAME.ordinal())];
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
