package com.minecraft.core.proxy.util.reconnect;

import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.Kick;

public class ReconnectBridge extends DownstreamBridge {

    private final ProxyServer bungee;

    private final UserConnection user;

    private final ServerConnection server;

    public ReconnectBridge(ProxyServer bungee, UserConnection user, ServerConnection server) {
        super(bungee, user, server);
        this.bungee = bungee;
        this.user = user;
        this.server = server;
    }

    public void disconnected(ChannelWrapper channel) {
        this.server.getInfo().removePlayer(this.user);
        if (this.bungee.getReconnectHandler() != null)
            this.bungee.getReconnectHandler().setServer(this.user);
        if (!this.server.isObsolete()) {
            this.server.setObsolete(true);
            if (ProxyServer.getInstance().getPlayer(this.user.getUniqueId()) != null)
                reconnect();
        }
        ServerDisconnectEvent serverDisconnectEvent = new ServerDisconnectEvent(this.user, this.server.getInfo());
        this.bungee.getPluginManager().callEvent((Event) serverDisconnectEvent);
    }

    public void exception(Throwable t) {
        if (this.server.isObsolete())
            return;
        this.server.setObsolete(true);
        reconnect();
    }

    public void handle(Kick kick) {
        String kickMessage = ChatColor.stripColor(BaseComponent.toLegacyText(kick.getMessage()));
        if (kickMessage.equals("O servidor em que você estava não está mais disponível.") || kickMessage.equals("O servidor foi fechado") || kickMessage.equals("Server closed")) {
            reconnect();
        } else {
            this.user.disconnect0(kick.getMessage());
        }
        this.server.setObsolete(true);
        throw CancelSendSignal.INSTANCE;
    }

    private void reconnect() {
        (new ReconnectTask(this.user)).run();
    }

}