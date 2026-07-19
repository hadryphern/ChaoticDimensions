package net.blue.chaoticd.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/** Renders the supplied 64x64 Rosalita chest skins without using a block atlas. */
public final class RosalitaChestRenderer<T extends ChestBlockEntity> implements BlockEntityRenderer<T> {
    private static final ResourceLocation SINGLE = new ResourceLocation(ChaoticDimensions.MOD_ID, "textures/entity/chest/rosalita_normal.png");
    private static final ResourceLocation LEFT = new ResourceLocation(ChaoticDimensions.MOD_ID, "textures/entity/chest/rosalita_normal_left.png");
    private static final ResourceLocation RIGHT = new ResourceLocation(ChaoticDimensions.MOD_ID, "textures/entity/chest/rosalita_normal_right.png");

    private final ModelPart singleLid;
    private final ModelPart singleBottom;
    private final ModelPart singleLock;
    private final ModelPart leftLid;
    private final ModelPart leftBottom;
    private final ModelPart leftLock;
    private final ModelPart rightLid;
    private final ModelPart rightBottom;
    private final ModelPart rightLock;

    public RosalitaChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart single = context.bakeLayer(ModelLayers.CHEST);
        singleLid = single.getChild("lid");
        singleBottom = single.getChild("bottom");
        singleLock = single.getChild("lock");
        ModelPart left = context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
        leftLid = left.getChild("lid");
        leftBottom = left.getChild("bottom");
        leftLock = left.getChild("lock");
        ModelPart right = context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
        rightLid = right.getChild("lid");
        rightBottom = right.getChild("bottom");
        rightLock = right.getChild("lock");
    }

    @Override
    public void render(T chest, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = chest.getBlockState();
        Direction facing = state.hasProperty(ChestBlock.FACING) ? state.getValue(ChestBlock.FACING) : Direction.SOUTH;
        ChestType type = state.hasProperty(ChestBlock.TYPE) ? state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        float openness = chest.getOpenNess(partialTick);
        float lidAngle = 1.0F - openness;
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(textureFor(type)));
        if (type == ChestType.LEFT) {
            renderParts(leftLid, leftBottom, leftLock, lidAngle, poseStack, vertices, packedLight);
        } else if (type == ChestType.RIGHT) {
            renderParts(rightLid, rightBottom, rightLock, lidAngle, poseStack, vertices, packedLight);
        } else {
            renderParts(singleLid, singleBottom, singleLock, lidAngle, poseStack, vertices, packedLight);
        }
        poseStack.popPose();
    }

    private static ResourceLocation textureFor(ChestType type) {
        return switch (type) {
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
            default -> SINGLE;
        };
    }

    private static void renderParts(ModelPart lid, ModelPart bottom, ModelPart lock, float lidAngle,
                                    PoseStack poseStack, VertexConsumer vertices, int packedLight) {
        lid.xRot = -(lidAngle * ((float) Math.PI / 2.0F));
        lock.xRot = lid.xRot;
        lid.render(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY);
        lock.render(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY);
        bottom.render(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
