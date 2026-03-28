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
import net.minecraft.util.Mth;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicPlayerScreen extends Screen {
    private static final int LIST_START_Y = 30;
    private static final int TITLE_Y = 10;
    private static final int VOLUME_BAR_WIDTH = 130;
    private static final int VOLUME_BAR_X_OFFSET = -30;
    private static final float VOLUME_VALUE_STEP = 0.05f;
    private static final int COVER_SIZE = 64;

    private static final Map<String, ResourceLocation> screenTextureCache = new HashMap<>();

    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 5;
    private Button playPauseBtn;
    private Button loopBtn;
    private Button shuffleBtn;
    private float volumeSliderValue;
    private boolean isDraggingVolume = false;

    public MusicPlayerScreen() {
        super(Component.literal("Custom Music Player"));
        volumeSliderValue = MusicManager.getInstance().getVolume();
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = LIST_START_Y;
        List<MusicTrack> playlist = MusicManager.getInstance().getPlaylist();
        int maxPage = Math.max(0, (playlist.size() - 1) / ITEMS_PER_PAGE);
        
        // Track list pagination: only show when there are multiple pages.
        if (maxPage > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                if (currentPage > 0) currentPage--;
                this.rebuildWidgets();
            }).bounds(centerX - 100, startY + ITEMS_PER_PAGE * 20 + 5, 20, 20).build());
            
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                if (currentPage < maxPage) currentPage++;
                this.rebuildWidgets();
            }).bounds(centerX + 80, startY + ITEMS_PER_PAGE * 20 + 5, 20, 20).build());
        }

        // Playback Controls
        int ctrlY = startY + ITEMS_PER_PAGE * 20 + 35;
        this.addRenderableWidget(Button.builder(Component.literal("Prev"), b -> {
            MusicManager.getInstance().playPrev();
            playPauseBtn.setMessage(Component.literal("Stop"));
        }).bounds(centerX - 80, ctrlY, 40, 20).build());
        
        playPauseBtn = this.addRenderableWidget(Button.builder(Component.literal(MusicManager.getInstance().isPlaying() ? "Stop" : "Play"), b -> {
            if (MusicManager.getInstance().isPlaying()) {
                MusicManager.getInstance().stop();
                b.setMessage(Component.literal("Play"));
            } else {
                int resumeIndex = MusicManager.getInstance().getCurrentTrackIndex();
                if (resumeIndex < 0) {
                    resumeIndex = 0;
                }
                MusicManager.getInstance().playTrack(resumeIndex);
                b.setMessage(Component.literal("Stop"));
            }
        }).bounds(centerX - 30, ctrlY, 60, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.literal("Next"), b -> {
            MusicManager.getInstance().playNext();
            playPauseBtn.setMessage(Component.literal("Stop"));
        }).bounds(centerX + 40, ctrlY, 40, 20).build());
        
        // Loop and Shuffle
        int optY = ctrlY + 25;
        loopBtn = this.addRenderableWidget(Button.builder(Component.literal("Loop: " + (MusicManager.getInstance().isLooping() ? "ON" : "OFF")), b -> {
            boolean newLoop = !MusicManager.getInstance().isLooping();
            MusicManager.getInstance().setLooping(newLoop);
            b.setMessage(Component.literal("Loop: " + (newLoop ? "ON" : "OFF")));
        }).bounds(centerX - 100, optY, 60, 20).build());
        
        shuffleBtn = this.addRenderableWidget(Button.builder(Component.literal("Shuffle: " + (MusicManager.getInstance().isShuffle() ? "ON" : "OFF")), b -> {
            MusicManager.getInstance().toggleShuffle();
            b.setMessage(Component.literal("Shuffle: " + (MusicManager.getInstance().isShuffle() ? "ON" : "OFF")));
            this.currentPage = 0;
            this.rebuildWidgets();
        }).bounds(centerX - 30, optY, 70, 20).build());
        
        int volY = getVolumeY();
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            float nextVolume = Mth.clamp(volumeSliderValue - VOLUME_VALUE_STEP, 0.0f, 1.0f);
            setVolume(nextVolume);
        }).bounds(centerX - 60, volY - 3, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            float nextVolume = Mth.clamp(volumeSliderValue + VOLUME_VALUE_STEP, 0.0f, 1.0f);
            setVolume(nextVolume);
        }).bounds(centerX + 105, volY - 3, 20, 20).build());
        
        // Track list buttons
        int startIndex = currentPage * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int trackIndex = startIndex + i;
            if (trackIndex < playlist.size()) {
                MusicTrack track = playlist.get(trackIndex);
                int fi = trackIndex;
                this.addRenderableWidget(Button.builder(Component.literal("Play"), b -> {
                    MusicManager.getInstance().playTrack(fi);
                    playPauseBtn.setMessage(Component.literal("Stop"));
                }).bounds(centerX + 110, startY + i * 20, 40, 20).build());
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int centerX = this.width / 2;
        int startY = LIST_START_Y;
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
        guiGraphics.drawCenteredString(font, this.title, centerX, TITLE_Y, 0xFFFFFF);
        
        List<MusicTrack> playlist = MusicManager.getInstance().getPlaylist();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int trackIndex = startIndex + i;
            if (trackIndex < playlist.size()) {
                MusicTrack track = playlist.get(trackIndex);
                boolean isPlaying = MusicManager.getInstance().getCurrentTrack() == track;
                int color = isPlaying ? 0x00FF00 : 0xFFFFFF;
                String displayTitle = (trackIndex + 1) + ". " + clipToWidth(track.getTitle(), 162);
                guiGraphics.drawString(font, displayTitle, centerX - 100, startY + i * 20 + 6, color, true);
            }
        }
        
        int volY = getVolumeY();
        guiGraphics.drawString(font, "Volume: " + (int)(volumeSliderValue * 100) + "%", centerX - 100, volY, 0xFFFFFF);
        int sliderStartX = centerX + VOLUME_BAR_X_OFFSET;
        int sliderEndX = sliderStartX + VOLUME_BAR_WIDTH;
        guiGraphics.fill(sliderStartX, volY + 4, sliderEndX, volY + 6, 0xFF555555);
        
        int sliderX = (int)(sliderStartX + volumeSliderValue * VOLUME_BAR_WIDTH);
        guiGraphics.fill(sliderX - 2, volY, sliderX + 2, volY + 10, 0xFFFFFFFF);

        renderNowPlayingPanel(guiGraphics);
        
        guiGraphics.pose().popPose();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int volY = getVolumeY();
        int sliderStartX = centerX + VOLUME_BAR_X_OFFSET;
        int sliderEndX = sliderStartX + VOLUME_BAR_WIDTH;
        if (mouseY >= volY - 5 && mouseY <= volY + 15 && mouseX >= sliderStartX && mouseX <= sliderEndX) {
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
        int centerX = this.width / 2;
        int sliderStartX = centerX + VOLUME_BAR_X_OFFSET;
        float pct = (float) (mouseX - sliderStartX) / (float) VOLUME_BAR_WIDTH;
        setVolume(Mth.clamp(pct, 0.0f, 1.0f));
    }

    private void setVolume(float newVolume) {
        volumeSliderValue = newVolume;
        MusicManager.getInstance().setVolume(newVolume);
    }

    private int getVolumeY() {
        return LIST_START_Y + ITEMS_PER_PAGE * 20 + 75;
    }

    private void renderNowPlayingPanel(GuiGraphics guiGraphics) {
        MusicTrack currentTrack = MusicManager.getInstance().getCurrentTrack();
        if (currentTrack == null) {
            return;
        }

        int panelX = 20;
        int panelY = 40;
        int panelWidth = 210;
        int panelHeight = 92;
        guiGraphics.fill(panelX - 6, panelY - 6, panelX - 6 + panelWidth, panelY - 6 + panelHeight, 0x66000000);

        ResourceLocation coverTexture = getOrCreateCoverTexture(currentTrack);
        if (coverTexture != null) {
            guiGraphics.blit(coverTexture, panelX, panelY, 0, 0, COVER_SIZE, COVER_SIZE, COVER_SIZE, COVER_SIZE);
        } else {
            guiGraphics.fill(panelX, panelY, panelX + COVER_SIZE, panelY + COVER_SIZE, 0xFF444444);
        }

        String title = clipToWidth(currentTrack.getTitle(), 126);
        String artist = clipToWidth(currentTrack.getArtist(), 126);
        String album = clipToWidth(currentTrack.getAlbum(), 126);

        guiGraphics.drawString(font, "Now Playing", panelX + COVER_SIZE + 8, panelY, 0xFFDFAF, true);
        guiGraphics.drawString(font, "Title: " + title, panelX + COVER_SIZE + 8, panelY + 14, 0xFFFFFF, true);
        guiGraphics.drawString(font, "Artist: " + artist, panelX + COVER_SIZE + 8, panelY + 28, 0xCFCFCF, true);
        guiGraphics.drawString(font, "Album: " + album, panelX + COVER_SIZE + 8, panelY + 42, 0xAAAAAA, true);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
