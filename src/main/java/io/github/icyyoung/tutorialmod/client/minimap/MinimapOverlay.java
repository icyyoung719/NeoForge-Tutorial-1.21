package io.github.icyyoung.tutorialmod.client.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.DeltaTracker;

public class MinimapOverlay implements LayeredDraw.Layer {
    public static boolean enabled = true;
    public static final int MAP_SIZE = 64; // Radius in pixels (128x128 map)
    
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        
        int mapPixelRadius = MAP_SIZE;
        int mapDiameter = mapPixelRadius * 2;
        int rightMargin = 10;
        int topMargin = 10;
        
        int centerX = screenWidth - rightMargin - mapPixelRadius;
        int centerY = topMargin + mapPixelRadius;

        // Draw background
        guiGraphics.fill(centerX - mapPixelRadius - 2, centerY - mapPixelRadius - 2, 
                         centerX + mapPixelRadius + 2, centerY + mapPixelRadius + 2, 0x88000000);

        int playerX = (int) player.getX();
        int playerZ = (int) player.getZ();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        
        // Render map pixels
        for (int x = -mapPixelRadius; x < mapPixelRadius; x++) {
            for (int z = -mapPixelRadius; z < mapPixelRadius; z++) {
                int worldX = playerX + x;
                int worldZ = playerZ + z;
                
                int chunkX = worldX >> 4;
                int chunkZ = worldZ >> 4;
                
                byte[] chunkData = MapDataManager.getOrGenerateChunkData(mc.level, chunkX, chunkZ);
                if (chunkData != null) {
                    int localX = worldX & 15;
                    int localZ = worldZ & 15;
                    byte packed = chunkData[localX + (localZ * 16)];
                    if (packed != 0) {
                        int argb = MapColor.getColorFromPackedId(packed & 0xFF);
                        guiGraphics.fill(centerX + x, centerY + z, centerX + x + 1, centerY + z + 1, argb | 0xFF000000);
                    }
                }
            }
        }
        
        // Render waypoints
        for (Waypoint wp : MapStorage.waypoints) {
            float dx = wp.x - playerX;
            float dz = wp.z - playerZ;
            if (Math.abs(dx) < mapPixelRadius && Math.abs(dz) < mapPixelRadius) {
                guiGraphics.fill(centerX + (int)dx - 1, centerY + (int)dz - 1, 
                                 centerX + (int)dx + 2, centerY + (int)dz + 2, wp.color);
            }
        }

        // Render player (simple triangle or dot)
        guiGraphics.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, 0xFFFF0000);
        
        pose.popPose();
    }
}