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
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.series.secretlife.SecretLife;
import net.mat0u5.lifeseries.series.secretlife.TaskManager;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeries;

public class Events {

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
            Main.reload();
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
        try {
            UpdateChecker.onPlayerJoin(player);
            currentSeries.onPlayerJoin(player);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }
    private static void onPlayerDisconnect(MinecraftServer server, ServerPlayerEntity player) {
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
        try {
            if (entity instanceof ServerPlayerEntity) {
                Events.onPlayerDeath((ServerPlayerEntity) entity, source);
                return;
            }
            onMobDeath(entity, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static void onMobDeath(LivingEntity entity, DamageSource source) {
        try {
            currentSeries.onMobDeath(entity, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        try {
            currentSeries.onPlayerDeath(player, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            if (currentSeries instanceof SecretLife &&
                    player instanceof ServerPlayerEntity serverPlayer &&
                    world instanceof ServerWorld serverWorld) {
                TaskManager.onBlockUse(
                        serverPlayer,
                        serverWorld,
                        hitResult);
            }
            if (blacklist == null) return ActionResult.PASS;
            return blacklist.onBlockUse(player,world,hand,hitResult);
        } catch(Exception e) {
            Main.LOGGER.error(e.getMessage());
            return ActionResult.PASS;
        }
    }

    public static ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        try {
            if (blacklist == null) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.PASS;
            return blacklist.onBlockAttack(player,world,pos);
        } catch(Exception e) {
            Main.LOGGER.error(e.getMessage());
            return ActionResult.PASS;}
    }
}
