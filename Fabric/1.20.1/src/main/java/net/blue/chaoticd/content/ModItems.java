package net.blue.chaoticd.content;

import java.util.Map;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.item.ChaoticAppleItem;
import net.blue.chaoticd.content.item.CrystalineSeeItem;
import net.blue.chaoticd.content.item.DeathTotemItem;
import net.blue.chaoticd.content.item.EmeraldArmorMaterial;
import net.blue.chaoticd.content.item.EmeraldTier;
import net.blue.chaoticd.content.item.FutureProgressionTier;
import net.blue.chaoticd.content.item.JaxyArmorMaterial;
import net.blue.chaoticd.content.item.JaxyTier;
import net.blue.chaoticd.content.item.LeatherBackpackItem;
import net.blue.chaoticd.content.item.ProgressionMaterial;
import net.blue.chaoticd.content.item.RosalitaArmorMaterial;
import net.blue.chaoticd.content.item.RosalitaTier;
import net.blue.chaoticd.content.item.RubyArmorMaterial;
import net.blue.chaoticd.content.item.RubyTier;
import net.blue.chaoticd.content.item.TitaniumArmorMaterial;
import net.blue.chaoticd.content.item.TitaniumTier;
import net.blue.chaoticd.content.item.SapphireSwordItem;
import net.blue.chaoticd.content.item.SapphireTier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/** Items belonging to Chaotic Dimensions progression. */
public final class ModItems {
    public static final Item SAPPHIRE_GEM = register(
        "sapphire_gem",
        new Item(new Item.Properties().fireResistant())
    );

    public static final Item SAPPHIRE_SWORD = register(
        "sapphire_sword",
        new SapphireSwordItem(new Item.Properties().fireResistant())
    );

