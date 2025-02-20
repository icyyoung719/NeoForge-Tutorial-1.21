package io.github.icyyoung.tutorialmod.datagen.loot;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.block.custom.CornCropBlock;
import io.github.icyyoung.tutorialmod.block.custom.StrawberryCropBlock;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
        dropSelf(ModBlocks.CHAIR.get());

        dropSelf(ModBlocks.SAPPHIRE_WALL.get());
        dropSelf(ModBlocks.SAPPHIRE_TRAPDOOR.get());
        dropSelf(ModBlocks.SAPPHIRE_FENCE.get());
        dropSelf(ModBlocks.SAPPHIRE_FENCE_GATE.get());
        dropSelf(ModBlocks.SAPPHIRE_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.SAPPHIRE_BUTTON.get());
        dropSelf(ModBlocks.SAPPHIRE_STAIRS.get());

        dropSelf(ModBlocks.CATMINT.get());
        add(ModBlocks.POTTED_CATMINT.get(), createPotFlowerItemTable(ModBlocks.POTTED_CATMINT.get()));

        // tree
        dropSelf(ModBlocks.BLOODWOOD_LOG.get());
        dropSelf(ModBlocks.BLOODWOOD_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_BLOODWOOD_LOG.get());
        dropSelf(ModBlocks.STRIPPED_BLOODWOOD_WOOD.get());
        dropSelf(ModBlocks.BLOODWOOD_PLANKS.get());
        dropSelf(ModBlocks.BLOODWOOD_SAPLING.get());
        add(ModBlocks.BLOODWOOD_LEAVES.get(), block ->
                createLeavesDrops(block, ModBlocks.BLOODWOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));


        add(ModBlocks.SAPPHIRE_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.SAPPHIRE_SLAB.get()));
        add(ModBlocks.SAPPHIRE_DOOR.get(),
                block -> createDoorTable(ModBlocks.SAPPHIRE_DOOR.get()));


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

        //crops:strawberry
        LootItemCondition.Builder lootitemcondition$builder = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.STRAWBERRY_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StrawberryCropBlock.AGE, 5));

        this.add(ModBlocks.STRAWBERRY_CROP.get(), createCropDrops(ModBlocks.STRAWBERRY_CROP.get(), ModItems.STRAWBERRY.get(),
                ModItems.STRAWBERRY_SEEDS.get(), lootitemcondition$builder));
        //crops:corn
        LootItemCondition.Builder lootitemcondition$builder2 = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.CORN_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CornCropBlock.AGE, 7))
                .or(LootItemBlockStatePropertyCondition
                        .hasBlockStateProperties(ModBlocks.CORN_CROP.get())
                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CornCropBlock.AGE, 8)));
        this.add(ModBlocks.CORN_CROP.get(), createCropDrops(ModBlocks.CORN_CROP.get(), ModItems.CORN.get(),
                ModItems.CORN_SEEDS.get(), lootitemcondition$builder2));
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
