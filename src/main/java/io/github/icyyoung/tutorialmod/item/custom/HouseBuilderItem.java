package io.github.icyyoung.tutorialmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

public class HouseBuilderItem extends Item {
    private static final int HOUSE_WIDTH = 7;
    private static final int HOUSE_DEPTH = 5;
    private static final int HOUSE_HEIGHT = 5;
    private static final String TAG_HOUSE_TYPE = "house_type";
    private static final String HOUSE_TYPE_NORMAL = "normal";
    private static final String HOUSE_TYPE_VILLAGER = "villager";
        private static final ResourceLocation VILLAGER_HOUSE_TEMPLATE =
            ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_1");

    public HouseBuilderItem(Properties properties) {
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

        if (player.isCrouching()) {
            String switchedType = toggleHouseType(stack);
            player.sendSystemMessage(Component.translatable("message.tutorialmod.house_builder.mode_switched",
                    Component.translatable(getModeTranslationKey(switchedType))));
            return InteractionResult.SUCCESS;
        }

        String houseType = getHouseType(stack);

        BlockPos origin = context.getClickedPos().above();
        Direction front = player.getDirection().getOpposite();

        if (HOUSE_TYPE_VILLAGER.equals(houseType)) {
            if (!placeVillagerHouseFromTemplate(serverLevel, origin, front)) {
                player.sendSystemMessage(Component.translatable("message.tutorialmod.house_builder.blocked"));
                return InteractionResult.FAIL;
            }
        } else {
            if (!isAreaClear(serverLevel, origin, front)) {
                player.sendSystemMessage(Component.translatable("message.tutorialmod.house_builder.blocked"));
                return InteractionResult.FAIL;
            }

            placeNormalHouse(serverLevel, origin, front);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.sendSystemMessage(Component.translatable("message.tutorialmod.house_builder.success",
                Component.translatable(getModeTranslationKey(houseType))));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.house_builder.tooltip"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.house_builder.mode",
                Component.translatable(getModeTranslationKey(getHouseType(stack)))));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.house_builder.switch"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.house_builder.tooltip_blocked"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private boolean isAreaClear(ServerLevel level, BlockPos origin, Direction front) {
        for (int y = 0; y < HOUSE_HEIGHT; y++) {
            for (int x = 0; x < HOUSE_WIDTH; x++) {
                for (int z = 0; z < HOUSE_DEPTH; z++) {
                    BlockPos target = toWorld(origin, front, x, y, z);
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placeNormalHouse(ServerLevel level, BlockPos origin, Direction front) {
        buildFloor(level, origin, front);
        buildWalls(level, origin, front);
        buildRoof(level, origin, front);
        placeDoor(level, origin, front);
        placeWindows(level, origin, front);
        clearInterior(level, origin, front);
    }

    private boolean placeVillagerHouseFromTemplate(ServerLevel level, BlockPos origin, Direction front) {
        Optional<StructureTemplate> templateOptional = level.getStructureManager().get(VILLAGER_HOUSE_TEMPLATE);
        if (templateOptional.isEmpty()) {
            return false;
        }

        StructureTemplate template = templateOptional.get();
        StructurePlaceSettings placeSettings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotationFromFront(front));

        return template.placeInWorld(level, origin, origin, placeSettings, level.getRandom(), Block.UPDATE_ALL);
    }

    private void buildFloor(ServerLevel level, BlockPos origin, Direction front) {
        for (int x = 0; x < HOUSE_WIDTH; x++) {
            for (int z = 0; z < HOUSE_DEPTH; z++) {
                set(level, toWorld(origin, front, x, 0, z), Blocks.OAK_PLANKS);
            }
        }
    }

    private void buildWalls(ServerLevel level, BlockPos origin, Direction front) {
        for (int y = 1; y <= 3; y++) {
            for (int x = 0; x < HOUSE_WIDTH; x++) {
                for (int z = 0; z < HOUSE_DEPTH; z++) {
                    if (!isPerimeter(x, z)) {
                        continue;
                    }

                    if (isDoorOpening(x, y, z)) {
                        continue;
                    }

                    if (isCorner(x, z)) {
                        set(level, toWorld(origin, front, x, y, z), Blocks.OAK_LOG);
                    } else {
                        set(level, toWorld(origin, front, x, y, z), Blocks.OAK_PLANKS);
                    }
                }
            }
        }
    }

    private void buildRoof(ServerLevel level, BlockPos origin, Direction front) {
        for (int x = 0; x < HOUSE_WIDTH; x++) {
            for (int z = 0; z < HOUSE_DEPTH; z++) {
                set(level, toWorld(origin, front, x, 4, z), Blocks.OAK_PLANKS);
            }
        }
    }

    private void placeDoor(ServerLevel level, BlockPos origin, Direction front) {
        BlockPos doorBottom = toWorld(origin, front, 3, 1, 0);
        BlockPos doorTop = doorBottom.above();

        BlockState lowerDoor = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, front)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT);
        BlockState upperDoor = lowerDoor.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

        level.setBlock(doorBottom, lowerDoor, Block.UPDATE_ALL);
        level.setBlock(doorTop, upperDoor, Block.UPDATE_ALL);
    }

    private void placeWindows(ServerLevel level, BlockPos origin, Direction front) {
        set(level, toWorld(origin, front, 0, 2, 2), Blocks.GLASS_PANE);
        set(level, toWorld(origin, front, 6, 2, 2), Blocks.GLASS_PANE);
        set(level, toWorld(origin, front, 3, 2, 4), Blocks.GLASS_PANE);
    }

    private void clearInterior(ServerLevel level, BlockPos origin, Direction front) {
        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= HOUSE_WIDTH - 2; x++) {
                for (int z = 1; z <= HOUSE_DEPTH - 2; z++) {
                    BlockPos interiorPos = toWorld(origin, front, x, y, z);
                    level.setBlock(interiorPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
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

    private static String getHouseType(ItemStack stack) {
        String storedType = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
                .copyTag().getString(TAG_HOUSE_TYPE);
        if (HOUSE_TYPE_VILLAGER.equals(storedType)) {
            return HOUSE_TYPE_VILLAGER;
        }
        return HOUSE_TYPE_NORMAL;
    }

    private static String toggleHouseType(ItemStack stack) {
        String current = getHouseType(stack);
        String next = HOUSE_TYPE_NORMAL.equals(current) ? HOUSE_TYPE_VILLAGER : HOUSE_TYPE_NORMAL;

        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        var tag = customData.copyTag();
        tag.putString(TAG_HOUSE_TYPE, next);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));

        return next;
    }

    private static String getModeTranslationKey(String houseType) {
        return HOUSE_TYPE_VILLAGER.equals(houseType)
                ? "tooltip.tutorialmod.house_builder.mode.villager"
                : "tooltip.tutorialmod.house_builder.mode.normal";
    }

    private static boolean isPerimeter(int x, int z) {
        return x == 0 || x == HOUSE_WIDTH - 1 || z == 0 || z == HOUSE_DEPTH - 1;
    }

    private static boolean isCorner(int x, int z) {
        return (x == 0 || x == HOUSE_WIDTH - 1) && (z == 0 || z == HOUSE_DEPTH - 1);
    }

    private static boolean isDoorOpening(int x, int y, int z) {
        return z == 0 && x == 3 && (y == 1 || y == 2);
    }

    private static BlockPos toWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        return origin
                .relative(right, localX)
                .relative(front, localZ)
                .above(localY);
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
    }
}