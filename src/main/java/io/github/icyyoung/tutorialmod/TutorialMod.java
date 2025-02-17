package io.github.icyyoung.tutorialmod;

import com.mojang.logging.LogUtils;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.effect.ModEffects;
import io.github.icyyoung.tutorialmod.enchantment.ModEnchantmentEffects;
import io.github.icyyoung.tutorialmod.entity.ModEntities;
import io.github.icyyoung.tutorialmod.entity.client.GeckoRenderer;
import io.github.icyyoung.tutorialmod.item.ModArmorMaterials;
import io.github.icyyoung.tutorialmod.item.ModCreativeModeTabs;
import io.github.icyyoung.tutorialmod.item.ModItems;
import io.github.icyyoung.tutorialmod.loot.ModLootModifiers;
import io.github.icyyoung.tutorialmod.potion.ModPotions;
import io.github.icyyoung.tutorialmod.sound.ModSounds;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TutorialMod.MOD_ID)
public class TutorialMod
{
    public static final String MOD_ID = "tutorialmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TutorialMod(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModSounds.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModPotions.register(modEventBus);
        ModEffects.register(modEventBus);
        ModEnchantmentEffects.register(modEventBus);
        ModEntities.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        //for our custom flower
        event.enqueueWork(()->{
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.CATMINT.getId(),ModBlocks.POTTED_CATMINT);
        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)    {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BISMUTH);
            event.accept(ModItems.RAW_BISMUTH);
            event.accept(ModItems.SAPPHIRE);
            event.accept(ModItems.RAW_SAPPHIRE);

            event.accept(ModItems.METAL_DETECTOR);
        }

        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){
            event.accept(ModBlocks.BISMUTH_BLOCK);
            event.accept(ModBlocks.BISMUTH_ORE);
            event.accept(ModBlocks.SAPPHIRE_BLOCK);
            event.accept(ModBlocks.SAPPHIRE_ORE);
            event.accept(ModBlocks.RAW_SAPPHIRE_BLOCK);
            event.accept(ModBlocks.DEEPSLATE_SAPPHIRE_ORE);
            event.accept(ModBlocks.NETHER_SAPPHIRE_ORE);
            event.accept(ModBlocks.END_STONE_SAPPHIRE_ORE);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntities.GECKO.get(), GeckoRenderer::new);
        }
    }
}
