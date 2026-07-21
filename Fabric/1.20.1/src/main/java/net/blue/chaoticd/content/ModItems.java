package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.item.SapphireSwordItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** The first clean-slate items: the manual Sapphire assets and their sword. */
public final class ModItems {
    public static final Item SAPPHIRE_GEM = register("sapphire_gem", new Item(new Item.Properties()));
    public static final Item SAPPHIRE_SWORD = register("sapphire_sword", new SapphireSwordItem(new Item.Properties()));

    private ModItems() {
    }

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ChaoticDimensions.MOD_ID, id), item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> entries.accept(SAPPHIRE_SWORD));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> entries.accept(SAPPHIRE_GEM));
    }
}
