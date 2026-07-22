package net.blue.chaoticd.content;

import net.blue.chaoticd.rarity.ModRarities;
import net.blue.chaoticd.rarity.RarityDefinition;
import net.blue.chaoticd.rarity.RarityResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Backwards-compatible facade for the original rarity API.
 *
 * @deprecated New code should use {@link RarityResolver}, {@link net.blue.chaoticd.rarity.RarityRegistry}
 * and {@link RarityDefinition} directly.
 */
@Deprecated(forRemoval = false)
public final class ModItemRarities {
    public enum Rank {
        COMMON(ModRarities.COMMON.id()),
        UNCOMMON(ModRarities.UNCOMMON.id()),
        RARE(ModRarities.RARE.id()),
        VERY_RARE(ModRarities.VERY_RARE.id()),
        EXTREMELY_RARE(ModRarities.EXTREMELY_RARE.id()),
        ULTRA_RARE(ModRarities.ULTRA_RARE.id()),
        IMPOSSIBLE(ModRarities.IMPOSSIBLE.id()),
        FORBIDDEN(ModRarities.FORBIDDEN.id()),
        LEGENDARY(ModRarities.LEGENDARY.id()),
        EXTRAVAGANT(ModRarities.EXTRAVAGANT.id()),
        GOD(ModRarities.GOD.id()),
        ENDGAME(ModRarities.ENDGAME.id());

        private final ResourceLocation definitionId;

        Rank(ResourceLocation definitionId) {
            this.definitionId = definitionId;
        }

        public RarityDefinition definition() {
            return ModRarities.definition(definitionId);
        }

        public String translationKey() {
            return definition().translationKey();
        }

        public int color() {
            return definition().staticColor();
        }

        public int threshold() {
            return definition().progressionThreshold();
        }

        public static Rank fromDefinition(RarityDefinition definition) {
            for (Rank rank : values()) {
                if (rank.definitionId.equals(definition.id())) return rank;
            }
            return COMMON;
        }
    }

    private ModItemRarities() {
    }

    public static boolean isSapphire(ItemStack stack) {
        return ModRarities.isSapphire(stack);
    }

    public static Rank rank(ItemStack stack) {
        return Rank.fromDefinition(RarityResolver.global().resolveItem(stack));
    }

    public static Rank enchantmentRank(ItemStack stack, Enchantment enchantment, int level) {
        return Rank.fromDefinition(RarityResolver.global().resolveEnchantment(stack, enchantment, level));
    }

    public static boolean isChaoticEnchantment(Enchantment enchantment) {
        return ModRarities.isChaoticEnchantment(enchantment);
    }
}
