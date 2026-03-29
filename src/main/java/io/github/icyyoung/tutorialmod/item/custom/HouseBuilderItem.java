package io.github.icyyoung.tutorialmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
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
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class HouseBuilderItem extends Item {
    private static final int HOUSE_WIDTH = 27;
    private static final int HOUSE_DEPTH = 21;
    private static final int HOUSE_HEIGHT = 13;
    private static final int FIRST_FLOOR_Y = 1;
    private static final int FIRST_WALL_MIN_Y = 2;
    private static final int FIRST_WALL_MAX_Y = 5;
    private static final int SECOND_FLOOR_Y = 6;
    private static final int SECOND_WALL_MIN_Y = 7;
    private static final int SECOND_WALL_MAX_Y = 9;
    private static final int ATTIC_FLOOR_Y = 10;
    private static final int ROOF_BASE_Y = 11;
    private static final int ROOF_PEAK_Y = 12;
    private static final int FRONT_YARD_MIN_Z = -4;
    private static final int FRONT_YARD_MAX_Z = HOUSE_DEPTH + 2;
    private static final int FRONT_YARD_MIN_X = -2;
    private static final int FRONT_YARD_MAX_X = HOUSE_WIDTH + 1;
    private static final int DOOR_LEFT_X = HOUSE_WIDTH / 2 - 1;
    private static final int DOOR_RIGHT_X = HOUSE_WIDTH / 2;
    private static final String TAG_HOUSE_TYPE = "house_type";
    private static final String HOUSE_TYPE_NORMAL = "normal";
    private static final String HOUSE_TYPE_VILLAGER = "villager";
    private static final String HOUSE_TYPE_FOX = "fox";
    private static final String FOX_HOUSE_SCHEMATIC_PATH = "/data/tutorialmod/schematics/free_fox_house.schem";
    private static final ResourceLocation VILLAGER_HOUSE_TEMPLATE =
            ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_1");
    private static LoadedSchematic cachedFoxSchematic;

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
        } else if (HOUSE_TYPE_FOX.equals(houseType)) {
            if (!placeFoxHouseFromSchematic(serverLevel, origin, front)) {
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
            for (int x = FRONT_YARD_MIN_X; x <= FRONT_YARD_MAX_X; x++) {
                for (int z = FRONT_YARD_MIN_Z; z <= FRONT_YARD_MAX_Z; z++) {
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
        buildFoundationAndTerrace(level, origin, front);
        buildOuterShell(level, origin, front);
        buildFloorSlabs(level, origin, front);
        buildColonnadeAndArches(level, origin, front);
        buildGrandRoof(level, origin, front);
        placeGrandEntrance(level, origin, front);
        placeWindows(level, origin, front);
        buildInteriorPartitions(level, origin, front);
        buildMainStairAndUpperHall(level, origin, front);
        furnishRooms(level, origin, front);
        buildFireplaceAndChimney(level, origin, front);
        buildGardenFenceAndLighting(level, origin, front);
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

    private void buildFoundationAndTerrace(ServerLevel level, BlockPos origin, Direction front) {
        fill(level, origin, front, 0, HOUSE_WIDTH - 1, 0, 0, HOUSE_DEPTH - 1, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        fill(level, origin, front, 0, HOUSE_WIDTH - 1, FIRST_FLOOR_Y, 0, HOUSE_DEPTH - 1, Blocks.STONE_BRICKS.defaultBlockState());
        fill(level, origin, front, 1, HOUSE_WIDTH - 2, FIRST_FLOOR_Y, 1, HOUSE_DEPTH - 2, Blocks.DARK_OAK_PLANKS.defaultBlockState());

        fill(level, origin, front, DOOR_LEFT_X - 3, DOOR_RIGHT_X + 3, FIRST_FLOOR_Y, -3, -1, Blocks.POLISHED_ANDESITE.defaultBlockState());
        fill(level, origin, front, DOOR_LEFT_X - 2, DOOR_RIGHT_X + 2, 0, -4, -4, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        fill(level, origin, front, DOOR_LEFT_X - 3, DOOR_RIGHT_X + 3, 0, -3, -1,
                Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
    }

    private void buildOuterShell(ServerLevel level, BlockPos origin, Direction front) {
        for (int y = FIRST_WALL_MIN_Y; y <= ATTIC_FLOOR_Y; y++) {
            for (int x = 0; x < HOUSE_WIDTH; x++) {
                for (int z = 0; z < HOUSE_DEPTH; z++) {
                    if (!isPerimeter(x, z)) {
                        continue;
                    }
                    if (isDoorOpening(x, y, z)) {
                        set(level, toWorld(origin, front, x, y, z), Blocks.AIR);
                        continue;
                    }

                    if (isCorner(x, z) || x % 6 == 0 || z % 5 == 0) {
                        set(level, toWorld(origin, front, x, y, z), Blocks.POLISHED_DEEPSLATE);
                    } else {
                        set(level, toWorld(origin, front, x, y, z), Blocks.DEEPSLATE_BRICKS);
                    }
                }
            }
        }

        for (int y = FIRST_WALL_MIN_Y; y <= SECOND_WALL_MAX_Y; y++) {
            for (int x = 3; x <= HOUSE_WIDTH - 4; x += 4) {
                set(level, toWorld(origin, front, x, y, 1), Blocks.STONE_BRICK_WALL);
                set(level, toWorld(origin, front, x, y, HOUSE_DEPTH - 2), Blocks.STONE_BRICK_WALL);
            }
        }
    }

    private void buildFloorSlabs(ServerLevel level, BlockPos origin, Direction front) {
        fill(level, origin, front, 1, HOUSE_WIDTH - 2, SECOND_FLOOR_Y, 1, HOUSE_DEPTH - 2, Blocks.SPRUCE_PLANKS.defaultBlockState());
        fill(level, origin, front, 1, HOUSE_WIDTH - 2, ATTIC_FLOOR_Y, 1, HOUSE_DEPTH - 2, Blocks.DARK_OAK_PLANKS.defaultBlockState());

        fill(level, origin, front, 2, 5, SECOND_FLOOR_Y, 3, 7, Blocks.AIR.defaultBlockState());
        fill(level, origin, front, 6, 9, ATTIC_FLOOR_Y, 8, 12, Blocks.AIR.defaultBlockState());

        fill(level, origin, front, 1, HOUSE_WIDTH - 2, ROOF_BASE_Y, 1, HOUSE_DEPTH - 2,
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
    }

    private void buildColonnadeAndArches(ServerLevel level, BlockPos origin, Direction front) {
        for (int x = 6; x <= HOUSE_WIDTH - 7; x += 4) {
            fill(level, origin, front, x, x, FIRST_WALL_MIN_Y, -1, -1, Blocks.STONE_BRICKS.defaultBlockState());
            fill(level, origin, front, x, x, FIRST_WALL_MIN_Y, -2, -2, Blocks.STONE_BRICKS.defaultBlockState());
            fill(level, origin, front, x, x, FIRST_WALL_MIN_Y, -2, FIRST_WALL_MAX_Y, -2, Blocks.STONE_BRICK_WALL.defaultBlockState());
        }

        for (int x = 6; x <= HOUSE_WIDTH - 7; x++) {
            set(level, toWorld(origin, front, x, SECOND_FLOOR_Y, -2), Blocks.DEEPSLATE_BRICK_SLAB);
            setState(level, toWorld(origin, front, x, SECOND_FLOOR_Y - 1, -1),
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, front.getOpposite()));
        }
    }

    private void buildGrandRoof(ServerLevel level, BlockPos origin, Direction front) {
        for (int x = 0; x < HOUSE_WIDTH; x++) {
            for (int z = 0; z < HOUSE_DEPTH; z++) {
                if (isPerimeter(x, z)) {
                    set(level, toWorld(origin, front, x, ROOF_BASE_Y, z), Blocks.DEEPSLATE_TILES);
                }
            }
        }

        for (int x = 1; x < HOUSE_WIDTH - 1; x++) {
            for (int z = 1; z < HOUSE_DEPTH - 1; z++) {
                if (x == HOUSE_WIDTH / 2 || z == HOUSE_DEPTH / 2 || (x % 5 == 0 && z % 4 == 0)) {
                    set(level, toWorld(origin, front, x, ROOF_PEAK_Y, z), Blocks.DEEPSLATE_TILES);
                } else {
                    set(level, toWorld(origin, front, x, ROOF_PEAK_Y, z), Blocks.DARK_OAK_SLAB);
                }
            }
        }
    }

    private void placeGrandEntrance(ServerLevel level, BlockPos origin, Direction front) {
        BlockPos leftBottom = toWorld(origin, front, DOOR_LEFT_X, FIRST_WALL_MIN_Y, 0);
        BlockPos rightBottom = toWorld(origin, front, DOOR_RIGHT_X, FIRST_WALL_MIN_Y, 0);

        BlockState leftLowerDoor = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, front)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT);
        BlockState leftUpperDoor = leftLowerDoor.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

        BlockState rightLowerDoor = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, front)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.RIGHT);
        BlockState rightUpperDoor = rightLowerDoor.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

        setState(level, leftBottom, leftLowerDoor);
        setState(level, leftBottom.above(), leftUpperDoor);
        setState(level, rightBottom, rightLowerDoor);
        setState(level, rightBottom.above(), rightUpperDoor);

        for (int y = FIRST_WALL_MIN_Y; y <= FIRST_WALL_MAX_Y; y++) {
            set(level, toWorld(origin, front, DOOR_LEFT_X - 1, y, 0), Blocks.POLISHED_DEEPSLATE);
            set(level, toWorld(origin, front, DOOR_RIGHT_X + 1, y, 0), Blocks.POLISHED_DEEPSLATE);
        }
        for (int x = DOOR_LEFT_X - 1; x <= DOOR_RIGHT_X + 1; x++) {
            set(level, toWorld(origin, front, x, FIRST_WALL_MAX_Y, 0), Blocks.POLISHED_DEEPSLATE);
        }
    }

    private void placeWindows(ServerLevel level, BlockPos origin, Direction front) {
        for (int z = 4; z <= HOUSE_DEPTH - 5; z += 4) {
            placeTallWindow(level, origin, front, 0, z, FIRST_WALL_MIN_Y + 1, 2);
            placeTallWindow(level, origin, front, HOUSE_WIDTH - 1, z, FIRST_WALL_MIN_Y + 1, 2);
            placeTallWindow(level, origin, front, 0, z, SECOND_WALL_MIN_Y, 2);
            placeTallWindow(level, origin, front, HOUSE_WIDTH - 1, z, SECOND_WALL_MIN_Y, 2);
        }

        int[] backWindowXs = {4, 8, 18, 22};
        for (int x : backWindowXs) {
            placeTallWindow(level, origin, front, x, HOUSE_DEPTH - 1, FIRST_WALL_MIN_Y + 1, 2);
            placeTallWindow(level, origin, front, x, HOUSE_DEPTH - 1, SECOND_WALL_MIN_Y, 2);
            placeTallWindow(level, origin, front, x, 0, SECOND_WALL_MIN_Y, 2);
        }

        placeTallWindow(level, origin, front, HOUSE_WIDTH / 2, 0, FIRST_WALL_MAX_Y, 2);
    }

    private void buildInteriorPartitions(ServerLevel level, BlockPos origin, Direction front) {
        fill(level, origin, front, 8, 8, FIRST_WALL_MIN_Y, 2, FIRST_WALL_MAX_Y, HOUSE_DEPTH - 2, Blocks.STONE_BRICKS.defaultBlockState());
        fill(level, origin, front, 18, 18, FIRST_WALL_MIN_Y, 2, FIRST_WALL_MAX_Y, HOUSE_DEPTH - 2, Blocks.STONE_BRICKS.defaultBlockState());
        fill(level, origin, front, 8, 18, FIRST_WALL_MIN_Y, 10, FIRST_WALL_MAX_Y, 10, Blocks.STONE_BRICKS.defaultBlockState());

        fill(level, origin, front, 13, 13, SECOND_WALL_MIN_Y, 2, SECOND_WALL_MAX_Y, HOUSE_DEPTH - 2, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        fill(level, origin, front, 2, HOUSE_WIDTH - 3, SECOND_WALL_MIN_Y, 10, SECOND_WALL_MAX_Y, 10, Blocks.DARK_OAK_PLANKS.defaultBlockState());

        carveDoorway(level, origin, front, 8, 4, FIRST_WALL_MIN_Y, FIRST_WALL_MIN_Y + 1);
        carveDoorway(level, origin, front, 8, 14, FIRST_WALL_MIN_Y, FIRST_WALL_MIN_Y + 1);
        carveDoorway(level, origin, front, 18, 6, FIRST_WALL_MIN_Y, FIRST_WALL_MIN_Y + 1);
        carveDoorway(level, origin, front, 18, 16, FIRST_WALL_MIN_Y, FIRST_WALL_MIN_Y + 1);
        carveDoorway(level, origin, front, 13, 6, SECOND_WALL_MIN_Y, SECOND_WALL_MIN_Y + 1);
        carveDoorway(level, origin, front, 13, 14, SECOND_WALL_MIN_Y, SECOND_WALL_MIN_Y + 1);
        carveDoorway(level, origin, front, 6, 10, SECOND_WALL_MIN_Y, SECOND_WALL_MIN_Y + 1);
        carveDoorway(level, origin, front, 20, 10, SECOND_WALL_MIN_Y, SECOND_WALL_MIN_Y + 1);
    }

    private void buildMainStairAndUpperHall(ServerLevel level, BlockPos origin, Direction front) {
        for (int i = 0; i < 4; i++) {
            int y = FIRST_WALL_MIN_Y + i;
            int z = 3 + i;
            set(level, toWorld(origin, front, 3, y, z), Blocks.POLISHED_ANDESITE);
            set(level, toWorld(origin, front, 4, y, z), Blocks.POLISHED_ANDESITE);
            set(level, toWorld(origin, front, 2, y + 1, z), Blocks.DARK_OAK_FENCE);
            set(level, toWorld(origin, front, 5, y + 1, z), Blocks.DARK_OAK_FENCE);
        }

        for (int i = 0; i < 4; i++) {
            int y = SECOND_WALL_MIN_Y + i;
            int z = 8 + i;
            set(level, toWorld(origin, front, 7, y, z), Blocks.POLISHED_ANDESITE);
            set(level, toWorld(origin, front, 8, y, z), Blocks.POLISHED_ANDESITE);
            set(level, toWorld(origin, front, 6, y + 1, z), Blocks.DARK_OAK_FENCE);
            set(level, toWorld(origin, front, 9, y + 1, z), Blocks.DARK_OAK_FENCE);
        }

        fill(level, origin, front, 2, 10, SECOND_FLOOR_Y, 1, 2, Blocks.DARK_OAK_SLAB.defaultBlockState());
        fill(level, origin, front, 6, 12, ATTIC_FLOOR_Y, 12, 14, Blocks.DARK_OAK_SLAB.defaultBlockState());
    }

    private void furnishRooms(ServerLevel level, BlockPos origin, Direction front) {
        for (int x = 2; x <= 6; x++) {
            set(level, toWorld(origin, front, x, FIRST_WALL_MIN_Y, 13), Blocks.RED_CARPET);
            set(level, toWorld(origin, front, x, FIRST_WALL_MIN_Y, 14), Blocks.WHITE_CARPET);
        }
        set(level, toWorld(origin, front, 4, FIRST_WALL_MIN_Y, 16), Blocks.BOOKSHELF);
        set(level, toWorld(origin, front, 5, FIRST_WALL_MIN_Y, 16), Blocks.BOOKSHELF);
        set(level, toWorld(origin, front, 6, FIRST_WALL_MIN_Y, 16), Blocks.BOOKSHELF);

        for (int z = 12; z <= 16; z++) {
            set(level, toWorld(origin, front, 12, FIRST_WALL_MIN_Y, z), Blocks.DARK_OAK_FENCE);
            set(level, toWorld(origin, front, 14, FIRST_WALL_MIN_Y, z), Blocks.DARK_OAK_FENCE);
        }
        fill(level, origin, front, 12, 14, FIRST_WALL_MIN_Y + 1, 12, 16, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());

        set(level, toWorld(origin, front, 21, FIRST_WALL_MIN_Y, 4), Blocks.BLAST_FURNACE);
        set(level, toWorld(origin, front, 22, FIRST_WALL_MIN_Y, 4), Blocks.SMOKER);
        set(level, toWorld(origin, front, 23, FIRST_WALL_MIN_Y, 4), Blocks.CRAFTING_TABLE);
        set(level, toWorld(origin, front, 21, FIRST_WALL_MIN_Y, 6), Blocks.BARREL);
        set(level, toWorld(origin, front, 22, FIRST_WALL_MIN_Y, 6), Blocks.BARREL);
        set(level, toWorld(origin, front, 23, FIRST_WALL_MIN_Y, 6), Blocks.CHEST);

        fill(level, origin, front, 3, 7, SECOND_WALL_MIN_Y, 12, 16, Blocks.BLUE_CARPET.defaultBlockState());
        set(level, toWorld(origin, front, 4, SECOND_WALL_MIN_Y, 17), Blocks.CHEST);
        set(level, toWorld(origin, front, 6, SECOND_WALL_MIN_Y, 17), Blocks.BOOKSHELF);

        fill(level, origin, front, 18, 22, SECOND_WALL_MIN_Y, 12, 16, Blocks.GRAY_CARPET.defaultBlockState());
        set(level, toWorld(origin, front, 20, SECOND_WALL_MIN_Y, 17), Blocks.ENDER_CHEST);
        set(level, toWorld(origin, front, 21, SECOND_WALL_MIN_Y, 17), Blocks.BOOKSHELF);
        set(level, toWorld(origin, front, 22, SECOND_WALL_MIN_Y, 17), Blocks.BOOKSHELF);
    }

    private void buildFireplaceAndChimney(ServerLevel level, BlockPos origin, Direction front) {
        fill(level, origin, front, 2, 6, FIRST_WALL_MIN_Y, 18, FIRST_WALL_MIN_Y + 1, 19, Blocks.BRICKS.defaultBlockState());
        set(level, toWorld(origin, front, 4, FIRST_WALL_MIN_Y, 18), Blocks.CAMPFIRE);

        for (int y = FIRST_WALL_MIN_Y + 1; y <= ROOF_PEAK_Y; y++) {
            set(level, toWorld(origin, front, 4, y, 19), Blocks.BRICKS);
            set(level, toWorld(origin, front, 5, y, 19), Blocks.BRICKS);
            if (y >= SECOND_WALL_MIN_Y) {
                set(level, toWorld(origin, front, 4, y, 20), Blocks.BRICK_WALL);
                set(level, toWorld(origin, front, 5, y, 20), Blocks.BRICK_WALL);
            }
        }
    }

    private void buildGardenFenceAndLighting(ServerLevel level, BlockPos origin, Direction front) {
        for (int x = FRONT_YARD_MIN_X; x <= FRONT_YARD_MAX_X; x++) {
            if (x < DOOR_LEFT_X - 1 || x > DOOR_RIGHT_X + 1) {
                set(level, toWorld(origin, front, x, FIRST_FLOOR_Y, FRONT_YARD_MIN_Z), Blocks.DARK_OAK_FENCE);
            }
            set(level, toWorld(origin, front, x, FIRST_FLOOR_Y, FRONT_YARD_MAX_Z), Blocks.DARK_OAK_FENCE);
        }
        for (int z = FRONT_YARD_MIN_Z; z <= FRONT_YARD_MAX_Z; z++) {
            set(level, toWorld(origin, front, FRONT_YARD_MIN_X, FIRST_FLOOR_Y, z), Blocks.DARK_OAK_FENCE);
            set(level, toWorld(origin, front, FRONT_YARD_MAX_X, FIRST_FLOOR_Y, z), Blocks.DARK_OAK_FENCE);
        }

        fill(level, origin, front, DOOR_LEFT_X, DOOR_RIGHT_X, FIRST_FLOOR_Y, FRONT_YARD_MIN_Z + 1, -1, Blocks.STONE_BRICKS.defaultBlockState());

        placeLampPost(level, origin, front, FRONT_YARD_MIN_X + 2, FRONT_YARD_MIN_Z + 2);
        placeLampPost(level, origin, front, FRONT_YARD_MAX_X - 2, FRONT_YARD_MIN_Z + 2);
        placeLampPost(level, origin, front, FRONT_YARD_MIN_X + 2, FRONT_YARD_MAX_Z - 2);
        placeLampPost(level, origin, front, FRONT_YARD_MAX_X - 2, FRONT_YARD_MAX_Z - 2);

        for (int x = 5; x <= HOUSE_WIDTH - 6; x += 5) {
            set(level, toWorld(origin, front, x, FIRST_WALL_MAX_Y, 0), Blocks.LANTERN);
            set(level, toWorld(origin, front, x, FIRST_WALL_MAX_Y, HOUSE_DEPTH - 1), Blocks.LANTERN);
        }
    }

    private void placeTallWindow(ServerLevel level, BlockPos origin, Direction front, int x, int z, int baseY, int height) {
        for (int y = baseY; y < baseY + height; y++) {
            set(level, toWorld(origin, front, x, y, z), Blocks.BLACK_STAINED_GLASS_PANE);
        }
    }

    private void carveDoorway(ServerLevel level, BlockPos origin, Direction front, int x, int z, int minY, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            set(level, toWorld(origin, front, x, y, z), Blocks.AIR);
        }
    }

    private void placeLampPost(ServerLevel level, BlockPos origin, Direction front, int x, int z) {
        set(level, toWorld(origin, front, x, FIRST_FLOOR_Y, z), Blocks.POLISHED_DEEPSLATE);
        set(level, toWorld(origin, front, x, FIRST_FLOOR_Y + 1, z), Blocks.DARK_OAK_FENCE);
        set(level, toWorld(origin, front, x, FIRST_FLOOR_Y + 2, z), Blocks.DARK_OAK_FENCE);
        set(level, toWorld(origin, front, x, FIRST_FLOOR_Y + 3, z), Blocks.LANTERN);
    }

    private void fill(ServerLevel level, BlockPos origin, Direction front,
                      int minX, int maxX, int minY, int minZ, int maxY, int maxZ,
                      BlockState state) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setState(level, toWorld(origin, front, x, y, z), state);
                }
            }
        }
    }

    private void fill(ServerLevel level, BlockPos origin, Direction front,
                      int minX, int maxX, int y, int minZ, int maxZ,
                      BlockState state) {
        fill(level, origin, front, minX, maxX, y, minZ, y, maxZ, state);
    }

    private boolean placeFoxHouseFromSchematic(ServerLevel level, BlockPos origin, Direction front) {
        LoadedSchematic schematic = getFoxSchematic();
        if (schematic == null || !isSchematicAreaClear(level, origin, front, schematic)) {
            return false;
        }

        Rotation rotation = rotationFromFront(front);
        int width = schematic.width();
        int length = schematic.length();
        int height = schematic.height();

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    BlockState state = schematic.blockAt(x, y, z);
                    if (state.isAir()) {
                        continue;
                    }

                    BlockState rotatedState = state.rotate(rotation);
                    BlockPos worldPos = toWorld(origin, front, x, y, z);
                    setState(level, worldPos, rotatedState);
                }
            }
        }
        return true;
    }

    private boolean isSchematicAreaClear(ServerLevel level, BlockPos origin, Direction front, LoadedSchematic schematic) {
        int width = schematic.width();
        int length = schematic.length();
        int height = schematic.height();

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    BlockState schematicState = schematic.blockAt(x, y, z);
                    if (schematicState.isAir()) {
                        continue;
                    }

                    BlockPos target = toWorld(origin, front, x, y, z);
                    BlockState current = level.getBlockState(target);
                    if (!current.isAir() && !current.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static LoadedSchematic getFoxSchematic() {
        if (cachedFoxSchematic != null) {
            return cachedFoxSchematic;
        }

        try (InputStream inputStream = HouseBuilderItem.class.getResourceAsStream(FOX_HOUSE_SCHEMATIC_PATH)) {
            if (inputStream == null) {
                return null;
            }

            CompoundTag root = NbtIo.readCompressed(inputStream, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            short width = root.getShort("Width");
            short height = root.getShort("Height");
            short length = root.getShort("Length");

            if (width <= 0 || height <= 0 || length <= 0) {
                return null;
            }

            CompoundTag paletteTag = root.getCompound("Palette");
            if (paletteTag.isEmpty()) {
                return null;
            }

            int totalBlocks = width * height * length;
            int[] paletteIndices = decodeVarIntArray(root.getByteArray("BlockData"), totalBlocks);

            Map<Integer, BlockState> palette = new HashMap<>();
            for (String key : paletteTag.getAllKeys()) {
                int index = paletteTag.getInt(key);
                palette.put(index, parseBlockState(key));
            }

            BlockState[] states = new BlockState[totalBlocks];
            for (int i = 0; i < totalBlocks; i++) {
                states[i] = palette.getOrDefault(paletteIndices[i], Blocks.AIR.defaultBlockState());
            }

            cachedFoxSchematic = new LoadedSchematic(width, height, length, states);
            return cachedFoxSchematic;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static int[] decodeVarIntArray(byte[] data, int expectedSize) {
        int[] values = new int[expectedSize];
        int index = 0;
        int cursor = 0;

        while (cursor < data.length && index < expectedSize) {
            int value = 0;
            int shift = 0;
            boolean malformed = false;

            while (true) {
                if (cursor >= data.length || shift > 28) {
                    malformed = true;
                    break;
                }

                int current = data[cursor++] & 0xFF;
                value |= (current & 0x7F) << shift;

                if ((current & 0x80) == 0) {
                    break;
                }
                shift += 7;
            }

            values[index++] = malformed ? 0 : value;
        }

        while (index < expectedSize) {
            values[index++] = 0;
        }

        return values;
    }

    private static BlockState parseBlockState(String serializedState) {
        String blockId = serializedState;
        String propertySection = null;

        int propertyStart = serializedState.indexOf('[');
        if (propertyStart >= 0 && serializedState.endsWith("]")) {
            blockId = serializedState.substring(0, propertyStart);
            propertySection = serializedState.substring(propertyStart + 1, serializedState.length() - 1);
        }

        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        if (block == null || block == Blocks.AIR && !"minecraft:air".equals(blockId)) {
            return Blocks.AIR.defaultBlockState();
        }

        BlockState state = block.defaultBlockState();
        if (propertySection == null || propertySection.isEmpty()) {
            return state;
        }

        String[] properties = propertySection.split(",");
        for (String propertyPair : properties) {
            String[] kv = propertyPair.split("=", 2);
            if (kv.length != 2) {
                continue;
            }

            Property<?> property = block.getStateDefinition().getProperty(kv[0]);
            if (property == null) {
                continue;
            }

            state = trySetPropertyUnchecked(state, property, kv[1]);
        }

        return state;
    }

    @SuppressWarnings("unchecked")
    private static BlockState trySetPropertyUnchecked(BlockState state, Property<?> property, String value) {
        return trySetPropertyRaw(state, (Property) property, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState trySetPropertyRaw(BlockState state, Property property, String value) {
        Optional parsed = property.getValue(value);
        return parsed.isPresent() ? state.setValue(property, (Comparable) parsed.get()) : state;
    }

    private record LoadedSchematic(short width, short height, short length, BlockState[] blocks) {
        private BlockState blockAt(int x, int y, int z) {
            int index = x + (y * length + z) * width;
            if (index < 0 || index >= blocks.length) {
                return Blocks.AIR.defaultBlockState();
            }
            return blocks[index];
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
        if (HOUSE_TYPE_FOX.equals(storedType)) {
            return HOUSE_TYPE_FOX;
        }
        return HOUSE_TYPE_NORMAL;
    }

    private static String toggleHouseType(ItemStack stack) {
        String current = getHouseType(stack);
        String next;
        if (HOUSE_TYPE_NORMAL.equals(current)) {
            next = HOUSE_TYPE_VILLAGER;
        } else if (HOUSE_TYPE_VILLAGER.equals(current)) {
            next = HOUSE_TYPE_FOX;
        } else {
            next = HOUSE_TYPE_NORMAL;
        }

        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        var tag = customData.copyTag();
        tag.putString(TAG_HOUSE_TYPE, next);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));

        return next;
    }

    private static String getModeTranslationKey(String houseType) {
        if (HOUSE_TYPE_VILLAGER.equals(houseType)) {
            return "tooltip.tutorialmod.house_builder.mode.villager";
        }
        if (HOUSE_TYPE_FOX.equals(houseType)) {
            return "tooltip.tutorialmod.house_builder.mode.fox";
        }
        return "tooltip.tutorialmod.house_builder.mode.normal";
    }

    private static boolean isPerimeter(int x, int z) {
        return x == 0 || x == HOUSE_WIDTH - 1 || z == 0 || z == HOUSE_DEPTH - 1;
    }

    private static boolean isCorner(int x, int z) {
        return (x == 0 || x == HOUSE_WIDTH - 1) && (z == 0 || z == HOUSE_DEPTH - 1);
    }

    private static boolean isDoorOpening(int x, int y, int z) {
        return z == 0 && (x == DOOR_LEFT_X || x == DOOR_RIGHT_X) && (y == FIRST_WALL_MIN_Y || y == FIRST_WALL_MIN_Y + 1);
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

    private static void setState(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }
}