package io.github.icyyoung.tutorialmod.client.minimap;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FullScreenMapScreen extends Screen {
    private double offsetX;
    private double offsetZ;
    private double zoom = 1.0;
    private boolean isDragging = false;
    private double lastMouseX, lastMouseY;

    private static class RegionTexture {
        DynamicTexture texture;
        ResourceLocation location;
        boolean dirty = true;
    }

    // Cache of Region Textures (1 Region = 16x16 chunks = 256x256 blocks)
    private final Map<Long, RegionTexture> regionTextures = new HashMap<>();

    public FullScreenMapScreen() {
        super(Component.literal("Full Screen Map"));
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            this.offsetX = mc.player.getX();
            this.offsetZ = mc.player.getZ();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        // Free textures
        Minecraft mc = Minecraft.getInstance();
        for (RegionTexture rt : regionTextures.values()) {
            if (rt.texture != null) {
                rt.texture.close();
            }
            if (rt.location != null) {
                mc.getTextureManager().release(rt.location);
            }
        }
        regionTextures.clear();
    }

    private RegionTexture getRegionTexture(int regionX, int regionZ) {
        long key = ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
        return regionTextures.computeIfAbsent(key, k -> {
            RegionTexture rt = new RegionTexture();
            rt.texture = new DynamicTexture(256, 256, true);
            rt.location = ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "map_region_" + regionX + "_" + regionZ);
            Minecraft.getInstance().getTextureManager().register(rt.location, rt.texture);
            return rt;
        });
    }

    private void updateRegionTexture(RegionTexture rt, int regionX, int regionZ) {
        NativeImage image = rt.texture.getPixels();
        if (image == null) return;
        Minecraft mc = Minecraft.getInstance();

        boolean hasAnyData = false;
        // clear local
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                image.setPixelRGBA(i, j, 0); // transparent
            }
        }

        int startChunkX = regionX << 4;
        int startChunkZ = regionZ << 4;

        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {
                int currChunkX = startChunkX + cx;
                int currChunkZ = startChunkZ + cz;
                int[] data = MapDataManager.getChunkData(mc.level, currChunkX, currChunkZ);
                if (data != null) {
                    hasAnyData = true;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int argb = data[x + (z * 16)];
                            if (argb != 0) {
                                image.setPixelRGBA((cx << 4) + x, (cz << 4) + z, argb);
                            }
                        }
                    }
                }
            }
        }

        if (hasAnyData) {
            rt.texture.upload();
        }
        rt.dirty = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        guiGraphics.fill(0, 0, this.width, this.height, 0xDD000000);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        int viewWidth = (int) (this.width / zoom);
        int viewHeight = (int) (this.height / zoom);

        int minX = (int) (offsetX - viewWidth / 2);
        int maxX = (int) (offsetX + viewWidth / 2);
        int minZ = (int) (offsetZ - viewHeight / 2);
        int maxZ = (int) (offsetZ + viewHeight / 2);

        int minRegionX = (minX >> 8) - 1;
        int maxRegionX = (maxX >> 8) + 1;
        int minRegionZ = (minZ >> 8) - 1;
        int maxRegionZ = (maxZ >> 8) + 1;

        for (int rx = minRegionX; rx <= maxRegionX; rx++) {
            for (int rz = minRegionZ; rz <= maxRegionZ; rz++) {
                RegionTexture rt = getRegionTexture(rx, rz);
                if (rt.dirty) {
                    updateRegionTexture(rt, rx, rz);
                }
                
                int worldX = rx << 8;
                int worldZ = rz << 8;

                int screenX = (int) (centerX + (worldX - offsetX) * zoom);
                int screenY = (int) (centerY + (worldZ - offsetZ) * zoom);
                int size = (int) Math.ceil(256 * zoom);

                // Use blit with texture bounds matching the desired size
                if (size > 0 && screenX + size > 0 && screenY + size > 0 && screenX < this.width && screenY < this.height) {
                    guiGraphics.blit(rt.location, screenX, screenY, 0, 0, size, size, size, size);
                }
            }
        }

        Minecraft mc = Minecraft.getInstance();
        String currentDim = mc.level != null ? mc.level.dimension().location().toString() : "";

        // Render Waypoints
        for (Waypoint wp : MapStorage.waypoints) {
            if (!currentDim.equals(wp.dimension)) continue;
            int screenX = (int) (centerX + (wp.x - offsetX) * zoom);
            int screenY = (int) (centerY + (wp.z - offsetZ) * zoom);
            guiGraphics.fill(screenX - 2, screenY - 2, screenX + 2, screenY + 2, wp.color);
            guiGraphics.drawCenteredString(this.font, wp.name, screenX, screenY - 12, 0xFFFFFF);
        }

        // Render Player
        if (mc.player != null) {
            int px = (int) (centerX + (mc.player.getX() - offsetX) * zoom);
            int py = (int) (centerY + (mc.player.getZ() - offsetZ) * zoom);

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(px, py, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(mc.player.getYRot() - 180f));

            int arrowColor = 0xFF00FFFF; // Cyan arrow
            guiGraphics.fill(-1, -4, 2, -3, arrowColor);
            guiGraphics.fill(-2, -3, 3, -2, arrowColor);
            guiGraphics.fill(-3, -2, 4, -1, arrowColor);
            guiGraphics.fill(-1, -1, 2, 3, arrowColor);

            poseStack.popPose();
        }

        pose.popPose();

        guiGraphics.drawCenteredString(this.font, "Press M to close. Click + Drag to pan. Scroll to zoom. Insert to Add Waypoint.", centerX, 10, 0xFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            zoom *= 1.2;
        } else if (scrollY < 0) {
            zoom /= 1.2;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            offsetX -= (mouseX - lastMouseX) / zoom;
            offsetZ -= (mouseY - lastMouseY) / zoom;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_M) {
            this.onClose();
            return true;
        }
        if (keyCode == 260) { // Insert key
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                MapStorage.waypoints.add(new Waypoint(
                    "WP " + (MapStorage.waypoints.size() + 1), 
                    (int)mc.player.getX(), 
                    (int)mc.player.getZ(), 
                    0xFF00FF00,
                    mc.level.dimension().location().toString()
                ));
                MapStorage.save();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
