package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

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
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // other providers here
        generator.addProvider(
                //正常
                event.includeClient(),
                new ModBlockStateProvider(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new ModItemModelProvider(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new ModLanguageProvider(output, "en_us")
        );
        generator.addProvider(
                event.includeServer(),
                new ModRecipeProvider(output, lookupProvider)
        );
        generator.addProvider(
                event.includeServer(),
                new ModLootTableProvider(output, lookupProvider)
        );
        generator.addProvider(
                event.includeServer(),
                new ModBlockTagGenerator(output, lookupProvider,existingFileHelper)
        );
        generator.addProvider(
                event.includeServer(),
                new ModGlobalLootModifiersProvider(output, lookupProvider));

//        ModBlockTagGenerator blockTagGenerator = generator.addProvider(event.includeServer(),
//                new ModBlockTagGenerator(output, lookupProvider, existingFileHelper));
//        generator.addProvider(
//                event.includeServer(),
//                new ModItemTagGenerator(output, lookupProvider,blockTagGenerator.contentsGetter())
//        );
    }
}
