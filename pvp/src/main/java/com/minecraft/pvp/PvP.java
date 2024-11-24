/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.command.InfoCommand;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.listener.AwayListener;
import com.minecraft.core.bukkit.listener.EnchantmentListener;
import com.minecraft.core.bukkit.listener.SoupListener;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.ranking.RankingHandler;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.pvp.commands.*;
import com.minecraft.pvp.game.GameStorage;
import com.minecraft.pvp.kit.KitStorage;
import com.minecraft.pvp.listeners.*;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.user.UserStorage;
import com.minecraft.pvp.util.Visibility;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;
import java.util.UUID;

@Getter
public class PvP extends BukkitGame implements BukkitInterface {

    private static PvP instance;
    private UserStorage userStorage;
    private KitStorage kitStorage;
    private GameStorage gameStorage;
    private Visibility visibility;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        setupWorlds("arena", "fps", "lava", "damage");

        this.userStorage = new UserStorage();
        this.kitStorage = new KitStorage();
        this.gameStorage = new GameStorage();
        this.visibility = new Visibility();

        setRankingFactory(new RankingFactory(RankingTarget.PVP));
        getRankingFactory().query();

        getRankingFactory().registerRankingHandler(new RankingHandler() {
            @Override
            public void onRankingUpgrade(Account account, Ranking old, Ranking upgrade) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    player.sendMessage("§eVocê foi promovido para " + upgrade.getColor() + upgrade.getSymbol() + " " + upgrade.getName() + ".");
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 3F);

                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }
                async(() -> account.getDataStorage().saveTable(Tables.PVP));
            }

            @Override
            public void onRankingDowngrade(Account account, Ranking downgrade) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    player.sendMessage("§eVocê foi rebaixado para " + downgrade.getColor() + downgrade.getSymbol() + " " + downgrade.getName() + ".");

                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }

                async(() -> account.getDataStorage().saveTable(Tables.PVP));
            }

            @Override
            public void onChallengerAssign(Account account) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }
            }

            @Override
            public void onChallengerDesign(Account account) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }
            }
        });

        BukkitFrame bukkitFrame = getEngine().getBukkitFrame();

        bukkitFrame.registerCommands(new KitCommand(), new Kit2Command(), new SpawnCommand(), new BuildCommand(), new RankingCommand(), new KillCommand());
        bukkitFrame.registerCommands(new InfoCommand<>(User.class, str -> User.fetch(UUID.fromString(str))));

        new SoupListener(this);

        getServer().getPluginManager().registerEvents(new UserLoader(), this);
        getServer().getPluginManager().registerEvents(new ServerListeners(), this);
        getServer().getPluginManager().registerEvents(new DeathListeners(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantmentListener(), this);
        getServer().getPluginManager().registerEvents(new ItemFrameListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListeners(), this);

        Constants.setServerType(ServerType.PVP);
        Constants.setLobbyType(ServerType.PVP_LOBBY);
        Constants.getServerStorage().listen(ServerType.PVP_LOBBY);

        startServerDump();

        getServer().getPluginManager().registerEvents(new AwayListener(), this);

        ItemStack soup = new ItemStack(Material.MUSHROOM_SOUP);
        ShapelessRecipe cocoa = new ShapelessRecipe(soup);
        cocoa.addIngredient(Material.BOWL);
        cocoa.addIngredient(Material.INK_SACK, 3);
        Bukkit.addRecipe(cocoa);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static PvP getPvP() {
        return instance;
    }

    protected void setupWorlds(String... names) {
        Arrays.stream(names).forEach(name -> {
            WorldCreator worldCreator = new WorldCreator(name);

            worldCreator.generateStructures(false);
            worldCreator.type(WorldType.FLAT);

            World world = Bukkit.getServer().createWorld(worldCreator);

            for (Entity entity : world.getEntities()) {
                if (entity instanceof ItemFrame)
                    continue;
                entity.remove();
            }

            world.setPVP(true);
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("naturalRegeneration", "false");
            world.setGameRuleValue("sendCommandFeedback", "false");
            world.setGameRuleValue("logAdminCommands", "false");

            if (name.equalsIgnoreCase("arena")) {
                world.setGameRuleValue("doFireTick", "false");
            }

            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MIN_VALUE);
            world.setThunderDuration(Integer.MIN_VALUE);

            world.setSpawnLocation(0, 71, 0);
            world.setAutoSave(false);
            world.setTime(6000);

            getLogger().info("Loaded " + name + " world");
        });
    }

}