package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * @Description 所有Datagen的注册类
 * @Author icyyoung
 * @Date 2024/9/4
 */

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TutorialMod.MOD_ID)
public class DataGenerateHandler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // other providers here
        generator.addProvider(
                //正常
                event.includeClient(),
                new ModBlockStateGenerator(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new ModItemModelGenerator(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new ModLanguageGenerator(output, "en_us")
        );
    }
}
