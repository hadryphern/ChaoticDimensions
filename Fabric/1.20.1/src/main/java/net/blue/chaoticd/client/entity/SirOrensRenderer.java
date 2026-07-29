package net.blue.chaoticd.client.entity;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.entity.SirOrensEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Uses the normal villager model while a custom PNG is optional.  Returning
 * vanilla's base skin until the artist adds the file avoids a missing-texture
 * purple/black villager in development worlds.
 */
public final class SirOrensRenderer extends MobRenderer<SirOrensEntity, VillagerModel<SirOrensEntity>> {
    private static final ResourceLocation CUSTOM_TEXTURE = new ResourceLocation(
        ChaoticDimensions.MOD_ID,
        "textures/entity/villager/sir_orens.png"
    );
    private static final ResourceLocation VANILLA_TEXTURE = new ResourceLocation(
        "minecraft",
        "textures/entity/villager/villager.png"
    );

    public SirOrensRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SirOrensEntity entity) {
        return Minecraft.getInstance().getResourceManager().getResource(CUSTOM_TEXTURE).isPresent()
            ? CUSTOM_TEXTURE
            : VANILLA_TEXTURE;
    }
}
