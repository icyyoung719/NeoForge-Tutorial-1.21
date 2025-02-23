package io.github.icyyoung.tutorialmod.entity.ai;

import io.github.icyyoung.tutorialmod.entity.custom.RhinoEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/23
 */

public class RhinoAttackGoal extends MeleeAttackGoal {
    private final RhinoEntity entity;
    private int attackDelay = 40;
    private int ticksUntilNextAttack = 40;
    private boolean shouldCountTillNextAttack = false;

    public RhinoAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.entity = ((RhinoEntity) mob);
    }

    @Override
    public void start() {
        super.start();
        attackDelay = 40;
        ticksUntilNextAttack = 40;
    }


    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (isEnemyWithinAttackRange(target)) {
            shouldCountTillNextAttack = true;

            if(isTimeToStartAttackAnimation()) {
                entity.setAttacking(true);
            }
            if (isTimeToAttack()) {
                this.mob.getLookControl().setLookAt(target.getX(),target.getY(),target.getZ());
                performAttack(target);
            }
        } else {
            resetAttackCoolDown();
            shouldCountTillNextAttack = false;
            entity.setAttacking(false);
            entity.attackAnimationTimeout = 0;
        }
    }



    private boolean isEnemyWithinAttackRange(LivingEntity enemy) {
        return this.mob.distanceTo(enemy) <= 3f;
    }

    protected void resetAttackCoolDown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(attackDelay*2);
    }

    @Override
    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean isTimeToStartAttackAnimation() {
        return this.ticksUntilNextAttack <= attackDelay;
    }

    @Override
    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected void performAttack(LivingEntity enemy){
        this.resetAttackCooldown();
        this.mob.swing(InteractionHand.MAIN_HAND);
        this.mob.doHurtTarget(enemy);
    }

    @Override
    public void tick() {
        super.tick();
        if (shouldCountTillNextAttack) {
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        }
    }

    @Override
    public void stop() {
        entity.setAttacking(false);
        super.stop();
    }
}
