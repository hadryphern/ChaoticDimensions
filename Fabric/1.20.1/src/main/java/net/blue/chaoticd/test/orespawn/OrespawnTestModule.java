package net.blue.chaoticd.test.orespawn;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.test.orespawn.command.OrespawnTestCommands;
import net.blue.chaoticd.test.orespawn.registry.OrespawnTestEntities;
import net.blue.chaoticd.test.orespawn.registry.OrespawnTestItemGroups;
import net.blue.chaoticd.test.orespawn.registry.OrespawnTestItems;

/**
 * Isolated bootstrap for local Orespawn study infrastructure.
 *
 * <p>This module contains original test harness code only. It never loads the
 * legacy JAR, its classes, assets, audio or data at runtime. Its registry IDs
 * are deliberately registered on both sides regardless of the local toggle:
 * dynamic registry omission causes unsafe client/server mismatches in Fabric.
 * When disabled, the tab has no entries and action commands are unavailable.</p>
 */
public final class OrespawnTestModule {
    private static boolean initialized;
    private static boolean enabled;

    private OrespawnTestModule() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        OrespawnTestConfig.initialize();
        enabled = OrespawnTestConfig.isEnabled();

        // Registries are a deterministic client/server contract. These are
        // inert, original harness IDs; no legacy content becomes available
        // merely because they exist in the registry.
        OrespawnTestEntities.initialize();
        OrespawnTestItems.initialize();
        OrespawnTestItemGroups.initialize();
        OrespawnTestCommands.initialize();

        if (!enabled) {
            ChaoticDimensions.LOGGER.info(
                "[Orespawn Test] Disabled by default. Registered only inert compatibility IDs; the tab is empty and action commands are blocked. Configure {} only for disposable development worlds.",
                OrespawnTestConfig.path()
            );
            return;
        }

        ChaoticDimensions.LOGGER.info(
            "[Orespawn Test] ENABLED locally: 2 original test items, 1 original proxy entity, 0 imported assets, 0 imported sounds, 0 imported legacy classes."
        );
    }

    public static boolean isEnabled() {
        return initialized && enabled;
    }
}
