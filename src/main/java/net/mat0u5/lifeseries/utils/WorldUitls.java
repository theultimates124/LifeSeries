package net.mat0u5.lifeseries.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class WorldUitls {
    public static double findSafeY(World world, Vec3d pos) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());
        for (boolean movingUp : List.of(true,false)) {
            // Check upwards or downwards for the first safe position
            while (mutablePos.getY() >= world.getBottomY() && mutablePos.getY() < world.getHeight()) {
                if (isSafeSpot(world, mutablePos)) {
                    return mutablePos.getY(); // Found a safe spot
                }
                mutablePos.move(0, movingUp ? 1 : -1, 0);
            }
            mutablePos = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());
        }
        // Fallback to original position if no safe spot found
        return pos.getY();
    }
    public static boolean isSafeSpot(World world, BlockPos.Mutable pos) {
        // Check if the block below is solid
        boolean isSolidBlockBelow = world.getBlockState(pos.down()).isSolidBlock(world, pos.down());

        // Check if the current position and one above are non-collision blocks (air, water, etc.)
        boolean isNonCollisionAbove = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                && world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty();

        return isSolidBlockBelow && isNonCollisionAbove;
    }
}
