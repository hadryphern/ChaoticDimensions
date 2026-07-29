package net.blue.chaoticd.content.item;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for centralized Jaxy armor values. */
public enum JaxyArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return armorBaseDurability(type)
            * ProgressionMaterial.JAXY.armorDurabilityMultiplier();
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return ProgressionMaterial.JAXY.armorDefense(type);
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.JAXY.getEnchantmentValue();
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.JAXY.getRepairIngredient();
    }

    @Override
    public String getName() {
        return ChaoticDimensions.MOD_ID + ":jaxy";
    }

    @Override
    public float getToughness() {
        return ProgressionMaterial.JAXY.armorToughness();
    }

    @Override
    public float getKnockbackResistance() {
        return ProgressionMaterial.JAXY.armorKnockbackResistance();
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
