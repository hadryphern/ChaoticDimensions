package net.blue.chaoticd.content.item;

import java.util.function.Supplier;
import net.blue.chaoticd.content.ModItems;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Canonical post-Netherite progression data.
 *
 * <p>The {@linkplain #stepMultiplier() step multiplier} is always relative to
 * the preceding entry.  The cumulative multiplier is therefore derived here,
 * never copied into individual item classes.  Literal values remain in use
 * through Hero.  The four later stages are compressed only for attributes
 * whose Minecraft 1.20.1 representation would otherwise overflow an
 * {@code int}, make an item impossible to serialize safely, or become an
 * impractical mining speed.  The compression is logarithmic, monotonic and
 * anchored at Hero, so a later material is always stronger than an earlier
 * material.</p>
 */
public enum ProgressionMaterial implements Tier {
    NETHERITE(
        "netherite",
        "item.minecraft.netherite_ingot",
        1.0D,
        4,
        () -> Items.NETHERITE_INGOT,
        false,
        false,
        "Vanilla baseline"
    ),
    EMERALD(
        "emerald",
        "item.chaoticd.emerald_ingot",
        2.0D,
        5,
        () -> ModItems.EMERALD_INGOT,
        true,
        true,
        "Luck V and Emerald Luck systems"
    ),
    RUBY(
        "ruby",
        "item.chaoticd.ruby",
        3.0D,
        6,
        () -> ModItems.RUBY,
        true,
        true,
        "None defined"
    ),
    JAXY(
        "jaxy",
        "item.chaoticd.jaxy_gem",
        2.0D,
        7,
        () -> ModItems.JAXY_GEM,
        true,
        true,
        "None defined"
    ),
    CHLOROPHYTE(
        "chlorophyte",
        "item.chaoticd.chlorophyte_ingot",
        5.0D,
        8,
        () -> ModItems.CHLOROPHYTE_INGOT,
        true,
        false,
        "None defined"
    ),
    TITANIUM(
        "titanium",
        "item.chaoticd.titanium_ingot",
        10.0D,
        8,
        () -> ModItems.TITANIUM_INGOT,
        true,
        true,
        "Full-set progression mitigation"
    ),
    VYLAM(
        "vylam",
        "item.chaoticd.vylam_gem",
        5.0D,
        8,
        () -> ModItems.VYLAM_GEM,
        false,
        false,
        "Reserved: no tool assets are registered"
    ),
    HERO(
        "hero",
        "item.chaoticd.hero_gem",
        25.0D,
        9,
        () -> ModItems.HERO_GEM,
        true,
        false,
        "None defined"
    ),
    ROSALITA(
        "rosalita",
        "item.chaoticd.rosalita_gem",
        75.0D,
        10,
        () -> ModItems.ROSALITA_GEM,
        true,
        true,
        "None defined"
    ),
    SAPPHIRE(
        "sapphire",
        "item.chaoticd.sapphire_gem",
        150.0D,
        11,
        () -> ModItems.SAPPHIRE_GEM,
        true,
        false,
        "Sapphiric and Dheathic on the special sword"
    ),
    SHADOW(
        "shadow",
        "item.chaoticd.shadow_gem",
        150.0D,
        12,
        () -> ModItems.SHADOW_GEM,
        true,
        false,
        "None defined"
    ),
    VORTEX(
        "vortex",
        "item.chaoticd.vortex_gem",
        250.0D,
        12,
        () -> ModItems.VORTEX_GEM,
        false,
        false,
        "Reserved: no tool assets are registered"
    );

    /** Vanilla Netherite tool baseline. */
    private static final int NETHERITE_USES = 2_031;
    private static final float NETHERITE_SPEED = 9.0F;
    private static final float NETHERITE_ATTACK_BONUS = 4.0F;
    private static final int NETHERITE_ENCHANTABILITY = 15;
    private static final int NETHERITE_ARMOR_DURABILITY_MULTIPLIER = 37;

    /*
     * All caps are safely below the signed-int limit used by Item and
     * ItemStack damage values.  Armor uses a separate cap because its base
     * durability factor is multiplied by the slot-specific vanilla value.
     */
    private static final int MAX_TOOL_USES = 1_500_000_000;
    private static final int MAX_ARMOR_DURABILITY_MULTIPLIER = 90_000_000;
    private static final float MAX_MINING_SPEED = 1_000_000.0F;
    private static final float MAX_ATTACK_BONUS = 1_000_000.0F;
    private static final int MAX_ENCHANTABILITY = 100_000;
    private static final int MAX_FULL_SET_DAMAGE_DIVISOR = 1_024;

    private final String id;
    private final String materialTranslationKey;
    private final double stepMultiplier;
    private final int miningLevel;
    private final Supplier<Item> repairItem;
    private final boolean hasRegisteredTools;
    private final boolean hasRegisteredArmor;
    private final String specialEffectDescription;

    ProgressionMaterial(
        String id,
        String materialTranslationKey,
        double stepMultiplier,
        int miningLevel,
        Supplier<Item> repairItem,
        boolean hasRegisteredTools,
        boolean hasRegisteredArmor,
        String specialEffectDescription
    ) {
        this.id = id;
        this.materialTranslationKey = materialTranslationKey;
        this.stepMultiplier = stepMultiplier;
        this.miningLevel = miningLevel;
        this.repairItem = repairItem;
        this.hasRegisteredTools = hasRegisteredTools;
        this.hasRegisteredArmor = hasRegisteredArmor;
        this.specialEffectDescription = specialEffectDescription;
    }

    public String id() {
        return id;
    }

    /** Translation key of the material item used in player-facing tooltips. */
    public String materialTranslationKey() {
        return materialTranslationKey;
    }

    /** The material immediately before this stage, or {@code null} for Netherite. */
    public ProgressionMaterial previous() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    /** Multiplier relative to {@link #previous()}. */
    public double stepMultiplier() {
        return stepMultiplier;
    }

    /** Literal cumulative multiplier relative to Netherite, represented as a double. */
    public double cumulativeMultiplier() {
        double result = 1.0D;

        for (int index = 1; index <= ordinal(); index++) {
            result *= values()[index].stepMultiplier;
        }

        return result;
    }

    public int miningLevel() {
        return miningLevel;
    }

    public boolean hasRegisteredTools() {
        return hasRegisteredTools;
    }

    public boolean hasRegisteredArmor() {
        return hasRegisteredArmor;
    }

    public String specialEffectDescription() {
        return specialEffectDescription;
    }

    public double theoreticalUses() {
        return NETHERITE_USES * cumulativeMultiplier();
    }

    public double theoreticalMiningSpeed() {
        return NETHERITE_SPEED * cumulativeMultiplier();
    }

    public double theoreticalAttackBonus() {
        return NETHERITE_ATTACK_BONUS * cumulativeMultiplier();
    }

    public double theoreticalEnchantmentValue() {
        return NETHERITE_ENCHANTABILITY * cumulativeMultiplier();
    }

    public double theoreticalArmorDurabilityMultiplier() {
        return NETHERITE_ARMOR_DURABILITY_MULTIPLIER * cumulativeMultiplier();
    }

    @Override
    public int getUses() {
        return compressedInt(
            theoreticalUses(),
            HERO.theoreticalUses(),
            MAX_TOOL_USES,
            VORTEX.theoreticalUses()
        );
    }

    @Override
    public float getSpeed() {
        return compressedFloat(
            theoreticalMiningSpeed(),
            HERO.theoreticalMiningSpeed(),
            MAX_MINING_SPEED,
            VORTEX.theoreticalMiningSpeed()
        );
    }

    @Override
    public float getAttackDamageBonus() {
        return compressedFloat(
            theoreticalAttackBonus(),
            HERO.theoreticalAttackBonus(),
            MAX_ATTACK_BONUS,
            VORTEX.theoreticalAttackBonus()
        );
    }

    @Override
    public int getLevel() {
        return miningLevel;
    }

    @Override
    public int getEnchantmentValue() {
        /*
         * Enchantment calculations use int arithmetic internally.  A smaller
         * literal anchor prevents enchantability from becoming millions while
         * preserving the complete material order.
         */
        return compressedInt(
            theoreticalEnchantmentValue(),
            CHLOROPHYTE.theoreticalEnchantmentValue(),
            MAX_ENCHANTABILITY,
            VORTEX.theoreticalEnchantmentValue()
        );
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(repairItem.get());
    }

    /**
     * Constructor modifier for a normal sword.  Together with the player base
     * damage and this tier's bonus, it scales the Netherite sword baseline.
     */
    public int swordAttackModifier() {
        return Math.max(0, roundedAttackBonus() - 1);
    }

    /** Constructor modifier for a normal pickaxe or shovel. */
    public int pickaxeAttackModifier() {
        return Math.max(0, roundedAttackBonus() / 2 - 1);
    }

    /** Constructor modifier for a normal axe. */
    public int axeAttackModifier() {
        return safeModifier((long) roundedAttackBonus() + (roundedAttackBonus() / 2L) - 1L);
    }

    /** Constructor modifier for a normal hoe. */
    public int hoeAttackModifier() {
        return safeModifier((roundedAttackBonus() / 4L) - 1L - roundedAttackBonus());
    }

    /** Slot durability multiplier used by the matching ArmorMaterial facade. */
    public int armorDurabilityMultiplier() {
        return compressedInt(
            theoreticalArmorDurabilityMultiplier(),
            HERO.theoreticalArmorDurabilityMultiplier(),
            MAX_ARMOR_DURABILITY_MULTIPLIER,
            VORTEX.theoreticalArmorDurabilityMultiplier()
        );
    }

    /**
     * Vanilla's armor formula provides no useful benefit above 20 total armor
     * points.  Native armor is therefore kept at a valid Netherite-shaped
     * profile and the meaningful post-Netherite scaling is handled by the
     * bounded full-set protection layer.
     */
    public int armorDefense(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS, HELMET -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
        };
    }

    public float armorToughness() {
        return (float) Math.min(
            20.0D,
            3.0D * (1.0D + logarithmBaseTwo(cumulativeMultiplier()))
        );
    }

    public float armorKnockbackResistance() {
        return (float) Math.min(
            1.0D,
            0.10D * (1.0D + logarithmBaseTwo(cumulativeMultiplier()))
        );
    }

    /**
     * Bounded server-side mitigation used only by complete registered armor
     * sets.  It remains literal through Titanium and stays strictly ordered
     * afterwards.
     */
    public int fullSetDamageDivisor() {
        return compressedInt(
            cumulativeMultiplier(),
            TITANIUM.cumulativeMultiplier(),
            MAX_FULL_SET_DAMAGE_DIVISOR,
            VORTEX.cumulativeMultiplier()
        );
    }

    private int roundedAttackBonus() {
        return Math.max(0, Math.round(getAttackDamageBonus()));
    }

    private static int compressedInt(
        double raw,
        double linearAnchor,
        int maximum,
        double absoluteMaximum
    ) {
        double compressed = compressed(raw, linearAnchor, maximum, absoluteMaximum);
        if (!Double.isFinite(compressed)) {
            return maximum;
        }

        return (int) Math.max(1L, Math.min(maximum, Math.round(compressed)));
    }

    private static float compressedFloat(
        double raw,
        double linearAnchor,
        float maximum,
        double absoluteMaximum
    ) {
        double compressed = compressed(raw, linearAnchor, maximum, absoluteMaximum);
        if (!Double.isFinite(compressed)) {
            return maximum;
        }

        return (float) Math.max(1.0D, Math.min(maximum, compressed));
    }

    private static double compressed(
        double raw,
        double linearAnchor,
        double maximum,
        double absoluteMaximum
    ) {
        if (!Double.isFinite(raw) || raw >= absoluteMaximum) {
            return maximum;
        }

        if (raw <= linearAnchor) {
            return raw;
        }

        double numerator = Math.log(raw / linearAnchor);
        double denominator = Math.log(absoluteMaximum / linearAnchor);
        if (!Double.isFinite(numerator) || !Double.isFinite(denominator) || denominator <= 0.0D) {
            return maximum;
        }

        double progress = Math.max(0.0D, Math.min(1.0D, numerator / denominator));
        return linearAnchor + ((maximum - linearAnchor) * progress);
    }

    private static double logarithmBaseTwo(double value) {
        return Math.log(Math.max(1.0D, value)) / Math.log(2.0D);
    }

    private static int safeModifier(long value) {
        return (int) Math.max(
            Integer.MIN_VALUE + 1L,
            Math.min(Integer.MAX_VALUE - 1L, value)
        );
    }
}
