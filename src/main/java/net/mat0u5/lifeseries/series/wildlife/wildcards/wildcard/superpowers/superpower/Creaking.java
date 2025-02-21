package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.ToggleableSuperpower;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.TeamUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class Creaking extends ToggleableSuperpower {

    private final List<String> createdTeams = new ArrayList<>();
    private final List<UUID> createdEntities = new ArrayList<>();

    public Creaking(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.CREAKING;
    }

    @Override
    public void activate() {
        super.activate();
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;

        Team playerTeam = TeamUtils.getPlayerTeam(player);
        if (playerTeam == null) return;
        String newTeamName = "creaking_"+player.getNameForScoreboard();
        TeamUtils.deleteTeam(newTeamName);
        TeamUtils.createTeam(newTeamName, playerTeam.getColor());
        createdTeams.add(newTeamName);

        //? if <= 1.21 {
        for (int i = 0; i < 2; i++) {
            BlockPos spawnPos =  getCloseBlockPos(player.getServerWorld(), player.getBlockPos(), 6);
            HuskEntity entity = EntityType.HUSK.spawn(player.getServerWorld(), spawnPos, SpawnReason.COMMAND);
            if (entity != null) {
                Objects.requireNonNull(entity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(0.5);
                createdEntities.add(entity.getUuid());
                makeFriendly(newTeamName, entity, player);
            }
        }
        //?} else {
        /*for (int i = 0; i < 3; i++) {
            BlockPos spawnPos =  getCloseBlockPos(player.getServerWorld(), player.getBlockPos(), 6);
            Entity entity = EntityType.CREAKING.spawn(player.getServerWorld(), spawnPos, SpawnReason.COMMAND);
            if (entity != null) {
                entity.setInvulnerable(true);
                createdEntities.add(entity.getUuid());
                makeFriendly(newTeamName, entity, player);
            }
        }
        *///?}
    }

    @Override
    public void deactivate() {
        // Also gets triggered when the players team is changed.
        super.deactivate();
        if (server != null) {
            List<Entity> toKill = new ArrayList<>();
            for (ServerWorld world : server.getWorlds()) {
                for (Entity entity : world.iterateEntities()) {
                    if (createdEntities.contains(entity.getUuid())) {
                        toKill.add(entity);
                    }
                }
            }
            toKill.forEach(Entity::discard);
            createdEntities.clear();
        }
        for (String teamAdded : createdTeams) {
            TeamUtils.deleteTeam(teamAdded);
        }
        createdTeams.clear();
        if (getPlayer() != null) {
            if (TeamUtils.getPlayerTeam(getPlayer()) == null) {
                currentSeries.reloadPlayerTeam(getPlayer());
            }
        }
    }

    @Override
    public int deactivateCooldown() {
        return 10;
    }

    public BlockPos getCloseBlockPos(ServerWorld world, BlockPos targetPos, double minDistanceFromTarget) {
        for (int attempts = 0; attempts < 20; attempts++) {
            Vec3d offset = new Vec3d(
                    world.random.nextDouble() * 2 - 1,
                    1,
                    world.random.nextDouble() * 2 - 1
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
            if (world.getBlockState(newPos).isAir() && world.getBlockState(pos.up(yOffset+1)).isAir() && world.getBlockState(pos.up(yOffset+2)).isAir()) {
                return newPos;
            }
        }
        return null;
    }

    private static void makeFriendly(String teamName, Entity entity, ServerPlayerEntity player) {
        TeamUtils.addEntityToTeam(teamName, player);
        TeamUtils.addEntityToTeam(teamName, entity);
        player.getServerWorld().spawnParticles(
                ParticleTypes.EXPLOSION,
                entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ(),
                1, 0, 0, 0, 0
        );
    }
}
