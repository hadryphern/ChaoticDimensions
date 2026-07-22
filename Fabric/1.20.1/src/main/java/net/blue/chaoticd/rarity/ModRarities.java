package net.blue.chaoticd.rarity;

import java.util.Map;
import java.util.Optional;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.ModEnchantments;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModPotions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/** Built-in Chaotic Dimensions rarity definitions and the preserved progression rules. */
public final class ModRarities {
    public static final ResourceLocation COMMON_ID = id("common");
    private static final long DEFAULT_CYCLE_MILLIS = 3_600L;

    public static final RarityDefinition COMMON = staticRarity("common", 0x55DFFF, 0, 0, 1);
    public static final RarityDefinition UNCOMMON = staticRarity("uncommon", 0x55FF55, 1, 4, 2);
    public static final RarityDefinition RARE = staticRarity("rare", 0x55FFFF, 2, 10, 6);
    public static final RarityDefinition VERY_RARE = staticRarity("very_rare", 0xFF55FF, 3, 20, 16);
    public static final RarityDefinition EXTREMELY_RARE = staticRarity("extremely_rare", 0x5555FF, 4, 36, 25);
    public static final RarityDefinition ULTRA_RARE = staticRarity("ultra_rare", 0xAA00FF, 5, 60, 40);
    public static final RarityDefinition IMPOSSIBLE = staticRarity("impossible", 0xFF5555, 6, 100, 65);
    public static final RarityDefinition FORBIDDEN = animatedRarity("forbidden", 0xAAAAAA, 7, 170, 100,
        0xAAAAAA, 0xFFFFFF);
    public static final RarityDefinition LEGENDARY = RarityDefinition.builder(id("legendary"),
            "rarity.chaoticd.legendary", 0xFFD700)
        .priority(8).progression(280, 150)
        .rainbowAnimation(DEFAULT_CYCLE_MILLIS, 2.0F / 3.0F, 1.0F)
        .style(RarityStyle.PRESERVE_WITH_GRADIENT).fallback(COMMON_ID).build();
    public static final RarityDefinition EXTRAVAGANT = animatedRarity("extravagant", 0xFFD700, 9, 430, 210,
        0xFFD700, 0x55DFFF);
    public static final RarityDefinition GOD = animatedRarity("god", 0xFFD700, 10, 620, 300,
        0xFFD700, 0xFFFFFF);
    public static final RarityDefinition ENDGAME = animatedRarity("endgame", 0xAA00FF, 11, 850, 420,
        0xAA00FF, 0xFF55FF, 0x5555FF, 0x111111);

    private static final RarityDefinition[] DEFINITIONS = {
        COMMON, UNCOMMON, RARE, VERY_RARE, EXTREMELY_RARE, ULTRA_RARE,
        IMPOSSIBLE, FORBIDDEN, LEGENDARY, EXTRAVAGANT, GOD, ENDGAME
    };

    private static final Map<Enchantment, Integer> VANILLA_CAPS = Map.ofEntries(
        Map.entry(Enchantments.SHARPNESS, 5),
        Map.entry(Enchantments.SMITE, 5),
        Map.entry(Enchantments.BANE_OF_ARTHROPODS, 5),
        Map.entry(Enchantments.BLOCK_EFFICIENCY, 5),
        Map.entry(Enchantments.UNBREAKING, 3),
        Map.entry(Enchantments.MOB_LOOTING, 3),
        Map.entry(Enchantments.BLOCK_FORTUNE, 3),
        Map.entry(Enchantments.SWEEPING_EDGE, 3),
        Map.entry(Enchantments.THORNS, 3),
        Map.entry(Enchantments.ALL_DAMAGE_PROTECTION, 4),
        Map.entry(Enchantments.FIRE_PROTECTION, 4),
        Map.entry(Enchantments.BLAST_PROTECTION, 4),
        Map.entry(Enchantments.PROJECTILE_PROTECTION, 4),
        Map.entry(Enchantments.FALL_PROTECTION, 4),
        Map.entry(Enchantments.KNOCKBACK, 2),
        Map.entry(Enchantments.FIRE_ASPECT, 2)
    );

    private static volatile boolean bootstrapped;

    private ModRarities() {
    }

    /** Idempotent lazy bootstrap; safe after normal Fabric content registration has completed. */
    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        RarityRegistry registry = RarityRegistry.global();
        for (RarityDefinition definition : DEFINITIONS) registry.registerDefinition(definition);
        registry.setFallback(COMMON);

