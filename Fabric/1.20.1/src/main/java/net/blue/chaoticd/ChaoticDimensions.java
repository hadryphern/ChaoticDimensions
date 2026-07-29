package net.blue.chaoticd;

import net.blue.chaoticd.content.ModBlockEntities;
import net.blue.chaoticd.content.ModBlocks;
import net.blue.chaoticd.content.ModEffects;
import net.blue.chaoticd.content.ModEnchantments;
import net.blue.chaoticd.content.ModEntities;
import net.blue.chaoticd.content.ModFluids;
import net.blue.chaoticd.content.ModGameplayEvents;
import net.blue.chaoticd.content.ModItemGroups;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModLootTables;
import net.blue.chaoticd.content.ModMenus;
import net.blue.chaoticd.content.ModPotions;
import net.blue.chaoticd.gameplay.ChaoticGearInitialization;
import net.blue.chaoticd.gameplay.CrystalHarvestRules;
import net.blue.chaoticd.gameplay.DreamFluidSystems;
import net.blue.chaoticd.gameplay.EmeraldLuckSystems;
import net.blue.chaoticd.gameplay.SirOrensSpawnSystem;
import net.blue.chaoticd.gameplay.SirOrensCommands;
import net.blue.chaoticd.network.StackSizeProtocol;
import net.blue.chaoticd.worldgen.LegacyWorldgen;
import net.blue.chaoticd.worldgen.ModWorldgenFeatures;
import net.blue.chaoticd.worldgen.ShadowDimensionSystems;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.bernie.geckolib.GeckoLib;

public final class ChaoticDimensions implements ModInitializer {

    public static final String MOD_ID = "chaoticd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        GeckoLib.initialize();

        StackSizeProtocol.initializeServer();

        ModEnchantments.initialize();
        ModEffects.initialize();
        ModFluids.initialize();
        ModEntities.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModMenus.initialize();

        ModWorldgenFeatures.initialize();
        LegacyWorldgen.initialize();

        ShadowDimensionSystems.initialize();
        DreamFluidSystems.initialize();
        CrystalHarvestRules.initialize();
        SirOrensSpawnSystem.initialize();
        SirOrensCommands.initialize();

        ChaoticGearInitialization.initialize();
        EmeraldLuckSystems.initialize();

        ModLootTables.initialize();
        ModPotions.initialize();

        ModItemGroups.initialize();

        ModGameplayEvents.initialize();

        LOGGER.info("Chaotic Dimensions loaded successfully.");
    }
}
