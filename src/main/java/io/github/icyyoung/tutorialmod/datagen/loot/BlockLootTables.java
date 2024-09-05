package io.github.icyyoung.tutorialmod.datagen.loot;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description 挖矿掉落物的datagen
 * @Author icyyoung
 * @Date 2024/9/5
 */

public class BlockLootTables extends BlockLootSubProvider {
    public BlockLootTables(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }
    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::value).collect(Collectors.toList());
    }

    @Override
    protected void generate() {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        dropSelf(ModBlocks.BISMUTH_BLOCK.get());
        dropSelf(ModBlocks.SAPPHIRE_BLOCK.get());
        dropSelf(ModBlocks.RAW_SAPPHIRE_BLOCK.get());
        dropSelf(ModBlocks.SOUND_BLOCK.get());

        add(ModBlocks.BISMUTH_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.BISMUTH_ORE.get(), ModItems.RAW_BISMUTH.get(),registrylookup));             ;
        add(ModBlocks.SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get(),registrylookup));
        add(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get(),registrylookup));
        add(ModBlocks.NETHER_SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.NETHER_SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get(),registrylookup));
        add(ModBlocks.END_STONE_SAPPHIRE_ORE.get(),
                block -> createCopperLikeOreDrops(ModBlocks.END_STONE_SAPPHIRE_ORE.get(), ModItems.RAW_SAPPHIRE.get(),registrylookup));

    }

    protected LootTable.Builder createCopperLikeOreDrops(Block pBlock,Item pItem,HolderLookup.RegistryLookup<Enchantment> pRegistrylookup){
        return this.createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(pItem)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(pRegistrylookup.getOrThrow(Enchantments.FORTUNE)))
                )
        );
    }
}