        registry.registerItem(ModItems.SAPPHIRE_GEM, LEGENDARY);
        registry.registerItem(ModItems.SAPPHIRE_SWORD, LEGENDARY);
        registry.registerItem(ModItems.SAPPHIRE_PICKAXE, LEGENDARY);
        registry.registerItem(ModItems.SAPPHIRE_AXE, LEGENDARY);
        registry.registerItem(ModItems.SAPPHIRE_SHOVEL, LEGENDARY);
        registry.registerItem(ModItems.SAPPHIRE_HOE, LEGENDARY);

        // Specialized state rule: all four Sapphiric potion item forms share the same Potion NBT.
        registry.registerItemProvider(100, (context, ignored) ->
            PotionUtils.getPotion(context.stack()) == ModPotions.SAPPHIRIC
                ? Optional.of(ULTRA_RARE) : Optional.empty());

        // Existing vanilla-material classifications, now isolated as an extensible category provider.
        registry.registerCategoryProvider(0, (context, ignored) -> {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(context.stack().getItem());
            String path = itemId.getPath();
            if (path.contains("netherite")) return Optional.of(EXTREMELY_RARE);
            if (path.contains("diamond")) return Optional.of(RARE);
            if (path.contains("iron") || path.contains("golden")) return Optional.of(UNCOMMON);
            return Optional.empty();
        });

        // Context comes first so the certified level-L Sapphire kit remains Legendary.
        registry.registerEnchantmentProvider(200, (context, ignored) ->
            isSapphireLegendaryLevel(context.stack(), context.enchantmentLevel())
                ? Optional.of(LEGENDARY) : Optional.empty());

        registry.registerEnchantment(ModEnchantments.SAPPHIRIC, ULTRA_RARE);
        registry.registerEnchantment(ModEnchantments.DHEATHIC, ULTRA_RARE);
        registry.registerEnchantment(ModEnchantments.BIG_BERTHA, ULTRA_RARE);
        registry.registerEnchantment(ModEnchantments.ROYAL, ULTRA_RARE);
        registry.registerEnchantment(ModEnchantments.DISPARADA, ULTRA_RARE);

        registry.registerEnchantmentProvider(0, (context, ignored) -> {
            Integer vanillaCap = VANILLA_CAPS.get(context.enchantment());
            return vanillaCap != null && context.enchantmentLevel() > vanillaCap
                ? Optional.of(VERY_RARE) : Optional.empty();
        });
        bootstrapped = true;
    }

    public static RarityDefinition definition(ResourceLocation id) {
        bootstrap();
        return RarityRegistry.global().require(id);
    }

    public static boolean isSapphire(ItemStack stack) {
        return stack.is(ModItems.SAPPHIRE_GEM) || stack.is(ModItems.SAPPHIRE_SWORD)
            || stack.is(ModItems.SAPPHIRE_PICKAXE) || stack.is(ModItems.SAPPHIRE_AXE)
            || stack.is(ModItems.SAPPHIRE_SHOVEL) || stack.is(ModItems.SAPPHIRE_HOE);
    }

    public static boolean isSapphireLegendaryLevel(ItemStack stack, int level) {
        return isSapphire(stack) && level >= 50;
    }

    public static boolean isChaoticEnchantment(Enchantment enchantment) {
        return enchantment == ModEnchantments.SAPPHIRIC || enchantment == ModEnchantments.DHEATHIC
            || enchantment == ModEnchantments.BIG_BERTHA || enchantment == ModEnchantments.ROYAL
            || enchantment == ModEnchantments.DISPARADA;
    }

    private static RarityDefinition staticRarity(String path, int color, int priority, int threshold, int score) {
        return RarityDefinition.builder(id(path), "rarity.chaoticd." + path, color)
            .priority(priority).progression(threshold, score).fallback(COMMON_ID).build();
    }

    private static RarityDefinition animatedRarity(String path, int color, int priority, int threshold, int score,
                                                    int... palette) {
        return RarityDefinition.builder(id(path), "rarity.chaoticd." + path, color)
            .priority(priority).progression(threshold, score)
            .paletteAnimation(DEFAULT_CYCLE_MILLIS, RarityDefinition.Easing.SMOOTHSTEP, palette)
            .style(RarityStyle.PRESERVE_WITH_GRADIENT).fallback(COMMON_ID).build();
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, path);
    }
}
