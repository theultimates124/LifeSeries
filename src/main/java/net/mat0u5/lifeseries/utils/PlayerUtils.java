package net.mat0u5.lifeseries.utils;

import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PlayerUtils {
    public static void sendTitle(ServerPlayerEntity player, Text title) {
        TitleS2CPacket titlePacket = new TitleS2CPacket(title);
        player.networkHandler.sendPacket(titlePacket); // Send packet to the player
    }
}
