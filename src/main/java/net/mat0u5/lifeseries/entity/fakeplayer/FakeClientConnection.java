package net.mat0u5.lifeseries.entity.fakeplayer;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.PacketListener;

/*
    Used code from https://github.com/gnembon/fabric-carpet
 */
public class FakeClientConnection extends ClientConnection {
    public FakeClientConnection(NetworkSide side) {
        super(side);
    }
    @Override
    public void tryDisableAutoRead() {}
    @Override
    public void handleDisconnection() {}
    @Override
    public void setInitialPacketListener(PacketListener packetListener) {}
    @Override
    public <T extends PacketListener> void transitionInbound(NetworkState<T> protocolInfo, T packetListener) {}
}