package net.blue.chaoticd.worldgen.shadow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** Data-driven dimensions for contained Shadow lava pools. */
public record ShadowLavaConfiguration(
    int minRadius,
    int maxRadius,
    int minDepth,
    int maxDepth
) implements FeatureConfiguration {
    public static final Codec<ShadowLavaConfiguration> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.intRange(2, 8)
                .fieldOf("min_radius")
                .forGetter(ShadowLavaConfiguration::minRadius),
            Codec.intRange(2, 8)
                .fieldOf("max_radius")
                .forGetter(ShadowLavaConfiguration::maxRadius),
            Codec.intRange(1, 6)
                .fieldOf("min_depth")
                .forGetter(ShadowLavaConfiguration::minDepth),
            Codec.intRange(1, 8)
                .fieldOf("max_depth")
                .forGetter(ShadowLavaConfiguration::maxDepth)
        ).apply(instance, ShadowLavaConfiguration::new)
    );

    public ShadowLavaConfiguration {
        if (minRadius > maxRadius) {
            throw new IllegalArgumentException("min_radius cannot exceed max_radius");
        }

        if (minDepth > maxDepth) {
            throw new IllegalArgumentException("min_depth cannot exceed max_depth");
        }
    }
}
