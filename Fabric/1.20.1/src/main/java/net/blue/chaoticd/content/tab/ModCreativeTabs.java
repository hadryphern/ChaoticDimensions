package net.blue.chaoticd.content.tab;

import java.util.LinkedHashSet;
import java.util.Set;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Separates recovered Forge content from new Fabric-native content. */
public final class ModCreativeTabs {
    private static final Set<Item> NEW_CONTENT = new LinkedHashSet<>();

    private ModCreativeTabs() {
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(ChaoticDimensions.MOD_ID, "legacy"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.chaoticd.legacy"))
                .icon(() -> new ItemStack(ModItems.get("ruby")))
                .displayItems((parameters, output) -> ModItems.values().forEach(output::accept))
                .build());

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(ChaoticDimensions.MOD_ID, "main"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                .title(Component.translatable("itemGroup.chaoticd.main"))
                .icon(() -> new ItemStack(Items.COMPASS))
                .displayItems((parameters, output) -> {
                    ModItems.newContentValues().forEach(output::accept);
                    NEW_CONTENT.forEach(output::accept);
                })
                .build());
    }

    /** Adds a newly designed Fabric item to the main Chaotic Dimensions tab. */
    public static void addNewContent(Item item) {
        NEW_CONTENT.add(item);
    }
}