    public static final Item SAPPHIRE_PICKAXE = register(
        "sapphire_pickaxe",
        new PickaxeItem(
            SapphireTier.INSTANCE,
            ProgressionMaterial.SAPPHIRE.pickaxeAttackModifier(),
            -2.8F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item SAPPHIRE_AXE = register(
        "sapphire_axe",
        new AxeItem(
            SapphireTier.INSTANCE,
            ProgressionMaterial.SAPPHIRE.axeAttackModifier(),
            -3.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item SAPPHIRE_SHOVEL = register(
        "sapphire_shovel",
        new ShovelItem(
            SapphireTier.INSTANCE,
            ProgressionMaterial.SAPPHIRE.pickaxeAttackModifier(),
            -3.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item SAPPHIRE_HOE = register(
        "sapphire_hoe",
        new HoeItem(
            SapphireTier.INSTANCE,
            ProgressionMaterial.SAPPHIRE.hoeAttackModifier(),
            -1.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item CHAOTIC_APPLE = register(
        "chaotic_apple",
        new ChaoticAppleItem(
            new Item.Properties().food(
                new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationMod(1.2F)
                    .alwaysEat()
                    .build()
            )
        )
    );

    public static final Item DEATH_TOTEM = register(
        "death_totem",
        new DeathTotemItem(
            new Item.Properties()
                .stacksTo(1)
                .fireResistant()
        )
    );

    public static final Item EMERALD_INGOT = register(
        "emerald_ingot",
        new Item(
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item DREAM_FLUID_BUCKET = register(
        "dream_fluid_bucket",
        new BucketItem(
            ModFluids.DREAM_FLUID,
            new Item.Properties()
                .craftRemainder(Items.BUCKET)
                .stacksTo(1)
        )
    );

    public static final Item CRYSTALINE_SEE = register(
        "crystaline_see",
        new CrystalineSeeItem(
            new Item.Properties()
                .stacksTo(16)
        )
    );



    public static final Item CRYSTALINE_EYE = register(
        "crystaline_eye",
        new Item(new Item.Properties().stacksTo(16))
    );

    public static final Item GOLD_SPECIAL_APPLE = register(
        "gold_special_apple",
        new Item(new Item.Properties().food(
            new FoodProperties.Builder()
                .nutrition(8)
                .saturationMod(1.2F)
                .alwaysEat()
                .build()
        ))
    );

    public static final Item DIMENSION_APPLE = register(
        "dimension_apple",
        new Item(new Item.Properties().food(
            new FoodProperties.Builder()
                .nutrition(6)
                .saturationMod(0.8F)
                .alwaysEat()
                .build()
        ))
    );

    public static final Item LEATHER_BACKPACK = register(
        "leather_backpack",
        new LeatherBackpackItem(new Item.Properties().stacksTo(1))
    );

    public static final Item BEDROCK_STICK = register(
        "bedrock_stick",
        new Item(new Item.Properties())
    );

    public static final Item ROSALITA_GEM = register(
        "rosalita_gem",
        new Item(new Item.Properties())
    );

    /* New material assets. Their sources are deliberately added only when a
     * matching biome, block texture and/or mob roster exists. */
    public static final Item SHADOW_GEM = material("shadow_gem");
    public static final Item SHADOW_NUGGET = material("shadow_nugget");
    public static final Item VYLAM_GEM = material("vylam_gem");
    public static final Item CHLOROPHYTE_INGOT = material("chlorophyte_ingot");
    public static final Item HERO_GEM = material("hero_gem");
    public static final Item DERMAN_GEM = material("derman_gem");
    public static final Item SUN_TEAR = material("sun_tear");
    public static final Item SUN_PEAK = material("sun_peak");

    /* Legacy trade and progression materials. They deliberately have no
     * crafting recipes; each source is separately gated by its dimension,
     * entity classification or future content. */
    public static final Item SHADOW_SOUL = tradeMaterial("shadow_soul");
    public static final Item VORTEX_GEM = material("vortex_gem");
    public static final Item VORTEX_SOUL = tradeMaterial("vortex_soul");
    public static final Item VOID_SOUL = tradeMaterial("void_soul");
    public static final Item DEMONITH = tradeMaterial("demonith");
    public static final Item AURORA_SOUL = tradeMaterial("aurora_soul");
    public static final Item CRYSTALINE_SOUL = tradeMaterial("crystaline_soul");
    public static final Item THE_CRYSTALINE = tradeMaterial("the_crystaline");
    public static final Item DEMONIC_SOULD = tradeMaterial("demonic_sould");
    public static final Item THE_UNDERGUER = tradeMaterial("the_underguer");
    public static final Item THE_CALICE_QUEEN = tradeMaterial("the_calice_queen");
    public static final Item MONTHRA_SCALE = tradeMaterial("monthra_scale");
    public static final Item CRYSTALINE_SIGIL = tradeMaterial("crystaline_sigil");
    public static final Item UNDERGUER_SIGIL = tradeMaterial("underguer_sigil");
    public static final Item VOID = tradeMaterial("void");

    public static final Item SHADOW_SWORD = register(
        "shadow_sword",
        new SwordItem(
            FutureProgressionTier.SHADOW,
            ProgressionMaterial.SHADOW.swordAttackModifier(),
            -2.4F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item SHADOW_PICKAXE = register(
        "shadow_pickaxe",
        new PickaxeItem(
            FutureProgressionTier.SHADOW,
            ProgressionMaterial.SHADOW.pickaxeAttackModifier(),
            -2.8F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item CHLOROPHYTE_PICKAXE = register(
        "chlorophyte_pickaxe",
        new PickaxeItem(
            FutureProgressionTier.CHLOROPHYTE,
            ProgressionMaterial.CHLOROPHYTE.pickaxeAttackModifier(),
            -2.8F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item HERO_SWORD = register(
        "hero_sword",
        new SwordItem(
            FutureProgressionTier.HERO,
            ProgressionMaterial.HERO.swordAttackModifier(),
            -2.4F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item HERO_AXE = register(
        "hero_axe",
        new AxeItem(
            FutureProgressionTier.HERO,
            ProgressionMaterial.HERO.axeAttackModifier(),
            -3.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item HERO_PICKAXE = register(
        "hero_pickaxe",
        new PickaxeItem(
            FutureProgressionTier.HERO,
            ProgressionMaterial.HERO.pickaxeAttackModifier(),
            -2.8F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item HERO_SHOVEL = register(
        "hero_shovel",
        new ShovelItem(
            FutureProgressionTier.HERO,
            ProgressionMaterial.HERO.pickaxeAttackModifier(),
            -3.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item HERO_HOE = register(
        "hero_hoe",
        new HoeItem(
            FutureProgressionTier.HERO,
            ProgressionMaterial.HERO.hoeAttackModifier(),
            -1.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item WATER_INGOT = register(
        "water_ingot",
        new Item(new Item.Properties())
    );

    public static final Item LAVA_INGOT = register(
        "lava_ingot",
        new Item(new Item.Properties().fireResistant())
    );

    public static final Item TITANIUM_INGOT = register(
        "titanium_ingot",
        new Item(new Item.Properties())
    );

    /** Smithing template named Titan Sould in-game, following the supplied plan. */
    public static final Item TITAN_SOULD = register(
        "titan_sould",
        new Item(new Item.Properties().stacksTo(16).fireResistant())
    );

    /** Jaxy material mined from Jax/Jaxy ore variants. */
    public static final Item JAXY_GEM = register(
        "jaxy_gem",
        new Item(new Item.Properties().fireResistant())
    );

    /** Smithing template named Solar Obsidian in-game. */
    public static final Item SOLAR_OBSIDIAN = register(
        "solar_obsidian",
        new Item(new Item.Properties().stacksTo(16).fireResistant())
    );

    public static final Item JAXY_SWORD = register(
        "jaxy_sword",
        new SwordItem(JaxyTier.INSTANCE, ProgressionMaterial.JAXY.swordAttackModifier(), -2.4F, new Item.Properties().fireResistant())
    );

    public static final Item JAXY_PICKAXE = register(
        "jaxy_pickaxe",
        new PickaxeItem(JaxyTier.INSTANCE, ProgressionMaterial.JAXY.pickaxeAttackModifier(), -2.8F, new Item.Properties().fireResistant())
    );

    public static final Item JAXY_AXE = register(
        "jaxy_axe",
        new AxeItem(JaxyTier.INSTANCE, ProgressionMaterial.JAXY.axeAttackModifier(), -3.0F, new Item.Properties().fireResistant())
    );

    public static final Item JAXY_SHOVEL = register(
        "jaxy_shovel",
        new ShovelItem(JaxyTier.INSTANCE, ProgressionMaterial.JAXY.pickaxeAttackModifier(), -3.0F, new Item.Properties().fireResistant())
    );

    public static final Item JAXY_HOE = register(
        "jaxy_hoe",
        new HoeItem(JaxyTier.INSTANCE, ProgressionMaterial.JAXY.hoeAttackModifier(), 0.0F, new Item.Properties().fireResistant())
    );

    public static final Item JAXY_HELMET = register(
        "jaxy_helmet",
        new ArmorItem(JaxyArmorMaterial.INSTANCE, ArmorItem.Type.HELMET,
            new Item.Properties().fireResistant())
    );

    public static final Item JAXY_CHESTPLATE = register(
        "jaxy_chestplate",
        new ArmorItem(JaxyArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE,
            new Item.Properties().fireResistant())
    );

    public static final Item JAXY_LEGGINGS = register(
        "jaxy_leggings",
        new ArmorItem(JaxyArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS,
            new Item.Properties().fireResistant())
    );

    public static final Item JAXY_BOOTS = register(
        "jaxy_boots",
        new ArmorItem(JaxyArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS,
            new Item.Properties().fireResistant())
    );

    public static final Item TITANIUM_SWORD = register(
        "titanium_sword",
        new SwordItem(TitaniumTier.INSTANCE, ProgressionMaterial.TITANIUM.swordAttackModifier(), -2.4F, new Item.Properties().fireResistant())
    );

    public static final Item TITANIUM_PICKAXE = register(
        "titanium_pickaxe",
        new PickaxeItem(TitaniumTier.INSTANCE, ProgressionMaterial.TITANIUM.pickaxeAttackModifier(), -2.8F, new Item.Properties().fireResistant())
    );

    public static final Item TITANIUM_AXE = register(
        "titanium_axe",
        new AxeItem(TitaniumTier.INSTANCE, ProgressionMaterial.TITANIUM.axeAttackModifier(), -3.0F, new Item.Properties().fireResistant())
    );

    public static final Item TITANIUM_SHOVEL = register(
        "titanium_shovel",
        new ShovelItem(TitaniumTier.INSTANCE, ProgressionMaterial.TITANIUM.pickaxeAttackModifier(), -3.0F, new Item.Properties().fireResistant())
    );

    public static final Item TITANIUM_HOE = register(
        "titanium_hoe",
        new HoeItem(TitaniumTier.INSTANCE, ProgressionMaterial.TITANIUM.hoeAttackModifier(), 0.0F, new Item.Properties().fireResistant())
    );

    public static final Item TITANIUM_HELMET = register(
        "titanium_helmet",
        new ArmorItem(
            TitaniumArmorMaterial.INSTANCE,
            ArmorItem.Type.HELMET,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item TITANIUM_CHESTPLATE = register(
        "titanium_chestplate",
        new ArmorItem(
            TitaniumArmorMaterial.INSTANCE,
            ArmorItem.Type.CHESTPLATE,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item TITANIUM_LEGGINGS = register(
        "titanium_leggings",
        new ArmorItem(
            TitaniumArmorMaterial.INSTANCE,
            ArmorItem.Type.LEGGINGS,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item TITANIUM_BOOTS = register(
        "titanium_boots",
        new ArmorItem(
            TitaniumArmorMaterial.INSTANCE,
            ArmorItem.Type.BOOTS,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item ROSALITA_SWORD = register(
        "rosalita_sword",
        new SwordItem(RosalitaTier.INSTANCE, ProgressionMaterial.ROSALITA.swordAttackModifier(), -2.4F, new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_PICKAXE = register(
        "rosalita_pickaxe",
        new PickaxeItem(RosalitaTier.INSTANCE, ProgressionMaterial.ROSALITA.pickaxeAttackModifier(), -2.8F, new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_AXE = register(
        "rosalita_axe",
        new AxeItem(RosalitaTier.INSTANCE, ProgressionMaterial.ROSALITA.axeAttackModifier(), -3.0F, new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_SHOVEL = register(
        "rosalita_shovel",
        new ShovelItem(RosalitaTier.INSTANCE, ProgressionMaterial.ROSALITA.pickaxeAttackModifier(), -3.0F, new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_HOE = register(
        "rosalita_hoe",
        new HoeItem(RosalitaTier.INSTANCE, ProgressionMaterial.ROSALITA.hoeAttackModifier(), 0.0F, new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_HELMET = register(
        "rosalita_helmet",
        new ArmorItem(RosalitaArmorMaterial.INSTANCE, ArmorItem.Type.HELMET,
            new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_CHESTPLATE = register(
        "rosalita_chestplate",
        new ArmorItem(RosalitaArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE,
            new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_LEGGINGS = register(
        "rosalita_leggings",
        new ArmorItem(RosalitaArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS,
            new Item.Properties().fireResistant())
    );

    public static final Item ROSALITA_BOOTS = register(
        "rosalita_boots",
        new ArmorItem(RosalitaArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS,
            new Item.Properties().fireResistant())
    );

    public static final Item RUBY_NUGGET = register(
        "ruby_nugget",
        new Item(new Item.Properties().fireResistant())
    );

    public static final Item RUBY = register(
        "ruby",
        new Item(new Item.Properties().fireResistant())
    );

    public static final Item RUBY_PLATE = register(
        "ruby_plate",
        new Item(new Item.Properties().fireResistant())
    );

    public static final Item RUBY_SWORD = register(
        "ruby_sword",
        new SwordItem(
            RubyTier.INSTANCE,
            ProgressionMaterial.RUBY.swordAttackModifier(),
            -2.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_PICKAXE = register(
        "ruby_pickaxe",
        new PickaxeItem(
            RubyTier.INSTANCE,
            ProgressionMaterial.RUBY.pickaxeAttackModifier(),
            -2.4F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_AXE = register(
        "ruby_axe",
        new AxeItem(
            RubyTier.INSTANCE,
            ProgressionMaterial.RUBY.axeAttackModifier(),
            -2.6F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_SHOVEL = register(
        "ruby_shovel",
        new ShovelItem(
            RubyTier.INSTANCE,
            ProgressionMaterial.RUBY.pickaxeAttackModifier(),
            -2.4F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_HOE = register(
        "ruby_hoe",
        new HoeItem(
            RubyTier.INSTANCE,
            ProgressionMaterial.RUBY.hoeAttackModifier(),
            0.0F,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_HELMET = register(
        "ruby_helmet",
        new ArmorItem(
            RubyArmorMaterial.INSTANCE,
            ArmorItem.Type.HELMET,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_CHESTPLATE = register(
        "ruby_chestplate",
        new ArmorItem(
            RubyArmorMaterial.INSTANCE,
            ArmorItem.Type.CHESTPLATE,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_LEGGINGS = register(
        "ruby_leggings",
        new ArmorItem(
            RubyArmorMaterial.INSTANCE,
            ArmorItem.Type.LEGGINGS,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item RUBY_BOOTS = register(
        "ruby_boots",
        new ArmorItem(
            RubyArmorMaterial.INSTANCE,
            ArmorItem.Type.BOOTS,
            new Item.Properties().fireResistant()
        )
    );

    public static final Item AURORA_PEARL = register(
        "aurora_pearl",
        new Item(new Item.Properties().stacksTo(16))
    );

    public static final Item DIMENSION_PIG_SPAWN_EGG = register(
        "dimension_pig_spawn_egg",
        new SpawnEggItem(ModEntities.DIMENSION_PIG, 0x8A4B79, 0xE5A6D7, new Item.Properties())
    );

    public static final Item GOLD_DIMENSION_PIG_SPAWN_EGG = register(
        "gold_dimension_pig_spawn_egg",
        new SpawnEggItem(ModEntities.GOLD_DIMENSION_PIG, 0xD5A82A, 0xFFF08A, new Item.Properties())
    );

    public static final Item APPLE_COW_SPAWN_EGG = register(
        "apple_cow_spawn_egg",
        new SpawnEggItem(ModEntities.APPLE_COW, 0x6C2E1A, 0xD92B2B, new Item.Properties())
    );

    public static final Item GOLDEN_APPLE_COW_SPAWN_EGG = register(
        "golden_apple_cow_spawn_egg",
        new SpawnEggItem(ModEntities.GOLDEN_APPLE_COW, 0xC69A23, 0xFFF1A0, new Item.Properties())
    );

    public static final Item CRYSTAL_APPLE_COW_SPAWN_EGG = register(
        "crystal_apple_cow_spawn_egg",
        new SpawnEggItem(ModEntities.CRYSTAL_APPLE_COW, 0x8D6FD1, 0x65E6F4, new Item.Properties())
    );

    public static final Item CRYSTAL_GOLDEN_APPLE_SPAWN_EGG = register(
        "crystal_golden_apple_spawn_egg",
        new SpawnEggItem(ModEntities.CRYSTAL_GOLDEN_APPLE, 0xA96ED4, 0xFFD75A, new Item.Properties())
    );

    public static final Item CRYSTAL_CREEPER_SPAWN_EGG = register(
        "crystal_creeper_spawn_egg",
        new SpawnEggItem(ModEntities.CRYSTAL_CREEPER, 0x7869C6, 0xD6F5FF, new Item.Properties())
    );

    /*
     * The Emerald tools are built around a tier with twice Netherite's
     * durability, mining speed and attack bonus.
     *
     * Their constructor damage values were also adjusted so the final attacks
     * remain a clear direct upgrade instead of merely reusing Netherite values.
     */
    public static final Item EMERALD_SWORD = register(
        "emerald_sword",
        new SwordItem(
            EmeraldTier.INSTANCE,
            ProgressionMaterial.EMERALD.swordAttackModifier(),
            -2.4F,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_PICKAXE = register(
        "emerald_pickaxe",
        new PickaxeItem(
            EmeraldTier.INSTANCE,
            ProgressionMaterial.EMERALD.pickaxeAttackModifier(),
            -2.8F,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_AXE = register(
        "emerald_axe",
        new AxeItem(
            EmeraldTier.INSTANCE,
            ProgressionMaterial.EMERALD.axeAttackModifier(),
            -3.0F,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_SHOVEL = register(
        "emerald_shovel",
        new ShovelItem(
            EmeraldTier.INSTANCE,
            ProgressionMaterial.EMERALD.pickaxeAttackModifier(),
            -3.0F,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_HOE = register(
        "emerald_hoe",
        new HoeItem(
            EmeraldTier.INSTANCE,
            ProgressionMaterial.EMERALD.hoeAttackModifier(),
            0.0F,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_HELMET = register(
        "emerald_helmet",
        new ArmorItem(
            EmeraldArmorMaterial.INSTANCE,
            ArmorItem.Type.HELMET,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_CHESTPLATE = register(
        "emerald_chestplate",
        new ArmorItem(
            EmeraldArmorMaterial.INSTANCE,
            ArmorItem.Type.CHESTPLATE,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_LEGGINGS = register(
        "emerald_leggings",
        new ArmorItem(
            EmeraldArmorMaterial.INSTANCE,
            ArmorItem.Type.LEGGINGS,
            new Item.Properties()
                .fireResistant()
        )
    );

    public static final Item EMERALD_BOOTS = register(
        "emerald_boots",
        new ArmorItem(
            EmeraldArmorMaterial.INSTANCE,
            ArmorItem.Type.BOOTS,
            new Item.Properties()
                .fireResistant()
        )
    );

    private ModItems() {
    }

    private static Item register(String id, Item item) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            new ResourceLocation(
                ChaoticDimensions.MOD_ID,
                id
            ),
            item
        );
    }

    private static Item tradeMaterial(String id) {
        return register(id, new Item(new Item.Properties().fireResistant()));
    }

    private static Item material(String id) {
        return register(id, new Item(new Item.Properties().fireResistant()));
    }

    /**
     * Canonical Sapphire Sword stack used after it actually enters a player's
     * inventory. Creative tabs intentionally use the raw item instead.
     */
    public static ItemStack createSapphireSword() {
        ItemStack result =
            new ItemStack(SAPPHIRE_SWORD);

        EnchantmentHelper.setEnchantments(
            Map.of(
                ModEnchantments.SAPPHIRIC,
                1,
                ModEnchantments.DHEATHIC,
                1
            ),
            result
        );

        return result;
    }

    public static ItemStack createSapphireTool(
        SapphireToolType type
    ) {
        ItemStack result =
            new ItemStack(type.item());

        Map<
            net.minecraft.world.item.enchantment.Enchantment,
            Integer
        > enchantments =
            switch (type) {
                case PICKAXE -> Map.of(
                    Enchantments.BLOCK_FORTUNE,
                    50,
                    Enchantments.BLOCK_EFFICIENCY,
                    50,
                    Enchantments.UNBREAKING,
                    50
                );
                case AXE -> Map.of(
                    Enchantments.BLOCK_FORTUNE,
                    50,
                    Enchantments.BLOCK_EFFICIENCY,
                    50,
                    Enchantments.UNBREAKING,
                    50,
                    Enchantments.SHARPNESS,
                    50
                );
                case SHOVEL, HOE -> Map.of(
                    Enchantments.BLOCK_EFFICIENCY,
                    50,
                    Enchantments.UNBREAKING,
                    50
                );
            };

        EnchantmentHelper.setEnchantments(
            enchantments,
            result
        );

        return result;
    }

    public static boolean isSapphireGear(
        ItemStack stack
    ) {
        return stack.is(SAPPHIRE_SWORD)
            || stack.is(SAPPHIRE_PICKAXE)
            || stack.is(SAPPHIRE_AXE)
            || stack.is(SAPPHIRE_SHOVEL)
            || stack.is(SAPPHIRE_HOE);
    }

    public static boolean isEmeraldGear(
        ItemStack stack
    ) {
        return stack.is(EMERALD_SWORD)
            || stack.is(EMERALD_PICKAXE)
            || stack.is(EMERALD_AXE)
            || stack.is(EMERALD_SHOVEL)
            || stack.is(EMERALD_HOE)
            || stack.is(EMERALD_HELMET)
            || stack.is(EMERALD_CHESTPLATE)
            || stack.is(EMERALD_LEGGINGS)
            || stack.is(EMERALD_BOOTS);
    }

    /** Returns the centralized material profile for currently registered gear. */
    public static ProgressionMaterial progressionMaterial(
        ItemStack stack
    ) {
        if (stack.is(EMERALD_SWORD)
            || stack.is(EMERALD_PICKAXE)
            || stack.is(EMERALD_AXE)
            || stack.is(EMERALD_SHOVEL)
            || stack.is(EMERALD_HOE)
            || isEmeraldArmor(stack)) {
            return ProgressionMaterial.EMERALD;
        }
        if (stack.is(RUBY_SWORD)
            || stack.is(RUBY_PICKAXE)
            || stack.is(RUBY_AXE)
            || stack.is(RUBY_SHOVEL)
            || stack.is(RUBY_HOE)
            || stack.is(RUBY_HELMET)
            || stack.is(RUBY_CHESTPLATE)
            || stack.is(RUBY_LEGGINGS)
            || stack.is(RUBY_BOOTS)) {
            return ProgressionMaterial.RUBY;
        }
        if (stack.is(JAXY_SWORD)
            || stack.is(JAXY_PICKAXE)
            || stack.is(JAXY_AXE)
            || stack.is(JAXY_SHOVEL)
            || stack.is(JAXY_HOE)
            || stack.is(JAXY_HELMET)
            || stack.is(JAXY_CHESTPLATE)
            || stack.is(JAXY_LEGGINGS)
            || stack.is(JAXY_BOOTS)) {
            return ProgressionMaterial.JAXY;
        }
        if (stack.is(CHLOROPHYTE_PICKAXE)) {
            return ProgressionMaterial.CHLOROPHYTE;
        }
        if (stack.is(TITANIUM_SWORD)
            || stack.is(TITANIUM_PICKAXE)
            || stack.is(TITANIUM_AXE)
            || stack.is(TITANIUM_SHOVEL)
            || stack.is(TITANIUM_HOE)
            || stack.is(TITANIUM_HELMET)
            || stack.is(TITANIUM_CHESTPLATE)
            || stack.is(TITANIUM_LEGGINGS)
            || stack.is(TITANIUM_BOOTS)) {
            return ProgressionMaterial.TITANIUM;
        }
        if (stack.is(HERO_SWORD)
            || stack.is(HERO_PICKAXE)
            || stack.is(HERO_AXE)
            || stack.is(HERO_SHOVEL)
            || stack.is(HERO_HOE)) {
            return ProgressionMaterial.HERO;
        }
        if (stack.is(ROSALITA_SWORD)
            || stack.is(ROSALITA_PICKAXE)
            || stack.is(ROSALITA_AXE)
            || stack.is(ROSALITA_SHOVEL)
            || stack.is(ROSALITA_HOE)
            || stack.is(ROSALITA_HELMET)
            || stack.is(ROSALITA_CHESTPLATE)
            || stack.is(ROSALITA_LEGGINGS)
            || stack.is(ROSALITA_BOOTS)) {
            return ProgressionMaterial.ROSALITA;
        }
        if (isSapphireGear(stack)) {
            return ProgressionMaterial.SAPPHIRE;
        }
        if (stack.is(SHADOW_SWORD) || stack.is(SHADOW_PICKAXE)) {
            return ProgressionMaterial.SHADOW;
        }

        return null;
    }

    public static boolean isEmeraldArmor(
        ItemStack stack
    ) {
        return stack.is(EMERALD_HELMET)
            || stack.is(EMERALD_CHESTPLATE)
            || stack.is(EMERALD_LEGGINGS)
            || stack.is(EMERALD_BOOTS);
    }

    public enum SapphireToolType {
        PICKAXE(SAPPHIRE_PICKAXE),
        AXE(SAPPHIRE_AXE),
        SHOVEL(SAPPHIRE_SHOVEL),
        HOE(SAPPHIRE_HOE);

        private final Item item;

        SapphireToolType(Item item) {
            this.item = item;
        }

        public Item item() {
            return item;
        }
    }

    public static void initialize() {
        // Static fields perform registry insertion.
    }
}
