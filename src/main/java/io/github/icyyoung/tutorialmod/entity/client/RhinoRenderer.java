package io.github.icyyoung.tutorialmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.entity.custom.RhinoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/23
 */

public class RhinoRenderer extends MobRenderer<RhinoEntity, RhinoModel<RhinoEntity>> {
    public RhinoRenderer(EntityRendererProvider.Context context) {
        super(context, new RhinoModel<>(context.bakeLayer(RhinoModel.LAYER_LOCATION)), 2f);
    }

    @Override
    public ResourceLocation getTextureLocation(RhinoEntity rhinoEntity) {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/entity/rhino.png");
    }

    @Override
    public void render(RhinoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(entity.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
