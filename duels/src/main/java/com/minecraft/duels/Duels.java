package com.minecraft.duels;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.command.InfoCommand;
import com.minecraft.core.bukkit.listener.SoupListener;
import com.minecraft.core.server.ServerType;
import com.minecraft.duels.command.DemandCommand;
import com.minecraft.duels.listener.DamageListener;
import com.minecraft.duels.listener.PlayerListener;
import com.minecraft.duels.listener.ServerListener;
import com.minecraft.duels.listener.WorldListener;
import com.minecraft.duels.mode.list.*;
import com.minecraft.duels.room.storage.RoomStorage;
import com.minecraft.duels.user.User;
import com.minecraft.duels.user.UserStorage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Iterator;
import java.util.UUID;

@Getter
public class Duels extends BukkitGame {

    @Getter
    private static Duels instance;
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

        if (getPluginUpdater().isUpdated())
            return;

        userStorage = new UserStorage();
        userStorage.start(this);

        roomStorage = new RoomStorage(this);
        roomStorage.start();

        Constants.setServerType(ServerType.DUELS);
        Constants.setLobbyType(ServerType.DUELS_LOBBY);

        new Gladiator().load();
        new GladiatorOld().load();
        new Soup().load();
        new Simulator().load();
        new UHC().load();
        new Sumo().load();
        new Scrim().load();
        new Boxing().load();

        getBukkitFrame().registerCommands(new DemandCommand());

        getNPCProvider().getNpcListener().unload();
        getHologramProvider().getHologramListener().unload();

        new SoupListener(this);

        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);

        getServerStorage().listen(ServerType.MAIN_LOBBY, ServerType.DUELS_LOBBY);

        getBukkitFrame().registerCommands(new InfoCommand<>(User.class, str -> User.fetch(UUID.fromString(str))));

        startServerDump();
        makeRecipe();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        roomStorage.end();
    }

    protected void makeRecipe() {
        ItemStack soup = new ItemStack(Material.MUSHROOM_SOUP);
        ShapelessRecipe cocoa = new ShapelessRecipe(soup);
        cocoa.addIngredient(Material.BOWL);
        cocoa.addIngredient(Material.INK_SACK, 3);
        Bukkit.addRecipe(cocoa);

        Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

        while (iterator.hasNext()) {

            Recipe recipe = iterator.next();

            if (recipe.getResult() != null) {
                if (recipe.getResult().getType() == Material.BOAT || recipe.getResult().getType() == Material.FISHING_ROD || recipe.getResult().getType() == Material.BUCKET || recipe.getResult().getType().name().contains("FENCE"))
                    iterator.remove();
            }
        }
    }
}