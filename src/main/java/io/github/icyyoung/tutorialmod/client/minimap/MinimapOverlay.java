package io.github.icyyoung.tutorialmod.client.minimap;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.DeltaTracker;

public class MinimapOverlay implements LayeredDraw.Layer {
    public static boolean enabled = true;
    public static final int MAP_SIZE = 64;

    private DynamicTexture minimapTexture;
    private ResourceLocation minimapTextureLocation;

    private void initTexture() {
        if (minimapTexture == null) {
            minimapTexture = new DynamicTexture(MAP_SIZE * 2, MAP_SIZE * 2, true);
            minimapTextureLocation = ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "dynamic_minimap");
            Minecraft.getInstance().getTextureManager().register(minimapTextureLocation, minimapTexture);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        initTexture();

        int screenWidth = guiGraphics.guiWidth();
        int mapDiameter = MAP_SIZE * 2;
        int centerX = screenWidth - 10 - MAP_SIZE;
        int centerY = 10 + MAP_SIZE;

        int playerX = (int) player.getX();
        int playerZ = (int) player.getZ();

        NativeImage image = minimapTexture.getPixels();
        if (image != null) {
            // clear background
            for(int i = 0; i < MAP_SIZE * 2; i++) {
                for(int j = 0; j < MAP_SIZE * 2; j++) {
                    image.setPixelRGBA(i, j, 0x88000000);
                }
            }

            int minChunkX = (playerX - MAP_SIZE) >> 4;
            int maxChunkX = (playerX + MAP_SIZE) >> 4;
            int minChunkZ = (playerZ - MAP_SIZE) >> 4;
            int maxChunkZ = (playerZ + MAP_SIZE) >> 4;

            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    int[] chunkData = MapDataManager.getOrGenerateChunkData(mc.level, cx, cz);
                    if (chunkData != null) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                int worldX = (cx << 4) + x;
                                int worldZ = (cz << 4) + z;

                                int screenX = worldX - playerX + MAP_SIZE;
                                int screenZ = worldZ - playerZ + MAP_SIZE;

                                if (screenX >= 0 && screenX < MAP_SIZE * 2 && screenZ >= 0 && screenZ < MAP_SIZE * 2) {        
                                    int argb = chunkData[x + (z * 16)];
                                    if (argb != 0) {
                                        image.setPixelRGBA(screenX, screenZ, argb);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            minimapTexture.upload();
        }

        // Draw Map border
        guiGraphics.fill(centerX - MAP_SIZE - 2, centerY - MAP_SIZE - 2, centerX + MAP_SIZE + 2, centerY - MAP_SIZE, 0xFF000000);
        guiGraphics.fill(centerX - MAP_SIZE - 2, centerY + MAP_SIZE, centerX + MAP_SIZE + 2, centerY + MAP_SIZE + 2, 0xFF000000);
        guiGraphics.fill(centerX - MAP_SIZE - 2, centerY - MAP_SIZE, centerX - MAP_SIZE, centerY + MAP_SIZE, 0xFF000000);      
        guiGraphics.fill(centerX + MAP_SIZE, centerY - MAP_SIZE, centerX + MAP_SIZE + 2, centerY + MAP_SIZE, 0xFF000000);      

        guiGraphics.blit(minimapTextureLocation, centerX - MAP_SIZE, centerY - MAP_SIZE, 0, 0, mapDiameter, mapDiameter, mapDiameter, mapDiameter);

        String currentDim = mc.level.dimension().location().toString();

        // Draw waypoints
        for (Waypoint wp : MapStorage.waypoints) {
            if (!currentDim.equals(wp.dimension)) continue;
            float dx = wp.x - playerX;
            float dz = wp.z - playerZ;
            if (Math.abs(dx) < MAP_SIZE && Math.abs(dz) < MAP_SIZE) {
                guiGraphics.fill(centerX + (int)dx - 1, centerY + (int)dz - 1,
                                 centerX + (int)dx + 2, centerY + (int)dz + 2, wp.color);
            }
        }

        // Draw Player Arrow
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(player.getYRot() - 180f));

        int arrowColor = 0xFF00FFFF; // Cyan arrow to contrast well with map
        guiGraphics.fill(-1, -4, 2, -3, arrowColor);
        guiGraphics.fill(-2, -3, 3, -2, arrowColor);
        guiGraphics.fill(-3, -2, 4, -1, arrowColor);
        guiGraphics.fill(-1, -1, 2, 3, arrowColor);

        pose.popPose();
    }
}
