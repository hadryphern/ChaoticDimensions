package net.blue.chaoticd.worldgen.water;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** Data-driven tuning for Aurora lakes, pond islands and waterfalls. */
public record AuroraWaterConfiguration(
    Mode mode,
    int minRadius,
    int maxRadius,
    int minDepth,
    int maxDepth
) implements FeatureConfiguration {
    public static final Codec<AuroraWaterConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Mode.CODEC.fieldOf("mode").forGetter(AuroraWaterConfiguration::mode),
        Codec.intRange(2, 12).fieldOf("min_radius").forGetter(AuroraWaterConfiguration::minRadius),
        Codec.intRange(2, 12).fieldOf("max_radius").forGetter(AuroraWaterConfiguration::maxRadius),
        Codec.intRange(1, 12).fieldOf("min_depth").forGetter(AuroraWaterConfiguration::minDepth),
        Codec.intRange(1, 16).fieldOf("max_depth").forGetter(AuroraWaterConfiguration::maxDepth)
    ).apply(instance, AuroraWaterConfiguration::new));

    public AuroraWaterConfiguration {
        if (minRadius > maxRadius) {
            throw new IllegalArgumentException("min_radius cannot exceed max_radius");
        }
        if (minDepth > maxDepth) {
            throw new IllegalArgumentException("min_depth cannot exceed max_depth");
        }
    }

    public enum Mode {
        SURFACE_LAKE("surface_lake"),
        POND_ISLAND("pond_island"),
        WATERFALL("waterfall");

        private static final Codec<Mode> CODEC = Codec.STRING.xmap(
            Mode::fromSerializedName,
            Mode::serializedName
        );

        private final String serializedName;

        Mode(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        private static Mode fromSerializedName(String name) {
            String normalized = name.toLowerCase(Locale.ROOT);

            for (Mode mode : values()) {
                if (mode.serializedName.equals(normalized)) {
                    return mode;
                }
            }

            throw new IllegalArgumentException(
                "Unknown Aurora water feature mode: " + name
            );
        }
    }
}