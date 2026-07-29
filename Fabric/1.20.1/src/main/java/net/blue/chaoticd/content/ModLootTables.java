package net.blue.chaoticd.content;

import java.util.Set;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Very rare Ruby Nugget injection into requested vanilla dungeon chests. */
public final class ModLootTables {
    private static final float RUBY_NUGGET_CHANCE = 0.02F;

    private static final Set<ResourceLocation> RUBY_NUGGET_CHESTS = Set.of(
        vanilla("chests/simple_dungeon"),
        vanilla("chests/abandoned_mineshaft"),
        vanilla("chests/nether_bridge"),
        vanilla("chests/bastion_bridge"),
        vanilla("chests/bastion_hoglin_stable"),
        vanilla("chests/bastion_other"),
        vanilla("chests/bastion_treasure"),
        vanilla("chests/end_city_treasure"),
        vanilla("chests/stronghold_corridor"),
        vanilla("chests/stronghold_crossing"),
        vanilla("chests/stronghold_library"),
        vanilla("chests/ancient_city"),
        vanilla("chests/woodland_mansion"),
        vanilla("chests/desert_pyramid"),
        vanilla("chests/jungle_temple"),
        vanilla("chests/ruined_portal"),
        vanilla("chests/shipwreck_treasure")
    );

    private ModLootTables() {
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!source.isBuiltin() || !RUBY_NUGGET_CHESTS.contains(id)) {
                return;
            }

            LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(RUBY_NUGGET_CHANCE))
                .add(
                    LootItem.lootTableItem(ModItems.RUBY_NUGGET)
                        .apply(
                            SetItemCountFunction.setCount(
                                UniformGenerator.between(1.0F, 2.0F)
                            )
                        )
                );

            tableBuilder.pool(pool.build());
            
        });
    }

    private static ResourceLocation vanilla(String path) {
        return new ResourceLocation("minecraft", path);
    }
}
