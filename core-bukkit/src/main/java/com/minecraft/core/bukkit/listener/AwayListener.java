package com.minecraft.core.bukkit.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.actionbar.ActionBar;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerStorage;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AwayListener extends DynamicListener {

    public AwayListener() {
        ServerStorage serverStorage = Constants.getServerStorage();

        if (!serverStorage.isListen(ServerType.LIMBO_AFK))
            serverStorage.listen(ServerType.LIMBO_AFK);
    }

    private final Map<UUID, Long> lastMovement = new HashMap<>();
    private final Map<UUID, Location> lastLocation = new HashMap<>();

    @EventHandler
    public void onServerHeartbeat(ServerHeartbeatEvent event) {
        if (event.isPeriodic(20)) {
            for (Player player : Bukkit.getOnlinePlayers()) {

                if (haveMoved(player)) {
                    continue;
                }

                UUID uuid = player.getUniqueId();

                long awayTime = get(uuid);

                if (awayTime >= 180000) { // 180000 = 3 minutes (3 * 60 * 1000)

                    Account account = Account.fetch(uuid);

                    if (account == null)
                        continue;

                    if (account.getRank().isStaffer())
                        continue;

                    System.out.println(player.getName() + " is away. (" + awayTime / 1000 + "s) kicking him.");

                    Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.LIMBO_AFK);

                    if (server == null) {
                        System.out.println("No AFK servers found. Skipping it...");
                        return;
                    }

                    Language language = account.getLanguage();

                    player.sendMessage("");
                    player.sendMessage(language.translate("antiafk.afk_warn"));
                    player.sendMessage(language.translate("antiafk.how_to_back"));
                    player.sendMessage("");

                    account.connect(server);
                }
            }

        }

        if(isCertainHour(5, 30)) { // this also shouldn't be here.
            Bukkit.getServer().shutdown();
        }

    }

    public static boolean isCertainHour(int hour, int min) {
        LocalTime now = LocalTime.now();
        return now.getHour() == hour && now.getMinute() == min;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastMovement.remove(uuid);
        lastLocation.remove(uuid);
    }

    private void reset(UUID uuid) {
        lastMovement.put(uuid, System.currentTimeMillis());
    }

    private boolean haveMoved(Player player) {

        UUID uuid = player.getUniqueId();

        Location currentLocation = player.getLocation();

        Location cacheLocation = lastLocation.get(uuid);

        if (cacheLocation == null) {
            lastLocation.put(uuid, currentLocation);
            return true;
        }

        if (currentLocation.getBlockX() != cacheLocation.getBlockX() || currentLocation.getBlockY() != cacheLocation.getBlockY() || currentLocation.getBlockZ() != cacheLocation.getBlockZ()) {
            lastLocation.put(uuid, currentLocation);
            reset(uuid);
            return true;
        }

        return false;
    }

    public long get(UUID uuid) {
        return System.currentTimeMillis() - lastMovement.computeIfAbsent(uuid, l -> System.currentTimeMillis());
    }
}
