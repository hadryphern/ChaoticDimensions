package net.blue.chaoticd.content.item;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for centralized Ruby armor values. */
public enum RubyArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return armorBaseDurability(type)
            * ProgressionMaterial.RUBY.armorDurabilityMultiplier();
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return ProgressionMaterial.RUBY.armorDefense(type);
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.RUBY.getEnchantmentValue();
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.RUBY.getRepairIngredient();
    }

    @Override
    public String getName() {
        return ChaoticDimensions.MOD_ID + ":ruby";
    }

    @Override
    public float getToughness() {
        return ProgressionMaterial.RUBY.armorToughness();
    }

    @Override
    public float getKnockbackResistance() {
        return ProgressionMaterial.RUBY.armorKnockbackResistance();
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
