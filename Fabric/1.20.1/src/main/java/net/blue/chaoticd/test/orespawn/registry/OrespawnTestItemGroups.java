package net.blue.chaoticd.test.orespawn.registry;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.test.orespawn.OrespawnTestModule;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Stable creative-tab shell for the removable, explicitly enabled test harness. */
public final class OrespawnTestItemGroups {
    public static final CreativeModeTab ORESPAWN = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "orespawn"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.chaoticd.orespawn"))
            .icon(() -> new ItemStack(OrespawnTestModule.isEnabled()
                ? OrespawnTestItems.REFERENCE_MARKER
                : Items.BARRIER))
            .displayItems((parameters, entries) -> {
                if (!OrespawnTestModule.isEnabled()) {
                    return;
                }

                // Materials/test marker first, then the test entity spawn egg.
                entries.accept(OrespawnTestItems.REFERENCE_MARKER);
                entries.accept(OrespawnTestItems.REFERENCE_PROXY_SPAWN_EGG);
            })
            .build()
    );

    private OrespawnTestItemGroups() {
    }

    public static void initialize() {
        // Static field performs the registry insertion.
    }
}
