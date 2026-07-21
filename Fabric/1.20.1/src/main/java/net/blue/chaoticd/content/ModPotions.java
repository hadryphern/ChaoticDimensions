package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

/** A normal vanilla potion bottle carrying the Sapphiric effect for 45 seconds. */
public final class ModPotions {
    public static final Potion SAPPHIRIC = Registry.register(BuiltInRegistries.POTION,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "sapphiric"),
        new Potion("sapphiric", new MobEffectInstance(ModEffects.SAPPHIRIC, 20 * 45)));

    private ModPotions() {
    }

    public static void initialize() {
        PotionBrewing.addMix(Potions.AWKWARD, ModItems.SAPPHIRE_GEM, SAPPHIRIC);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries ->
            entries.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), SAPPHIRIC)));
    }
}
