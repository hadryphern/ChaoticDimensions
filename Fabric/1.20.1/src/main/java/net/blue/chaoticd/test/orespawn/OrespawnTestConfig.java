package net.blue.chaoticd.test.orespawn;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Local opt-in switch for the removable Orespawn reference test harness.
 *
 * <p>The value is deliberately read before test presentation and actions are
 * exposed. It defaults to false, so normal installations show no test-tab
 * entries and block all action commands. Inert registry IDs remain stable on
 * both sides to prevent a client/server registry mismatch.</p>
 */
public final class OrespawnTestConfig {
    public static final String FILE_NAME = "chaoticd-orespawn-test.properties";
    public static final String ENABLE_KEY = "enableOrespawnTestContent";

    private static boolean initialized;
    private static boolean enabled;
    private static Path path;

    private OrespawnTestConfig() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        Properties properties = new Properties();

        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                properties.setProperty(ENABLE_KEY, "false");
                try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    properties.store(writer,
                        "Temporary local Orespawn reference test harness. "
                            + "Enable only in a disposable development world.");
                }
            } else {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException | IllegalArgumentException | SecurityException exception) {
            ChaoticDimensions.LOGGER.warn(
                "[Orespawn Test] Could not read {}. Keeping the module disabled.",
                path,
                exception
            );
            enabled = false;
            return;
        }

        String configuredValue = properties.getProperty(ENABLE_KEY, "false").trim();
        if (!configuredValue.equalsIgnoreCase("true") && !configuredValue.equalsIgnoreCase("false")) {
            ChaoticDimensions.LOGGER.warn(
                "[Orespawn Test] Invalid {}={} in {}. Keeping the module disabled.",
                ENABLE_KEY,
                configuredValue,
                path
            );
            enabled = false;
            return;
        }

        enabled = Boolean.parseBoolean(configuredValue);
    }

    public static boolean isEnabled() {
        if (!initialized) {
            initialize();
        }
        return enabled;
    }

    public static Path path() {
        if (!initialized) {
            initialize();
        }
        return path;
    }
}
