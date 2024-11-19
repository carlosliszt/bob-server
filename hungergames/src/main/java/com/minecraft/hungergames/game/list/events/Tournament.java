package com.minecraft.hungergames.game.list.events;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.util.disguise.PlayerDisguise;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.util.skin.Skin;
import com.minecraft.core.util.skin.util.CustomProperty;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.list.Event;
import com.minecraft.hungergames.user.User;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class Tournament extends Event {

    public Tournament(HungerGames hungerGames) {
        super(hungerGames);
        setName("Torneio");

        getVariables().setFinalArenaSpawn(true);
        getVariables().setInvincibility(300);
        getVariables().setFinalArena(1800);
        getVariables().setMostEndGameMechanic(false);
        setArenaDisableKits(false);
        Constants.setServerType(ServerType.TOURNAMENT);

        try {
            getVariable("hg.respawn").setValue(false);
            getVariable("hg.late_join").setValue(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        super.start();
        encrypt();
    }

    public void encrypt() {

        if (!nameEncryption)
            return;

        Bukkit.getOnlinePlayers().forEach(player -> {
            Account account = Account.fetch(player.getUniqueId());
            PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class));
            Bukkit.getPluginManager().callEvent(event);
        });
    }

    @Override
    public void load() {
        super.load();
        getKits(' ', "Achilles Timelord Stomper Demoman Endermage AntiTower Launcher Phantom Switcher Fisherman Cultivator Camel Tank Blink Rider Berserker Checkpoint Chameleon Boxer Monk Digger Gladiator").forEach(kit -> kit.setActive(false, false));
        getVariable("hg.advantages.minimum_rank").setValue(Rank.PARTNER_PLUS);
    }

    @Variable(name = "hg.tournament.name_encryption")
    public boolean nameEncryption = true;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {

        Player player = event.getPlayer();

        CustomProperty customProperty = Skin.getRandomSkin().getCustomProperty();
        PlayerDisguise.changeSkin(player, new Property(customProperty.getName(), customProperty.getValue(), customProperty.getSignature()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        encrypt();
    }

    @Override
    public void die(User user) {
        super.die(user);

        try {
            Rank rank = (Rank) getVariable("hg.advantages.minimum_rank").getValue();

            if (!user.getAccount().hasPermission(rank)) {
                if (!user.isAlive() && user.isOnline()) {
                    Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.HG_LOBBY);

                    if (server == null)
                        server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY);

                    if (server != null)
                        user.getAccount().connect(server);
                    else
                        user.getPlayer().kickPlayer("§cVocê morreu. Tente novamente na próxima!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerUpdateTablist(PlayerTeamAssignEvent event) {

        if (!nameEncryption)
            return;

        User user = User.fetch(event.getViewer().getUniqueId());

        if (user.isAlive()) {
            if (!event.getAccount().getUniqueId().equals(user.getUniqueId())) {
                event.getTeam().setPrefix("§7§k");
            } else if (event.getAccount().getUniqueId().equals(user.getUniqueId())) {
                event.getTeam().setPrefix("§e");
                event.getTeam().setSuffix(" §b(T)");
            }
        }
    }

    @EventHandler
    public void onAdminLeave(PlayerVanishDisableEvent event) {
        run(this::encrypt, 2L);
    }

    @EventHandler
    public void onAdminJoin(PlayerVanishEnableEvent event) {
        run(this::encrypt, 2L);
    }
}
