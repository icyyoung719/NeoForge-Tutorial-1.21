package io.github.icyyoung.tutorialmod.event;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.item.ModItems;
import io.github.icyyoung.tutorialmod.network.payload.SortChestPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

import io.github.icyyoung.tutorialmod.client.minimap.FullScreenMapScreen;
import io.github.icyyoung.tutorialmod.client.minimap.MinimapOverlay;
import io.github.icyyoung.tutorialmod.client.keymapping.ModKeyBindings;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import io.github.icyyoung.tutorialmod.client.minimap.MapDataManager;
import io.github.icyyoung.tutorialmod.client.minimap.MapStorage;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/21
 */

@EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ContainerScreen containerScreen)) {
            return;
        }
        if (!(containerScreen.getMenu() instanceof ChestMenu chestMenu)) {
            return;
        }

        int buttonX = containerScreen.getGuiLeft() + containerScreen.getXSize() - 20;
        int buttonY = containerScreen.getGuiTop() + 4;
        Button sortButton = Button.builder(Component.translatable("button.tutorialmod.sort_chest"),
                        button -> PacketDistributor.sendToServer(new SortChestPayload(chestMenu.containerId)))
                .bounds(buttonX, buttonY, 16, 16)
                .tooltip(Tooltip.create(Component.translatable("tooltip.tutorialmod.sort_chest")))
                .build();
        event.addListener(sortButton);
    }

    @SubscribeEvent
    public static void onComputeFovModifierEvent(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if(player.isUsingItem() && event.getPlayer().getUseItem().getItem() == ModItems.SAPPHIRE_BOW.get()){
            float fovModifier = 1f;
            int ticksUsingItem = event.getPlayer().getTicksUsingItem();
            float deltaTicks = (float)ticksUsingItem / 20f;
            if(deltaTicks > 1f) {
                deltaTicks = 1f;
            } else {
                deltaTicks *= deltaTicks;
            }
            fovModifier *= 1f - deltaTicks * 0.15f;
            event.setNewFovModifier(fovModifier);
        }
    }
    @SubscribeEvent
    public static void onSapphireBowHit(ProjectileImpactEvent evt) {
        Projectile arrow = evt.getProjectile();
        if (arrow.getOwner() instanceof Player player) {
            HitResult rayTraceResult = evt.getRayTraceResult();

            if (rayTraceResult instanceof EntityHitResult result
                    && result.getEntity() instanceof LivingEntity living
                    && arrow.getOwner() != result.getEntity() && !result.getEntity().getType().is(Tags.EntityTypes.BOSSES)) {

                if (arrow.getPersistentData().contains(ModItems.SAPPHIRE_BOW.getRegisteredName())) {
                    double sourceX = player.getX(), sourceY = player.getY(), sourceZ = player.getZ();
                    float sourceYaw = player.getYRot(), sourcePitch = player.getXRot();
                    @Nullable Entity playerVehicle = player.getVehicle();

                    player.setYRot(living.getYRot());
                    player.teleportTo(living.getX(), living.getY(), living.getZ());
                    player.invulnerableTime = 40;
                    player.level().broadcastEntityEvent(player, (byte) 46);
                    if (living.isPassenger() && living.getVehicle() != null) {
                        player.startRiding(living.getVehicle(), true);
                        living.stopRiding();
                    }
                    player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);

                    living.setYRot(sourceYaw);
                    living.setXRot(sourcePitch);
                    living.teleportTo(sourceX, sourceY, sourceZ);
                    living.level().broadcastEntityEvent(player, (byte) 46);
                    if (playerVehicle != null) {
                        living.startRiding(playerVehicle, true);
                        player.stopRiding();
                    }
                    living.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                }
            } else if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) rayTraceResult).getBlockPos();

                if (arrow.getPersistentData().contains(ModItems.SAPPHIRE_BOW.getRegisteredName())) {
                    player.teleportTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
                    player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    player.invulnerableTime = 40;
                    player.level().broadcastEntityEvent(player, (byte) 46);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            // Update explored map state occasionally
            if (mc.player.tickCount % 20 == 0) {
                int centerCX = mc.player.chunkPosition().x;
                int centerCZ = mc.player.chunkPosition().z;
                // Generate immediately nearby chunks just in case
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        MapDataManager.updateChunk(mc.level, centerCX + dx, centerCZ + dz);
                    }
                }
            }

            while (ModKeyBindings.MINIMAP_TOGGLE_KEY.consumeClick()) {
                MinimapOverlay.enabled = !MinimapOverlay.enabled;
            }

            while (ModKeyBindings.FULL_MAP_KEY.consumeClick()) {
                mc.setScreen(new FullScreenMapScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        String id;
        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            id = mc.getSingleplayerServer().getWorldData().getLevelName();
        } else if (mc.getCurrentServer() != null) {
            id = mc.getCurrentServer().ip;
        } else {
            id = "unknown_world";
        }
        MapStorage.setWorld(id);
    }

    @SubscribeEvent
    public static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        MapStorage.save();
    }
}
