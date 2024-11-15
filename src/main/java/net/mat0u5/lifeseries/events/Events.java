package net.mat0u5.lifeseries.events;



import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mat0u5.lifeseries.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class Events {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(Events::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(Events::onServerStopping);
        //UseBlockCallback.EVENT.register(Events::onBlockUse);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(server, handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(Events::onServerTickEnd);
    }

    private static void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
    }
    private static void onServerStopping(MinecraftServer server) {
    }
    private static void onServerStart(MinecraftServer server) {
        Main.server = server;
        System.out.println("MinecraftServer instance captured.");
    }
    private static void onServerTickEnd(MinecraftServer server) {
    }
    /*
    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
    }*/
}
