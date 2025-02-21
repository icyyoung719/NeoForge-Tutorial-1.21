package io.github.icyyoung.tutorialmod.event;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

import javax.annotation.Nullable;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/21
 */

@EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ModClientEvents {
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
}
