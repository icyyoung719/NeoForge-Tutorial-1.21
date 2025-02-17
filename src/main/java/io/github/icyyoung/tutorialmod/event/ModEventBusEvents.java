package io.github.icyyoung.tutorialmod.event;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.entity.ModEntities;
import io.github.icyyoung.tutorialmod.entity.client.GeckoModel;
import io.github.icyyoung.tutorialmod.entity.custom.GeckoEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/17
 */

@EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GeckoModel.LAYER_LOCATION, GeckoModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntities.GECKO.get(), GeckoEntity.createAttributes().build());
    }
}
