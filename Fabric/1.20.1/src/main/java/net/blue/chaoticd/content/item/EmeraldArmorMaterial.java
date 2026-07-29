package net.blue.chaoticd.content.item;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Compatibility facade for centralized Emerald armor values.
 */
public enum EmeraldArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return armorBaseDurability(type)
            * ProgressionMaterial.EMERALD.armorDurabilityMultiplier();
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return ProgressionMaterial.EMERALD.armorDefense(type);
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.EMERALD.getEnchantmentValue();
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.EMERALD.getRepairIngredient();
    }

    @Override
    public String getName() {
        return ChaoticDimensions.MOD_ID + ":emerald";
    }

    @Override
    public float getToughness() {
        return ProgressionMaterial.EMERALD.armorToughness();
    }

    @Override
    public float getKnockbackResistance() {
        return ProgressionMaterial.EMERALD.armorKnockbackResistance();
    }

    private static int armorBaseDurability(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 13;
            case LEGGINGS -> 15;
            case CHESTPLATE -> 16;
            case HELMET -> 11;
        };
    }
}
