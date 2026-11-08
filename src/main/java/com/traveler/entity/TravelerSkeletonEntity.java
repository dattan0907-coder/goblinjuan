package com.traveler.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class TravelerSkeletonEntity extends Monster implements GeoEntity {
    // Animation names, taken from dead_skeleton.animation.json
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.traveler_skeleton.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.traveler_skeleton.walk");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenLoop("animation.traveler_skeleton.charge");
    private static final RawAnimation BLOCK = RawAnimation.begin()
            .thenPlay("animation.traveler_skeleton.begin_block")
            .thenLoop("animation.traveler_skeleton.block");
    private static final RawAnimation BLOCK_STOP = RawAnimation.begin().thenPlay("animation.traveler_skeleton.block_stop");
    private static final RawAnimation BLOCK_DESTROY = RawAnimation.begin().thenPlay("animation.traveler_skeleton.block_destroy");
    private static final RawAnimation SLASH = RawAnimation.begin().thenPlay("animation.traveler_skeleton.slash");

    private static final EntityDataAccessor<Boolean> BLOCKING =
            SynchedEntityData.defineId(TravelerSkeletonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int MAX_BLOCK_HITS = 5;
    private static final int STUN_TICKS = 35; // ~ block_destroy length (1.75s)

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int blockHits;
    private int shieldCooldown;
    private int stunTicks;

    public TravelerSkeletonEntity(EntityType<? extends TravelerSkeletonEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLOCKING, false);
    }

    public boolean isBlocking() {
        return this.entityData.get(BLOCKING);
    }

    public void setBlocking(boolean blocking) {
        this.entityData.set(BLOCKING, blocking);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BlockGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.shieldCooldown > 0) this.shieldCooldown--;
            if (this.stunTicks > 0) this.stunTicks--;
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.stunTicks > 0;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !this.level().isClientSide) {
            this.triggerAnim("action", "slash");
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.isBlocking()
                && !source.is(DamageTypeTags.BYPASSES_SHIELD) && this.isAttackFromFront(source)) {
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            this.blockHits++;
            if (this.blockHits >= MAX_BLOCK_HITS) {
                this.breakShield();
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    private boolean isAttackFromFront(DamageSource source) {
        Vec3 pos = source.getSourcePosition();
        if (pos == null) return false;
        Vec3 look = this.getViewVector(1.0F);
        Vec3 toMe = pos.vectorTo(this.position());
        toMe = new Vec3(toMe.x, 0.0, toMe.z).normalize();
        return toMe.dot(look) < 0.0;
    }

    private void breakShield() {
        this.setBlocking(false);
        this.blockHits = 0;
        this.shieldCooldown = 200;
        this.stunTicks = STUN_TICKS;
        this.getNavigation().stop();
        this.playSound(SoundEvents.SHIELD_BREAK, 1.0F, 1.0F);
        this.triggerAnim("action", "block_destroy");
    }

    // ---- sounds ----
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SKELETON_STEP, 0.15F, 1.0F);
    }

    // ---- GeckoLib ----
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Looping base layer: idle / walk / charge / block
        controllers.add(new AnimationController<>(this, "main", 5, this::mainPredicate));
        // One-shot layer, started with triggerAnim(...)
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("slash", SLASH)
                .triggerableAnim("block_stop", BLOCK_STOP)
                .triggerableAnim("block_destroy", BLOCK_DESTROY));
    }

    private PlayState mainPredicate(AnimationState<TravelerSkeletonEntity> state) {
        if (this.isBlocking()) {
            return state.setAndContinue(BLOCK);
        }
        if (state.isMoving()) {
            return state.setAndContinue(this.isAggressive() ? CHARGE : WALK);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ---- AI goal: raise buckler when a target is close ----
    private static class BlockGoal extends Goal {
        private final TravelerSkeletonEntity mob;
        private int ticksLeft;

        BlockGoal(TravelerSkeletonEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive()
                    && mob.shieldCooldown <= 0
                    && mob.stunTicks <= 0
                    && mob.distanceToSqr(target) <= 36.0
                    && mob.getRandom().nextInt(40) == 0;
        }

        @Override
        public void start() {
            mob.blockHits = 0;
            mob.setBlocking(true);
            mob.getNavigation().stop();
            ticksLeft = 40 + mob.getRandom().nextInt(40);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mob.getTarget();
            return mob.isBlocking() && ticksLeft > 0 && target != null && target.isAlive();
        }

        @Override
        public void tick() {
            ticksLeft--;
            LivingEntity target = mob.getTarget();
            if (target != null) {
                mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }

        @Override
        public void stop() {
            if (mob.isBlocking()) {
                mob.setBlocking(false);
                mob.shieldCooldown = 60;
                mob.triggerAnim("action", "block_stop");
            }
        }
    }
}
