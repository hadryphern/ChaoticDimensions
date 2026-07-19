package net.blue.chaoticd.content.block;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Reserved for author-provided stripped-log art.
 *
 * <p>Rosalita's stripped variants are intentionally not reachable yet: the
 * required {@code stripped_rosalita_log(.png)} source art was not supplied,
 * so this class must not swap a correct block for a fabricated visual.</p>
 */
public final class RosalitaAxeStripping {
    private RosalitaAxeStripping() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);
            if (!(held.getItem() instanceof AxeItem)) {
                return InteractionResult.PASS;
            }

            return InteractionResult.PASS;
        });
    }

}
