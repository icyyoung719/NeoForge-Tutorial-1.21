package io.github.icyyoung.tutorialmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.entity.custom.GeckoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/17
 */

public class GeckoRenderer extends MobRenderer<GeckoEntity, GeckoModel<GeckoEntity>> {
    public GeckoRenderer(EntityRendererProvider.Context context) {
        super(context, new GeckoModel<>(context.bakeLayer(GeckoModel.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(GeckoEntity geckoEntity) {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/entity/gecko/gecko_blue.png");
    }

    @Override
    public void render(GeckoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(entity.isBaby()) {
            poseStack.scale(0.45f, 0.45f, 0.45f);
        } else {
            poseStack.scale(1f, 1f, 1f);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
