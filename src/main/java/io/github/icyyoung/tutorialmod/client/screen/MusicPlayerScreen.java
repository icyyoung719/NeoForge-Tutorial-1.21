package io.github.icyyoung.tutorialmod.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.icyyoung.tutorialmod.client.music.MusicManager;
import io.github.icyyoung.tutorialmod.client.music.MusicTrack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicPlayerScreen extends Screen {
    private static final int MAX_PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 216;
    private static final int PANEL_PADDING = 10;
    private static final int ROW_HEIGHT = 18;
    private static final int COVER_SIZE = 14;
    private static final int ITEMS_PER_PAGE = 5;
    private static final float VOLUME_VALUE_STEP = 0.05f;

    private static final Map<String, ResourceLocation> screenTextureCache = new HashMap<>();

    private int currentPage = 0;
    private Button shuffleBtn;
    private Button playPauseBtn;
    private Button repeatBtn;
    private Button overlayBtn;
    private float volumeSliderValue;
    private boolean isDraggingVolume = false;
    private int observedTrackIndex = -2;

    private record UiLayout(
        int panelX,
        int panelY,
        int panelWidth,
        int panelHeight,
        int headerY,
        int rowsStartY,
        int currentRowY,
        int controlsY,
        int progressY,
        int volumeY,
        int titleColX,
        int artistColX,
        int durationColX,
        int rowPlayButtonX,
        int progressBarX,
        int progressBarWidth,
        int volumeBarX,
        int volumeBarWidth,
        int volMinusX,
        int volPlusX
    ) {}

    public MusicPlayerScreen() {
        super(Component.literal("BGM Manager"));
        volumeSliderValue = MusicManager.getInstance().getVolume();
    }

    @Override
    protected void init() {
        super.init();
        if (observedTrackIndex == -2) {
            syncPageToCurrentTrack(false);
        }

        UiLayout l = buildLayout();

        List<MusicTrack> playlist = MusicManager.getInstance().getPlaylist();
        int maxPage = Math.max(0, (playlist.size() - 1) / ITEMS_PER_PAGE);
        currentPage = Mth.clamp(currentPage, 0, maxPage);

        this.addRenderableWidget(Button.builder(actionLabel("Refresh", ChatFormatting.YELLOW), b -> {
            MusicManager.getInstance().loadTracks();
            observedTrackIndex = MusicManager.getInstance().getCurrentTrackIndex();
            syncPageToCurrentTrack(false);
            this.rebuildWidgets();
        }).bounds(l.panelX + 8, l.panelY + 6, 58, 20).build());

        overlayBtn = this.addRenderableWidget(Button.builder(overlayLabel(), b -> {
            MusicManager.getInstance().toggleNowPlayingOverlay();
            refreshControlStates();
        }).bounds(l.panelX + 70, l.panelY + 6, 92, 20).build());

        if (maxPage > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                if (currentPage > 0) currentPage--;
                this.rebuildWidgets();
            }).bounds(l.panelX + l.panelWidth - 64, l.panelY + 6, 20, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                if (currentPage < maxPage) currentPage++;
                this.rebuildWidgets();
            }).bounds(l.panelX + l.panelWidth - 40, l.panelY + 6, 20, 20).build());
        }

        int shuffleW = 58;
        int prevW = 44;
        int playW = 74;
        int nextW = 44;
        int repeatW = 88;
        int gap = 6;
        int controlsTotalWidth = shuffleW + prevW + playW + nextW + repeatW + gap * 4;
        int controlsStartX = l.panelX + (l.panelWidth - controlsTotalWidth) / 2;

        shuffleBtn = this.addRenderableWidget(Button.builder(toggleStyleLabel("Shuffle", MusicManager.getInstance().isShuffle()), b -> {
            MusicManager.getInstance().toggleShuffle();
            refreshControlStates();
        }).bounds(controlsStartX, l.controlsY, shuffleW, 18).build());

        this.addRenderableWidget(Button.builder(actionLabel("Prev", ChatFormatting.AQUA), b -> {
            MusicManager.getInstance().playPrev();
            refreshControlStates();
        }).bounds(controlsStartX + shuffleW + gap, l.controlsY, prevW, 18).build());

        playPauseBtn = this.addRenderableWidget(Button.builder(playPauseLabel(), b -> {
            MusicManager.getInstance().togglePlayPause();
            refreshControlStates();
        }).bounds(controlsStartX + shuffleW + gap + prevW + gap, l.controlsY, playW, 18).build());

        this.addRenderableWidget(Button.builder(actionLabel("Next", ChatFormatting.AQUA), b -> {
            MusicManager.getInstance().playNext();
            refreshControlStates();
        }).bounds(controlsStartX + shuffleW + gap + prevW + gap + playW + gap, l.controlsY, nextW, 18).build());

        repeatBtn = this.addRenderableWidget(Button.builder(toggleStyleLabel("Repeat", MusicManager.getInstance().isLooping()), b -> {
            boolean newLoop = !MusicManager.getInstance().isLooping();
            MusicManager.getInstance().setLooping(newLoop);
            refreshControlStates();
        }).bounds(controlsStartX + shuffleW + gap + prevW + gap + playW + gap + nextW + gap, l.controlsY, repeatW, 18).build());

        this.addRenderableWidget(Button.builder(actionLabel("Vol -", ChatFormatting.GOLD), b -> {
            float nextVolume = Mth.clamp(volumeSliderValue - VOLUME_VALUE_STEP, 0.0f, 1.0f);
            setVolume(nextVolume);
        }).bounds(l.volMinusX, l.volumeY - 1, 44, 18).build());

        this.addRenderableWidget(Button.builder(actionLabel("Vol +", ChatFormatting.GOLD), b -> {
            float nextVolume = Mth.clamp(volumeSliderValue + VOLUME_VALUE_STEP, 0.0f, 1.0f);
            setVolume(nextVolume);
        }).bounds(l.volPlusX, l.volumeY - 1, 44, 18).build());

        int startIndex = currentPage * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int trackIndex = startIndex + i;
            if (trackIndex < playlist.size()) {
                int fi = trackIndex;
                this.addRenderableWidget(Button.builder(actionLabel(">", ChatFormatting.GREEN), b -> {
                    MusicManager.getInstance().playTrack(fi);
                    refreshControlStates();
                }).bounds(l.rowPlayButtonX, l.rowsStartY + i * ROW_HEIGHT + 1, 14, 14).build());
            }
        }

        refreshControlStates();
        observedTrackIndex = MusicManager.getInstance().getCurrentTrackIndex();
    }

    @Override
    public void tick() {
        super.tick();
        int currentTrackIndex = MusicManager.getInstance().getCurrentTrackIndex();
        if (currentTrackIndex != observedTrackIndex) {
            observedTrackIndex = currentTrackIndex;
            syncPageToCurrentTrack(true);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        UiLayout l = buildLayout();

        refreshControlStates();

        // Draw panel base first.
        drawPanelFrame(guiGraphics, l);

        // Render widgets next so buttons are not dimmed by panel overlay.
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render textual/content layer last on a higher z to keep it crisp.
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);

        guiGraphics.drawCenteredString(font, Component.literal("BGM Manager"), l.panelX + l.panelWidth / 2, l.panelY + 10, 0xFFD36B);

        guiGraphics.drawString(font, "Cover", l.panelX + 12, l.headerY, 0xD0D0D0, false);
        guiGraphics.drawString(font, "Title", l.titleColX, l.headerY, 0xD0D0D0, false);
        guiGraphics.drawString(font, "Artist", l.artistColX, l.headerY, 0xD0D0D0, false);
        guiGraphics.drawString(font, "Length", l.durationColX, l.headerY, 0xD0D0D0, false);

        List<MusicTrack> playlist = MusicManager.getInstance().getPlaylist();
        int startIndex = currentPage * ITEMS_PER_PAGE;

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int trackIndex = startIndex + i;
            if (trackIndex < playlist.size()) {
                MusicTrack track = playlist.get(trackIndex);
                boolean isPlaying = MusicManager.getInstance().getCurrentTrack() == track;

                int rowTop = l.rowsStartY + i * ROW_HEIGHT;
                int rowColor = isPlaying ? 0x5530A030 : 0x33101010;
                guiGraphics.fill(l.panelX + 8, rowTop, l.panelX + l.panelWidth - 8, rowTop + ROW_HEIGHT - 2, rowColor);

                renderSmallCover(guiGraphics, track, l.panelX + 12, rowTop + 2);

                int textColor = isPlaying ? 0xC2FFB0 : 0xFFFFFF;
                int titleWidth = Math.max(70, l.artistColX - l.titleColX - 10);
                int artistWidth = Math.max(50, l.durationColX - l.artistColX - 10);
                guiGraphics.drawString(font, clipToWidth(track.getTitle(), titleWidth), l.titleColX, rowTop + 5, textColor, false);
                guiGraphics.drawString(font, clipToWidth(track.getArtist(), artistWidth), l.artistColX, rowTop + 5, textColor, false);
                guiGraphics.drawString(font, formatSeconds(track.getDurationSeconds()), l.durationColX, rowTop + 5, textColor, false);
            }
        }

        MusicTrack currentTrack = MusicManager.getInstance().getCurrentTrack();
        String nowPlayingText = "Now Playing: ";
        if (currentTrack != null) {
            nowPlayingText += clipToWidth(currentTrack.getTitle(), Math.max(100, l.panelWidth / 3)) + " - "
                + clipToWidth(currentTrack.getArtist(), Math.max(90, l.panelWidth / 4));
        } else {
            nowPlayingText += "-";
        }
        guiGraphics.drawString(font, nowPlayingText, l.panelX + 10, l.currentRowY, 0xFFECECEC, false);

        int elapsed = MusicManager.getInstance().getCurrentElapsedSeconds();
        int total = currentTrack == null ? 0 : currentTrack.getDurationSeconds();
        float progress = total <= 0 ? 0.0f : (float) elapsed / (float) total;
        progress = Mth.clamp(progress, 0.0f, 1.0f);

        guiGraphics.drawString(font, "[" + formatSeconds(elapsed) + "]", l.panelX + 10, l.progressY, 0xFFFFFF, false);
        drawBar(guiGraphics, l.progressBarX, l.progressY + 4, l.progressBarWidth, 6, progress, 0xFF4EA7FF, 0xFF2A2A2A);
        guiGraphics.drawString(font, "[" + formatSeconds(total) + "]", l.progressBarX + l.progressBarWidth + 8, l.progressY, 0xFFFFFF, false);

        guiGraphics.drawString(font, "Volume: " + (int) (volumeSliderValue * 100) + "%", l.panelX + 10, l.volumeY, 0xFFFFFF, false);
        drawBar(guiGraphics, l.volumeBarX, l.volumeY + 4, l.volumeBarWidth, 6, volumeSliderValue, 0xFF8FD14F, 0xFF2A2A2A);

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        UiLayout l = buildLayout();

        if (mouseY >= l.volumeY && mouseY <= l.volumeY + 14 && mouseX >= l.volumeBarX && mouseX <= l.volumeBarX + l.volumeBarWidth) {
            isDraggingVolume = true;
            updateVolumeSlider(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDraggingVolume) {
            isDraggingVolume = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingVolume) {
            updateVolumeSlider(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateVolumeSlider(double mouseX) {
        UiLayout l = buildLayout();
        float pct = (float) (mouseX - l.volumeBarX) / (float) l.volumeBarWidth;
        setVolume(Mth.clamp(pct, 0.0f, 1.0f));
    }

    private void setVolume(float newVolume) {
        volumeSliderValue = newVolume;
        MusicManager.getInstance().setVolume(newVolume);
    }

    private void drawPanelFrame(GuiGraphics guiGraphics, UiLayout l) {
        guiGraphics.fill(l.panelX, l.panelY, l.panelX + l.panelWidth, l.panelY + l.panelHeight, 0x990A0A0A);
        guiGraphics.fill(l.panelX, l.panelY, l.panelX + l.panelWidth, l.panelY + 1, 0xFFFFFFFF);
        guiGraphics.fill(l.panelX, l.panelY + l.panelHeight - 1, l.panelX + l.panelWidth, l.panelY + l.panelHeight, 0xFFFFFFFF);
        guiGraphics.fill(l.panelX, l.panelY, l.panelX + 1, l.panelY + l.panelHeight, 0xFFFFFFFF);
        guiGraphics.fill(l.panelX + l.panelWidth - 1, l.panelY, l.panelX + l.panelWidth, l.panelY + l.panelHeight, 0xFFFFFFFF);

        guiGraphics.fill(l.panelX + 8, l.panelY + 30, l.panelX + l.panelWidth - 8, l.panelY + 31, 0xFFAAAAAA);
        guiGraphics.fill(l.panelX + 8, l.panelY + 46, l.panelX + l.panelWidth - 8, l.panelY + 47, 0xFFAAAAAA);
        guiGraphics.fill(l.panelX + 8, l.panelY + 140, l.panelX + l.panelWidth - 8, l.panelY + 141, 0xFFAAAAAA);
        guiGraphics.fill(l.panelX + 8, l.panelY + 156, l.panelX + l.panelWidth - 8, l.panelY + 157, 0xFFAAAAAA);
        guiGraphics.fill(l.panelX + 8, l.panelY + 178, l.panelX + l.panelWidth - 8, l.panelY + 179, 0xFFAAAAAA);
        guiGraphics.fill(l.panelX + 8, l.panelY + 194, l.panelX + l.panelWidth - 8, l.panelY + 195, 0xFFAAAAAA);
    }

    private UiLayout buildLayout() {
        int panelWidth = Math.min(MAX_PANEL_WIDTH, Math.max(390, this.width - 16));
        int panelHeight = Math.min(PANEL_HEIGHT, Math.max(200, this.height - 16));
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        int headerY = panelY + 34;
        int rowsStartY = panelY + 48;
        int currentRowY = panelY + 143;
        int controlsY = panelY + 158;
        int progressY = panelY + 182;
        int volumeY = panelY + 198;

        int titleColX = panelX + 40;
        int durationColX = panelX + panelWidth - 100;
        int artistColX = panelX + Math.min(240, (int) (panelWidth * 0.47f));
        int rowPlayButtonX = panelX + panelWidth - 22;

        int progressBarX = panelX + 84;
        int progressBarWidth = Math.max(160, panelWidth - 170);

        int volPlusX = panelX + panelWidth - 54;
        int volMinusX = volPlusX - 50;
        int volumeBarX = panelX + 68;
        int volumeBarWidth = Math.max(110, volMinusX - volumeBarX - 8);

        return new UiLayout(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            headerY,
            rowsStartY,
            currentRowY,
            controlsY,
            progressY,
            volumeY,
            titleColX,
            artistColX,
            durationColX,
            rowPlayButtonX,
            progressBarX,
            progressBarWidth,
            volumeBarX,
            volumeBarWidth,
            volMinusX,
            volPlusX
        );
    }

    private void renderSmallCover(GuiGraphics guiGraphics, MusicTrack track, int x, int y) {
        ResourceLocation tex = getOrCreateCoverTexture(track);
        if (tex != null) {
            guiGraphics.blit(tex, x, y, 0, 0, COVER_SIZE, COVER_SIZE, COVER_SIZE, COVER_SIZE);
            return;
        }
        guiGraphics.fill(x, y, x + COVER_SIZE, y + COVER_SIZE, 0xFF444444);
    }

    private ResourceLocation getOrCreateCoverTexture(MusicTrack track) {
        if (track.getCoverArt() == null) {
            return null;
        }

        ResourceLocation cached = screenTextureCache.get(track.getFileName());
        if (cached != null || screenTextureCache.containsKey(track.getFileName())) {
            return cached;
        }

        ResourceLocation texLoc = null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(track.getCoverArt())) {
            java.awt.image.BufferedImage bImage = ImageIO.read(in);
            if (bImage != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(bImage, "png", out);
                try (ByteArrayInputStream pngIn = new ByteArrayInputStream(out.toByteArray())) {
                    NativeImage image = NativeImage.read(pngIn);
                    DynamicTexture texture = new DynamicTexture(image);
                    String textureId = "music_screen_cover_" + track.getFileName().hashCode();
                    texLoc = ResourceLocation.fromNamespaceAndPath("tutorialmod", textureId.replace('-', 'n'));
                    Minecraft.getInstance().getTextureManager().register(texLoc, texture);
                }
            }
        } catch (Exception ignored) {
        }

        screenTextureCache.put(track.getFileName(), texLoc);
        return texLoc;
    }

    private String clipToWidth(String text, int maxWidth) {
        if (text == null || text.isBlank()) {
            return "Unknown";
        }

        String stripped = text.strip();
        String clipped = font.plainSubstrByWidth(stripped, maxWidth);
        if (clipped.length() < stripped.length()) {
            return clipped + "...";
        }
        return clipped;
    }

    private String formatSeconds(int seconds) {
        int safe = Math.max(0, seconds);
        int mm = safe / 60;
        int ss = safe % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    private void drawBar(GuiGraphics guiGraphics, int x, int y, int width, int height, float ratio, int fillColor, int bgColor) {
        guiGraphics.fill(x, y, x + width, y + height, bgColor);
        int filled = (int) (width * Mth.clamp(ratio, 0.0f, 1.0f));
        if (filled > 0) {
            guiGraphics.fill(x, y, x + filled, y + height, fillColor);
        }
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, 0xFFC0C0C0);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFFC0C0C0);
        guiGraphics.fill(x - 1, y - 1, x, y + height + 1, 0xFFC0C0C0);
        guiGraphics.fill(x + width, y - 1, x + width + 1, y + height + 1, 0xFFC0C0C0);
    }

    private void refreshControlStates() {
        MusicManager manager = MusicManager.getInstance();
        if (shuffleBtn != null) {
            shuffleBtn.setMessage(toggleStyleLabel("Shuffle", manager.isShuffle()));
        }
        if (repeatBtn != null) {
            repeatBtn.setMessage(toggleStyleLabel("Repeat", manager.isLooping()));
        }
        if (playPauseBtn != null) {
            playPauseBtn.setMessage(playPauseLabel());
        }
        if (overlayBtn != null) {
            overlayBtn.setMessage(overlayLabel());
        }
    }

    private Component playPauseLabel() {
        MusicManager manager = MusicManager.getInstance();
        if (manager.isPlaying()) {
            return toggleStyleLabel("Pause", true);
        }
        return toggleStyleLabel("Play", false);
    }

    private Component toggleStyleLabel(String text, boolean active) {
        ChatFormatting color = active ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        return Component.literal(text).withStyle(color);
    }

    private Component actionLabel(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }

    private Component overlayLabel() {
        return toggleStyleLabel("Overlay", MusicManager.getInstance().isNowPlayingOverlayEnabled());
    }

    private void syncPageToCurrentTrack(boolean rebuildIfChanged) {
        int currentTrackIndex = MusicManager.getInstance().getCurrentTrackIndex();
        int targetPage = pageForTrackIndex(currentTrackIndex);
        if (targetPage != currentPage) {
            currentPage = targetPage;
            if (rebuildIfChanged) {
                this.rebuildWidgets();
            }
        }
    }

    private int pageForTrackIndex(int trackIndex) {
        if (trackIndex < 0) {
            return 0;
        }
        return trackIndex / ITEMS_PER_PAGE;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
