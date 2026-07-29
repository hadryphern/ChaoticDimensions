package net.blue.chaoticd.content.item;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for centralized Rosalita armor values. */
public enum RosalitaArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return armorBaseDurability(type)
            * ProgressionMaterial.ROSALITA.armorDurabilityMultiplier();
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return ProgressionMaterial.ROSALITA.armorDefense(type);
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.ROSALITA.getEnchantmentValue();
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.ROSALITA.getRepairIngredient();
    }

    @Override
    public String getName() {
        return ChaoticDimensions.MOD_ID + ":rosalita";
    }

    @Override
    public float getToughness() {
        return ProgressionMaterial.ROSALITA.armorToughness();
    }

    @Override
    public float getKnockbackResistance() {
        return ProgressionMaterial.ROSALITA.armorKnockbackResistance();
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
