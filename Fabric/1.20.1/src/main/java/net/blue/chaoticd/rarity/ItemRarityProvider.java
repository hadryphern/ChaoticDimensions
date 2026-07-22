package net.blue.chaoticd.rarity;

import java.util.Optional;

/** Extensible rule for ItemStacks whose rarity depends on state, NBT or another runtime property. */
@FunctionalInterface
public interface ItemRarityProvider {
    Optional<RarityDefinition> resolve(RarityContext context, RarityRegistry registry);
}
