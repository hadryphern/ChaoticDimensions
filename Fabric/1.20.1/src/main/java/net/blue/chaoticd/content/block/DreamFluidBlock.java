package net.blue.chaoticd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

/**
 * Dream Fluid block with heavy, lava-like movement.
 *
 * <p>Players are killed by DreamFluidSystems. This class is responsible only
 * for making entities and dropped items move through the liquid very slowly.</p>
 */
public final class DreamFluidBlock extends LiquidBlock {
    private static final double HORIZONTAL_DRAG = 0.22D;
    private static final double ENTITY_VERTICAL_DRAG = 0.12D;
    private static final double ITEM_VERTICAL_DRAG = 0.08D;
    private static final double MAXIMUM_ITEM_SINK_SPEED = -0.012D;

    public DreamFluidBlock(
        FlowingFluid fluid,
        BlockBehaviour.Properties properties
    ) {
        super(fluid, properties);
    }

    @Override
    @SuppressWarnings("deprecation") // LiquidBlock's 1.20.1 entity hook is deprecated upstream but required here.
    public void entityInside(
        BlockState state,
        Level level,
        BlockPos pos,
        Entity entity
    ) {
        Vec3 movement =
            entity.getDeltaMovement();

        double verticalMovement;

        if (entity instanceof ItemEntity) {
            verticalMovement =
                Math.max(
                    movement.y
                        * ITEM_VERTICAL_DRAG,
                    MAXIMUM_ITEM_SINK_SPEED
                );
        } else {
            verticalMovement =
                movement.y
                    * ENTITY_VERTICAL_DRAG;
        }

        entity.setDeltaMovement(
            movement.x * HORIZONTAL_DRAG,
            verticalMovement,
            movement.z * HORIZONTAL_DRAG
        );

        entity.fallDistance = 0.0F;
    }
}
