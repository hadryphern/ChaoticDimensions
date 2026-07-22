package net.blue.chaoticd;

import net.fabricmc.api.ModInitializer;
import net.blue.chaoticd.content.ModEffects;
import net.blue.chaoticd.content.ModBlocks;
import net.blue.chaoticd.content.ModEnchantments;
import net.blue.chaoticd.content.ModGameplayEvents;
import net.blue.chaoticd.content.ModItemGroups;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModPotions;
import net.blue.chaoticd.content.ModRecipes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main Fabric entry point for Chaotic Dimensions content and gameplay systems. */
public final class ChaoticDimensions implements ModInitializer {
    public static final String MOD_ID = "chaoticd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEnchantments.initialize();
        ModEffects.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModPotions.initialize();
        ModItemGroups.initialize();
        ModRecipes.initialize();
        ModGameplayEvents.initialize();
        LOGGER.info("Chaotic Dimensions content, Aurora blocks and enchantments loaded");
    }
}
