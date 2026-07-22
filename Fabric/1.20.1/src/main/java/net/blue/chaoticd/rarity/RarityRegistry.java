package net.blue.chaoticd.rarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Central registry for rarity definitions and resolution rules.
 *
 * <p>Registration is synchronized and normally happens during mod bootstrap. Reads use immutable
 * snapshots, so tooltip rendering never iterates a list being modified by another thread.</p>
 */
public final class RarityRegistry {
    private static final RarityRegistry GLOBAL = new RarityRegistry();

    private final Map<ResourceLocation, RarityDefinition> definitions = new LinkedHashMap<>();
    private final Map<Item, ResourceLocation> items = new IdentityHashMap<>();
    private final Map<Enchantment, ResourceLocation> enchantments = new IdentityHashMap<>();
    private final List<TagRule> itemTags = new ArrayList<>();
    private final List<ProviderEntry<ItemRarityProvider>> itemProviders = new ArrayList<>();
    private final List<ProviderEntry<ItemRarityProvider>> categoryProviders = new ArrayList<>();
    private final List<ProviderEntry<EnchantmentRarityProvider>> enchantmentProviders = new ArrayList<>();

    private volatile Map<ResourceLocation, RarityDefinition> definitionSnapshot = Map.of();
    private volatile List<RarityDefinition> progressionSnapshot = List.of();
    private volatile Map<Item, ResourceLocation> itemSnapshot = Map.of();
    private volatile Map<Enchantment, ResourceLocation> enchantmentSnapshot = Map.of();
    private volatile List<TagRule> itemTagSnapshot = List.of();
    private volatile List<ProviderEntry<ItemRarityProvider>> itemProviderSnapshot = List.of();
    private volatile List<ProviderEntry<ItemRarityProvider>> categoryProviderSnapshot = List.of();
    private volatile List<ProviderEntry<EnchantmentRarityProvider>> enchantmentProviderSnapshot = List.of();
    private volatile ResourceLocation fallbackId;

    public static RarityRegistry global() {
        return GLOBAL;
    }

    public synchronized RarityDefinition registerDefinition(RarityDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (definitions.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalArgumentException("Duplicate rarity definition: " + definition.id());
        }
        definitionSnapshot = Map.copyOf(definitions);
        progressionSnapshot = definitions.values().stream()
            .sorted(Comparator.comparingInt(RarityDefinition::progressionThreshold)
                .thenComparingInt(RarityDefinition::priority))
            .toList();
        return definition;
    }

    public synchronized void setFallback(RarityDefinition definition) {
        requireRegistered(definition);
        fallbackId = definition.id();
    }

    public synchronized void registerItem(Item item, RarityDefinition definition) {
        requireRegistered(definition);
        items.put(Objects.requireNonNull(item, "item"), definition.id());
        itemSnapshot = Map.copyOf(items);
    }

    public synchronized void registerItemTag(TagKey<Item> tag, RarityDefinition definition) {
        requireRegistered(definition);
        itemTags.add(new TagRule(Objects.requireNonNull(tag, "tag"), definition.id()));
        itemTagSnapshot = List.copyOf(itemTags);
    }

    /** Registers an NBT/state-sensitive item rule. Higher priority executes first. */
    public synchronized void registerItemProvider(int priority, ItemRarityProvider provider) {
        itemProviders.add(new ProviderEntry<>(priority, Objects.requireNonNull(provider, "provider")));
        itemProviders.sort(ProviderEntry.HIGHEST_PRIORITY_FIRST);
        itemProviderSnapshot = List.copyOf(itemProviders);
    }

    /** Registers a broad material/category fallback. Higher priority executes first. */
    public synchronized void registerCategoryProvider(int priority, ItemRarityProvider provider) {
        categoryProviders.add(new ProviderEntry<>(priority, Objects.requireNonNull(provider, "provider")));
        categoryProviders.sort(ProviderEntry.HIGHEST_PRIORITY_FIRST);
        categoryProviderSnapshot = List.copyOf(categoryProviders);
    }

    public synchronized void registerEnchantment(Enchantment enchantment, RarityDefinition definition) {
        requireRegistered(definition);
        enchantments.put(Objects.requireNonNull(enchantment, "enchantment"), definition.id());
        enchantmentSnapshot = Map.copyOf(enchantments);
    }

    /** Registers a stack/level-sensitive enchantment rule. Higher priority executes first. */
    public synchronized void registerEnchantmentProvider(int priority, EnchantmentRarityProvider provider) {
        enchantmentProviders.add(new ProviderEntry<>(priority, Objects.requireNonNull(provider, "provider")));
        enchantmentProviders.sort(ProviderEntry.HIGHEST_PRIORITY_FIRST);
        enchantmentProviderSnapshot = List.copyOf(enchantmentProviders);
    }

    public Optional<RarityDefinition> definition(ResourceLocation id) {
        return Optional.ofNullable(definitionSnapshot.get(id));
    }

    public RarityDefinition require(ResourceLocation id) {
        return definition(id).orElseThrow(() -> new IllegalArgumentException("Unknown rarity: " + id));
    }

    public RarityDefinition fallback() {
        ResourceLocation currentFallback = fallbackId;
        if (currentFallback == null) {
            throw new IllegalStateException("No fallback rarity has been registered");
        }
        return require(currentFallback);
    }

    public List<RarityDefinition> definitionsByProgression() {
        return progressionSnapshot;
    }

    Optional<RarityDefinition> registeredItem(Item item) {
        return definitionNullable(itemSnapshot.get(item));
    }

    Optional<RarityDefinition> registeredEnchantment(Enchantment enchantment) {
        return definitionNullable(enchantmentSnapshot.get(enchantment));
    }

    Optional<RarityDefinition> taggedItem(ItemStack stack) {
        for (TagRule rule : itemTagSnapshot) {
            if (stack.is(rule.tag())) return definition(rule.definitionId());
        }
        return Optional.empty();
    }

    List<ProviderEntry<ItemRarityProvider>> itemProviders() {
        return itemProviderSnapshot;
    }

    List<ProviderEntry<ItemRarityProvider>> categoryProviders() {
        return categoryProviderSnapshot;
    }

    List<ProviderEntry<EnchantmentRarityProvider>> enchantmentProviders() {
        return enchantmentProviderSnapshot;
    }

    private Optional<RarityDefinition> definitionNullable(ResourceLocation nullableId) {
        return nullableId == null ? Optional.empty() : definition(nullableId);
    }

    private void requireRegistered(RarityDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        RarityDefinition registered = definitions.get(definition.id());
        if (registered != definition) {
            throw new IllegalArgumentException("Definition must be registered first: " + definition.id());
        }
    }

    record TagRule(TagKey<Item> tag, ResourceLocation definitionId) {
    }

    record ProviderEntry<T>(int priority, T provider) {
        private static final Comparator<ProviderEntry<?>> HIGHEST_PRIORITY_FIRST =
            Comparator.<ProviderEntry<?>>comparingInt(ProviderEntry::priority).reversed();
    }
}
