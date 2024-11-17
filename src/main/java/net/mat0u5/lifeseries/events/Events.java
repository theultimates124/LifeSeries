package net.mat0u5.lifeseries.events;



import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mat0u5.lifeseries.Main;
import net.minecraft.block.Block;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeries;

public class Events {

    public static void register() {
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

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity) {
                Events.onPlayerDeath((ServerPlayerEntity) entity, damageSource);
            }
        });
    }

    private static void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
        currentSeries.onPlayerJoin(player);
    }
    private static void onServerStopping(MinecraftServer server) {
    }
    private static void onServerStart(MinecraftServer server) {
        Main.server = server;
        System.out.println("MinecraftServer instance captured.");
        currentSeries.initialize();
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
    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        currentSeries.onPlayerDeath(player, source);
    }

    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        return blacklist.onBlockUse(player,world,hand,hitResult);
    }
    public static ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        if (world.isClient()) return ActionResult.PASS;
        return blacklist.onBlockAttack(player,world,pos);
    }
}
