package io.github.icyyoung.tutorialmod.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * @Description 某一类Loot的底层实现
 * @Author icyyoung
 * @Date 2024/9/6
 */

public class AddSusSandModifier extends LootModifier {
    public static final MapCodec<AddSusSandModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(inst.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("field3").forGetter(e -> e.field3))
    ).apply(inst, AddSusSandModifier::new));
    private final Item item;
    private final Item field3;

    public AddSusSandModifier(LootItemCondition[] conditionsIn, Item item, Item field3) {
        super(conditionsIn);
        this.item = item;
        this.field3 = field3;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Add your items to generatedLoot here.
        for(LootItemCondition condition : this.conditions) {
            if(!condition.test(context)) {
                return generatedLoot;
            }
        }

        if(context.getRandom().nextFloat()<0.5){
            //50%的概率
            generatedLoot.clear();
            generatedLoot.add(new ItemStack(item, 1));
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
