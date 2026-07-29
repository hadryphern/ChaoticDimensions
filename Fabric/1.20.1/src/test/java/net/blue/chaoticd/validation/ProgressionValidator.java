package net.blue.chaoticd.validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.blue.chaoticd.content.item.ProgressionMaterial;
import net.blue.chaoticd.content.progression.MiningProgression;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Tiers;

/**
 * Headless acceptance checks for the tool, armor and ore progression.  The
 * in-game drop gate itself remains the Fabric Mining Level API: this
 * validator verifies the exact levels and datapack tags it consumes.
 */
public final class ProgressionValidator {
    private static final Path ROOT = Path.of("build/resources/main");
    private static final Path FABRIC_TAGS = ROOT.resolve(
        "data/fabric/tags/blocks"
    );
    private static final Path MINEABLE_PICKAXE = ROOT.resolve(
        "data/minecraft/tags/blocks/mineable/pickaxe.json"
    );
    private static final Path LOOT_TABLES = ROOT.resolve(
        "data/chaoticd/loot_tables/blocks"
    );

    private static final Map<Integer, Set<String>> EXPECTED_LEVEL_TAGS =
        expectedLevelTags();

    private static final List<String> ORE_LOOT_TABLES = List.of(
        "ruby_ore",
        "deepslate_ruby_ore",
        "nether_ruby_ore",
        "aurora_ruby_ore",
        "jax_ore",
        "deepslate_jaxy_ore",
        "nether_jax_ore",
        "aurora_jax_ore",
        "titanium_ore",
        "deepslate_titanium_ore",
        "rosalita_ore",
        "deepslate_rosalita_ore",
        "nether_rosalita_ore",
        "aurora_rosalita_ore",
        "sapphire_ore",
        "aurora_sapphire_ore"
    );

    private ProgressionValidator() {
    }

    public static void main(String[] args) throws Exception {
        validateMaterialChain();
        validateMiningMatrix();
        validateNativeMiningTags();
        validateLootTables();
        validateTranslations();

        System.out.println(
            "Progression validation passed: exact 2x tiers, mining tags and ore loot rules are coherent."
        );
    }

