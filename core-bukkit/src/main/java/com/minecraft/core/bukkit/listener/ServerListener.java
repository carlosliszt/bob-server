/*
 * Copyright (C) YoloMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.*;
import com.minecraft.core.bukkit.event.server.RedisPubSubEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.WordCensor;
import com.minecraft.core.bukkit.util.command.command.BukkitCommand;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.disguise.PlayerDisguise;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.reflection.FieldHelper;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.clan.communication.ClanIntegrationMessage;
import com.minecraft.core.clan.member.Member;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.ServerCategory;
import com.viaversion.viaversion.ViaVersionPlugin;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import dev.imanity.knockback.api.Knockback;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ServerListener implements Listener, BukkitInterface, VariableStorage {

    public ServerListener() {
        loadVariables();
    }

   /* @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof PacketPlayOutEntityMetadata) {

            PacketPlayOutEntityMetadata metadata = (PacketPlayOutEntityMetadata) event.getPacket();
            int entityId = event.getValue("a");

            if (entityId != event.getPlayer().getEntityId()) {
                List<Integer> players = new ArrayList<>(event.getPlayer().getWorld().getEntities()).stream().filter(c -> c instanceof Player).map(Entity::getEntityId).collect(Collectors.toList());

                if (players.contains(entityId)) {
                }
            }
        }
    } */

    @EventHandler
    public void onRedisPubSub(RedisPubSubEvent event) {
        if (event.getChannel().equals(Redis.CLAN_INTEGRATION_CHANNEL)) {

            ClanIntegrationMessage integrationMessage = Constants.GSON.fromJson(event.getMessage(), ClanIntegrationMessage.class);
            ClanIntegrationMessage.MessageCause messageCause = integrationMessage.getMessageCause();

            Clan clan = Constants.getClanService().fetch(integrationMessage.getIndex());

            if (clan == null && messageCause != ClanIntegrationMessage.MessageCause.CREATION) {
                System.out.println("(" + messageCause.name() + ") Clan '" + integrationMessage.getClanName() + "' not loaded, ignoring it");
                return;
            }

            if (messageCause == ClanIntegrationMessage.MessageCause.CREATION) {

                Member owner = integrationMessage.getTarget();
                Player player = Bukkit.getPlayer(owner.getUniqueId());

                if (player == null)
                    return;

                clan = new Clan(integrationMessage.getIndex(), integrationMessage.getClanName(), integrationMessage.getClanTag(), 18, System.currentTimeMillis(), 0, "GRAY");
                clan.getMembers().add(owner);
                Constants.getClanService().add(clan);

                Account account = Account.fetch(player.getUniqueId());
                account.getData(Columns.CLAN).setData(clan.getIndex());

                final int cost = integrationMessage.getCost();

                if (cost > 0) {
                    Bukkit.getScheduler().runTaskAsynchronously(BukkitGame.getEngine(), () -> {
                        account.getDataStorage().loadIfUnloaded(Columns.HG_COINS);
                        account.removeInt(cost, Columns.HG_COINS);
                        account.getDataStorage().saveColumn(Columns.HG_COINS);
                    });
                }
            } else if (messageCause == ClanIntegrationMessage.MessageCause.DISBAND) {

                clan.getMembers().forEach(member -> {

                    Player player = Bukkit.getPlayer(member.getUniqueId());

                    if (player == null)
                        return;

                    Account account = Account.fetch(player.getUniqueId());
                    account.getData(Columns.CLAN).setData(-1);
                    account.getData(Columns.CLAN).setChanged(false);
                });

                Constants.getClanService().forget(clan);
            } else if (messageCause == ClanIntegrationMessage.MessageCause.MEMBER_JOIN) {
                clan.getMembers().add(integrationMessage.getTarget());

                Account account = Account.fetch(integrationMessage.getTarget().getUniqueId());

                if (account != null)
                    account.getData(Columns.CLAN).setData(integrationMessage.getIndex());

            } else if (messageCause == ClanIntegrationMessage.MessageCause.MEMBER_LEFT) {
                clan.getMembers().remove(integrationMessage.getTarget());

                Account account = Account.fetch(integrationMessage.getTarget().getUniqueId());

                if (account != null) {
                    account.getData(Columns.CLAN).setData(-1);
                    account.getData(Columns.CLAN).setChanged(false);
                }
            }

            clan.getMembers().forEach(member -> {

                Player player = Bukkit.getPlayer(member.getUniqueId());

                if (player == null) {
                    return;
                }

                Account account = Account.fetch(player.getUniqueId());

                new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class)).fire();
            });
        }
    }

    @EventHandler
    public void onPlayerInteractVanish(PlayerInteractEntityEvent event) {

        if (Constants.getServerCategory() == ServerCategory.LOBBY)
            return;

        if (!Vanish.getInstance().isVanished(event.getPlayer().getUniqueId()))
            return;

        if (!(event.getRightClicked() instanceof Player))
            return;

        event.getPlayer().performCommand("invsee " + event.getRightClicked().getName());
    }

    private final List<String> DEFAULT_COMMANDS = Arrays.asList("about", "pl", "ver", "version", "plugins");

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTabCompletedEvent(PlayerCommandTabCompleteEvent event) {

        for (BukkitCommand bukkitCommand : BukkitGame.getEngine().getBukkitFrame().getBukkitCommandList()) { // BUKKIT COMMANDS

            if (!event.getAccount().hasPermission(bukkitCommand.getCommandInfo().getRank()))
                continue;

            if (startsWith("/" + bukkitCommand.getCommandInfo().getName(), event.getMessage()))
                event.getCompleterList().add("/" + bukkitCommand.getCommandInfo().getName());

            for (String str : bukkitCommand.getCommandInfo().getAliases()) {
                if (startsWith("/" + str, event.getMessage()))
                    event.getCompleterList().add("/" + str);
            }
        }

        for (CommandInfo commandInfo : BukkitGame.getEngine().getBukkitFrame().getProxyCommands()) { // PROXY COMMANDS

            if (!event.getAccount().hasPermission(commandInfo.getRank()) && !event.getAccount().hasPermission("command." + commandInfo.getName()))
                continue;

            if (startsWith("/" + commandInfo.getName(), event.getMessage()))
                event.getCompleterList().add("/" + commandInfo.getName());

            for (String str : commandInfo.getAliases()) {
                if (startsWith("/" + str, event.getMessage()))
                    event.getCompleterList().add("/" + str);
            }
        }

        for (String str : DEFAULT_COMMANDS) {
            if (startsWith("/" + str, event.getMessage()))
                event.getCompleterList().add("/" + str);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPreprocessEvent(PlayerCommandPreprocessEvent event) {

        Account account = Account.fetch(event.getPlayer().getUniqueId());

        if (account.hasPermission(Rank.STREAMER_PLUS))
            return;

        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(event.getPlayer().getUniqueId(), "command.cooldown");

        if (cooldown != null && !cooldown.expired()) {
            event.getPlayer().sendMessage(account.getLanguage().translate("wait_to_execute_commands", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            event.setCancelled(true);
        } else {
            CooldownProvider.getGenericInstance().addCooldown(event.getPlayer().getUniqueId(), "command.cooldown", 1, false);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onWordCensor(AsyncPlayerChatEvent event) {

        Account account = Account.fetch(event.getPlayer().getUniqueId());

        if (!account.getPreference(Preference.CHAT)) {
            event.getPlayer().sendMessage(account.getLanguage().translate("chat.prefs.not_enabled"));
            event.setCancelled(true);
            return;
        }

        WordCensor censor = BukkitGame.getEngine().getWordCensor();

        if (account.getRank().getId() < Rank.ADMINISTRATOR.getId())
            event.setMessage(censor.filter(event.getMessage()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        Set<Player> recipients = event.getRecipients();

        recipients.removeIf(p -> p == null || !p.getWorld().getUID().equals(player.getWorld().getUID()));

        Account account = Account.fetch(player.getUniqueId());

        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(event.getPlayer().getUniqueId(), "chat.cooldown");

        boolean isStaff = account.hasPermission(Rank.STREAMER_PLUS);

        if (!isStaff) {
            if (cooldown != null && !cooldown.expired()) {
                event.getPlayer().sendMessage(account.getLanguage().translate("wait_to_chat", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                return;
            } else {
                CooldownProvider.getGenericInstance().addCooldown(event.getPlayer().getUniqueId(), "chat.cooldown", 3, false);
            }
        }

        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
        Medal medal = account.getProperty("account_medal").getAs(Medal.class);

        if (isStaff)
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));

        recipients.forEach(recipient -> {
            Account account_recipient = Account.fetch(recipient.getUniqueId());

            if (account_recipient == null)
                return;

            if (!isStaff && !account_recipient.getPreference(Preference.CHAT))
                return;

            PrefixType prefixType = account_recipient.getProperty("account_prefix_type").getAs(PrefixType.class);

            TextComponent textComponent = new TextComponent(medal == Medal.NONE ? "" : medal.getColor() + medal.getIcon() + " ");
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(medal.getFormattedName() + "§f: §7" + medal.getDescription()).create()));

            TextComponent textComponent1 = new TextComponent((tag == Tag.MEMBER ? tag.getMemberSetting(prefixType) : prefixType.getFormatter().format(tag)) + account.getDisplayName() + " §7»§r " + event.getMessage());

            recipient.sendMessage(textComponent, textComponent1);
        });
    }

    @EventHandler
    public void onPlayerChangedTagEvent(PlayerUpdateTablistEvent event) {

        Account account = event.getAccount();

        Tag playerTag = event.getTag();
        PrefixType playerPrefixtype = event.getPrefixType();

        Player player = Bukkit.getPlayer(account.getUniqueId());

        if (player == null)
            return;

        String teamOrder = "tag:" + playerTag.getOrder() + account.getRanking().getOrder() + player.getEntityId();

        PlayerTeamAssignEvent assignEvent = (PlayerTeamAssignEvent) new PlayerTeamAssignEvent(account, player, createTeamIfNotExists(player, player.getName(), teamOrder, (playerTag == Tag.MEMBER ? playerTag.getMemberSetting(playerPrefixtype) : playerPrefixtype.getFormatter().format(playerTag)), "")).fire();
        Team selfView = assignEvent.getTeam();

        for (Team old : player.getScoreboard().getTeams()) {
            if (old.hasEntry(player.getName()) && !old.getName().equals(selfView.getName()))
                old.unregister();
        }

        for (Player players : player.getWorld().getPlayers()) {

            if (players.getEntityId() == player.getEntityId())
                continue;

            Account accounts = Account.fetch(players.getUniqueId());

            Tag playersTag = accounts.getProperty("account_tag").getAs(Tag.class);

            String playersTeamOrder = "tag:" + playersTag.getOrder() + accounts.getRanking().getOrder() + players.getEntityId();
            playersTeamOrder = playersTeamOrder + Constants.KEY(16 - playersTeamOrder.length(), true);

            PlayerTeamAssignEvent teamAssignEvent = (PlayerTeamAssignEvent) new PlayerTeamAssignEvent(accounts, player, createTeamIfNotExists(player, players.getName(), playersTeamOrder, (playersTag == Tag.MEMBER ? playersTag.getMemberSetting(playerPrefixtype) : playerPrefixtype.getFormatter().format(playersTag)), "")).fire();
            Team to = teamAssignEvent.getTeam();

            for (Team old2 : player.getScoreboard().getTeams()) {
                if (old2.hasEntry(players.getName()) && !old2.getName().equals(to.getName()))
                    old2.unregister();
            }
            PrefixType playersPrefixType = accounts.getProperty("account_prefix_type").getAs(PrefixType.class);
            new PlayerTeamAssignEvent(account, players, createTeamIfNotExists(players, player.getName(), teamOrder, (playerTag == Tag.MEMBER ? playersTag.getMemberSetting(playerPrefixtype) : playersPrefixType.getFormatter().format(playerTag)), "")).fire();
        }
    }

    public Team createTeamIfNotExists(Player p, String entry, String teamID, String prefix, String suffix) {
        if (p.getScoreboard() == null)
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        Team team = p.getScoreboard().getTeam(teamID);
        if (team == null)
            team = p.getScoreboard().registerNewTeam(teamID);
        if (!team.hasEntry(entry))
            team.addEntry(entry);

        team.setPrefix(prefix);
        team.setSuffix(suffix);
        return team;
    }

    @Variable(name = "view_distance", permission = Rank.ADMINISTRATOR)
    private int viewDistance = Bukkit.getViewDistance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (player.spigot().getViewDistance() != viewDistance)
            player.spigot().setViewDistance(viewDistance);

        player.setTicksLived(1);

        Account account = Account.fetch(player.getUniqueId());

        player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());

        BukkitGame.getEngine().getNPCProvider().getPacketInjector().addPlayer(player);

        String customName = account.getData(Columns.NICK).getAsString();

        if (account.hasCustomName()) {
            PlayerDisguise.changeNickname(player, customName, false);
        }

        System.out.println("Selected knockback: " + ((CraftPlayer) player).getHandle().getKnockback().getName());

        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

        Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));

        Rank rank = account.getRank();

        account.setVersion(ViaVersionPlugin.getInstance().getApi().getPlayerVersion(player));

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(tablistPacket);

        final Knockback knockback = BukkitGame.getEngine().getKnockbackService().getKnockbackByName("New");
        if (knockback != null)
            BukkitGame.getEngine().getKnockbackService().setKnockback(player, BukkitGame.getEngine().getKnockbackService().getKnockbackByName("New"));

        Vanish.getInstance().getPlayerVanish().forEach((uuid, vanishRank) -> {

            Player p = Bukkit.getPlayer(uuid);

            if (p == null)
                return;

            if (rank.getCategory().getImportance() < vanishRank.getCategory().getImportance()) {

                PlayerHideEvent playerHideEvent = new PlayerHideEvent(p, player);
                Bukkit.getPluginManager().callEvent(playerHideEvent);

                if (!playerHideEvent.isCancelled())
                    player.hidePlayer(p);

            } else {

                PlayerShowEvent playerShowEvent = new PlayerShowEvent(p, player);
                Bukkit.getPluginManager().callEvent(playerShowEvent);

                if (!playerShowEvent.isCancelled())
                    player.showPlayer(p);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        BukkitGame.getEngine().getNPCProvider().getPacketInjector().removePlayer(player);

        Scoreboard board = player.getScoreboard();
        if (board != null) {
            for (Team t : board.getTeams())
                t.unregister();
            for (Objective ob : board.getObjectives())
                ob.unregister();
        }

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        player.setDisplayName(player.getName());

        player.getScoreboard().getTeams().forEach(t -> {
            if (t.getName().startsWith("tag:"))
                t.unregister();
        });

        event.getPlayer().setOp(false);

        Vanish.getInstance().getPlayerVanish().remove(player.getUniqueId());

        for (Player on : Bukkit.getOnlinePlayers()) {
            if (!on.equals(player) && !on.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
                on.getScoreboard().getTeams().forEach(team -> {
                    if (team.getName().startsWith("tag:")) {
                        team.removeEntry(player.getName());
                        if (team.getEntries().isEmpty()) {
                            team.unregister();
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getSlotType() == InventoryType.SlotType.OUTSIDE || e.getCurrentItem() == null)
            return;
        if (e.getInventory().getHolder() instanceof Selector.Holder) {
            e.setCancelled(true);
            Selector.Holder holder = (Selector.Holder) e.getInventory().getHolder();
            Selector selector = holder.getSelector();
            Player player = (Player) e.getWhoClicked();
            if (selector.getBackSlot() == e.getSlot()) {
                selector.getBackConsumer().accept(player);
            } else if (selector.getNextPageSlot() == e.getSlot()) {
                if (selector.hasPage(holder.getPage() + 1)) {
                    selector.open(player, holder.getPage() + 1);
                }
            } else if (selector.getPreviousPageSlot() == e.getSlot()) {
                if (selector.hasPage(holder.getPage() - 1)) {
                    selector.open(player, holder.getPage() - 1);
                }
            } else if (selector.getAllowedSlots().contains(e.getSlot())) {
                if (e.getCurrentItem().getType() != Material.AIR) {
                    holder.getSelector().getOnChooseItem().accept(player, e.getCurrentItem());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof Selector.Holder) {

            Selector.Holder holder = (Selector.Holder) e.getInventory().getHolder();
            Selector selector = holder.getSelector();
            selector.removePlayer((Player) e.getPlayer());
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

    private final PacketPlayOutPlayerListHeaderFooter tablistPacket = buildPacket("\n§b§lYOLO\n", "\n§fVisite nosso site: §a" + Constants.SERVER_WEBSITE + "\n");

    public PacketPlayOutPlayerListHeaderFooter buildPacket(String head, String foot) {
        IChatBaseComponent header = IChatBaseComponent.ChatSerializer.a("{'color':'', 'text':'" + head + "'}");
        IChatBaseComponent footer = IChatBaseComponent.ChatSerializer.a("{'color':'', 'text':'" + foot + "'}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        try {
            FieldHelper.setValue(packet, "a", header);
            FieldHelper.setValue(packet, "b", footer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packet;
    }
}
