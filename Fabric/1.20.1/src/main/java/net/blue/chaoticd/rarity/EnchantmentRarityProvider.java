package net.blue.chaoticd.rarity;

import java.util.Optional;

/** Extensible context-sensitive rule for one enchantment line. */
@FunctionalInterface
public interface EnchantmentRarityProvider {
    Optional<RarityDefinition> resolve(RarityContext context, RarityRegistry registry);
}
