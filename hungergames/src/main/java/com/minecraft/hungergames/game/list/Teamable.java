package com.minecraft.hungergames.game.list;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.game.team.Team;
import com.minecraft.hungergames.game.team.TeamStorage;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.game.Loadable;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import com.minecraft.hungergames.util.selector.object.TeamSelector;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Loadable
public class Teamable extends Game {

    private final TeamStorage teamStorage;
    private final TeamSelector teamSelector;

    public Teamable(HungerGames hungerGames) {
        super(hungerGames);
        this.teamSelector = new TeamSelector();
        this.teamStorage = new TeamStorage(hungerGames, new Team(0, "hg.teams.red", ChatColor.RED, Color.fromRGB(255, 85, 85)),
                new Team(1, "hg.teams.blue", ChatColor.BLUE, Color.fromRGB(85, 85, 255)));
    }

    @Override
    public void checkWin() {

        if (!isAutoVictory())
            return;

        List<Team> teams = new ArrayList<>(getTeamStorage().getTeams());
        teams.removeIf(team -> team.getMembers().isEmpty());

        if (teams.isEmpty()) {
            Bukkit.shutdown();
        } else if (teams.size() == 1) {
            Set<User> users = new HashSet<>(teams.get(0).getMembers());
            win(users);
        }
        teams.clear();
    }

    @Override
    public void pointCompass(User user, Action action) {

        Player player = user.getPlayer();
        TeamStorage teamStorage = getTeamStorage();

        if (action.name().contains("LEFT")) {

            final int teamIndex = player.getMetadata("team_index").get(0).asInt();

            Team team = teamStorage.next(teamIndex);

            if (team == null) {
                player.sendMessage("§cNenhum outro time encontrado, bússola continua apontando para " + user.getAccount().getLanguage().translate(teamStorage.getTeam(teamIndex).getName()));
                return;
            }

            player.getMetadata("team_index").set(0, new GameMetadata(team.getId()));
            player.sendMessage("§aBússola apontando para o time " + team.getChatColor() + user.getAccount()
                    .getLanguage().translate(team.getName()));
        } else {

            if (!player.hasMetadata("team_index"))
                player.setMetadata("team_index", new GameMetadata(0));

            final int teamIndex = player.getMetadata("team_index").get(0).asInt();

            Team team = teamStorage.getTeam(teamIndex);

            if (team == null) {
                player.sendMessage("§cO time que sua bússola estava apontando não existe mais.");
                player.getMetadata("team_index").set(0, new GameMetadata(0));
                return;
            }

            if (team.getMembers().isEmpty()) {
                Team next = teamStorage.next(teamIndex);

                if (next != null) {
                    player.sendMessage("§cNenhum jogador encontrado, bússola apontando para o time " + next.getChatColor() + user.getAccount().getLanguage().translate(next.getName()));
                }

                return;
            }


            Player comparator = null;

            for (User other : team.getMembers()) {

                if (other.getUniqueId().equals(user.getUniqueId()))
                    continue;

                Player p = other.getPlayer();

                if (p.getLocation().distanceSquared(player.getLocation()) >= 225) {
                    if (comparator == null || comparator.getLocation().distanceSquared(player.getLocation()) > p.getLocation().distanceSquared(player.getLocation())) {
                        comparator = p;
                    }
                }
            }

            if (comparator == null) {
                player.sendMessage("§cNenhum jogador encontrado.");
                player.setCompassTarget(getGame().getVariables().getSpawnpoint());
            } else {
                player.setCompassTarget(comparator.getLocation());
                player.sendMessage(user.getAccount().getLanguage().translate("hg.game.user.compass_pointing_to", comparator.getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player && !(event.getDamager() instanceof Arrow)) {
            event.setCancelled(true);
        }
    }

    private final ItemStack baseItemStack = new InteractableItem(new ItemFactory(Material.NAME_TAG).setName("item.select_team").getStack(), new InteractableItem.Interact() {
        @Override
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            getTeamSelector().getInventory().open(player);
            return true;
        }
    }).getItemStack();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Account account = Account.fetch(player.getUniqueId());

        player.setOp(account.hasPermission(Rank.PARTNER_PLUS));

        if (getStage() != GameStage.WAITING)
            return;

        player.getInventory().setItem(8, new ItemFactory().setItemStack(baseItemStack.clone()).setName(account.getLanguage() == Language.PORTUGUESE ? "§aSelecionar time" : "§aSelect team").getStack());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (getStage() != GameStage.WAITING)
            return;

        User user = getUser(event.getPlayer().getUniqueId());

        if (user.hasTeam()) { // Removing player from team if he log out during WAITING stage.
            user.getTeam().getMembers().remove(user);
//            refresh();
        }
    }

    @EventHandler
    public void onLivingUser(LivingUserDieEvent event) {
        User user = event.getUser();

        if (user.hasTeam()) {
            user.getTeam().getMembers().remove(user);
            user.setTeam(null);
        }
//        refresh();
    }
}
