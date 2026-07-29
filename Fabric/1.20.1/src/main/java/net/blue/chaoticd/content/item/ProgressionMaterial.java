package net.blue.chaoticd.content.item;

import java.util.function.Supplier;
import net.blue.chaoticd.content.ModItems;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Canonical material data for the post-Netherite progression.
 *
 * <p>Every material step is exactly twice the attributes of the preceding
 * one.  This rule applies to durability, mining speed, attack values,
 * enchantability, armor durability, armor points and the complete-set damage
 * reduction.  The complete chain is still numerically safe in Minecraft
 * 1.20.1: Vortex is only 2^11 above Netherite, so no compression, logarithmic
 * scaling or hidden caps are necessary.</p>
 */
public enum ProgressionMaterial implements Tier {
    NETHERITE(
        "netherite",
        4,
        () -> Items.NETHERITE_INGOT,
        false,
        false
    ),
    EMERALD(
        "emerald",
        5,
        () -> ModItems.EMERALD_INGOT,
        true,
        true
    ),
    RUBY(
        "ruby",
        6,
        () -> ModItems.RUBY,
        true,
        true
    ),
    JAXY(
        "jaxy",
        7,
        () -> ModItems.JAXY_GEM,
        true,
        true
    ),
    CHLOROPHYTE(
        "chlorophyte",
        8,
        () -> ModItems.CHLOROPHYTE_INGOT,
        true,
        false
    ),
    TITANIUM(
        "titanium",
        8,
        () -> ModItems.TITANIUM_INGOT,
        true,
        true
    ),
    VYLAM(
        "vylam",
        8,
        () -> ModItems.VYLAM_GEM,
        false,
        false
    ),
    HERO(
        "hero",
        9,
        () -> ModItems.HERO_GEM,
        true,
        false
    ),
    ROSALITA(
        "rosalita",
        10,
        () -> ModItems.ROSALITA_GEM,
        true,
        true
    ),
    SAPPHIRE(
        "sapphire",
        11,
        () -> ModItems.SAPPHIRE_GEM,
        true,
        false
    ),
    SHADOW(
        "shadow",
        12,
        () -> ModItems.SHADOW_GEM,
        true,
        false
    ),
    VORTEX(
        "vortex",
        12,
        () -> ModItems.VORTEX_GEM,
        false,
        false
    );

    private static final int NETHERITE_USES = 2_031;
    private static final int NETHERITE_SPEED = 9;
    private static final int NETHERITE_ATTACK_BONUS = 4;
    private static final int NETHERITE_ENCHANTABILITY = 15;
    private static final int NETHERITE_ARMOR_DURABILITY_MULTIPLIER = 37;
    private static final int NETHERITE_SWORD_DAMAGE = 8;
    private static final int NETHERITE_PICKAXE_DAMAGE = 6;
    private static final int NETHERITE_AXE_DAMAGE = 10;
    private static final int NETHERITE_HOE_DAMAGE = 1;

    private final String id;
    private final int miningLevel;
    private final Supplier<Item> repairItem;
    private final boolean hasRegisteredTools;
    private final boolean hasRegisteredArmor;

    ProgressionMaterial(
        String id,
        int miningLevel,
        Supplier<Item> repairItem,
        boolean hasRegisteredTools,
        boolean hasRegisteredArmor
    ) {
        this.id = id;
        this.miningLevel = miningLevel;
        this.repairItem = repairItem;
        this.hasRegisteredTools = hasRegisteredTools;
        this.hasRegisteredArmor = hasRegisteredArmor;
    }

    public String id() {
        return id;
    }

    /** The material immediately before this stage, or {@code null} for Netherite. */
    public ProgressionMaterial previous() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    /** Multiplier relative to the preceding material. */
    public double stepMultiplier() {
        return ordinal() == 0 ? 1.0D : 2.0D;
    }

    /** Exact cumulative multiplier relative to Netherite: 1, 2, 4, 8, ... */
    public double cumulativeMultiplier() {
        return factor();
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

    public double theoreticalUses() {
        return getUses();
    }

    public double theoreticalMiningSpeed() {
        return getSpeed();
    }

    public double theoreticalAttackBonus() {
        return getAttackDamageBonus();
    }

    public double theoreticalEnchantmentValue() {
        return getEnchantmentValue();
    }

    public double theoreticalArmorDurabilityMultiplier() {
        return armorDurabilityMultiplier();
    }

    @Override
    public int getUses() {
        return scaledInt(NETHERITE_USES);
    }

    @Override
    public float getSpeed() {
        return scaledInt(NETHERITE_SPEED);
    }

    @Override
    public float getAttackDamageBonus() {
        return scaledInt(NETHERITE_ATTACK_BONUS);
    }

    @Override
    public int getLevel() {
        return miningLevel;
    }

    @Override
    public int getEnchantmentValue() {
        return scaledInt(NETHERITE_ENCHANTABILITY);
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(repairItem.get());
    }

    /** Final displayed/main-hand damage of a normal sword of this material. */
    public int swordAttackDamage() {
        return scaledInt(NETHERITE_SWORD_DAMAGE);
    }

    /** Final displayed/main-hand damage of a normal pickaxe or shovel. */
    public int pickaxeAttackDamage() {
        return scaledInt(NETHERITE_PICKAXE_DAMAGE);
    }

    /** Final displayed/main-hand damage of a normal axe. */
    public int axeAttackDamage() {
        return scaledInt(NETHERITE_AXE_DAMAGE);
    }

    /** Final displayed/main-hand damage of a normal hoe. */
    public int hoeAttackDamage() {
        return scaledInt(NETHERITE_HOE_DAMAGE);
    }

    /** Constructor modifier for a normal sword. */
    public int swordAttackModifier() {
        return attackModifierFor(swordAttackDamage());
    }

    /** Constructor modifier for a normal pickaxe or shovel. */
    public int pickaxeAttackModifier() {
        return attackModifierFor(pickaxeAttackDamage());
    }

    /** Constructor modifier for a normal axe. */
    public int axeAttackModifier() {
        return attackModifierFor(axeAttackDamage());
    }

    /** Constructor modifier for a normal hoe. */
    public int hoeAttackModifier() {
        return attackModifierFor(hoeAttackDamage());
    }

    /** Slot durability multiplier used by each matching ArmorMaterial facade. */
    public int armorDurabilityMultiplier() {
        return scaledInt(NETHERITE_ARMOR_DURABILITY_MULTIPLIER);
    }

    /** Native armor points, doubled for every material step. */
    public int armorDefense(ArmorItem.Type type) {
        int netheriteDefense = switch (type) {
            case BOOTS, HELMET -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
        };
        return scaledInt(netheriteDefense);
    }

    /** Native armor toughness, doubled for every material step. */
    public float armorToughness() {
        return scaledInt(3);
    }

    /**
     * Knockback resistance is a vanilla probability and is therefore capped
     * at one (100%).  It doubles normally until it reaches that engine limit.
     */
    public float armorKnockbackResistance() {
        return Math.min(1.0F, 0.10F * factor());
    }

    /** Complete-set damage reduction, also exactly 2x for each material step. */
    public int fullSetDamageDivisor() {
        return factor();
    }

    private int attackModifierFor(int desiredDamage) {
        return desiredDamage - 1 - Math.round(getAttackDamageBonus());
    }

    private int factor() {
        return 1 << ordinal();
    }

    private int scaledInt(int base) {
        return Math.multiplyExact(base, factor());
    }
}
