package net.blue.chaoticd.content.entity;

import software.bernie.geckolib.animatable.GeoEntity;

/** Common GeckoLib contract shared by all restored legacy creatures. */
public interface LegacyAnimatedMob extends GeoEntity {
    LegacyMobVariant variant();
}
