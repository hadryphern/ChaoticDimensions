package net.blue.chaoticd.rarity;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Automatic item/enchantment rarity resolution with one documented priority pipeline. */
public final class RarityResolver {
    public static final String NBT_ROOT = "ChaoticDimensions";
    public static final String NBT_RARITY = "Rarity";

    private static final RarityResolver GLOBAL = new RarityResolver(RarityRegistry.global(), true);

    private final RarityRegistry registry;
    private final boolean bootstrapDefaults;

    /** Creates a resolver for a custom registry. The global resolver contains the mod defaults. */
    public RarityResolver(RarityRegistry registry) {
        this(registry, false);
    }

    private RarityResolver(RarityRegistry registry, boolean bootstrapDefaults) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.bootstrapDefaults = bootstrapDefaults;
    }

    public static RarityResolver global() {
        return GLOBAL;
    }

    /**
     * Resolves the final stack rarity. Existing enchantment-score thresholds are deliberately kept.
     * Individual enchantment lines must call {@link #resolveEnchantment(ItemStack, Enchantment, int)}.
     */
    public RarityDefinition resolveItem(ItemStack stack) {
        ensureDefaults();
        // An explicit 1.20.1 NBT override is authoritative; enchantment aggregation applies otherwise.
        Optional<RarityDefinition> explicit = explicitRarity(stack);
        if (explicit.isPresent()) return explicit.get();
        int totalScore = resolveBaseItem(stack).progressionThreshold();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            if (!ModRarities.isSapphireLegendaryLevel(stack, entry.getValue())) {
                totalScore += resolveEnchantment(stack, entry.getKey(), entry.getValue()).enchantmentScore();
            }
        }
        return definitionForScore(totalScore);
    }

    /**
     * Base priority: NBT override, exact item, specialized provider, tag, category provider,
     * vanilla rarity, global fallback.
     */
    public RarityDefinition resolveBaseItem(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        ensureDefaults();
        RarityContext context = RarityContext.forItem(stack);
        Optional<RarityDefinition> result = explicitRarity(stack)
            .or(() -> registry.registeredItem(stack.getItem()))
            .or(() -> resolveItemProviders(registry.itemProviders(), context))
            .or(() -> registry.taggedItem(stack))
            .or(() -> resolveItemProviders(registry.categoryProviders(), context));
        return result.orElseGet(() -> switch (stack.getRarity()) {
            case UNCOMMON -> known(ModRarities.UNCOMMON);
            case RARE -> known(ModRarities.RARE);
            case EPIC -> known(ModRarities.VERY_RARE);
            default -> registry.fallback();
        });
    }

    /**
     * Enchantment priority: contextual provider (Sapphire L), exact enchantment registration,
     * vanilla Enchantment.Rarity, fallback. Providers are evaluated by explicit numeric priority.
     */
    public RarityDefinition resolveEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        ensureDefaults();
        RarityContext context = RarityContext.forEnchantment(stack, enchantment, level);
        for (RarityRegistry.ProviderEntry<EnchantmentRarityProvider> entry : registry.enchantmentProviders()) {
            Optional<RarityDefinition> result = entry.provider().resolve(context, registry);
            if (result.isPresent()) return result.get();
        }
        Optional<RarityDefinition> registered = registry.registeredEnchantment(enchantment);
        if (registered.isPresent()) return registered.get();
        return switch (enchantment.getRarity()) {
            case COMMON -> known(ModRarities.COMMON);
            case UNCOMMON -> known(ModRarities.UNCOMMON);
            case RARE -> known(ModRarities.RARE);
            case VERY_RARE -> known(ModRarities.VERY_RARE);
        };
    }

    public Optional<RarityDefinition> explicitRarity(ItemStack stack) {
        ensureDefaults();
        CompoundTag root = stack.getTagElement(NBT_ROOT);
        if (root == null || !root.contains(NBT_RARITY, Tag.TAG_STRING)) return Optional.empty();
        ResourceLocation id = ResourceLocation.tryParse(root.getString(NBT_RARITY));
        return id == null ? Optional.empty() : registry.definition(id);
    }

    /** 1.20.1-compatible explicit override: {ChaoticDimensions:{Rarity:"namespace:id"}}. */
    public void setExplicitRarity(ItemStack stack, RarityDefinition definition) {
        ensureDefaults();
        registry.require(definition.id());
        stack.getOrCreateTagElement(NBT_ROOT).putString(NBT_RARITY, definition.id().toString());
    }

    public void clearExplicitRarity(ItemStack stack) {
        CompoundTag root = stack.getTagElement(NBT_ROOT);
        if (root == null) return;
        root.remove(NBT_RARITY);
        if (root.isEmpty()) stack.removeTagKey(NBT_ROOT);
    }

    public RarityDefinition definitionForScore(int score) {
        ensureDefaults();
        RarityDefinition result = registry.fallback();
        for (RarityDefinition candidate : registry.definitionsByProgression()) {
            if (score >= candidate.progressionThreshold()) result = candidate;
        }
        return result;
    }

    private Optional<RarityDefinition> resolveItemProviders(
        Iterable<RarityRegistry.ProviderEntry<ItemRarityProvider>> providers, RarityContext context
    ) {
        for (RarityRegistry.ProviderEntry<ItemRarityProvider> entry : providers) {
            Optional<RarityDefinition> result = entry.provider().resolve(context, registry);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }

    private RarityDefinition known(RarityDefinition definition) {
        return registry.definition(definition.id()).orElseGet(registry::fallback);
    }

    private void ensureDefaults() {
        if (bootstrapDefaults) ModRarities.bootstrap();
    }
}
