package io.github.icyyoung.tutorialmod.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChestSortHelper {
    private ChestSortHelper() {
    }

    public static void sortChest(ChestMenu chestMenu) {
        int chestSlotCount = chestMenu.getRowCount() * 9;
        List<ItemStack> sourceStacks = new ArrayList<>();
        for (int i = 0; i < chestSlotCount; i++) {
            ItemStack stack = chestMenu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                sourceStacks.add(stack.copy());
            }
        }

        List<ItemStack> mergedStacks = mergeAndSort(sourceStacks);
        for (int i = 0; i < chestSlotCount; i++) {
            Slot slot = chestMenu.getSlot(i);
            if (i < mergedStacks.size()) {
                slot.set(mergedStacks.get(i));
            } else {
                slot.set(ItemStack.EMPTY);
            }
        }

        chestMenu.slotsChanged(chestMenu.getContainer());
        chestMenu.broadcastChanges();
    }

    private static List<ItemStack> mergeAndSort(List<ItemStack> sourceStacks) {
        Map<SortKey, Integer> countByKey = new LinkedHashMap<>();
        Map<SortKey, ItemStack> prototypeByKey = new LinkedHashMap<>();

        for (ItemStack stack : sourceStacks) {
            SortKey key = SortKey.fromStack(stack);
            countByKey.merge(key, stack.getCount(), Integer::sum);
            prototypeByKey.putIfAbsent(key, stack.copyWithCount(1));
        }

        List<SortKey> sortedKeys = new ArrayList<>(countByKey.keySet());
        sortedKeys.sort(Comparator
                .comparing(SortKey::itemId)
                .thenComparing(SortKey::componentsKey));

        List<ItemStack> output = new ArrayList<>();
        for (SortKey key : sortedKeys) {
            ItemStack prototype = prototypeByKey.get(key);
            int totalCount = countByKey.get(key);
            int maxStackSize = prototype.getMaxStackSize();
            while (totalCount > 0) {
                int amount = Math.min(maxStackSize, totalCount);
                output.add(prototype.copyWithCount(amount));
                totalCount -= amount;
            }
        }

        return output;
    }

    private record SortKey(String itemId, String componentsKey) {
        private static SortKey fromStack(ItemStack stack) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return new SortKey(key.toString(), stack.getComponentsPatch().toString());
        }
    }
}
