package net.mat0u5.lifeseries.utils;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.List;
import java.util.Random;

public class WorldUitls {
    static Random rnd = new Random();

    public static boolean shouldChunkLoad(ServerWorld world, ChunkPos chunkPos) {
        var border = world.getWorldBorder();
        double minX = border.getBoundWest() - 32; // 2 chunks = 32 blocks
        double maxX = border.getBoundEast() + 32;
        double minZ = border.getBoundNorth() - 32;
        double maxZ = border.getBoundSouth() + 32;

        double chunkMinX = chunkPos.getStartX();
        double chunkMaxX = chunkPos.getEndX();
        double chunkMinZ = chunkPos.getStartZ();
        double chunkMaxZ = chunkPos.getEndZ();

        return !(chunkMaxX < minX || chunkMinX > maxX || chunkMaxZ < minZ || chunkMinZ > maxZ);
    }

    public static BlockPos getRandomCoords(World world) {
        WorldBorder border = world.getWorldBorder();
        double minX = border.getBoundWest();
        double maxX = border.getBoundEast();
        double minZ = border.getBoundNorth();
        double maxZ = border.getBoundSouth();
        int chosenX = (int) rnd.nextDouble(minX, maxX);
        int chosenZ = (int) rnd.nextDouble(minZ, maxZ);
        int safeY = (int) findSafeY(world, new Vec3d(chosenX, world.getHeight(), chosenZ));
        return new BlockPos(chosenX, safeY, chosenZ);
    }

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
        boolean isSolidBlockBelow = world.getBlockState(pos.down()).hasSolidTopSurface(world, pos.down(), new ZombieEntity(world));

        // Check if the current position and one above are non-collision blocks (air, water, etc.)
        boolean isNonCollisionAbove = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                && world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty();

        return isSolidBlockBelow && isNonCollisionAbove;
    }

    public static void summonHarmlessLightning(ServerWorld world, ServerPlayerEntity player) {
        Vec3d playerPos = player.getPos();

        // Create a new lightning entity
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.setPos(playerPos.x, playerPos.y, playerPos.z);

        // Prevent the lightning from dealing damage or causing fire
        lightning.setCosmetic(true);

        // Spawn the lightning entity in the world
        world.spawnEntity(lightning);
    }
}
