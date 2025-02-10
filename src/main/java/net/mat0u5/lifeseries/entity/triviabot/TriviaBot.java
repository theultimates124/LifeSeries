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
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.server;

public class TriviaBot extends AmbientEntity implements AnimatedEntity {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "triviabot");
    public static final Model MODEL = BbModelLoader.load(Snail.ID);
    public static final ChunkTicketType<ChunkPos> BOT_TICKET = ChunkTicketType.create("triviabot", Comparator.comparingLong(ChunkPos::toLong), 100);

    public static final float MOVEMENT_SPEED = 0.45f;
    public static final int MAX_DISTANCE = 100;
    public static final int TP_MIN_RANGE = 30;

    public boolean gliding;
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
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0);
        //?} else {
        /*return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10000)
                .add(EntityAttributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.FLYING_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.STEP_HEIGHT, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 100)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 1)
                .add(EntityAttributes.ATTACK_DAMAGE, 0);
        *///?}
    }

    @Nullable
    public ServerPlayerEntity getBoundPlayer() {
        if (server == null) return null;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(boundPlayerUUID);
        if (player == null) {
            nullPlayerChecks++;
            return null;
        }
        nullPlayerChecks = 0;
        if (player.isSpectator()) return null;
        if (player.isDead()) return null;
        //if (player.isCreative()) return null;
        //if (!currentSeries.isAlive(player)) return null;
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

        if (nullPlayerChecks > 1000) {
            despawn();
        }

        if (age % 2 == 0) {
            updateAnimations();
        }

        ServerPlayerEntity boundPlayer = getBoundPlayer();
        if (boundPlayer != null) {
            if (age % 5 == 0) {
                updateNavigationTarget();
            }
        }

        handleHighVelocity();
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
        //? if <= 1.21 {
        this.kill();
        //?} else {
        /*this.kill((ServerWorld) getWorld());
         *///?}
        this.discard();
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }

    public void updateAnimations() {
        AnimationHandler.updateHurtVariant(this, holder);
        Animator animator = holder.getAnimator();
        //TODO
    }

    public void teleportNearPlayer(double minDistanceFromPlayer) {
        ServerPlayerEntity player = getBoundPlayer();
        if (player == null) return;
        if (getWorld() instanceof ServerWorld world) {
            this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT);
            AnimationUtils.spawnTeleportParticles(world, getPos());

            BlockPos tpTo = getBlockPosNearTarget(world, minDistanceFromPlayer);
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

    public BlockPos getBlockPosNearTarget(ServerWorld world, double minDistanceFromTarget) {
        if (getBoundPlayer() == null) return getBlockPos();

        BlockPos targetPos = getBoundPlayer().getBlockPos();

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
        Override vanilla things
     */
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
        return false;
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return true;
    }

}
