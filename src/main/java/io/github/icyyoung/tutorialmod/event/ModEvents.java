package io.github.icyyoung.tutorialmod.event;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.item.ModItems;
import io.github.icyyoung.tutorialmod.potion.ModPotions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2024/10/30
 */

@EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {
    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if(event.getType() == VillagerProfession.FARMER) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            // Level 1
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 12),
                    new ItemStack(ModItems.STRAWBERRY.get(), 12),
                    10, 8, 0.02f));
            // Level 2
            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 5),
                    new ItemStack(ModItems.CORN.get(), 6),
                    5, 9, 0.035f));
            // Level 3
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemCost(Items.GOLD_INGOT, 8),
                    new ItemStack(ModItems.CORN_SEEDS.get(), 2),
                    2, 12, 0.075f));
        }
//        if(event.getType() == VillagerProfession.LIBRARIAN) {
//            Holder<Enchantment> enchantmentHolder =
//            //Registry<Enchantment> registry = level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
//            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
//            EnchantedBookItem.createForEnchantment(new EnchantmentInstance(, 1));
//            ItemStack enchantedBook = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(AllE, 2));
//            // Level 1
//            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
//                    new ItemCost(Items.EMERALD, 32),
//                    enchantedBook,
//                    2, 8, 0.02f));
//        }
    }
    @SubscribeEvent
    public static void addCustomWanderingTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();
        genericTrades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 12),
                new ItemStack(ModItems.SAPPHIRE_BOOTS.get(), 1),
                3, 2, 0.2f));
        rareTrades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 24),
                new ItemStack(ModItems.METAL_DETECTOR.get(), 1),
                2, 12, 0.15f));
    }

    @SubscribeEvent
    public static void onBrewingRecipeRegister(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        builder.addMix(Potions.AWKWARD, Items.SLIME_BALL, ModPotions.SLIMEY_POTION);
    }
}