    private static void validateMaterialChain() {
        ProgressionMaterial previous = null;

        for (ProgressionMaterial material : ProgressionMaterial.values()) {
            requireFinite(material.theoreticalUses(), material + " theoretical uses");
            requireFinite(material.theoreticalMiningSpeed(), material + " theoretical speed");
            requireFinite(material.theoreticalAttackBonus(), material + " theoretical attack");
            requireFinite(material.theoreticalEnchantmentValue(), material + " theoretical enchantability");
            require(material.getUses() > 0 && material.getUses() <= 4_200_000,
                material + " uses are outside the safe ItemStack range");
            require(Float.isFinite(material.getSpeed()) && material.getSpeed() > 0.0F
                    && material.getSpeed() <= 20_000.0F,
                material + " mining speed is unsafe");
            require(Float.isFinite(material.getAttackDamageBonus())
                    && material.getAttackDamageBonus() > 0.0F
                    && material.getAttackDamageBonus() <= 10_000.0F,
                material + " attack bonus is unsafe");
            require(material.getEnchantmentValue() > 0
                    && material.getEnchantmentValue() <= 40_000,
                material + " enchantability is unsafe");
            require(material.armorDurabilityMultiplier() > 0
                    && material.armorDurabilityMultiplier() <= 100_000,
                material + " armor durability multiplier is unsafe");
            require(material.armorKnockbackResistance() >= 0.0F
                    && material.armorKnockbackResistance() <= 1.0F,
                material + " knockback resistance is unsafe");

            if (previous != null) {
                require(material.previous() == previous,
                    material + " has an incorrect predecessor");
                require(close(
                    material.cumulativeMultiplier(),
                    previous.cumulativeMultiplier() * 2.0D
                ), material + " cumulative multiplier is not based on its predecessor");
                require(close(material.stepMultiplier(), 2.0D),
                    material + " must be exactly 2x its predecessor");
                require(material.getUses() > previous.getUses(),
                    material + " uses are not strictly increasing");
                require(material.getSpeed() > previous.getSpeed(),
                    material + " speed is not strictly increasing");
                require(material.getAttackDamageBonus() > previous.getAttackDamageBonus(),
                    material + " damage bonus is not strictly increasing");
                require(material.getEnchantmentValue() > previous.getEnchantmentValue(),
                    material + " enchantability is not strictly increasing");
                require(material.armorDurabilityMultiplier()
                        > previous.armorDurabilityMultiplier(),
                    material + " armor durability is not strictly increasing");
                require(material.fullSetDamageDivisor()
                        > previous.fullSetDamageDivisor(),
                    material + " full-set mitigation is not strictly increasing");
                require(material.getUses() == previous.getUses() * 2,
                    material + " durability must be exactly 2x its predecessor");
                require(material.getSpeed() == previous.getSpeed() * 2.0F,
                    material + " mining speed must be exactly 2x its predecessor");
                require(material.getAttackDamageBonus() == previous.getAttackDamageBonus() * 2.0F,
                    material + " attack bonus must be exactly 2x its predecessor");
                require(material.getEnchantmentValue() == previous.getEnchantmentValue() * 2,
                    material + " enchantability must be exactly 2x its predecessor");
                require(material.armorDurabilityMultiplier()
                        == previous.armorDurabilityMultiplier() * 2,
                    material + " armor durability must be exactly 2x its predecessor");
                require(material.fullSetDamageDivisor()
                        == previous.fullSetDamageDivisor() * 2,
                    material + " complete armor protection must be exactly 2x its predecessor");
                require(material.swordAttackDamage() == previous.swordAttackDamage() * 2,
                    material + " sword damage must be exactly 2x its predecessor");
                require(material.pickaxeAttackDamage() == previous.pickaxeAttackDamage() * 2,
                    material + " pickaxe damage must be exactly 2x its predecessor");
                require(material.axeAttackDamage() == previous.axeAttackDamage() * 2,
                    material + " axe damage must be exactly 2x its predecessor");
                require(material.hoeAttackDamage() == previous.hoeAttackDamage() * 2,
                    material + " hoe damage must be exactly 2x its predecessor");
                require(close(material.armorToughness(), previous.armorToughness() * 2.0D),
                    material + " armor toughness must be exactly 2x its predecessor");
                require(close(
                    material.armorKnockbackResistance(),
                    Math.min(1.0D, previous.armorKnockbackResistance() * 2.0D)
                ), material + " knockback resistance must double until the vanilla 100% cap");

                for (ArmorItem.Type type : ArmorItem.Type.values()) {
                    require(material.armorDefense(type) == previous.armorDefense(type) * 2,
                        material + " " + type + " armor points must be exactly 2x its predecessor");
                }
            }

            previous = material;
        }

        require(ProgressionMaterial.EMERALD.getUses() == 4_062,
            "Emerald must be exactly 2x Netherite");
        require(ProgressionMaterial.RUBY.getUses() == 8_124,
            "Ruby must be exactly 2x Emerald");
        require(ProgressionMaterial.SAPPHIRE.getUses() == 1_039_872,
            "Sapphire must retain its exact 2x chain value");
        require(ProgressionMaterial.VORTEX.getUses() == 4_159_488,
            "Vortex must retain its exact 2x chain value");
        require(ProgressionMaterial.NETHERITE.swordAttackDamage() == 8,
            "Netherite sword baseline must remain eight damage");
        require(ProgressionMaterial.SAPPHIRE.swordAttackDamage() == 4_096,
            "Sapphire sword must follow the exact 2x chain");
    }

