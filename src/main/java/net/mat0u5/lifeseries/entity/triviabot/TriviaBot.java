package net.mat0u5.lifeseries.entity.triviabot;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.api.Animator;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.AnimationHandler;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.entity.triviabot.goal.TriviaBotGlideGoal;
import net.mat0u5.lifeseries.entity.triviabot.goal.TriviaBotTeleportGoal;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.TriviaBots;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.mat0u5.lifeseries.Main.server;

public class TriviaBot extends AmbientEntity implements AnimatedEntity {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "triviabot");
    public static final Model MODEL = BbModelLoader.load(ID);
    public static final ChunkTicketType<ChunkPos> BOT_TICKET = ChunkTicketType.create("triviabot", Comparator.comparingLong(ChunkPos::toLong), 100);

    public static final float MOVEMENT_SPEED = 0.45f;
    public static final int MAX_DISTANCE = 100;
    public static boolean CAN_START_RIDING = true;

    public boolean gliding = false;

    public boolean interactedWith = false;
    public long interactedAt = 0;
    public int timeToComplete = 0;
    public int difficulty = 0;
    public boolean submittedAnswer = false;
    public Boolean answeredRight = null;
    public boolean ranOutOfTime = false;
    public int snailTransformation = 0;

    public int nullPlayerChecks = 0;
    private long chunkTicketExpiryTicks = 0L;

    private final EntityHolder<TriviaBot> holder;
    public UUID boundPlayerUUID;

    public TriviaBot(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(holder, this);
        setInvulnerable(true);
        setPersistent();
        updateNavigation();
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return holder;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (boundPlayerUUID == null) return;
        nbt.putUuid("boundPlayer", boundPlayerUUID);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        UUID newUUID = nbt.getUuid("boundPlayer");
        if (newUUID != null) {
            boundPlayerUUID = newUUID;
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        //? if <= 1.21 {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10000)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100)
                .add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, 1)
                .add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 100)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0);
        //?} else {
        /*return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10000)
                .add(EntityAttributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.FLYING_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.STEP_HEIGHT, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 100)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 1)
                .add(EntityAttributes.SAFE_FALL_DISTANCE, 100)
                .add(EntityAttributes.ATTACK_DAMAGE, 0);
        *///?}
    }

    @Nullable
    public ServerPlayerEntity getBoundPlayer() {
        if (server == null) return null;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(boundPlayerUUID);
        if (player == null || player.isSpectator() && player.isDead()) {
            nullPlayerChecks++;
            return null;
        }
        nullPlayerChecks = 0;
        if (player.isSpectator()) return null;
        if (player.isDead()) return null;
        return player;
    }

    public void setBoundPlayer(ServerPlayerEntity player) {
        if (player == null) return;
        boundPlayerUUID = player.getUuid();
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new TriviaBotTeleportGoal(this));
        goalSelector.add(1, new TriviaBotGlideGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (age % 100 == 0) {
            if (!TriviaBots.bots.containsValue(this) || !WildcardManager.isActiveWildcard(Wildcards.TRIVIA_BOT)) {
                despawn();
            }
        }

        if (submittedAnswer && answeredRight != null) {
            if (answeredRight) {
                if (analyzing < -80) {
                    noClip = true;
                    float velocity = Math.min(0.5f, 0.25f * Math.abs((analyzing+80) / (20.0f)));
                    setVelocity(0,velocity,0);
                    if (analyzing < -200) despawn();
                }
            }
            else {
                if (analyzing < -100) {
                    noClip = true;
                    float velocity = Math.min(0.5f, 0.25f * Math.abs((analyzing+100) / (20.0f)));
                    setVelocity(0,velocity,0);
                    if (analyzing < -200) despawn();
                }
            }
        }
        else {
            handleHighVelocity();
            if (!interactedWith) {
                ServerPlayerEntity boundPlayer = getBoundPlayer();
                if (boundPlayer != null) {
                    if (age % 5 == 0) {
                        updateNavigationTarget();
                    }
                }
            }
            if (interactedWith && getRemainingTime() <= 0) {
                ranOutOfTime = true;
            }
            if (snailTransformation > 33) {
                transformIntoSnail();
            }
        }

        if (nullPlayerChecks > 1000) {
            despawn();
        }

        if (age % 2 == 0) {
            updateAnimations();
        }


        chunkLoading();
        clearStatusEffects();
        playSounds();
    }

    public void handleHighVelocity() {
        Vec3d velocity = getVelocity();
        if (velocity.y > 0.15) {
            setVelocity(velocity.x,0.15,velocity.z);
        }
        else if (velocity.y < -0.15) {
            setVelocity(velocity.x,-0.15,velocity.z);
        }
    }

    public void chunkLoading() {
        if (getWorld() instanceof ServerWorld world) {
            int i = ChunkSectionPos.getSectionCoordFloored(this.getPos().getX());
            int j = ChunkSectionPos.getSectionCoordFloored(this.getPos().getZ());
            BlockPos blockPos = BlockPos.ofFloored(this.getPos());
            ChunkPos chunkPos = this.getChunkPos();
            if ((--this.chunkTicketExpiryTicks <= 0L || i != ChunkSectionPos.getSectionCoord(blockPos.getX()) || j != ChunkSectionPos.getSectionCoord(blockPos.getZ()))) {
                world.resetIdleTimeout();
                this.chunkTicketExpiryTicks = addTicket(world, chunkPos) - 5L;
            }
        }
    }

    public static long addTicket(ServerWorld world, ChunkPos chunkPos) {
        world.getChunkManager().addTicket(BOT_TICKET, chunkPos, 2, chunkPos);
        return BOT_TICKET.getExpiryTicks();
    }

    public void despawn() {
        if (boundPlayerUUID != null) {
            TriviaBots.bots.remove(boundPlayerUUID);
        }
        //? if <= 1.21 {
        this.kill();
        //?} else {
        /*this.kill((ServerWorld) getWorld());
         *///?}
        this.discard();
    }

    public void transformIntoSnail() {
        if (getBoundPlayer() != null) {
            Snail triviaSnail = MobRegistry.SNAIL.spawn((ServerWorld) getWorld(), this.getBlockPos(), SpawnReason.COMMAND);
            if (triviaSnail != null) {
                triviaSnail.setBoundPlayer(getBoundPlayer());
                triviaSnail.setFromTrivia();
                triviaSnail.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.5f, 2);
                ServerWorld world = (ServerWorld) triviaSnail.getWorld();
                world.spawnParticles(
                        ParticleTypes.EXPLOSION,
                        this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                        10, 0.5, 0.5, 0.5, 0.5
                );
            }
        }
        despawn();
    }

    private int analyzing = -1;
    public void updateAnimations() {
        AnimationHandler.updateHurtVariant(this, holder);
        Animator animator = holder.getAnimator();
        if (ranOutOfTime) {
            if (snailTransformation == 0) {
                pauseAllAnimations("snail_transform");
                animator.playAnimation("snail_transform", 8);
            }
            snailTransformation++;
        }
        else if (analyzing > 0) {
            analyzing--;
            pauseAllAnimations("analyzing");
        }
        else if (submittedAnswer && answeredRight != null) {
            if (analyzing == 0) {
                if (answeredRight) {
                    pauseAllAnimations("answer_correct");
                    animator.playAnimation("answer_correct", 7);
                }
                else {
                    pauseAllAnimations("answer_incorrect");
                    animator.playAnimation("answer_incorrect", 6);
                }
            }
            analyzing--;
        }
        else if (interactedWith) {
            pauseAllAnimations("countdown");
            animator.playAnimation("countdown", 4);
        }
        else if (this.gliding) {
            pauseAllAnimations("glide");
            animator.playAnimation("glide", 3);
        }
        else if (this.limbAnimator.isLimbMoving() && this.limbAnimator.getSpeed() > 0.02) {
            pauseAllAnimations("walk");
            animator.playAnimation("walk", 1);
        }
        else {
            pauseAllAnimations("idle");
            animator.playAnimation("idle", 0, true);
        }
    }

    public void playAnalyzingAnimation() {
        Animator animator = holder.getAnimator();
        pauseAllAnimations("analyzing");
        animator.playAnimation("analyzing", 5);
        analyzing = 42;
    }

    public void pauseAllAnimations(String except) {
        Animator animator = holder.getAnimator();
        if (!except.equalsIgnoreCase("glide")) animator.pauseAnimation("glide");
        if (!except.equalsIgnoreCase("walk")) animator.pauseAnimation("walk");
        if (!except.equalsIgnoreCase("idle")) animator.pauseAnimation("idle");
        if (!except.equalsIgnoreCase("countdown")) animator.pauseAnimation("countdown");
        if (!except.equalsIgnoreCase("analyzing")) animator.pauseAnimation("analyzing");
        if (!except.equalsIgnoreCase("answer_incorrect")) animator.pauseAnimation("answer_incorrect");
        if (!except.equalsIgnoreCase("answer_correct")) animator.pauseAnimation("answer_correct");
        if (!except.equalsIgnoreCase("snail_transform")) animator.pauseAnimation("snail_transform");
    }

    public void teleportNearPlayer(double minDistanceFromPlayer) {
        ServerPlayerEntity player = getBoundPlayer();
        if (player == null) return;
        if (getWorld() instanceof ServerWorld world) {
            this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT);
            AnimationUtils.spawnTeleportParticles(world, getPos());

            BlockPos tpTo = getBlockPosNearTarget(world, player.getBlockPos(), minDistanceFromPlayer);
            Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);
            //? if <= 1.21 {
            teleport(player.getServerWorld(), tpTo.getX(), tpTo.getY(), tpTo.getZ(), flags, getYaw(), getPitch());
            //?} else {
            /*teleport(player.getServerWorld(), tpTo.getX(), tpTo.getY(), tpTo.getZ(), flags, getYaw(), getPitch(), false);
             *///?}

            this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT);
            AnimationUtils.spawnTeleportParticles(world, getPos());
        }
    }

    public void teleportAbovePlayer(double minDistanceFromPlayer, int distanceAbove) {
        distanceAbove = 0; //TODO
        ServerPlayerEntity player = getBoundPlayer();
        if (player == null) return;
        if (getWorld() instanceof ServerWorld world) {
            this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT);
            AnimationUtils.spawnTeleportParticles(world, getPos());

            BlockPos tpTo = getBlockPosNearTarget(world, player.getBlockPos().add(0, distanceAbove, 0), minDistanceFromPlayer);
            Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);
            //? if <= 1.21 {
            teleport(player.getServerWorld(), tpTo.getX(), tpTo.getY(), tpTo.getZ(), flags, getYaw(), getPitch());
            //?} else {
            /*teleport(player.getServerWorld(), tpTo.getX(), tpTo.getY(), tpTo.getZ(), flags, getYaw(), getPitch(), false);
             *///?}

            this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT);
            AnimationUtils.spawnTeleportParticles(world, getPos());
        }
    }

    public int getRemainingTime() {
        int timeSinceStart = (int) Math.ceil((System.currentTimeMillis() - interactedAt) / 1000.0);
        return timeToComplete - timeSinceStart;
    }

    public BlockPos getBlockPosNearTarget(ServerWorld world, BlockPos targetPos, double minDistanceFromTarget) {
        if (getBoundPlayer() == null) return getBlockPos();

        for (int attempts = 0; attempts < 10; attempts++) {
            Vec3d offset = new Vec3d(
                    random.nextDouble() * 2 - 1,
                    1,
                    random.nextDouble() * 2 - 1
            ).normalize().multiply(minDistanceFromTarget);

            BlockPos pos = targetPos.add((int) offset.getX(), 0, (int) offset.getZ());

            BlockPos validPos = findNearestAirBlock(pos, world);
            if (validPos != null) {
                return validPos;
            }
        }

        return targetPos;
    }

    private BlockPos findNearestAirBlock(BlockPos pos, World world) {
        for (int yOffset = -5; yOffset <= 5; yOffset++) {
            BlockPos newPos = pos.up(yOffset);
            if (world.getBlockState(newPos).isAir()) {
                return newPos;
            }
        }
        return null;
    }

    public void updateNavigation() {
        moveControl = new MoveControl(this);
        navigation = new MobNavigation(this, getWorld());
        updateNavigationTarget();
    }

    public void updateNavigationTarget() {
        if (getBoundPlayer() == null) return;
        navigation.setSpeed(MOVEMENT_SPEED);
        Path path = navigation.findPathTo(getBoundPlayer(), 3);
        if (path != null) navigation.startMovingAlong(path, MOVEMENT_SPEED);
    }

    @Nullable
    public BlockPos getGroundBlock() {
        Vec3d startPos = getPos();
        Vec3d endPos = startPos.add(0, getWorld().getBottomY(), 0);

        BlockHitResult result = getWorld().raycast(
                new RaycastContext(
                        startPos,
                        endPos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        this
                )
        );
        if (result.getType() == HitResult.Type.MISS) return null;
        return result.getBlockPos();
    }

    public double getDistanceToGroundBlock() {
        BlockPos belowBlock = getGroundBlock();
        if (belowBlock == null) return Double.NEGATIVE_INFINITY;
        return getY() - belowBlock.getY() - 1;
    }

    public void playSounds() {
        //TODO
    }

    /*
        Trivia stuff
     */


    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ServerPlayerEntity boundPlayer = getBoundPlayer();
        if (boundPlayer == null) return ActionResult.PASS;
        if (boundPlayer.getUuid() != player.getUuid()) return ActionResult.PASS;
        if (submittedAnswer) return ActionResult.PASS;
        if (interactedWith && getRemainingTime() <= 0) return ActionResult.PASS;

        if (!interactedWith) {
            interactedAt = System.currentTimeMillis();
            timeToComplete = 20;
            difficulty = 1;
        }

        NetworkHandlerServer.sendTriviaPacket(boundPlayer, "Test test test test test test test test test test question :)", difficulty, interactedAt, timeToComplete, List.of("a b c d e f g h i j k l m n o p", "Grian", "Mumbo", "waaaaaaaaaaaaanjfebsjkfaes"));
        interactedWith = true;

        return ActionResult.PASS;
    }

    public void handleAnswer(int answer) {
        submittedAnswer = true;
        answeredCorrect();
        //answeredIncorrect();
    }

    public void answeredCorrect() {
        answeredRight = true;
        playAnalyzingAnimation();

    }

    public void answeredIncorrect() {
        answeredRight = false;
        playAnalyzingAnimation();

    }

    /*
        Override vanilla things
     */

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }
    
    @Override
    public Vec3d applyFluidMovingSpeed(double gravity, boolean falling, Vec3d motion) {
        return motion;
    }

    @Override
    protected boolean shouldSwimInFluids() {
        return false;
    }

    @Override
    public boolean isTouchingWater() {
        return false;
    }

    @Override
    public void setSwimming(boolean swimming) {
        this.setFlag(4, false);
    }

    @Override
    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return CAN_START_RIDING;
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return true;
    }

}
