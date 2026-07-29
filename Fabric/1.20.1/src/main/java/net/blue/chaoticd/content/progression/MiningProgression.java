package net.blue.chaoticd.content.progression;

import net.blue.chaoticd.content.item.ProgressionMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Canonical mining-level rules shared by the data tags, the one vanilla Gold
 * compatibility override and the validation suite.
 */
public final class MiningProgression {
    private MiningProgression() {
    }

    public enum PickaxeStage {
        WOOD(0),
        STONE(1),
        COPPER(1),
        IRON(2),
        GOLD(2),
        DIAMOND(3),
        NETHERITE(4),
        EMERALD(ProgressionMaterial.EMERALD.miningLevel()),
        RUBY(ProgressionMaterial.RUBY.miningLevel()),
        JAXY(ProgressionMaterial.JAXY.miningLevel()),
        CHLOROPHYTE(ProgressionMaterial.CHLOROPHYTE.miningLevel()),
        TITANIUM(ProgressionMaterial.TITANIUM.miningLevel()),
        VYLAM(ProgressionMaterial.VYLAM.miningLevel()),
        HERO(ProgressionMaterial.HERO.miningLevel()),
        ROSALITA(ProgressionMaterial.ROSALITA.miningLevel()),
        SAPPHIRE(ProgressionMaterial.SAPPHIRE.miningLevel()),
        SHADOW(ProgressionMaterial.SHADOW.miningLevel()),
        VORTEX(ProgressionMaterial.VORTEX.miningLevel());

        private final int level;

        PickaxeStage(int level) {
            this.level = level;
        }

        public int level() {
            return level;
        }
    }

    public enum OreStage {
        STONE(0),
        COPPER(1),
        IRON(1),
        GOLD(2),
        DIAMOND(2),
        NETHERITE(3),
        EMERALD(4),
        RUBY(5),
        JAXY(6),
        CHLOROPHYTE(7),
        TITANIUM(8),
        VYLAM(8),
        HERO(8),
        ROSALITA(9),
        SAPPHIRE(10),
        SHADOW(11),
        VORTEX(12);

        private final int requiredLevel;

        OreStage(int requiredLevel) {
            this.requiredLevel = requiredLevel;
        }

        public int requiredLevel() {
            return requiredLevel;
        }
    }

    public static boolean canHarvest(PickaxeStage tool, OreStage ore) {
        return canHarvest(tool.level(), ore.requiredLevel());
    }

    public static boolean canHarvest(int toolLevel, int requiredLevel) {
        return toolLevel >= requiredLevel;
    }

    /**
     * Minecraft 1.20.1 declares Gold as level zero, while this mod's public
     * progression intentionally places the Gold Pickaxe beside Iron at level
     * two.  The Gold Pickaxe mixin consumes this value; all other native tiers
     * retain their engine-defined level.
     */
    public static int effectiveMiningLevel(Tier tier) {
        return tier == Tiers.GOLD ? PickaxeStage.GOLD.level() : tier.getLevel();
    }
}
