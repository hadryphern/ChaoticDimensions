package net.blue.chaoticd;

import net.fabricmc.api.ModInitializer;
import net.blue.chaoticd.content.ModEffects;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModPotions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Empty Fabric foundation for the next Chaotic Dimensions implementation. */
public final class ChaoticDimensions implements ModInitializer {
    public static final String MOD_ID = "chaoticd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEffects.initialize();
        ModItems.initialize();
        ModPotions.initialize();
        LOGGER.info("Chaotic Dimensions Sapphire content loaded");
    }
}
