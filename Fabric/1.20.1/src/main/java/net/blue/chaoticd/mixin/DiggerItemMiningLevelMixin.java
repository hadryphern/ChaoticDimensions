package net.blue.chaoticd.mixin;

import net.blue.chaoticd.content.progression.MiningProgression;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric natively handles every custom level through
 * {@code fabric:needs_tool_level_N}.  Gold is the only compatibility case:
 * vanilla declares it as level zero while Chaotic Dimensions defines the Gold
 * Pickaxe as level two.
 */
@Mixin(DiggerItem.class)
public abstract class DiggerItemMiningLevelMixin {
    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void chaoticd$applyGoldPickaxeLevel(
        BlockState state,
        CallbackInfoReturnable<Boolean> callback
    ) {
        DiggerItem item = (DiggerItem) (Object) this;
        if (item.getTier() != Tiers.GOLD
            || !(item instanceof PickaxeItem)
            || !state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return;
        }

        int requiredLevel = MiningLevelManager.getRequiredMiningLevel(state);
        callback.setReturnValue(MiningProgression.canHarvest(
            MiningProgression.effectiveMiningLevel(item.getTier()),
            requiredLevel
        ));
    }
}
