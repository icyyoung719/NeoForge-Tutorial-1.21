package io.github.icyyoung.tutorialmod.block;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.custom.SoundBlock;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TutorialMod.MOD_ID);

    public static final DeferredBlock<Block> BISMUTH_BLOCK = registerBlock("bismuth_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> BISMUTH_ORE = registerBlock("bismuth_ore",
         () -> new DropExperienceBlock(UniformInt.of(2, 4),
                 BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final DeferredBlock<Block> SAPPHIRE_BLOCK = registerBlock("sapphire_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
    public static final DeferredBlock<Block> RAW_SAPPHIRE_BLOCK = registerBlock("raw_sapphire_block",
            ()-> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> SAPPHIRE_ORE = registerBlock("sapphire_ore",
            () -> new DropExperienceBlock( UniformInt.of(2, 4),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEEPSLATE_SAPPHIRE_ORE = registerBlock("deepslate_sapphire_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 7),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .strength(3f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> NETHER_SAPPHIRE_ORE = registerBlock("nether_sapphire_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 7),
                 BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERRACK)
                 .strength(1f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> END_STONE_SAPPHIRE_ORE = registerBlock("end_stone_sapphire_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 7),
                 BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE)
                 .strength(5f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> SOUND_BLOCK = registerBlock("sound_block",
            () -> new SoundBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3f)));







    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block){
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
