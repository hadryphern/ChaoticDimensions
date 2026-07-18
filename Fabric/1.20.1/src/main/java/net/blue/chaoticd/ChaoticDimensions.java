package net.blue.chaoticd;

import net.fabricmc.api.ModInitializer;
import net.blue.chaoticd.content.block.ModBlocks;
import net.blue.chaoticd.content.block.RosalitaAxeStripping;
import net.blue.chaoticd.content.item.ModItems;
import net.blue.chaoticd.content.tab.ModCreativeTabs;
import net.blue.chaoticd.worldgen.ModWorldgen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main Fabric entrypoint for Chaotic Dimensions. */
public final class ChaoticDimensions implements ModInitializer {
    public static final String MOD_ID = "chaoticd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        RosalitaAxeStripping.initialize();
        ModItems.initialize();
        ModCreativeTabs.initialize();
        ModWorldgen.initialize();
        LOGGER.info("Initializing Chaotic Dimensions on Fabric");
    }
}
