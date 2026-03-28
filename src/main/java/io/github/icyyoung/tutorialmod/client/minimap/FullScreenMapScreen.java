package io.github.icyyoung.tutorialmod.client.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.MapColor;
import java.util.Map;

public class FullScreenMapScreen extends Screen {
    private double offsetX;
    private double offsetZ;
    private double zoom = 1.0;
    private boolean isDragging = false;
    private double lastMouseX, lastMouseY;

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        guiGraphics.fill(0, 0, this.width, this.height, 0xDD000000);
        
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        
        // Optimize rendering by finding visible bounds
        int viewWidth = (int) (this.width / zoom);
        int viewHeight = (int) (this.height / zoom);
        
        int minX = (int) (offsetX - viewWidth / 2);
        int maxX = (int) (offsetX + viewWidth / 2);
        int minZ = (int) (offsetZ - viewHeight / 2);
        int maxZ = (int) (offsetZ + viewHeight / 2);

        // Snap to chunks
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                int[] data = MapDataManager.getChunkData(chunkX, chunkZ);
                if (data != null) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int colorCol = data[x + (z * 16)];
                            if (colorCol != 0) {
                                int worldX = (chunkX << 4) + x;
                                int worldZ = (chunkZ << 4) + z;
                                
                                int screenX = (int) (centerX + (worldX - offsetX) * zoom);
                                int screenY = (int) (centerY + (worldZ - offsetZ) * zoom);
                                
                                int argb = 0xFF000000 | colorCol;
                                guiGraphics.fill(screenX, screenY, (int)(screenX + Math.ceil(zoom)), (int)(screenY + Math.ceil(zoom)), argb);
                            }
                        }
                    }
                }
            }
        }
        
        // Render Waypoints
        for (Waypoint wp : MapStorage.waypoints) {
            int screenX = (int) (centerX + (wp.x - offsetX) * zoom);
            int screenY = (int) (centerY + (wp.z - offsetZ) * zoom);
            guiGraphics.fill(screenX - 2, screenY - 2, screenX + 2, screenY + 2, wp.color);
            guiGraphics.drawString(this.font, wp.name, screenX + 4, screenY - 4, 0xFFFFFF);
        }
        
        // Render Player
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int px = (int) (centerX + (mc.player.getX() - offsetX) * zoom);
            int py = (int) (centerY + (mc.player.getZ() - offsetZ) * zoom);
            
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(px, py, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(mc.player.getYRot() - 180f)); 
            
            int arrowColor = 0xFF00FFFF; 
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
        // Just a simple way to add waypoint for now (Insert key)
        if (keyCode == 260) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                MapStorage.waypoints.add(new Waypoint("WP " + (MapStorage.waypoints.size() + 1), (int)mc.player.getX(), (int)mc.player.getZ(), 0xFF00FF00));
                MapStorage.save();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}