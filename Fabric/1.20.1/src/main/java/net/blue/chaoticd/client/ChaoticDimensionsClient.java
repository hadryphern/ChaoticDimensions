package net.blue.chaoticd.client;

import net.fabricmc.api.ClientModInitializer;

/** Registers visual behavior that must never be loaded on a dedicated server. */
public final class ChaoticDimensionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SapphiricVisuals.initialize();
    }
}
