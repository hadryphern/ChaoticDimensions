package net.blue.chaoticd.test.orespawn.registry;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.test.orespawn.item.OrespawnTestReferenceItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/** Original marker and proxy spawn egg; neither is copied from the legacy JAR. */
public final class OrespawnTestItems {
    public static final Item REFERENCE_MARKER = register(
        "orespawn_test_reference_marker",
        new OrespawnTestReferenceItem(new Item.Properties().stacksTo(1))
    );

    public static final Item REFERENCE_PROXY_SPAWN_EGG = register(
        "orespawn_test_reference_proxy_spawn_egg",
        new SpawnEggItem(
            OrespawnTestEntities.REFERENCE_PROXY,
            0x595278,
            0xD5D0E8,
            new Item.Properties()
        )
    );

    private OrespawnTestItems() {
    }

    private static Item register(String id, Item item) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id),
            item
        );
    }

    public static void initialize() {
        // Static fields perform the registry insertions.
    }
}
