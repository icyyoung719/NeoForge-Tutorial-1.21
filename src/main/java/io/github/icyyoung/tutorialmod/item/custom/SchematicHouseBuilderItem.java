package io.github.icyyoung.tutorialmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SchematicHouseBuilderItem extends Item {
    private static final String TAG_SCHEMATIC_INDEX = "schematic_index";
    private static final String DEFAULT_SCHEM_PATH = "/data/tutorialmod/schematics/free_fox_house.schem";
    private static final Path SCHEMATIC_DIR = FMLPaths.GAMEDIR.get().resolve("tutorialmod").resolve("house_builder");

    private static String cachedDirectoryFingerprint = "";
    private static List<Path> cachedSortedFiles = List.of();
    private static final Map<Path, LoadedSchematic> cachedParsedSchematics = new HashMap<>();
    private static LoadedSchematic cachedDefaultSchematic;

    public SchematicHouseBuilderItem(Properties properties) {
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

        SelectionResult selection = getSelection(serverLevel, getSchematicIndex(stack));
        if (selection == null) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.schematic_builder.no_schematic"));
            return InteractionResult.FAIL;
        }

        if (player.isCrouching()) {
            int nextIndex = getNextIndex(serverLevel, getSchematicIndex(stack));
            setSchematicIndex(stack, nextIndex);
            SelectionResult switched = getSelection(serverLevel, nextIndex);
            if (switched != null) {
                player.sendSystemMessage(Component.translatable("message.tutorialmod.schematic_builder.mode_switched", switched.displayName));
            }
            return InteractionResult.SUCCESS;
        }

        BlockPos origin = context.getClickedPos().above();
        Direction front = player.getDirection().getOpposite();

        if (!isAreaClear(serverLevel, origin, front, selection.schematic)) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.schematic_builder.blocked"));
            return InteractionResult.FAIL;
        }

        placeSchematic(serverLevel, origin, front, selection.schematic);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (selection.isFallback) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.schematic_builder.using_default"));
        }
        player.sendSystemMessage(Component.translatable("message.tutorialmod.schematic_builder.success", selection.displayName));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.schematic_builder.tooltip"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.schematic_builder.switch"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.schematic_builder.tooltip_blocked"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static int getSchematicIndex(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_SCHEMATIC_INDEX);
    }

    private static void setSchematicIndex(ItemStack stack, int index) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putInt(TAG_SCHEMATIC_INDEX, index);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static int getNextIndex(ServerLevel level, int current) {
        int total = getSelectableCount(level);
        if (total <= 0) {
            return 0;
        }
        return Math.floorMod(current + 1, total);
    }

    private static int getSelectableCount(ServerLevel level) {
        List<Path> files = getRuntimeSchematics();
        if (!files.isEmpty()) {
            return files.size();
        }
        return getDefaultSchematic() == null ? 0 : 1;
    }

    private static SelectionResult getSelection(ServerLevel level, int requestedIndex) {
        List<Path> files = getRuntimeSchematics();
        if (!files.isEmpty()) {
            int actualIndex = Math.floorMod(requestedIndex, files.size());
            Path selectedPath = files.get(actualIndex);
            LoadedSchematic schematic = loadRuntimeSchematic(selectedPath);
            if (schematic == null) {
                // If selected file is invalid, try any valid one from the same list.
                for (Path path : files) {
                    schematic = loadRuntimeSchematic(path);
                    if (schematic != null) {
                        return new SelectionResult(schematic, Component.literal(path.getFileName().toString()), false);
                    }
                }
            } else {
                return new SelectionResult(schematic, Component.literal(selectedPath.getFileName().toString()), false);
            }
        }

        LoadedSchematic fallback = getDefaultSchematic();
        if (fallback == null) {
            return null;
        }
        return new SelectionResult(fallback, Component.translatable("tooltip.tutorialmod.schematic_builder.default_name"), true);
    }

    private static LoadedSchematic loadRuntimeSchematic(Path path) {
        if (cachedParsedSchematics.containsKey(path)) {
            return cachedParsedSchematics.get(path);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            LoadedSchematic loaded = parseSchematic(inputStream);
            cachedParsedSchematics.put(path, loaded);
            return loaded;
        } catch (IOException ignored) {
            cachedParsedSchematics.put(path, null);
            return null;
        }
    }

    private static LoadedSchematic getDefaultSchematic() {
        if (cachedDefaultSchematic != null) {
            return cachedDefaultSchematic;
        }

        try (InputStream inputStream = SchematicHouseBuilderItem.class.getResourceAsStream(DEFAULT_SCHEM_PATH)) {
            if (inputStream == null) {
                return null;
            }
            cachedDefaultSchematic = parseSchematic(inputStream);
            return cachedDefaultSchematic;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static List<Path> getRuntimeSchematics() {
        try {
            Files.createDirectories(SCHEMATIC_DIR);
        } catch (IOException ignored) {
            return List.of();
        }

        List<Path> discovered = new ArrayList<>();
        try (Stream<Path> stream = Files.list(SCHEMATIC_DIR)) {
            stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".schem"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .forEach(discovered::add);
        } catch (IOException ignored) {
            return List.of();
        }

        String fingerprint = buildFingerprint(discovered);
        if (!fingerprint.equals(cachedDirectoryFingerprint)) {
            cachedDirectoryFingerprint = fingerprint;
            cachedSortedFiles = List.copyOf(discovered);
            cachedParsedSchematics.clear();
        }

        return cachedSortedFiles;
    }

    private static String buildFingerprint(List<Path> files) {
        StringBuilder builder = new StringBuilder();
        for (Path file : files) {
            builder.append(file.toAbsolutePath());
            builder.append('|');
            try {
                builder.append(Files.getLastModifiedTime(file).toMillis());
                builder.append('|');
                builder.append(Files.size(file));
            } catch (IOException ignored) {
                builder.append("-1|-1");
            }
            builder.append(';');
        }
        return builder.toString();
    }

    private static LoadedSchematic parseSchematic(InputStream inputStream) throws IOException {
        CompoundTag root = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
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

        List<BlockEntityData> blockEntities = new ArrayList<>();
        if (root.contains("BlockEntities", Tag.TAG_LIST)) {
            ListTag blockEntityList = root.getList("BlockEntities", Tag.TAG_COMPOUND);
            for (int i = 0; i < blockEntityList.size(); i++) {
                CompoundTag entityTag = blockEntityList.getCompound(i);
                int[] pos = entityTag.getIntArray("Pos");
                if (pos.length == 3) {
                    blockEntities.add(new BlockEntityData(pos[0], pos[1], pos[2], entityTag.copy()));
                } else {
                    blockEntities.add(new BlockEntityData(entityTag.getInt("x"), entityTag.getInt("y"), entityTag.getInt("z"), entityTag.copy()));
                }
            }
        }

        return new LoadedSchematic(width, height, length, states, blockEntities);
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

        Block block;
        try {
            block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        } catch (Exception ignored) {
            return Blocks.AIR.defaultBlockState();
        }

        if (block == Blocks.AIR && !"minecraft:air".equals(blockId)) {
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

    private static boolean isAreaClear(ServerLevel level, BlockPos origin, Direction front, LoadedSchematic schematic) {
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

    private static void placeSchematic(ServerLevel level, BlockPos origin, Direction front, LoadedSchematic schematic) {
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

                    BlockPos worldPos = toWorld(origin, front, x, y, z);
                    BlockState rotated = state.rotate(rotation);
                    level.setBlock(worldPos, rotated, Block.UPDATE_ALL);
                }
            }
        }

        for (BlockEntityData blockEntityData : schematic.blockEntities()) {
            BlockPos worldPos = toWorld(origin, front, blockEntityData.x(), blockEntityData.y(), blockEntityData.z());
            applyBlockEntityNbt(level, worldPos, blockEntityData.nbt());
        }
    }

    private static void applyBlockEntityNbt(ServerLevel level, BlockPos worldPos, CompoundTag rawTag) {
        BlockEntity blockEntity = level.getBlockEntity(worldPos);
        if (blockEntity == null) {
            return;
        }

        CompoundTag payload = extractBlockEntityPayload(rawTag);
        payload.putInt("x", worldPos.getX());
        payload.putInt("y", worldPos.getY());
        payload.putInt("z", worldPos.getZ());

        if (!payload.contains("id", Tag.TAG_STRING) && rawTag.contains("Id", Tag.TAG_STRING)) {
            payload.putString("id", rawTag.getString("Id"));
        }

        if (!payload.contains("id", Tag.TAG_STRING)) {
            ResourceLocation blockEntityTypeKey = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
            if (blockEntityTypeKey != null) {
                payload.putString("id", blockEntityTypeKey.toString());
            }
        }

        try {
            blockEntity.loadWithComponents(payload, level.registryAccess());
            blockEntity.setChanged();
        } catch (Exception ignored) {
            // Keep schematic placement resilient even when a particular block entity payload cannot be loaded.
        }
    }

    private static CompoundTag extractBlockEntityPayload(CompoundTag rawTag) {
        if (rawTag.contains("Data", Tag.TAG_COMPOUND)) {
            CompoundTag nested = rawTag.getCompound("Data").copy();
            if (!nested.contains("id", Tag.TAG_STRING) && rawTag.contains("Id", Tag.TAG_STRING)) {
                nested.putString("id", rawTag.getString("Id"));
            }
            return nested;
        }

        CompoundTag flattened = rawTag.copy();
        if (flattened.contains("Id", Tag.TAG_STRING) && !flattened.contains("id", Tag.TAG_STRING)) {
            flattened.putString("id", flattened.getString("Id"));
        }
        flattened.remove("Pos");
        flattened.remove("Id");
        return flattened;
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

    private static BlockPos toWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        return origin.relative(right, localX).relative(front, localZ).above(localY);
    }

    private record SelectionResult(LoadedSchematic schematic, Component displayName, boolean isFallback) {}

    private record BlockEntityData(int x, int y, int z, CompoundTag nbt) {}

    private record LoadedSchematic(short width, short height, short length, BlockState[] blocks,
                                   List<BlockEntityData> blockEntities) {
        private BlockState blockAt(int x, int y, int z) {
            int index = x + (y * length + z) * width;
            if (index < 0 || index >= blocks.length) {
                return Blocks.AIR.defaultBlockState();
            }
            return blocks[index];
        }
    }
}
