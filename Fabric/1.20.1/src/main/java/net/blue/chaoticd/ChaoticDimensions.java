package net.blue.chaoticd;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Empty Fabric foundation for the next Chaotic Dimensions implementation. */
public final class ChaoticDimensions implements ModInitializer {
    public static final String MOD_ID = "chaoticd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Chaotic Dimensions clean foundation loaded");
    }
}
