//package io.github.icyyoung.tutorialmod.loot;
//
//import com.mojang.serialization.MapCodec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.storage.loot.LootContext;
//import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
//import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
//import net.neoforged.neoforge.common.loot.LootModifier;
//import org.jetbrains.annotations.NotNull;
//
///**
// * @Description TODO
// * @Author icyyoung
// * @Date 2024/9/6
// */
//
//public class AddItemModifier extends LootModifier {
//    public static final MapCodec<AddItemModifier> CODEC = RecordCodecBuilder
//            .mapCodec(builder-> codecStart(builder).apply(builder, AddItemModifier::new));
//    private final Item item;
//
//    /**
//     * Constructs a LootModifier.
//     *
//     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
//     */
//    public AddItemModifier(LootItemCondition[] conditionsIn, Item item) {
//        super(conditionsIn);
//        this.item = item;
//    }
//
//    @Override
//    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
//        return generatedLoot;
//    }
//
//    @Override
//    public MapCodec<? extends IGlobalLootModifier> codec() {
//        return CODEC;
//    }
//}
