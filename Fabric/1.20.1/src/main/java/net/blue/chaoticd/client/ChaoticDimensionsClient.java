package net.blue.chaoticd.client;

import net.fabricmc.api.ClientModInitializer;

/** Client-only Fabric entrypoint. Renderers and screens are registered here. */
public final class ChaoticDimensionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RosalitaClientVisuals.initialize();
    }
}
