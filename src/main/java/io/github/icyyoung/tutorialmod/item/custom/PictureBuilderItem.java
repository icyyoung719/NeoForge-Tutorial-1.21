package io.github.icyyoung.tutorialmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class PictureBuilderItem extends Item {
    private static final String TAG_PICTURE_INDEX = "picture_index";
    private static final String TAG_PLACEMENT_MODE = "placement_mode";
    private static final Path PICTURE_DIR = FMLPaths.GAMEDIR.get().resolve("tutorialmod").resolve("picture_builder");

    private static final int MAX_BUILD_HEIGHT = 256;
    private static final int ALPHA_AIR_THRESHOLD = 32;

    private enum PlacementMode {
        WALL,
        FLOOR
    }

    private static final List<PaletteEntry> PALETTE = List.of(
            entry(Blocks.WHITE_WOOL, 233, 236, 236),
            entry(Blocks.LIGHT_GRAY_WOOL, 142, 142, 134),
            entry(Blocks.GRAY_WOOL, 61, 68, 71),
            entry(Blocks.BLACK_WOOL, 22, 22, 26),
            entry(Blocks.BROWN_WOOL, 114, 71, 40),
            entry(Blocks.RED_WOOL, 160, 39, 34),
            entry(Blocks.ORANGE_WOOL, 240, 118, 19),
            entry(Blocks.YELLOW_WOOL, 249, 198, 39),
            entry(Blocks.LIME_WOOL, 112, 185, 25),
            entry(Blocks.GREEN_WOOL, 84, 109, 27),
            entry(Blocks.CYAN_WOOL, 21, 137, 145),
            entry(Blocks.LIGHT_BLUE_WOOL, 58, 179, 218),
            entry(Blocks.BLUE_WOOL, 53, 57, 157),
            entry(Blocks.PURPLE_WOOL, 121, 42, 172),
            entry(Blocks.MAGENTA_WOOL, 190, 68, 201),
            entry(Blocks.PINK_WOOL, 237, 141, 172),

            entry(Blocks.WHITE_CONCRETE, 207, 213, 214),
            entry(Blocks.LIGHT_GRAY_CONCRETE, 125, 125, 115),
            entry(Blocks.GRAY_CONCRETE, 54, 57, 61),
            entry(Blocks.BLACK_CONCRETE, 8, 10, 15),
            entry(Blocks.BROWN_CONCRETE, 96, 60, 32),
            entry(Blocks.RED_CONCRETE, 142, 33, 33),
            entry(Blocks.ORANGE_CONCRETE, 224, 97, 1),
            entry(Blocks.YELLOW_CONCRETE, 240, 175, 21),
            entry(Blocks.LIME_CONCRETE, 94, 169, 24),
            entry(Blocks.GREEN_CONCRETE, 73, 91, 36),
            entry(Blocks.CYAN_CONCRETE, 21, 119, 136),
            entry(Blocks.LIGHT_BLUE_CONCRETE, 36, 137, 199),
            entry(Blocks.BLUE_CONCRETE, 44, 46, 143),
            entry(Blocks.PURPLE_CONCRETE, 100, 31, 156),
            entry(Blocks.MAGENTA_CONCRETE, 169, 48, 159),
            entry(Blocks.PINK_CONCRETE, 214, 101, 143),

            entry(Blocks.WHITE_TERRACOTTA, 209, 178, 161),
            entry(Blocks.LIGHT_GRAY_TERRACOTTA, 135, 107, 98),
            entry(Blocks.GRAY_TERRACOTTA, 57, 42, 35),
            entry(Blocks.BLACK_TERRACOTTA, 37, 23, 17),
            entry(Blocks.BROWN_TERRACOTTA, 77, 51, 36),
            entry(Blocks.RED_TERRACOTTA, 143, 61, 47),
            entry(Blocks.ORANGE_TERRACOTTA, 161, 83, 37),
            entry(Blocks.YELLOW_TERRACOTTA, 186, 133, 35),
            entry(Blocks.LIME_TERRACOTTA, 103, 117, 53),
            entry(Blocks.GREEN_TERRACOTTA, 76, 83, 42),
            entry(Blocks.CYAN_TERRACOTTA, 87, 91, 91),
            entry(Blocks.LIGHT_BLUE_TERRACOTTA, 113, 108, 137),
            entry(Blocks.BLUE_TERRACOTTA, 74, 59, 91),
            entry(Blocks.PURPLE_TERRACOTTA, 118, 70, 86),
            entry(Blocks.MAGENTA_TERRACOTTA, 149, 88, 108),
            entry(Blocks.PINK_TERRACOTTA, 162, 78, 79)
    );

    private static String cachedDirectoryFingerprint = "";
    private static List<Path> cachedSortedFiles = List.of();
    private static final Map<Path, ProcessedPicture> cachedProcessedPictures = new HashMap<>();

    public PictureBuilderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCrouching()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            PlacementMode nextMode = togglePlacementMode(stack);
            player.sendSystemMessage(Component.translatable("message.tutorialmod.picture_builder.placement_mode_switched",
                    Component.translatable(getPlacementModeTranslationKey(nextMode))));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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

        SelectionResult selection = getSelection(getPictureIndex(stack));
        if (selection == null) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.picture_builder.no_image"));
            return InteractionResult.FAIL;
        }

        if (player.isCrouching()) {
            int nextIndex = getNextIndex(getPictureIndex(stack));
            setPictureIndex(stack, nextIndex);
            SelectionResult switched = getSelection(nextIndex);
            if (switched != null) {
                player.sendSystemMessage(Component.translatable("message.tutorialmod.picture_builder.picture_switched", switched.displayName()));
            }
            return InteractionResult.SUCCESS;
        }

        BlockPos origin = context.getClickedPos().above();
        Direction front = player.getDirection().getOpposite();
        PlacementMode placementMode = getPlacementMode(stack);

        if (placementMode == PlacementMode.FLOOR) {
            placeFloorPicture(serverLevel, origin, front, selection.picture());
        } else {
            placeWallPicture(serverLevel, origin, front, selection.picture());
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.sendSystemMessage(Component.translatable("message.tutorialmod.picture_builder.success",
            Component.translatable(getPlacementModeTranslationKey(placementMode)), selection.displayName()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.picture_builder.tooltip"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.picture_builder.mode",
                Component.translatable(getPlacementModeTranslationKey(getPlacementMode(stack)))));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.picture_builder.switch_picture"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.picture_builder.switch_mode"));
        tooltipComponents.add(Component.translatable("tooltip.tutorialmod.picture_builder.tooltip_clear"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static int getPictureIndex(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_PICTURE_INDEX);
    }

    private static void setPictureIndex(ItemStack stack, int index) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        tag.putInt(TAG_PICTURE_INDEX, index);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static PlacementMode getPlacementMode(ItemStack stack) {
        int index = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_PLACEMENT_MODE);
        PlacementMode[] values = PlacementMode.values();
        return values[Math.floorMod(index, values.length)];
    }

    private static PlacementMode togglePlacementMode(ItemStack stack) {
        PlacementMode[] values = PlacementMode.values();
        PlacementMode nextMode = values[Math.floorMod(getPlacementMode(stack).ordinal() + 1, values.length)];
        setPlacementMode(stack, nextMode);
        return nextMode;
    }

    private static void setPlacementMode(ItemStack stack, PlacementMode mode) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        tag.putInt(TAG_PLACEMENT_MODE, mode.ordinal());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static String getPlacementModeTranslationKey(PlacementMode mode) {
        return switch (mode) {
            case WALL -> "tooltip.tutorialmod.picture_builder.mode.wall";
            case FLOOR -> "tooltip.tutorialmod.picture_builder.mode.floor";
        };
    }

    private static int getNextIndex(int current) {
        int total = getSelectableCount();
        if (total <= 0) {
            return 0;
        }
        return Math.floorMod(current + 1, total);
    }

    private static int getSelectableCount() {
        return getRuntimePictures().size();
    }

    private static SelectionResult getSelection(int requestedIndex) {
        List<Path> files = getRuntimePictures();
        if (files.isEmpty()) {
            return null;
        }

        int actualIndex = Math.floorMod(requestedIndex, files.size());
        Path selectedPath = files.get(actualIndex);
        ProcessedPicture picture = loadRuntimePicture(selectedPath);
        if (picture != null) {
            return new SelectionResult(picture, Component.literal(selectedPath.getFileName().toString()));
        }

        for (Path path : files) {
            picture = loadRuntimePicture(path);
            if (picture != null) {
                return new SelectionResult(picture, Component.literal(path.getFileName().toString()));
            }
        }

        return null;
    }

    private static ProcessedPicture loadRuntimePicture(Path path) {
        if (cachedProcessedPictures.containsKey(path)) {
            return cachedProcessedPictures.get(path);
        }

        try {
            BufferedImage source = ImageIO.read(path.toFile());
            if (source == null) {
                cachedProcessedPictures.put(path, null);
                return null;
            }

            BufferedImage scaled = scaleImage(source);
            if (scaled == null) {
                cachedProcessedPictures.put(path, null);
                return null;
            }

            ProcessedPicture picture = toProcessedPicture(scaled);
            cachedProcessedPictures.put(path, picture);
            return picture;
        } catch (IOException ignored) {
            cachedProcessedPictures.put(path, null);
            return null;
        }
    }

    private static List<Path> getRuntimePictures() {
        try {
            Files.createDirectories(PICTURE_DIR);
        } catch (IOException ignored) {
            return List.of();
        }

        List<Path> discovered = new ArrayList<>();
        try (Stream<Path> stream = Files.list(PICTURE_DIR)) {
            stream.filter(path -> Files.isRegularFile(path) && isSupportedImage(path))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .forEach(discovered::add);
        } catch (IOException ignored) {
            return List.of();
        }

        String fingerprint = buildFingerprint(discovered);
        if (!fingerprint.equals(cachedDirectoryFingerprint)) {
            cachedDirectoryFingerprint = fingerprint;
            cachedSortedFiles = List.copyOf(discovered);
            cachedProcessedPictures.clear();
        }

        return cachedSortedFiles;
    }

    private static boolean isSupportedImage(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
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

    private static BufferedImage scaleImage(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        if (height <= MAX_BUILD_HEIGHT) {
            return toArgbImage(source);
        }

        double scale = Math.min(1.0D, (double) MAX_BUILD_HEIGHT / height);
        int targetWidth = Math.max(1, (int) Math.floor(width * scale));
        int targetHeight = Math.max(1, (int) Math.floor(height * scale));

        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return target;
    }

    private static BufferedImage toArgbImage(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }

        BufferedImage converted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = converted.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return converted;
    }

    private static ProcessedPicture toProcessedPicture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BlockState[] blocks = new BlockState[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha <= ALPHA_AIR_THRESHOLD) {
                    blocks[x + y * width] = Blocks.AIR.defaultBlockState();
                    continue;
                }

                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;
                blocks[x + y * width] = nearestBlock(red, green, blue);
            }
        }

        return new ProcessedPicture(width, height, blocks);
    }

    private static BlockState nearestBlock(int red, int green, int blue) {
        PaletteEntry best = PALETTE.getFirst();
        int bestDistance = colorDistanceSquared(red, green, blue, best.red(), best.green(), best.blue());

        for (int i = 1; i < PALETTE.size(); i++) {
            PaletteEntry entry = PALETTE.get(i);
            int distance = colorDistanceSquared(red, green, blue, entry.red(), entry.green(), entry.blue());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entry;
            }
        }

        return best.state();
    }

    private static int colorDistanceSquared(int r1, int g1, int b1, int r2, int g2, int b2) {
        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;
        return dr * dr + dg * dg + db * db;
    }

    @SuppressWarnings("deprecation")
    private static void placeWallPicture(ServerLevel level, BlockPos origin, Direction front, ProcessedPicture picture) {
        int width = picture.width();
        int height = picture.height();
        Rotation rotation = rotationFromFront(front);

        for (int imageY = 0; imageY < height; imageY++) {
            int localY = (height - 1) - imageY;
            for (int x = 0; x < width; x++) {
                BlockPos worldPos = toWorld(origin, front, x, localY, 0);
                level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        for (int imageY = 0; imageY < height; imageY++) {
            int localY = (height - 1) - imageY;
            for (int x = 0; x < width; x++) {
                BlockState state = picture.blockAt(x, imageY);
                if (state.isAir()) {
                    continue;
                }

                BlockPos worldPos = toWorld(origin, front, x, localY, 0);
                level.setBlock(worldPos, state.rotate(rotation), Block.UPDATE_ALL);
            }
        }
    }

    private static void placeFloorPicture(ServerLevel level, BlockPos origin, Direction front, ProcessedPicture picture) {
        int width = picture.width();
        int height = picture.height();

        for (int imageZ = 0; imageZ < height; imageZ++) {
            int localZ = (height - 1) - imageZ;
            for (int x = 0; x < width; x++) {
                BlockState state = picture.blockAt(x, imageZ);
                if (state.isAir()) {
                    continue;
                }

                BlockPos worldPos = toFloorWorld(origin, front, x, localZ);
                level.setBlock(worldPos, state, Block.UPDATE_ALL);
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

    private static BlockPos toWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        return origin.relative(right, localX).relative(front, localZ).above(localY);
    }

    private static BlockPos toFloorWorld(BlockPos origin, Direction front, int localX, int localZ) {
        Direction right = front.getClockWise();
        return origin.relative(right, localX).relative(front, localZ);
    }

    private static PaletteEntry entry(Block block, int red, int green, int blue) {
        return new PaletteEntry(block.defaultBlockState(), red, green, blue);
    }

    private record PaletteEntry(BlockState state, int red, int green, int blue) {}

    private record SelectionResult(ProcessedPicture picture, Component displayName) {}

    private record ProcessedPicture(int width, int height, BlockState[] blocks) {
        private BlockState blockAt(int x, int y) {
            int index = x + y * width;
            if (index < 0 || index >= blocks.length) {
                return Blocks.AIR.defaultBlockState();
            }
            return blocks[index];
        }
    }
}