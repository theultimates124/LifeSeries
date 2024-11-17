package net.mat0u5.lifeseries.events;



import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.utils.AnimationUtils;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class Events {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(Events::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(Events::onServerStopping);
        //UseBlockCallback.EVENT.register(Events::onBlockUse);
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
    /*
    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
    }*/
}
