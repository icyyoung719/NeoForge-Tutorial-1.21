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
 * @Description TODO
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TutorialMod.MOD_ID);

    public static final Supplier<CreativeModeTab> BISMUTH_ITEM_TAB = CREATIVE_MOD_TAB.register("bismuth_item_tab",
            () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.BISMUTH.get()))
                    .title(Component.translatable("creativetab.tutorialmod.bismuth_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.BISMUTH.get());
                        output.accept(ModItems.RAW_BISMUTH.get());
                    }).build());

    public static final Supplier<CreativeModeTab> BISMUTH_BLOCK_TAB = CREATIVE_MOD_TAB.register("bismuth_block_tab",
            () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModBlocks.BISMUTH_BLOCK.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "bismuth_item_tab"))
                    .title(Component.translatable("creativetab.tutorialmod.bismuth_block"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.BISMUTH_BLOCK.get());
                        output.accept(ModBlocks.BISMUTH_ORE.get());
                    }).build());


    public static void register(IEventBus eventBus){
        CREATIVE_MOD_TAB.register(eventBus);
    }
}
