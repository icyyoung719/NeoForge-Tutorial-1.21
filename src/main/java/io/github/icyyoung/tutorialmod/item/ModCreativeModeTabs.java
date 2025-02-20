package io.github.icyyoung.tutorialmod.item;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @Description This will only actually register when opening inventory
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TutorialMod.MOD_ID);

    public static final Supplier<CreativeModeTab> BISMUTH_ITEM_TAB = CREATIVE_MOD_TAB.register("bismuth_item_tab",
            () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.SAPPHIRE.get()))
                    .title(Component.translatable("creativetab.tutorialmod.bismuth_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.BISMUTH.get());
                        output.accept(ModItems.RAW_BISMUTH.get());
                        output.accept(ModItems.SAPPHIRE.get());
                        output.accept(ModItems.RAW_SAPPHIRE.get());
                        output.accept(ModItems.METAL_DETECTOR.get());
                        output.accept(ModItems.CHISEL.get());
                        output.accept(ModItems.STRAWBERRY.get());
                        output.accept(ModItems.PINE_CONE.get());
                        output.accept(ModItems.SAPPHIRE_STAFF.get());
                        output.accept(ModItems.SAPPHIRE_SWORD.get());
                        output.accept(ModItems.SAPPHIRE_SHOVEL.get());
                        output.accept(ModItems.SAPPHIRE_PICKAXE.get());
                        output.accept(ModItems.SAPPHIRE_AXE.get());
                        output.accept(ModItems.SAPPHIRE_HOE.get());
                        output.accept(ModItems.SAPPHIRE_HELMET.get());
                        output.accept(ModItems.SAPPHIRE_CHESTPLATE.get());
                        output.accept(ModItems.SAPPHIRE_LEGGINGS.get());
                        output.accept(ModItems.SAPPHIRE_BOOTS.get());
                        output.accept(ModItems.STRAWBERRY_SEEDS.get());
                        output.accept(ModItems.CORN_SEEDS.get());
                        output.accept(ModItems.CORN.get());
                        output.accept(ModItems.BAR_BRAWL_MUSIC_DISC.get());
                        output.accept(ModItems.GECKO_SPAWN_EGG.get());
                        output.accept(ModItems.TOMAHAWK.get());
                        output.accept(ModItems.RADIATION_STAFF);
                    }).build());

    public static final Supplier<CreativeModeTab> BISMUTH_BLOCK_TAB = CREATIVE_MOD_TAB.register("bismuth_block_tab",
            () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModBlocks.SAPPHIRE_BLOCK.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "bismuth_item_tab"))
                    .title(Component.translatable("creativetab.tutorialmod.bismuth_block"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.BISMUTH_BLOCK.get());
                        output.accept(ModBlocks.BISMUTH_ORE.get());
                        output.accept(ModBlocks.SAPPHIRE_BLOCK.get());
                        output.accept(ModBlocks.SAPPHIRE_ORE.get());
                        output.accept(ModBlocks.RAW_SAPPHIRE_BLOCK.get());
                        output.accept(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get());
                        output.accept(ModBlocks.END_STONE_SAPPHIRE_ORE.get());
                        output.accept(ModBlocks.NETHER_SAPPHIRE_ORE.get());
                        output.accept(ModBlocks.SOUND_BLOCK.get());

                        output.accept(ModBlocks.SAPPHIRE_BUTTON.get());
                        output.accept(ModBlocks.SAPPHIRE_PRESSURE_PLATE.get());
                        output.accept(ModBlocks.SAPPHIRE_STAIRS.get());
                        output.accept(ModBlocks.SAPPHIRE_SLAB.get());
                        output.accept(ModBlocks.SAPPHIRE_WALL.get());
                        output.accept(ModBlocks.SAPPHIRE_FENCE.get());
                        output.accept(ModBlocks.SAPPHIRE_FENCE_GATE.get());
                        output.accept(ModBlocks.SAPPHIRE_TRAPDOOR.get());
                        output.accept(ModBlocks.SAPPHIRE_DOOR.get());
                        output.accept(ModBlocks.CATMINT.get());
                        output.accept(ModBlocks.CHAIR.get());

                        output.accept(ModBlocks.BLOODWOOD_LOG.get());
                        output.accept(ModBlocks.BLOODWOOD_WOOD.get());
                        output.accept(ModBlocks.STRIPPED_BLOODWOOD_LOG.get());
                        output.accept(ModBlocks.STRIPPED_BLOODWOOD_WOOD.get());
                        output.accept(ModBlocks.BLOODWOOD_PLANKS.get());
                        output.accept(ModBlocks.BLOODWOOD_SAPLING.get());
                        output.accept(ModBlocks.BLOODWOOD_LEAVES.get());
                    }).build());


//    public static final Supplier<CreativeModeTab> TUTORIAL_TAB = CREATIVE_MOD_TAB.register("tutorial_tab",
//            () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.SAPPHIRE.get()))
//                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "bismuth_block_tab"))
//                    .title(Component.translatable("creativetab.tutorialmod.sapphire_items"))
//                    .displayItems((itemDisplayParameters, output) -> {
//                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MOD_TAB.register(eventBus);
    }
}
