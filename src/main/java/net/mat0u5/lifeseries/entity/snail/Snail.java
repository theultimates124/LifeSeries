package net.mat0u5.lifeseries.entity.snail;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.AnimationHandler;
import net.mat0u5.lifeseries.entity.pathfinder.PathFinder;
import net.mat0u5.lifeseries.entity.snail.goal.*;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class Snail extends HostileEntity implements AnimatedEntity {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "snail");
    public static final Model MODEL = BbModelLoader.load(ID);
    private final EntityHolder<Snail> holder;
    public UUID boundPlayerUUID;
    public boolean attacking;
    public boolean flying;
    public boolean gliding;
    public boolean mining;
    public boolean setNavigation = false;
    public PathFinder groundPathFinder;
    public PathFinder pathFinder;
    public int nullPlayerChecks = 0;

    public static final float MOVEMENT_SPEED = 0.35f;
    public static final float FLYING_SPEED = 0.3f;
    public static final int STATIONARY_TP_COOLDOWN = 1200; // No movement for 1 minute teleports the snail
    public static final int TP_MIN_RANGE = 15;
    public static final int MAX_DISTANCE = 150; // Distance over this teleports the snail to the player
    public static final int JUMP_COOLDOWN = 40;
    public static final int JUMP_RANGE_SQUARED = 12;

    public Snail(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(holder, this);
        setInvulnerable(true);
        setPersistent();
        groundPathFinder = MobRegistry.PATH_FINDER.spawn((ServerWorld) this.getWorld(), this.getBlockPos(), SpawnReason.COMMAND);
        pathFinder = MobRegistry.PATH_FINDER.spawn((ServerWorld) this.getWorld(), this.getBlockPos(), SpawnReason.COMMAND);
    }

    public void setBoundPlayer(ServerPlayerEntity player) {
        if (player == null) return;
        boundPlayerUUID = player.getUuid();
    }

    @Override
    public EntityHolder<Snail> getHolder() {
        return holder;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        //? if <= 1.21 {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10000)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, FLYING_SPEED)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 150)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 20);
        //?} else {
        /*return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10000)
                .add(EntityAttributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.FLYING_SPEED, FLYING_SPEED)
                .add(EntityAttributes.STEP_HEIGHT, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 150)
                .add(EntityAttributes.ATTACK_DAMAGE, 20);
        *///?}
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SnailTeleportGoal(this));

        goalSelector.add(1, new SnailLandGoal(this));
        goalSelector.add(2, new SnailMineTowardsPlayerGoal(this)); //TODO
        goalSelector.add(3, new SnailFlyGoal(this));

        goalSelector.add(4, new SnailGlideGoal(this));
        goalSelector.add(5, new SnailJumpAttackPlayerGoal(this));
        goalSelector.add(6, new SnailStartFlyingGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (nullPlayerChecks > 1000) {
            //Despawn

            //? if <= 1.21 {
            groundPathFinder.kill();
            pathFinder.kill();
            kill();
            //?} else {
            /*groundPathFinder.kill((ServerWorld) groundPathFinder.getWorld());
            pathFinder.kill((ServerWorld) groundPathFinder.getWorld());
            kill((ServerWorld) getWorld());
             *///?}
            groundPathFinder.remove(RemovalReason.KILLED);
            pathFinder.remove(RemovalReason.KILLED);
            this.remove(RemovalReason.KILLED);
        }

        Vec3d velocity = getVelocity();
        if (velocity.y > 0.15) {
            setVelocity(velocity.x,0.15,velocity.z);
        }
        else if (velocity.y < -0.15) {
            setVelocity(velocity.x,-0.15,velocity.z);
        }
        if (age % 2 == 0) {
            //TODO
            AnimationHandler.updateHurtVariant(this, holder);
            AnimationHandler.updateWalkAnimation(this, holder);
        }
        ServerPlayerEntity boundPlayer = getBoundPlayer();
        if (boundPlayer != null) {
            if (this.getBoundingBox().expand(0.05).intersects(boundPlayer.getBoundingBox())) {
                //TODO
                //? if <= 1.21 {
                boundPlayer.kill();
                 //?} else {
                /*boundPlayer.kill(boundPlayer.getServerWorld());
                *///?}

            }
            if (age % 100 == 0 || !setNavigation) {
                setNavigation = true;
                updateMoveControl();
                updateNavigation();
            }
            else if (age % 5 == 0) {
                updateNavigationTarget();
            }
        }

        updatePathFinders();
    }

    public void updatePathFinders() {
        ServerWorld world = (ServerWorld) this.getWorld();
        //? if <= 1.21 {
        this.pathFinder.teleport(world, this.getX(), this.getY(), this.getZ(), EnumSet.noneOf(PositionFlag.class), getYaw(), getPitch());
        BlockPos pos = getGroundBlock();
        if (pos == null) return;
        this.groundPathFinder.teleport(world, this.getX(), pos.getY()+1, this.getZ(), EnumSet.noneOf(PositionFlag.class), getYaw(), getPitch());
        //?} else {
        /*this.pathFinder.teleport(world, this.getX(), this.getY(), this.getZ(), EnumSet.noneOf(PositionFlag.class), getYaw(), getPitch(), false);
        BlockPos pos = getGroundBlock();
        if (pos == null) return;
        this.groundPathFinder.teleport(world, this.getX(), pos.getY()+1, this.getZ(), EnumSet.noneOf(PositionFlag.class), getYaw(), getPitch(), false);
        *///?}
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

    public boolean isTargetOnGround() {
        if (getBoundPlayer() == null) return false;
        return getBoundPlayer().isOnGround();
    }

    public void teleportNearPlayer(double minDistanceFromPlayer) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        if (getBoundPlayer() == null) return;

        BlockPos tpTo = getBlockPosNearTarget(minDistanceFromPlayer);
        Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);
        //? if <= 1.21 {
        teleport(serverWorld, tpTo.getX(), tpTo.getY(), tpTo.getZ(), flags, getYaw(), getPitch());
        //?} else {
        /*teleport(serverWorld, tpTo.getX(), tpTo.getY(), tpTo.getZ(), flags, getYaw(), getPitch(), false);
        *///?}

    }

    public BlockPos getBlockPosNearTarget(double minDistanceFromTarget) {
        if (getBoundPlayer() == null) return getBlockPos();

        BlockPos targetPos = getBoundPlayer().getBlockPos();
        Vec3d offset = new Vec3d(
                random.nextDouble() * 2 - 1,
                0,
                random.nextDouble() * 2 - 1
        ).normalize().multiply(minDistanceFromTarget);
        return targetPos.add((int) offset.getX(), (int) offset.getY(), (int) offset.getZ());
    }

    public boolean canPathToPlayer(boolean flying) {
        return pathFinder.canPathfind(getBoundPlayer(), flying);
    }

    public boolean canPathToPlayerFromGround(boolean flying) {
        return groundPathFinder.canPathfind(getBoundPlayer(), flying);
    }

    public void updateNavigation() {
        if (mining) {
            setNavigationMining();
        }
        else if (flying) {
            setNavigationFlying();
        }
        else {
            setNavigationWalking();
        }
    }

    public void updateMoveControl() {
        if (flying) {
            setMoveControlFlight();
        }
        else {
            setMoveControlWalking();
        }
    }
    public void setNavigationFlying() {
        navigation = new BirdNavigation(this, getWorld());
        updateNavigationTarget();
    }

    public void setNavigationWalking() {
        navigation = new MobNavigation(this, getWorld());
        updateNavigationTarget();
    }

    public void setNavigationMining() {
        navigation = new MiningNavigation(this, getWorld());
        updateNavigationTarget();
    }

    public void updateNavigationTarget() {
        if (getBoundPlayer() == null) return;
        if (navigation instanceof BirdNavigation) {
            navigation.setSpeed(1);
            navigation.startMovingTo(getBoundPlayer(), 1);
        }
        else {
            navigation.setSpeed(MOVEMENT_SPEED);
            navigation.startMovingTo(getBoundPlayer(), MOVEMENT_SPEED);
        }
    }

    public void setMoveControlFlight() {
        setNoGravity(true);
        moveControl = new FlightMoveControl(this, 20, true);
    }

    public void setMoveControlWalking() {
        setNoGravity(false);
        moveControl = new MoveControl(this);
    }

    public void playAttackSound() {

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
        //if (player.isCreative()) return null;
        //if (!currentSeries.isAlive(player)) return null;
        return player;
    }
}
