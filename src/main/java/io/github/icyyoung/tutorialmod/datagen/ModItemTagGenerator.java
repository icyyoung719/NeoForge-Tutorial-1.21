package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/**
 * @Description 生成物品的Tag，如盔甲，唱片等
 * @Author icyyoung
 * @Date 2024/9/5
 */

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ItemTags.LOGS_THAT_BURN)
                .add(ModBlocks.BLOODWOOD_LOG.get().asItem())
                .add(ModBlocks.BLOODWOOD_WOOD.get().asItem())
                .add(ModBlocks.STRIPPED_BLOODWOOD_LOG.get().asItem())
                .add(ModBlocks.STRIPPED_BLOODWOOD_WOOD.get().asItem());

        this.tag(ItemTags.PLANKS)
                .add(ModBlocks.BLOODWOOD_PLANKS.get().asItem());
    }
}
