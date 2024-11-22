package com.minecraft.arcade.tiogerson;

import com.minecraft.arcade.tiogerson.command.DemandCommand;
import com.minecraft.arcade.tiogerson.command.FindRoomCommand;
import com.minecraft.arcade.tiogerson.command.ForceStartCommand;
import com.minecraft.arcade.tiogerson.listener.*;
import com.minecraft.arcade.tiogerson.mode.list.Normal;
import com.minecraft.arcade.tiogerson.room.storage.RoomStorage;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.arcade.tiogerson.user.UserStorage;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.command.InfoCommand;
import com.minecraft.core.server.ServerType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ArcadeMain extends BukkitGame {

    @Getter
    private static ArcadeMain instance;

    private UserStorage userStorage;
    private RoomStorage roomStorage;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        if(this.getPluginUpdater().isUpdated())
            return;

        userStorage = new UserStorage();
        userStorage.start(this);

        roomStorage = new RoomStorage(this);
        roomStorage.start();

        Constants.setServerType(ServerType.TIOGERSON);
        Constants.setLobbyType(ServerType.PROTOTYPE);

        new Normal().load();

        getBukkitFrame().registerCommands(new DemandCommand(), new ForceStartCommand(), new FindRoomCommand());

        getNPCProvider().getNpcListener().unload();
        getHologramProvider().getHologramListener().unload();

        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);

        getServerStorage().listen(ServerType.MAIN_LOBBY, ServerType.PROTOTYPE);

        getBukkitFrame().registerCommands(new InfoCommand<>(User.class, str -> User.fetch(UUID.fromString(str))));

        startServerDump();

    }

    @Override
    public void onDisable() {
        super.onDisable();
        roomStorage.end();
    }

}
