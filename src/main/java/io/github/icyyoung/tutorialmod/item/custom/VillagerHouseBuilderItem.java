package io.github.icyyoung.tutorialmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

public class VillagerHouseBuilderItem extends Item {
    private static final String TAG_TEMPLATE_INDEX = "villager_template_index";
    private static final List<ResourceLocation> TEMPLATE_POOL = List.of(
            ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_1"),
            ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_2"),
            ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_3"),
            ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_5"),
            ResourceLocation.withDefaultNamespace("village/plains/houses/plains_medium_house_1")
    );

    public VillagerHouseBuilderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (TEMPLATE_POOL.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.villager_builder.no_template"));
            return InteractionResult.FAIL;
        }

        if (player.isCrouching()) {
            int nextIndex = Math.floorMod(getTemplateIndex(stack) + 1, TEMPLATE_POOL.size());
            setTemplateIndex(stack, nextIndex);
            player.sendSystemMessage(Component.translatable("message.tutorialmod.villager_builder.mode_switched",
                    Component.literal(TEMPLATE_POOL.get(nextIndex).toString())));
            return InteractionResult.SUCCESS;
        }

        int selectedIndex = Math.floorMod(getTemplateIndex(stack), TEMPLATE_POOL.size());
        ResourceLocation templateId = TEMPLATE_POOL.get(selectedIndex);
        BlockPos origin = context.getClickedPos().above();
        Direction front = player.getDirection().getOpposite();

        if (!placeTemplate(serverLevel, origin, front, templateId)) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.villager_builder.blocked"));
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.sendSystemMessage(Component.translatable("message.tutorialmod.villager_builder.success",
                Component.literal(templateId.toString())));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.villager_builder.tooltip"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.villager_builder.switch"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.villager_builder.tooltip_blocked"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static int getTemplateIndex(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_TEMPLATE_INDEX);
    }

    private static void setTemplateIndex(ItemStack stack, int index) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        tag.putInt(TAG_TEMPLATE_INDEX, index);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static boolean placeTemplate(ServerLevel level, BlockPos origin, Direction front, ResourceLocation templateId) {
        Optional<StructureTemplate> templateOptional = level.getStructureManager().get(templateId);
        if (templateOptional.isEmpty()) {
            return false;
        }

        StructureTemplate template = templateOptional.get();
        StructurePlaceSettings placeSettings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotationFromFront(front));

        return template.placeInWorld(level, origin, origin, placeSettings, level.getRandom(), Block.UPDATE_ALL);
    }

    private static Rotation rotationFromFront(Direction front) {
        return switch (front) {
            case SOUTH -> Rotation.NONE;
            case WEST -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
