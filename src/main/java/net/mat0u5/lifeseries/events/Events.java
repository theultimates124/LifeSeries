package net.mat0u5.lifeseries.events;



import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.config.DatabaseManager;
import net.mat0u5.lifeseries.config.DatapackManager;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.WorldUitls;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!(player instanceof ServerPlayerEntity)) {
                return ActionResult.PASS; // Only handle server-side events
            }

            return Events.onBlockAttack((ServerPlayerEntity) player, world, pos);
        });
        UseBlockCallback.EVENT.register(Events::onBlockUse);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(server, handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(Events::onServerTickEnd);

        ServerLivingEntityEvents.AFTER_DEATH.register(Events::onEntityDeath);
    }
    private static void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
        currentSeries.onPlayerJoin(player);
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        PlayerUtils.applyResorucepack(serverPlayer);
    }
    private static void onServerStopping(MinecraftServer server) {
    }
    private static void onServerStarting(MinecraftServer server) {
        Main.server = server;
    }
    private static void onServerStart(MinecraftServer server) {
        Main.server = server;
        currentSeries.initialize();
        DatabaseManager.initialize();
        if (currentSeries.getSeries() == SeriesList.DOUBLE_LIFE) {
            ((DoubleLife) currentSeries).loadSoulmates();
        }
        new DatapackManager().onServerStarted(server);
    }
    private static void onServerTickEnd(MinecraftServer server) {
        try {
            if (Main.currentSession != null) {
                Main.currentSession.tick(server);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void onEntityDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayerEntity) {
            Events.onPlayerDeath((ServerPlayerEntity) entity, source);
            return;
        }
        onMobDeath(entity, source);
    }
    public static void onMobDeath(LivingEntity entity, DamageSource source) {
        currentSeries.onMobDeath(entity, source);
    }
    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        currentSeries.onPlayerDeath(player, source);
    }

    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (blacklist == null) return ActionResult.PASS;
        return blacklist.onBlockUse(player,world,hand,hitResult);
    }
    public static ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        if (blacklist == null) return ActionResult.PASS;
        if (world.isClient()) return ActionResult.PASS;
        return blacklist.onBlockAttack(player,world,pos);
    }
}
