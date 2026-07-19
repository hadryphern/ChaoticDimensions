package net.blue.chaoticd.content.block;

import net.blue.chaoticd.content.item.ModItems;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;

/** Registers the Rosalita wood family with vanilla fire, furnace and composter rules. */
public final class RosalitaVanillaIntegrations {
    private RosalitaVanillaIntegrations() {
    }

    public static void initialize() {
        FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
        for (String id : new String[] {
            "rosalita_log", "rosalita_wood", "rosalita_planks", "rosalita_stairs", "rosalita_slab",
            "rosalita_fence", "rosalita_fence_gate", "rosalita_door", "rosalita_trapdoor",
            "rosalita_pressure_plate", "rosalita_button", "rosalita_ladder"
        }) {
            fire.add(ModBlocks.get(id), 5, 20);
        }
        fire.add(ModBlocks.get("rosalita_leaves"), 30, 60);

        for (String id : new String[] {"rosalita_log", "rosalita_wood", "rosalita_planks", "rosalita_stairs", "rosalita_slab"}) {
            FuelRegistry.INSTANCE.add(ModItems.get(id), 300);
        }
        FuelRegistry.INSTANCE.add(ModItems.get("rosalita_fence"), 300);
        FuelRegistry.INSTANCE.add(ModItems.get("rosalita_fence_gate"), 300);
        FuelRegistry.INSTANCE.add(ModItems.get("rosalita_door"), 200);
        FuelRegistry.INSTANCE.add(ModItems.get("rosalita_trapdoor"), 300);
        FuelRegistry.INSTANCE.add(ModItems.get("rosalita_ladder"), 300);
        FuelRegistry.INSTANCE.add(ModItems.get("rosalita_stick"), 100);

        CompostingChanceRegistry.INSTANCE.add(ModItems.get("rosalita_leaves"), 0.3F);
        CompostingChanceRegistry.INSTANCE.add(ModItems.get("rosalita_sapling"), 0.3F);
    }
}
