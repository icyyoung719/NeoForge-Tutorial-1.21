package io.github.icyyoung.tutorialmod.entity.client;

import io.github.icyyoung.tutorialmod.entity.custom.ChairEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/20
 */

public class ChairRenderer extends EntityRenderer<ChairEntity> {
    public ChairRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ChairEntity chairEntity) {
        return null;
    }

    @Override
    public boolean shouldRender(ChairEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
