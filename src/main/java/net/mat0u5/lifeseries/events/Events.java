package net.mat0u5.lifeseries.events;



import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.config.DatapackManager;
import net.mat0u5.lifeseries.config.UpdateChecker;
import net.mat0u5.lifeseries.entity.fakeplayer.FakePlayer;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.series.secretlife.SecretLife;
import net.mat0u5.lifeseries.series.secretlife.TaskManager;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeries;

public class Events {
    public static boolean skipNextTickReload = false;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(Events::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(Events::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(Events::onServerStopping);

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(Events::onReload);

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!(player instanceof ServerPlayerEntity)) {
                return ActionResult.PASS; // Only handle server-side events
            }

            return Events.onBlockAttack((ServerPlayerEntity) player, world, pos);
        });
        UseBlockCallback.EVENT.register(Events::onBlockUse);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(server, handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerDisconnect(server, handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(Events::onServerTickEnd);

        ServerLivingEntityEvents.AFTER_DEATH.register(Events::onEntityDeath);
    }

    private static void onReload(MinecraftServer server, LifecycledResourceManager resourceManager) {
        try {
            if (!Main.isLogicalSide()) return;
            Main.reload();
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
        if (isFakePlayer(player)) return;

        try {
            playerStartJoining(player);
            currentSeries.onPlayerJoin(player);
            blacklist.onInventoryUpdated(player, player.getInventory());
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onPlayerFinishJoining(MinecraftServer server, ServerPlayerEntity player) {
        if (isFakePlayer(player)) return;

        try {
            UpdateChecker.onPlayerJoin(player);
            currentSeries.onPlayerFinishJoining(player);
            NetworkHandlerServer.tryKickFailedHandshake(player);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onPlayerDisconnect(MinecraftServer server, ServerPlayerEntity player) {
        if (isFakePlayer(player)) return;

        try {
            currentSeries.onPlayerDisconnect(player);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onServerStopping(MinecraftServer server) {
        try {
            UpdateChecker.shutdownExecutor();
        }catch (Exception e) {}
    }

    private static void onServerStarting(MinecraftServer server) {
        Main.server = server;
    }

    private static void onServerStart(MinecraftServer server) {
        try {
            Main.server = server;
            currentSeries.initialize();
            blacklist.reloadBlacklist();
            if (currentSeries.getSeries() == SeriesList.DOUBLE_LIFE) {
                ((DoubleLife) currentSeries).loadSoulmates();
            }
            new DatapackManager().onServerStarted(server);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onServerTickEnd(MinecraftServer server) {
        try {
            skipNextTickReload = false;
            if (!Main.isLogicalSide()) return;
            checkPlayerFinishJoiningTick();
            if (server.getTickManager().isFrozen()) return;
            if (Main.currentSession != null) {
                Main.currentSession.tick(server);
            }
            OtherUtils.onTick();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void onEntityDeath(LivingEntity entity, DamageSource source) {
        if (isFakePlayer(entity)) return;

        try {
            if (!Main.isLogicalSide()) return;
            if (entity instanceof ServerPlayerEntity) {
                Events.onPlayerDeath((ServerPlayerEntity) entity, source);
                return;
            }
            onMobDeath(entity, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static void onMobDeath(LivingEntity entity, DamageSource source) {
        if (isFakePlayer(entity)) return;

        try {
            if (!Main.isLogicalSide()) return;
            currentSeries.onMobDeath(entity, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        if (isFakePlayer(player)) return;

        try {
            if (!Main.isLogicalSide()) return;
            currentSeries.onPlayerDeath(player, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (isFakePlayer(player)) return ActionResult.PASS;

        if (player instanceof ServerPlayerEntity serverPlayer &&
                world instanceof ServerWorld serverWorld && Main.isLogicalSide()) {
            try {
                if (currentSeries instanceof SecretLife) {
                    TaskManager.onBlockUse(
                            serverPlayer,
                            serverWorld,
                            hitResult);
                }
                if (blacklist == null) return ActionResult.PASS;
                return blacklist.onBlockUse(serverPlayer,serverWorld,hand,hitResult);
            } catch(Exception e) {
                Main.LOGGER.error(e.getMessage());
                return ActionResult.PASS;
            }
        }
        return ActionResult.PASS;
    }

    public static ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        if (isFakePlayer(player)) return ActionResult.PASS;

        try {
            if (!Main.isLogicalSide()) return ActionResult.PASS;
            if (blacklist == null) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.PASS;
            return blacklist.onBlockAttack(player,world,pos);
        } catch(Exception e) {
            Main.LOGGER.error(e.getMessage());
            return ActionResult.PASS;}
    }

    /*
        Non-events
     */
    public static HashMap<UUID, Vec3d> joiningPlayers = new HashMap<>();
    public static HashMap<UUID, Float> joiningPlayersYaw = new HashMap<>();
    public static HashMap<UUID, Float> joiningPlayersPitch = new HashMap<>();
    public static void playerStartJoining(ServerPlayerEntity player) {
        NetworkHandlerServer.sendHandshake(player);
        NetworkHandlerServer.sendUpdatePacketTo(player);
        joiningPlayers.put(player.getUuid(), player.getPos());
        joiningPlayersYaw.put(player.getUuid(), player.getYaw());
        joiningPlayersPitch.put(player.getUuid(), player.getPitch());
    }
    public static void checkPlayerFinishJoiningTick() {
        for (Map.Entry<UUID, Vec3d> entry : joiningPlayers.entrySet()) {
            UUID uuid = entry.getKey();
            ServerPlayerEntity player = PlayerUtils.getPlayer(uuid);
            if (player == null) continue;
            if (player.getPos().equals(entry.getValue())) continue;
            onPlayerFinishJoining(player.server, player);
            finishedJoining(player.getUuid());
            return;
        }
        //Yaw
        for (Map.Entry<UUID, Float> entry : joiningPlayersYaw.entrySet()) {
            UUID uuid = entry.getKey();
            ServerPlayerEntity player = PlayerUtils.getPlayer(uuid);
            if (player == null) continue;
            if (player.getYaw() == entry.getValue()) continue;
            onPlayerFinishJoining(player.server, player);
            finishedJoining(player.getUuid());
            return;
        }
        //Pitch
        for (Map.Entry<UUID, Float> entry : joiningPlayersPitch.entrySet()) {
            UUID uuid = entry.getKey();
            ServerPlayerEntity player = PlayerUtils.getPlayer(uuid);
            if (player == null) continue;
            if (player.getPitch() == entry.getValue()) continue;
            onPlayerFinishJoining(player.server, player);
            finishedJoining(player.getUuid());
            return;
        }

    }
    public static void finishedJoining(UUID uuid) {
        joiningPlayers.remove(uuid);
        joiningPlayersYaw.remove(uuid);
        joiningPlayersPitch.remove(uuid);
    }
    public static boolean isFakePlayer(Entity entity) {
        return entity instanceof FakePlayer;
    }
}