    private static void validateMiningMatrix() {
        for (MiningProgression.PickaxeStage tool : MiningProgression.PickaxeStage.values()) {
            for (MiningProgression.OreStage ore : MiningProgression.OreStage.values()) {
                boolean expected = tool.level() >= ore.requiredLevel();
                require(MiningProgression.canHarvest(tool, ore) == expected,
                    tool + " -> " + ore + " does not follow level >= requirement");
            }
        }

        require(MiningProgression.effectiveMiningLevel(Tiers.GOLD) == 2,
            "Gold Pickaxe must be promoted from vanilla level 0 to level 2");
        require(MiningProgression.effectiveMiningLevel(Tiers.NETHERITE) == 4,
            "Netherite must keep vanilla level 4");

        assertHarvest(MiningProgression.PickaxeStage.WOOD, MiningProgression.OreStage.STONE, true);
        assertHarvest(MiningProgression.PickaxeStage.WOOD, MiningProgression.OreStage.COPPER, false);
        assertHarvest(MiningProgression.PickaxeStage.STONE, MiningProgression.OreStage.IRON, true);
        assertHarvest(MiningProgression.PickaxeStage.STONE, MiningProgression.OreStage.GOLD, false);
        assertHarvest(MiningProgression.PickaxeStage.IRON, MiningProgression.OreStage.DIAMOND, true);
        assertHarvest(MiningProgression.PickaxeStage.IRON, MiningProgression.OreStage.NETHERITE, false);
        assertHarvest(MiningProgression.PickaxeStage.DIAMOND, MiningProgression.OreStage.NETHERITE, true);
        assertHarvest(MiningProgression.PickaxeStage.NETHERITE, MiningProgression.OreStage.EMERALD, true);
        assertHarvest(MiningProgression.PickaxeStage.EMERALD, MiningProgression.OreStage.RUBY, true);
        assertHarvest(MiningProgression.PickaxeStage.RUBY, MiningProgression.OreStage.JAXY, true);
        assertHarvest(MiningProgression.PickaxeStage.JAXY, MiningProgression.OreStage.CHLOROPHYTE, true);
        assertHarvest(MiningProgression.PickaxeStage.CHLOROPHYTE, MiningProgression.OreStage.TITANIUM, true);
        assertHarvest(MiningProgression.PickaxeStage.CHLOROPHYTE, MiningProgression.OreStage.HERO, true);
        assertHarvest(MiningProgression.PickaxeStage.VYLAM, MiningProgression.OreStage.HERO, true);
        assertHarvest(MiningProgression.PickaxeStage.VYLAM, MiningProgression.OreStage.ROSALITA, false);
        assertHarvest(MiningProgression.PickaxeStage.HERO, MiningProgression.OreStage.ROSALITA, true);
        assertHarvest(MiningProgression.PickaxeStage.ROSALITA, MiningProgression.OreStage.SAPPHIRE, true);
        assertHarvest(MiningProgression.PickaxeStage.SAPPHIRE, MiningProgression.OreStage.SHADOW, true);
        assertHarvest(MiningProgression.PickaxeStage.SAPPHIRE, MiningProgression.OreStage.VORTEX, false);
        assertHarvest(MiningProgression.PickaxeStage.SHADOW, MiningProgression.OreStage.VORTEX, true);
        assertHarvest(MiningProgression.PickaxeStage.VORTEX, MiningProgression.OreStage.VORTEX, true);
    }

    private static void validateNativeMiningTags() throws IOException {
        Set<String> mineableWithPickaxe = values(MINEABLE_PICKAXE);

        for (Map.Entry<Integer, Set<String>> entry : EXPECTED_LEVEL_TAGS.entrySet()) {
            Path path = FABRIC_TAGS.resolve("needs_tool_level_" + entry.getKey() + ".json");
            Set<String> actual = values(path);
            require(actual.containsAll(entry.getValue()),
                path + " is missing required entries: " + difference(entry.getValue(), actual));

            for (String block : entry.getValue()) {
                if (block.startsWith("chaoticd:")) {
                    require(mineableWithPickaxe.contains(block),
                        block + " must remain in #minecraft:mineable/pickaxe");
                }
            }
        }

        require(!values(FABRIC_TAGS.resolve("needs_tool_level_8.json"))
                .contains("chaoticd:rosalita_ore"),
            "Rosalita must not remain at mining level 8");
        require(!values(FABRIC_TAGS.resolve("needs_tool_level_8.json"))
                .contains("chaoticd:sapphire_ore"),
            "Sapphire must not remain at mining level 8");
        require(!values(FABRIC_TAGS.resolve("needs_tool_level_7.json"))
                .contains("chaoticd:titanium_ore"),
            "Titanium must not remain at mining level 7");
    }

