package net.mat0u5.lifeseries.entity.pathfinder;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.AnimationHandler;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PathFinder extends AmbientEntity implements AnimatedEntity {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "pathfinder");
    public static final float MOVEMENT_SPEED = 0.35f;
    public static final float FLYING_SPEED = 0.3f;

    public PathFinder(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
        setInvulnerable(true);
        setNoGravity(true);
        noClip = true;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return null;
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
    public void tick() {
        setOnGround(true);
    }

    public void setNavigation(boolean flying) {
        if (flying) {
            moveControl = new FlightMoveControl(this, 20, true);
            navigation = new BirdNavigation(this, getWorld());
            navigation.setCanSwim(true);
        }
        else {
            moveControl = new MoveControl(this);
            navigation = new MobNavigation(this, getWorld());
            navigation.setCanSwim(true);
        }
    }

    public boolean canPathfind(Entity pathfindTo, boolean flying) {
        setNavigation(flying);
        Path path = navigation.findPathTo(pathfindTo, 0);
        if (path == null) return false;
        PathNode end = path.getEnd();
        if (end == null) return false;
        return end.getBlockPos().equals(pathfindTo.getBlockPos());
    }
}
