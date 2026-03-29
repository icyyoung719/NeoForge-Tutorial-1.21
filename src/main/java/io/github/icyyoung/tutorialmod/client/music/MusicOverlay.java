package io.github.icyyoung.tutorialmod.client.music;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import net.minecraft.client.DeltaTracker;

public class MusicOverlay implements LayeredDraw.Layer {
    private static final Map<String, ResourceLocation> textureCache = new HashMap<>();

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        MusicManager manager = MusicManager.getInstance();
        if (!manager.isNowPlayingOverlayEnabled()) {
            return;
        }

        MusicTrack track = manager.getCurrentTrack();
        if (track != null && manager.isPlaying()) {
            int windowHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            
            int boxWidth = 186;
            int boxHeight = 46;
            int x = 10;
            int y = windowHeight - boxHeight - 10;
            
            guiGraphics.fill(x, y, x + boxWidth, y + boxHeight, 0x88000000);
            
            if (track.getCoverArt() != null) {
                ResourceLocation textureLocation = getOrCreateCoverTexture(track);
                if (textureLocation != null) {
                    guiGraphics.blit(textureLocation, x + 4, y + 4, 0, 0, 32, 32, 32, 32);
                } else {
                    guiGraphics.fill(x + 4, y + 4, x + 36, y + 36, 0xFF444444);
                }
            } else {
                guiGraphics.fill(x + 4, y + 4, x + 36, y + 36, 0xFF444444);
            }
            
            String title = clipToWidth(track.getTitle(), 136);
            String artist = clipToWidth(track.getArtist(), 136);
            
            guiGraphics.drawString(Minecraft.getInstance().font, title, x + 40, y + 7, 0xFFFFFF, true);
            guiGraphics.drawString(Minecraft.getInstance().font, artist, x + 40, y + 22, 0xCFCFCF, true);
        }
    }

    private String clipToWidth(String text, int maxWidth) {
        if (text == null || text.isBlank()) {
            return "Unknown";
        }

        String stripped = text.strip();
        String clipped = Minecraft.getInstance().font.plainSubstrByWidth(stripped, maxWidth);
        if (clipped.length() < stripped.length()) {
            return clipped + "...";
        }
        return clipped;
    }

    private ResourceLocation getOrCreateCoverTexture(MusicTrack track) {
        String key = track.getFileName();
        if (textureCache.containsKey(key)) {
            return textureCache.get(key);
        }

        ResourceLocation texLoc = null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(track.getCoverArt())) {
            BufferedImage bImage = ImageIO.read(in);
            if (bImage != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                boolean converted = ImageIO.write(bImage, "png", out);
                byte[] pngBytes = out.toByteArray();
                if (converted && pngBytes.length > 8) {
                    try (ByteArrayInputStream pngIn = new ByteArrayInputStream(pngBytes)) {
                        NativeImage image = NativeImage.read(pngIn);
                        DynamicTexture texture = new DynamicTexture(image);
                        String textureId = "music_cover_" + key.hashCode();
                        texLoc = ResourceLocation.fromNamespaceAndPath("tutorialmod", textureId.replace('-', 'n'));
                        Minecraft.getInstance().getTextureManager().register(texLoc, texture);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // Cache both success and failure to avoid repeated decode attempts every frame.
        textureCache.put(key, texLoc);
        return texLoc;
    }
}