    private static void validateLootTables() throws IOException {
        for (String id : ORE_LOOT_TABLES) {
            Path table = LOOT_TABLES.resolve(id + ".json");
            String source = Files.readString(table);
            require(source.contains("minecraft:silk_touch"),
                id + " must retain Silk Touch support");
            require(source.contains("minecraft:apply_bonus"),
                id + " must retain Fortune support");
            require(source.contains("minecraft:fortune"),
                id + " must retain its Fortune enchantment rule");
        }
    }

    private static void validateTranslations() throws IOException {
        for (String language : List.of("pt_br", "en_us", "es_co", "es_mx")) {
            Path file = ROOT.resolve("assets/chaoticd/lang/" + language + ".json");
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonObject languageFile = JsonParser.parseReader(reader).getAsJsonObject();
                require(!languageFile.has("tooltip.chaoticd.progression_pickaxe"),
                    language + " must not expose mining-level tooltips");
            }
        }
    }

    private static Map<Integer, Set<String>> expectedLevelTags() {
        Map<Integer, Set<String>> result = new LinkedHashMap<>();
        result.put(4, Set.of(
            "minecraft:emerald_ore",
            "minecraft:deepslate_emerald_ore",
            "minecraft:emerald_block"
        ));
        result.put(5, Set.of(
            "chaoticd:ruby_ore",
            "chaoticd:deepslate_ruby_ore",
            "chaoticd:nether_ruby_ore",
            "chaoticd:aurora_ruby_ore",
            "chaoticd:ruby_block"
        ));
        result.put(6, Set.of(
            "chaoticd:jax_ore",
            "chaoticd:deepslate_jaxy_ore",
            "chaoticd:nether_jax_ore",
            "chaoticd:aurora_jax_ore",
            "chaoticd:jaxy_block"
        ));
        result.put(8, Set.of(
            "chaoticd:titanium_ore",
            "chaoticd:deepslate_titanium_ore",
            "chaoticd:titanium_block"
        ));
        result.put(9, Set.of(
            "chaoticd:rosalita_ore",
            "chaoticd:deepslate_rosalita_ore",
            "chaoticd:nether_rosalita_ore",
            "chaoticd:aurora_rosalita_ore",
            "chaoticd:rosalita_block"
        ));
        result.put(10, Set.of(
            "chaoticd:sapphire_ore",
            "chaoticd:aurora_sapphire_ore"
        ));
        return result;
    }

    private static Set<String> values(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray values = object.getAsJsonArray("values");
            Set<String> result = new LinkedHashSet<>();

            for (JsonElement value : values) {
                result.add(value.getAsString());
            }

            return result;
        }
    }

    private static Set<String> difference(Set<String> expected, Set<String> actual) {
        Set<String> result = new LinkedHashSet<>(expected);
        result.removeAll(actual);
        return result;
    }

    private static void assertHarvest(
        MiningProgression.PickaxeStage tool,
        MiningProgression.OreStage ore,
        boolean expected
    ) {
        require(MiningProgression.canHarvest(tool, ore) == expected,
            tool + " -> " + ore + " expected " + expected);
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) <= Math.max(1.0D, Math.abs(right)) * 0.0000001D;
    }

    private static void requireFinite(double value, String label) {
        require(Double.isFinite(value), label + " is not finite");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
