package io.github.icyyoung.tutorialmod.item;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.entity.ModEntities;
import io.github.icyyoung.tutorialmod.item.custom.ChiselItem;
import io.github.icyyoung.tutorialmod.item.custom.FuelItem;
import io.github.icyyoung.tutorialmod.item.custom.MetalDetectorItem;
import io.github.icyyoung.tutorialmod.item.custom.ModArmorItem;
import io.github.icyyoung.tutorialmod.sound.ModSounds;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2024/8/15
 */

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TutorialMod.MOD_ID);
    //simple items
    public static final DeferredItem<Item> BISMUTH = ITEMS.register("bismuth",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_BISMUTH = ITEMS.register("raw_bismuth",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SAPPHIRE = ITEMS.register("sapphire",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_SAPPHIRE = ITEMS.register("raw_sapphire",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STRAWBERRY = ITEMS.register("strawberry",
            () -> new Item(new Item.Properties().food(ModFoods.STRAWBERRY)));
    public static final DeferredItem<Item> SAPPHIRE_STAFF = ITEMS.register("sapphire_staff",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CORN = ITEMS.register("corn",
            () -> new Item(new Item.Properties()));
    //simple tools
    public static final DeferredItem<Item> SAPPHIRE_SWORD = ITEMS.register("sapphire_sword",
                () -> new SwordItem(ModToolTiers.SAPPHIRE,
                        new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.SAPPHIRE,3,-2.4f))));
    public static final DeferredItem<Item> SAPPHIRE_PICKAXE = ITEMS.register("sapphire_pickaxe",
                () -> new PickaxeItem(ModToolTiers.SAPPHIRE,
                        new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.SAPPHIRE,1,-2.8f))));
    public static final DeferredItem<Item> SAPPHIRE_AXE = ITEMS.register("sapphire_axe",
                () -> new AxeItem(ModToolTiers.SAPPHIRE,
                        new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.SAPPHIRE,6,-3.2f))));
    public static final DeferredItem<Item> SAPPHIRE_SHOVEL = ITEMS.register("sapphire_shovel",
                () -> new ShovelItem(ModToolTiers.SAPPHIRE,
                        new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.SAPPHIRE,1.5f,-3.0f))));
    public static final DeferredItem<Item> SAPPHIRE_HOE = ITEMS.register("sapphire_hoe",
                () -> new HoeItem(ModToolTiers.SAPPHIRE,
                    new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.SAPPHIRE,0,-3.0f))));
    //simple armor
    public static final DeferredItem<Item> SAPPHIRE_HELMET = ITEMS.register("sapphire_helmet",
                () -> new ModArmorItem(ModArmorMaterials.SAPPHIRE, ArmorItem.Type.HELMET,
                        new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(16))));
    public static final DeferredItem<Item> SAPPHIRE_CHESTPLATE = ITEMS.register("sapphire_chestplate",
            () -> new ModArmorItem(ModArmorMaterials.SAPPHIRE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(16))));
    public static final DeferredItem<Item> SAPPHIRE_LEGGINGS = ITEMS.register("sapphire_leggings",
            () -> new ModArmorItem(ModArmorMaterials.SAPPHIRE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(16))));
    public static final DeferredItem<Item> SAPPHIRE_BOOTS = ITEMS.register("sapphire_boots",
            () -> new ModArmorItem(ModArmorMaterials.SAPPHIRE, ArmorItem.Type.BOOTS,
                new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(16))));





    //custom items
    public static final DeferredItem<Item> METAL_DETECTOR = ITEMS.register("metal_detector",
            () -> new MetalDetectorItem(new Item.Properties().durability(100)));
    public static final DeferredItem<Item> CHISEL = ITEMS.register("chisel",
            () -> new ChiselItem(new Item.Properties().durability(32)));
    public static final DeferredItem<Item> PINE_CONE = ITEMS.register("pine_cone",
            () -> new FuelItem(new Item.Properties(),400));
    //seeds
    public static final DeferredItem<Item> STRAWBERRY_SEEDS = ITEMS.register("strawberry_seeds",
            () -> new ItemNameBlockItem(ModBlocks.STRAWBERRY_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> CORN_SEEDS = ITEMS.register("corn_seeds",
            () -> new ItemNameBlockItem(ModBlocks.CORN_CROP.get(), new Item.Properties()));

    //sound disks
    public static final DeferredItem<Item> BAR_BRAWL_MUSIC_DISC = ITEMS.register("bar_brawl_music_disc",
            () -> new Item(new Item.Properties().jukeboxPlayable(ModSounds.BAR_BRAWL_KEY).stacksTo(1)));
    // spawn eggs
    public static final DeferredItem<Item> GECKO_SPAWN_EGG = ITEMS.register("gecko_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GECKO, 0x31afaf, 0xffac00,
                    new Item.Properties()));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
