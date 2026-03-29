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
    private static final int MAX_PANEL_WIDTH = 580;
    private static final int PANEL_HEIGHT = 280;
    private static final int ROW_HEIGHT = 22;
    private static final int COVER_SIZE = 18;
    private static final float SCROLL_SPEED = ROW_HEIGHT * 0.5f;

    private static final Map<String, ResourceLocation> screenTextureCache = new HashMap<>();

    private Button shuffleBtn;
    private Button playPauseBtn;
    private Button repeatBtn;
    private Button overlayBtn;
    
    private float volumeSliderValue;
    private boolean isDraggingVolume = false;
    
    private float scrollAmount = 0.0f;
    private boolean isDraggingScrollbar = false;
    
    private int hoveredListIndex = -1;

    private record UiLayout(
        int panelX, int panelY, int panelWidth, int panelHeight,
        int leftPaneWidth, int rightPaneX, int rightPaneWidth,
        
        int listHeaderY, int listStartY, int listVisibleHeight,
        int scrollBarX, int scrollBarY, int scrollBarWidth, int scrollBarHeight,
        
        int colCoverX, int colTitleX, int colArtistX, int colDurationX,
        
        int controlsRow1Y,
        int controlsRow2Y,
        int nowPlayingY,
        int progressY,
        int volumeY,
        int utilityY,
        
        int progressBarX, int progressBarWidth,
        int volumeBarX, int volumeBarWidth,
        int btnGap
    ) {}

    public MusicPlayerScreen() {
        super(Component.literal("BGM Manager"));
        volumeSliderValue = MusicManager.getInstance().getVolume();
    }

    @Override
    protected void init() {
        super.init();
        UiLayout l = buildLayout();

        // Right Pane Controls (two rows to avoid horizontal overflow)
        int shuffleW = 80;
        int repeatW = 80;
        int topRowStartX = l.rightPaneX + (l.rightPaneWidth - (shuffleW + l.btnGap + repeatW)) / 2;

        shuffleBtn = this.addRenderableWidget(Button.builder(toggleStyleLabel("Shuffle", MusicManager.getInstance().isShuffle()), b -> {
            MusicManager.getInstance().toggleShuffle();
            refreshControlStates();
        }).bounds(topRowStartX, l.controlsRow1Y, shuffleW, 20).build());

        repeatBtn = this.addRenderableWidget(Button.builder(toggleStyleLabel("Repeat", MusicManager.getInstance().isLooping()), b -> {
            MusicManager.getInstance().setLooping(!MusicManager.getInstance().isLooping());
            refreshControlStates();
        }).bounds(topRowStartX + shuffleW + l.btnGap, l.controlsRow1Y, repeatW, 20).build());

        int prevW = 52;
        int playW = 74;
        int nextW = 52;
        int bottomRowStartX = l.rightPaneX + (l.rightPaneWidth - (prevW + l.btnGap + playW + l.btnGap + nextW)) / 2;

        this.addRenderableWidget(Button.builder(actionLabel("Prev", ChatFormatting.AQUA), b -> {
            MusicManager.getInstance().playPrev();
            refreshControlStates();
        }).bounds(bottomRowStartX, l.controlsRow2Y, prevW, 20).build());

        playPauseBtn = this.addRenderableWidget(Button.builder(playPauseLabel(), b -> {
            MusicManager.getInstance().togglePlayPause();
            refreshControlStates();
        }).bounds(bottomRowStartX + prevW + l.btnGap, l.controlsRow2Y, playW, 20).build());

        this.addRenderableWidget(Button.builder(actionLabel("Next", ChatFormatting.AQUA), b -> {
            MusicManager.getInstance().playNext();
            refreshControlStates();
        }).bounds(bottomRowStartX + prevW + l.btnGap + playW + l.btnGap, l.controlsRow2Y, nextW, 20).build());

        // Refresh & Overlay 
        int utilityTotalWidth = 68 + l.btnGap + 84;
        int utilityStartX = l.rightPaneX + (l.rightPaneWidth - utilityTotalWidth) / 2;
        
        this.addRenderableWidget(Button.builder(actionLabel("Refresh", ChatFormatting.YELLOW), b -> {
            MusicManager.getInstance().loadTracks();
        }).bounds(utilityStartX, l.utilityY, 68, 20).build());

        overlayBtn = this.addRenderableWidget(Button.builder(overlayLabel(), b -> {
            MusicManager.getInstance().toggleNowPlayingOverlay();
            refreshControlStates();
        }).bounds(utilityStartX + 68 + l.btnGap, l.utilityY, 84, 20).build());

        refreshControlStates();
    }

    @Override
    public void tick() {
        super.tick();
        // Hover state updating for the list is handled in render where we know the bounds and mouse pos.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        UiLayout l = buildLayout();
        refreshControlStates();
        updateHoverState(l, mouseX, mouseY);

        // Render base panels
        drawPanelFrames(guiGraphics, l);

        // Render standard widgets
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render custom content on a raised Z-level
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);

        // Header Title
        guiGraphics.drawCenteredString(font, Component.literal("BGM Manager").withStyle(ChatFormatting.GOLD), l.panelX + l.panelWidth / 2, l.panelY + 10, 0xFFFFFF);

        // --- Left Pane: List Headers ---
        guiGraphics.drawString(font, "Cover", l.colCoverX, l.listHeaderY, 0xD0D0D0, false);
        guiGraphics.drawString(font, "Title", l.colTitleX, l.listHeaderY, 0xD0D0D0, false);
        guiGraphics.drawString(font, "Artist", l.colArtistX, l.listHeaderY, 0xD0D0D0, false);
        guiGraphics.drawString(font, "Length", l.colDurationX, l.listHeaderY, 0xD0D0D0, false);

        // --- Left Pane: Scrollable List ---
        List<MusicTrack> playlist = MusicManager.getInstance().getPlaylist();
        float maxScroll = Math.max(0, playlist.size() * ROW_HEIGHT - l.listVisibleHeight);
        scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);
        
        guiGraphics.enableScissor(l.panelX + 4, l.listStartY, l.panelX + l.leftPaneWidth - 14, l.listStartY + l.listVisibleHeight);
        
        for (int i = 0; i < playlist.size(); i++) {
            int rowTop = (int) (l.listStartY + i * ROW_HEIGHT - scrollAmount);
            if (rowTop + ROW_HEIGHT < l.listStartY) continue; // Above view
            if (rowTop > l.listStartY + l.listVisibleHeight) break; // Below view

            MusicTrack track = playlist.get(i);
            boolean isPlaying = MusicManager.getInstance().getCurrentTrack() == track;
            boolean isHovered = (i == hoveredListIndex);

            // Row Background
            int rowColor;
            if (isPlaying) {
                rowColor = 0x5530A030; // Greenish
            } else if (isHovered) {
                rowColor = 0x44606060; // Lighter gray for hover
            } else {
                rowColor = (i % 2 == 0) ? 0x22101010 : 0x33101010; // Alternate dark grays
            }
            guiGraphics.fill(l.panelX + 6, rowTop, l.panelX + l.leftPaneWidth - 16, rowTop + ROW_HEIGHT, rowColor);

            // Row Content
            renderSmallCover(guiGraphics, track, l.colCoverX, rowTop + 2);

            int textColor = isPlaying ? 0xC2FFB0 : (isHovered ? 0xFFFFFF : 0xDDDDDD);
            int titleWidth = Math.max(70, l.colArtistX - l.colTitleX - 10);
            int artistWidth = Math.max(50, l.colDurationX - l.colArtistX - 10);
            
            guiGraphics.drawString(font, clipToWidth(track.getTitle(), titleWidth), l.colTitleX, rowTop + 7, textColor, false);
            guiGraphics.drawString(font, clipToWidth(track.getArtist(), artistWidth), l.colArtistX, rowTop + 7, textColor, false);
            guiGraphics.drawString(font, formatSeconds(track.getDurationSeconds()), l.colDurationX, rowTop + 7, textColor, false);
        }
        guiGraphics.disableScissor();

        // Scrollbar
        drawScrollbar(guiGraphics, l, playlist.size(), maxScroll);

        // --- Right Pane: Status & Sliders ---
        MusicTrack currentTrack = MusicManager.getInstance().getCurrentTrack();
        
        guiGraphics.drawString(font, "Now Playing:", l.rightPaneX + 10, l.nowPlayingY, 0xFFECECEC, false);
        String playingText = "-";
        if (currentTrack != null) {
            playingText = clipToWidth(currentTrack.getTitle() + " - " + currentTrack.getArtist(), l.rightPaneWidth - 20);
        }
        guiGraphics.drawString(font, playingText, l.rightPaneX + 10, l.nowPlayingY + 12, 0xFFD36B, false);

        int elapsed = MusicManager.getInstance().getCurrentElapsedSeconds();
        int total = currentTrack == null ? 0 : currentTrack.getDurationSeconds();
        float progress = total <= 0 ? 0.0f : (float) elapsed / (float) total;
        progress = Mth.clamp(progress, 0.0f, 1.0f);

        guiGraphics.drawString(font, "Progress:", l.rightPaneX + 10, l.progressY, 0xFFFFFF, false);
        guiGraphics.drawString(font, "[" + formatSeconds(elapsed) + "]", l.progressBarX - 44, l.progressY + 12, 0xFFFFFF, false);
        drawBar(guiGraphics, l.progressBarX, l.progressY + 14, l.progressBarWidth, 6, progress, 0xFF4EA7FF, 0xFF2A2A2A);
        guiGraphics.drawString(font, "[" + formatSeconds(total) + "]", l.progressBarX + l.progressBarWidth + 8, l.progressY + 12, 0xFFFFFF, false);

        int volumePercent = (int) (volumeSliderValue * 100);
        guiGraphics.drawString(font, "Volume:", l.rightPaneX + 10, l.volumeY, 0xFFFFFF, false);
        drawBar(guiGraphics, l.volumeBarX, l.volumeY + 14, l.volumeBarWidth, 6, volumeSliderValue, 0xFF8FD14F, 0xFF2A2A2A);
        guiGraphics.drawString(font, volumePercent + "%", l.volumeBarX + l.volumeBarWidth + 8, l.volumeY + 12, 0xFFFFFF, false);

        guiGraphics.pose().popPose();
    }

    private void updateHoverState(UiLayout l, int mouseX, int mouseY) {
        hoveredListIndex = -1;
        if (mouseX >= l.panelX + 6 && mouseX <= l.panelX + l.leftPaneWidth - 16) {
            if (mouseY >= l.listStartY && mouseY <= l.listStartY + l.listVisibleHeight) {
                int index = (int) ((mouseY - l.listStartY + scrollAmount) / ROW_HEIGHT);
                if (index >= 0 && index < MusicManager.getInstance().getPlaylist().size()) {
                    hoveredListIndex = index;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        UiLayout l = buildLayout();

        // Check Volume Slider
        if (mouseY >= l.volumeY + 12 && mouseY <= l.volumeY + 22
            && mouseX >= l.volumeBarX && mouseX <= l.volumeBarX + l.volumeBarWidth) {
            isDraggingVolume = true;
            updateVolumeSlider(mouseX, l);
            return true;
        }

        // Check Scrollbar
        if (mouseX >= l.scrollBarX && mouseX <= l.scrollBarX + l.scrollBarWidth && mouseY >= l.scrollBarY && mouseY <= l.scrollBarY + l.scrollBarHeight) {
            isDraggingScrollbar = true;
            return true;
        }

        // Check List Play Click
        if (hoveredListIndex != -1 && button == 0) {
            MusicManager.getInstance().playTrack(hoveredListIndex);
            refreshControlStates();
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
        if (isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        UiLayout l = buildLayout();
        
        if (isDraggingVolume) {
            updateVolumeSlider(mouseX, l);
            return true;
        }
        
        if (isDraggingScrollbar) {
            List<MusicTrack> playlist = MusicManager.getInstance().getPlaylist();
            float maxScroll = Math.max(0, playlist.size() * ROW_HEIGHT - l.listVisibleHeight);
            if (maxScroll > 0) {
                int thumbHeight = Math.max(20, (int) ((float) l.listVisibleHeight / (playlist.size() * ROW_HEIGHT) * l.scrollBarHeight));
                int trackScrollArea = l.scrollBarHeight - thumbHeight;
                if (trackScrollArea > 0) {
                    float scrollPct = (float) (mouseY - l.scrollBarY - thumbHeight / 2) / trackScrollArea;
                    scrollAmount = Mth.clamp(scrollPct * maxScroll, 0, maxScroll);
                }
            }
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<MusicTrack> playlist = MusicManager.getInstance().getPlaylist();
        UiLayout l = buildLayout();
        float maxScroll = Math.max(0, playlist.size() * ROW_HEIGHT - l.listVisibleHeight);
        
        scrollAmount -= (float) (scrollY * SCROLL_SPEED);
        scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);
        
        return true; // Consume scroll
    }

    private void updateVolumeSlider(double mouseX, UiLayout l) {
        float pct = (float) (mouseX - l.volumeBarX) / (float) l.volumeBarWidth;
        setVolume(Mth.clamp(pct, 0.0f, 1.0f));
    }

    private void setVolume(float newVolume) {
        volumeSliderValue = newVolume;
        MusicManager.getInstance().setVolume(newVolume);
    }

    private void drawPanelFrames(GuiGraphics guiGraphics, UiLayout l) {
        // Main Background
        guiGraphics.fill(l.panelX, l.panelY, l.panelX + l.panelWidth, l.panelY + l.panelHeight, 0x990A0A0A);
        
        // Borders
        int borderColor = 0xFFFFFFFF;
        guiGraphics.fill(l.panelX, l.panelY, l.panelX + l.panelWidth, l.panelY + 1, borderColor); // Top
        guiGraphics.fill(l.panelX, l.panelY + l.panelHeight - 1, l.panelX + l.panelWidth, l.panelY + l.panelHeight, borderColor); // Bottom
        guiGraphics.fill(l.panelX, l.panelY, l.panelX + 1, l.panelY + l.panelHeight, borderColor); // Left
        guiGraphics.fill(l.panelX + l.panelWidth - 1, l.panelY, l.panelX + l.panelWidth, l.panelY + l.panelHeight, borderColor); // Right
        
        // Split Line
        guiGraphics.fill(l.panelX + l.leftPaneWidth, l.panelY + 30, l.panelX + l.leftPaneWidth + 1, l.panelY + l.panelHeight, 0xFF666666);
        
        // Top Header Separator
        guiGraphics.fill(l.panelX, l.panelY + 28, l.panelX + l.panelWidth, l.panelY + 29, 0xFF666666);
        
        // Right Pane Sections Separators
        guiGraphics.fill(l.rightPaneX + 10, l.nowPlayingY - 4, l.panelX + l.panelWidth - 10, l.nowPlayingY - 3, 0xFF444444);
        guiGraphics.fill(l.rightPaneX + 10, l.progressY - 4, l.panelX + l.panelWidth - 10, l.progressY - 3, 0xFF444444);
        guiGraphics.fill(l.rightPaneX + 10, l.utilityY - 6, l.panelX + l.panelWidth - 10, l.utilityY - 5, 0xFF444444);
    }

    private void drawScrollbar(GuiGraphics guiGraphics, UiLayout l, int itemCount, float maxScroll) {
        guiGraphics.fill(l.scrollBarX, l.scrollBarY, l.scrollBarX + l.scrollBarWidth, l.scrollBarY + l.scrollBarHeight, 0x55000000);
        if (maxScroll > 0) {
            int thumbHeight = Math.max(20, (int) ((float) l.listVisibleHeight / (itemCount * ROW_HEIGHT) * l.scrollBarHeight));
            float scrollPct = scrollAmount / maxScroll;
            int thumbY = l.scrollBarY + (int) (scrollPct * (l.scrollBarHeight - thumbHeight));
            
            int color = isDraggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
            guiGraphics.fill(l.scrollBarX + 1, thumbY, l.scrollBarX + l.scrollBarWidth - 1, thumbY + thumbHeight, color);
        }
    }

    private UiLayout buildLayout() {
        int panelWidth = Math.min(MAX_PANEL_WIDTH, Math.max(500, this.width - 20));
        int panelHeight = Math.min(PANEL_HEIGHT, Math.max(240, this.height - 20));
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        int leftPaneWidth = (int) (panelWidth * 0.58f);
        int rightPaneX = panelX + leftPaneWidth;
        int rightPaneWidth = panelWidth - leftPaneWidth;

        int listHeaderY = panelY + 36;
        int listStartY = panelY + 50;
        int listVisibleHeight = panelHeight - 54;
        
        int scrollBarWidth = 6;
        int scrollBarX = panelX + leftPaneWidth - 8 - scrollBarWidth;
        int scrollBarY = listStartY;
        int scrollBarHeight = listVisibleHeight;

        int colCoverX = panelX + 10;
        int colTitleX = panelX + 46;
        int colArtistX = panelX + (int)(leftPaneWidth * 0.45f);
        int colDurationX = scrollBarX - 44;

        int controlsRow1Y = panelY + 38;
        int controlsRow2Y = controlsRow1Y + 24;
        int nowPlayingY = controlsRow2Y + 30;
        int progressY = nowPlayingY + 40;
        int volumeY = progressY + 44;
        int utilityY = volumeY + 28;

        int progressBarX = rightPaneX + 54;
        int progressBarWidth = Math.max(74, rightPaneWidth - 104);
        
        int volumeBarX = rightPaneX + 16;
        int volumeBarWidth = Math.max(60, rightPaneWidth - 66);
        int btnGap = 6;

        return new UiLayout(
            panelX, panelY, panelWidth, panelHeight,
            leftPaneWidth, rightPaneX, rightPaneWidth,
            listHeaderY, listStartY, listVisibleHeight,
            scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight,
            colCoverX, colTitleX, colArtistX, colDurationX,
            controlsRow1Y, controlsRow2Y, nowPlayingY, progressY, volumeY, utilityY,
            progressBarX, progressBarWidth, volumeBarX, volumeBarWidth, btnGap
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
        if (shuffleBtn != null) shuffleBtn.setMessage(toggleStyleLabel("Shuffle", manager.isShuffle()));
        if (repeatBtn != null) repeatBtn.setMessage(toggleStyleLabel("Repeat", manager.isLooping()));
        if (playPauseBtn != null) playPauseBtn.setMessage(playPauseLabel());
        if (overlayBtn != null) overlayBtn.setMessage(overlayLabel());
    }

    private Component playPauseLabel() {
        if (MusicManager.getInstance().isPlaying()) return toggleStyleLabel("Pause", true);
        return toggleStyleLabel("Play", false);
    }

    private Component toggleStyleLabel(String text, boolean active) {
        return Component.literal(text).withStyle(active ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    private Component actionLabel(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }

    private Component overlayLabel() {
        return toggleStyleLabel("Overlay", MusicManager.getInstance().isNowPlayingOverlayEnabled());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
