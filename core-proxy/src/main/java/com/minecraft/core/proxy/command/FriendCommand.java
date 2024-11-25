package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.account.friend.Friend;
import com.minecraft.core.account.friend.FriendRequest;
import com.minecraft.core.account.friend.FriendStatus;
import com.minecraft.core.account.friend.status.FriendStatusUpdate;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.database.mojang.MojangAPI;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FriendCommand implements ProxyInterface {

    @Command(name = "friend", aliases = {"f", "amigo", "amiga", "amigos", "amigas", "amigue", "amigues"}, platform = Platform.PLAYER)
    public void onFriendCommand(Context<ProxiedPlayer> context) {
        ProxiedPlayer player = context.getSender();

        if (context.argsCount() == 0) {
            Argument.HELP.execute(context);
        } else {
            Argument argument = Argument.fetch(context.getArg(0));

            if (argument == null) {
                BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), "friend add " + context.getArg(0));
                return;
            }

            if (argument.getMinimumArgs() > context.argsCount()) {
                Argument.HELP.execute(context);
                return;
            }
            argument.execute(context);
        }

    }

    @Completer(name = "friend")
    public List<String> handleComplete(Context<CommandSender> context) {
        List<String> list = new ArrayList<>();
        if (context.argsCount() == 1) {
            for (Argument argument : Argument.values()) {
                list.addAll(List.of(argument.getField()));
                list.addAll(getOnlineNicknames(context));
            }
            return list;
        }

        if (context.getArg(2).equalsIgnoreCase("status")) {
            list.clear();
            list.addAll(Arrays.asList("online", "offline"));
            return list;
        }

        return getOnlineNicknames(context);

    }

    @Getter
    public enum Argument implements ProxyInterface {
        ADD(2, "add", "adicionar") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {
                ProxiedPlayer player = context.getSender();
                String targetName = context.getArg(1);

                if (player.getName().equalsIgnoreCase(targetName)) {
                    context.sendMessage("§cVocê não pode adicionar você mesmo.");
                    return;
                }

                search(context, targetName, target -> {
                    target.loadSentFriendRequests();
                    target.loadFriends();
                    target.loadReceivedFriendRequests();
                    target.loadRanks();
                    target.getDataStorage().loadColumns(Collections.singletonList(Columns.TAG));

                    if (target == null) {
                        context.sendMessage("§cJogador não encontrado.");
                        return;
                    }

                    Account account = Account.fetch(player.getUniqueId());

                    if (account.getFriends().stream().anyMatch(friend -> friend.getUniqueId().equals(target.getUniqueId()))) {
                        context.sendMessage("§cVocê já é amigo desse jogador.");
                        return;
                    }

                    if (account.getFriends().size() >= 150) {
                        context.sendMessage("§cVocê atingiu o limite de amigos.");
                        return;
                    }

                    if (account.hasSentFriendRequest(target.getUniqueId())) {
                        context.sendMessage("§cVocê já enviou um pedido de amizade para esse jogador.");
                        return;
                    }

                    if (target.getPendingFriendRequests().stream().anyMatch(r -> r.getSenderUniqueId().equals(account.getUniqueId()))) {

                        FriendRequest request = target.getSentRequests().stream().filter(r -> r.getSenderUniqueId().equals(account.getUniqueId())).findFirst().orElse(null);

                        Friend sender = new Friend(account.getUsername(), account.getUniqueId(), System.currentTimeMillis());
                        Friend targetFriend = new Friend(target.getUsername(), target.getUniqueId(), System.currentTimeMillis());

                        target.addFriend(sender);
                        account.addFriend(targetFriend);
                        context.sendMessage("§aVocê aceitou o pedido de amizade de " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + "§a.");

                        target.cancelFriendRequest(request);
                        account.removeReceivedFriendRequest(request);

                        ProxiedPlayer targetPlayer = BungeeCord.getInstance().getPlayer(target.getUniqueId());

                        if (targetPlayer != null) {
                            targetPlayer.sendMessage("§aSeu pedido de amizade foi aceito por " + Tag.fromUniqueCode(account.getData(Columns.TAG).getAsString()).getFormattedColor() + account.getDisplayName() + "§a.");
                        }

                        async(() -> target.getDataStorage().saveTable(Tables.ACCOUNT));
                        async(() -> account.getDataStorage().saveTable(Tables.ACCOUNT));
                        return;
                    }

                    if (!target.getPreference(Preference.FRIEND_REQUEST)) {
                        context.sendMessage("§cEsse jogador não aceita pedidos de amizade.");
                        return;
                    }

                    FriendRequest request = new FriendRequest(target.getUsername(), target.getUniqueId(), FriendRequest.Status.PENDING, account.getUsername(), account.getUniqueId());

                    account.addSentFriendRequest(request);
                    target.addReceivedFriendRequest(request);

                    context.sendMessage("§aPedido de amizade enviado para " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + "§a.");

                    ProxiedPlayer targetPlayer = BungeeCord.getInstance().getPlayer(target.getUniqueId());

                    if (targetPlayer != null) {
                        TextComponent info = new TextComponent("§eVocê recebeu um pedido de amizade de " + Tag.fromUniqueCode(account.getData(Columns.TAG).getAsString()).getFormattedColor() + account.getDisplayName() + " ");
                        TextComponent accept = new TextComponent("§a§lACEITAR");
                        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§aClique para aceitar.")}));
                        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + account.getUsername()));

                        TextComponent divisa = new TextComponent(" §7/ ");

                        TextComponent deny = new TextComponent("§c§lRECUSAR");
                        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§cClique para recusar.")}));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + account.getUsername()));

                        targetPlayer.sendMessage(info, accept, divisa, deny);
                    }


                    async(() -> target.getDataStorage().saveTable(Tables.ACCOUNT));
                    async(() -> account.getDataStorage().saveTable(Tables.ACCOUNT));

                });

            }

        },
        REMOVE(2, "remove", "remover") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {

                ProxiedPlayer player = context.getSender();
                String targetName = context.getArg(1);

                search(context, targetName, target -> {

                    target.loadTags();
                    target.loadRanks();
                    target.loadSentFriendRequests();
                    target.loadReceivedFriendRequests();
                    target.loadFriends();
                    target.getDataStorage().loadColumns(Collections.singletonList(Columns.TAG));

                    Account account = Account.fetch(player.getUniqueId());

                    if (account.getFriends().stream().noneMatch(friend -> friend.getUniqueId().equals(target.getUniqueId()))) {
                        context.sendMessage("§cVocê não é amigo desse jogador.");
                        return;
                    }

                    Friend friend = account.getFriends().stream().filter(f -> f.getUniqueId().equals(target.getUniqueId())).findFirst().orElse(null);
                    Friend sender = target.getFriends().stream().filter(f -> f.getUniqueId().equals(account.getUniqueId())).findFirst().orElse(null);

                    account.removeFriend(friend);
                    target.removeFriend(sender);

                    context.sendMessage("§aVocê removeu " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + "§a da sua lista de amigos.");

                    async(() -> target.getDataStorage().saveTable(Tables.ACCOUNT));
                    async(() -> account.getDataStorage().saveTable(Tables.ACCOUNT));

                });

            }
        },
        LIST(1, "list", "lista") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {
                ProxiedPlayer player = context.getSender();
                Account account = Account.fetch(player.getUniqueId());

                if (account.getFriends().isEmpty()) {
                    context.sendMessage("§cVocê não tem amigos.");
                    return;
                }

                int page = 1;
                if (context.argsCount() > 1) {
                    page = Integer.parseInt(context.getArg(1));
                }

                int maxPage = (int) Math.ceil(account.getFriends().size() / 10.0);

                if (page > maxPage) {
                    context.sendMessage("§cPágina inválida.");
                    return;
                }

                List<Friend> friends = getFriends(account);

                context.sendMessage("§eSeus amigos §a(" + account.getFriends().size() + "/150) " + " §7(Página " + page + "/" + maxPage + "):");

                for (int i = (page - 1) * 10; i < page * 10; i++) {
                    if (i >= friends.size())
                        break;

                    Friend friend = friends.get(i);
                    search(context, friend.getName(), target -> {
                        target.loadRanks();
                        target.loadTags();
                        target.loadRanks();
                        target.loadSentFriendRequests();
                        target.loadReceivedFriendRequests();
                        target.loadFriends();


                        context.sendMessage(target.getRank().getDefaultTag().getFormattedColor() + target.getUsername() + " " + getStatus(target));
                    });
                }
            }

            public String getStatus(Account friend) {
                ProxiedPlayer target = BungeeCord.getInstance().getPlayer(friend.getUniqueId());
                if (target == null) {
                    return "§cestá offline.";
                } else {
                    if (FriendStatus.valueOf(friend.getData(Columns.FRIEND_STATUS).getAsString()) == FriendStatus.ONLINE) {
                        return "§eestá em §b" + Constants.getServerStorage().getServer(target.getServer().getInfo().getName()).getServerType().getName() + "§e.";
                    } else {
                        return "§cestá offline.";
                    }
                }
            }

            @NotNull
            private List<Friend> getFriends(Account account) {
                List<Friend> friends = new ArrayList<>(account.getFriends());
                friends.sort((f1, f2) -> {
                    ProxiedPlayer p1 = BungeeCord.getInstance().getPlayer(f1.getUniqueId());
                    ProxiedPlayer p2 = BungeeCord.getInstance().getPlayer(f2.getUniqueId());
                    FriendStatus status1 = FriendStatus.valueOf(account.getData(Columns.FRIEND_STATUS).getAsString());
                    FriendStatus status2 = FriendStatus.valueOf(account.getData(Columns.FRIEND_STATUS).getAsString());

                    int onlineComparison = Boolean.compare(p2 != null, p1 != null);
                    if (onlineComparison != 0) {
                        return onlineComparison;
                    }

                    if (p1 != null && p2 != null) {
                        int statusComparison = status1.compareTo(status2);
                        if (statusComparison != 0) {
                            return statusComparison;
                        }
                    }

                    if (p1 == null && p2 == null) {
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    }

                    return status1.compareTo(status2);
                });
                return friends;
            }
        },
        ACCEPT(2, "accept", "aceitar") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {

                ProxiedPlayer player = context.getSender();
                String targetName = context.getArg(1);

                search(context, targetName, target -> {

                    target.loadTags();
                    target.loadRanks();
                    target.loadSentFriendRequests();
                    target.loadReceivedFriendRequests();
                    target.loadFriends();
                    target.getDataStorage().loadColumns(Collections.singletonList(Columns.TAG));

                    Account account = Account.fetch(player.getUniqueId());

                    if (!target.hasSentFriendRequest(account.getUniqueId())) {
                        context.sendMessage("§cEsse jogador não enviou um pedido de amizade para você.");
                        return;
                    }

                    if (!account.hasReceivedFriendRequest(target.getUniqueId())) {
                        context.sendMessage("§cVocê não recebeu um pedido de amizade desse jogador.");
                        return;
                    }

                    FriendRequest request = account.getReceivedRequests().stream().filter(r -> r.getSenderUniqueId().equals(target.getUniqueId())).findFirst().orElse(null);

                    if (request == null) {
                        context.sendMessage("§cVocê não recebeu um pedido de amizade desse jogador.");
                        return;
                    }

                    target.cancelFriendRequest(request);

                    account.removeReceivedFriendRequest(request);

                    Friend targetFriend = new Friend(target.getUsername(), target.getUniqueId(), System.currentTimeMillis());
                    Friend sender = new Friend(account.getUsername(), account.getUniqueId(), System.currentTimeMillis());

                    account.addFriend(targetFriend);
                    target.addFriend(sender);

                    context.sendMessage("§aVocê aceitou o pedido de amizade de " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + "§a.");

                    ProxiedPlayer targetPlayer = BungeeCord.getInstance().getPlayer(target.getUniqueId());

                    if (targetPlayer != null) {
                        targetPlayer.sendMessage("§aSeu pedido de amizade foi aceito por " + Tag.fromUniqueCode(account.getData(Columns.TAG).getAsString()).getFormattedColor() + account.getDisplayName() + "§a.");
                    }

                    async(() -> target.getDataStorage().saveTable(Tables.ACCOUNT));
                    async(() -> account.getDataStorage().saveTable(Tables.ACCOUNT));

                });
            }
        },
        DENY(2, "deny", "recusar", "negar") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {

                ProxiedPlayer player = context.getSender();
                String targetName = context.getArg(1);

                search(context, targetName, target -> {

                    target.loadTags();
                    target.loadRanks();
                    target.loadSentFriendRequests();
                    target.loadReceivedFriendRequests();
                    target.loadFriends();
                    target.getDataStorage().loadColumns(Collections.singletonList(Columns.TAG));

                    Account account = Account.fetch(player.getUniqueId());

                    if (!target.hasSentFriendRequest(account.getUniqueId())) {
                        context.sendMessage("§cEsse jogador não enviou um pedido de amizade para você.");
                        return;
                    }

                    if (!account.hasReceivedFriendRequest(target.getUniqueId())) {
                        context.sendMessage("§cVocê não recebeu um pedido de amizade desse jogador.");
                        return;
                    }

                    FriendRequest request = account.getReceivedRequests().stream().filter(r -> r.getSenderUniqueId().equals(target.getUniqueId())).findFirst().orElse(null);

                    if (request == null) {
                        context.sendMessage("§cVocê não recebeu um pedido de amizade desse jogador.");
                        return;
                    }

                    target.cancelFriendRequest(request);

                    account.removeReceivedFriendRequest(request);

                    context.sendMessage("§aVocê negou o pedido de amizade de " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + "§a.");
                    async(() -> target.getDataStorage().saveTable(Tables.ACCOUNT));
                    async(() -> account.getDataStorage().saveTable(Tables.ACCOUNT));

                });

            }
        },
        REQUESTS(1, "requests", "pedidos", "pedido") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {
                ProxiedPlayer player = context.getSender();
                Account account = Account.fetch(player.getUniqueId());

                if (account.getPendingFriendRequests().isEmpty() && account.getReceivedPendingRequests().isEmpty()) {
                    context.sendMessage("§cVocê não tem pedidos de amizade.");
                    return;
                }

                int page = 1;
                if (context.argsCount() > 1) {
                    try {
                        page = Integer.parseInt(context.getArg(1));
                    } catch (NumberFormatException e) {
                        context.sendMessage("§cPágina inválida.");
                        return;
                    }
                }

                int totalRequests = account.getPendingFriendRequests().size() + account.getReceivedPendingRequests().size();
                int maxPage = (int) Math.ceil(totalRequests / 10.0);

                if (page > maxPage || page < 1) {
                    context.sendMessage("§cPágina inválida.");
                    return;
                }

                context.sendMessage("§ePedidos de amizade §a(" + totalRequests + ") §7(Página " + page + "/" + maxPage + "):");

                for (int i = (page - 1) * 10; i < page * 10; i++) {
                    if (i >= totalRequests)
                        break;

                    if (i < account.getPendingFriendRequests().size()) {
                        FriendRequest request = account.getPendingFriendRequests().get(i);
                        Account target = Account.fetch(request.getReceiver());
                        if (target != null) {
                            displaySent(context, target);
                        } else {
                            search(context, request.getReceiverName(), targetOffline -> {
                                displaySent(context, targetOffline);
                            });
                        }
                    } else {
                        FriendRequest request = account.getReceivedPendingRequests().get(i - account.getPendingFriendRequests().size());
                        Account target = Account.fetch(request.getSenderUniqueId());
                        if (target != null) {
                            displayReceived(context, target);
                        } else {
                            search(context, request.getSenderName(), targetOffline -> {
                                displayReceived(context, targetOffline);
                            });
                        }
                    }
                }
            }

            public void displaySent(Context<ProxiedPlayer> context, Account target) {

                target.loadTags();
                target.loadRanks();
                target.loadSentFriendRequests();
                target.loadReceivedFriendRequests();
                target.loadFriends();
                target.getDataStorage().loadColumns(Collections.singletonList(Columns.TAG));

                TextComponent info = new TextComponent("§ePedido para " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + " ");
                TextComponent cancel = new TextComponent("§c§lCANCELAR");
                cancel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§cClique para cancelar.")}));
                cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend cancel " + target.getUsername()));

                context.getSender().sendMessage(info, cancel);
            }

            public void displayReceived(Context<ProxiedPlayer> context, Account target) {

                target.loadTags();
                target.loadRanks();
                target.loadSentFriendRequests();
                target.loadReceivedFriendRequests();
                target.loadFriends();
                target.getDataStorage().loadColumns(Collections.singletonList(Columns.TAG));

                TextComponent info = new TextComponent("§ePedido de " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + " ");
                TextComponent accept = new TextComponent("§a§lACEITAR");
                accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§aClique para aceitar.")}));
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + target.getUsername()));

                TextComponent divisa = new TextComponent(" §7/ ");

                TextComponent deny = new TextComponent("§c§lRECUSAR");
                deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§cClique para recusar.")}));
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + target.getUsername()));

                context.getSender().sendMessage(info, accept, divisa, deny);
            }
        },
        STATUS(2, "status") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {
                ProxiedPlayer player = context.getSender();
                Account account = Account.fetch(player.getUniqueId());

                String status = context.getArg(1);

                if (status.equalsIgnoreCase("online")) {

                    account.getData(Columns.FRIEND_STATUS).setData(FriendStatus.ONLINE.name());
                    FriendStatusUpdate update = new FriendStatusUpdate(new Friend(account.getUsername(), account.getUniqueId(), System.currentTimeMillis()), null, FriendStatus.ONLINE, FriendStatusUpdate.Update.STATUS);

                    Constants.getRedis().publish(Redis.FRIEND_UPDATE_CHANNEL, Constants.GSON.toJson(status));

                    context.sendMessage("§aSeu status foi alterado para §aONLINE§a.");
                } else if (status.equalsIgnoreCase("offline")) {
                    account.getData(Columns.FRIEND_STATUS).setData(FriendStatus.OFFLINE.name());
                    FriendStatusUpdate update = new FriendStatusUpdate(new Friend(account.getUsername(), account.getUniqueId(), System.currentTimeMillis()), null, FriendStatus.OFFLINE, FriendStatusUpdate.Update.STATUS);
                    Constants.getRedis().publish(Redis.FRIEND_UPDATE_CHANNEL, Constants.GSON.toJson(status));
                    context.sendMessage("§aSeu status foi alterado para §cOFFLINE§a.");
                } else {
                    context.sendMessage("§cStatus inválido.");
                }

                async(() -> account.getDataStorage().saveTable(Tables.ACCOUNT));
            }
        },
        CANCEL(2, "cancel", "cancelar") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {

                ProxiedPlayer player = context.getSender();
                String targetName = context.getArg(1);

                search(context, targetName, target -> {
                    target.getDataStorage().loadColumns(Collections.singletonList(Columns.TAG));
                    target.loadFriends();
                    target.loadReceivedFriendRequests();
                    target.loadSentFriendRequests();

                    Account account = Account.fetch(player.getUniqueId());

                    if (!account.hasSentFriendRequest(target.getUniqueId())) {
                        context.sendMessage("§cVocê não enviou um pedido de amizade para esse jogador.");
                        return;
                    }

                    if (!target.hasReceivedFriendRequest(account.getUniqueId())) {
                        context.sendMessage("§cEsse jogador não recebeu um pedido de amizade seu.");
                        return;
                    }

                    FriendRequest request = account.getSentRequests().stream().filter(r -> r.getReceiver().equals(target.getUniqueId())).findFirst().orElse(null);

                    if (request == null) {
                        context.sendMessage("§cVocê não enviou um pedido de amizade para esse jogador.");
                        return;
                    }

                    target.cancelFriendRequest(request);

                    account.removeReceivedFriendRequest(request);

                    context.sendMessage("§aVocê cancelou o pedido de amizade para " + Tag.fromUniqueCode(target.getData(Columns.TAG).getAsString()).getFormattedColor() + target.getDisplayName() + "§a.");

                    async(() -> target.getDataStorage().saveTable(Tables.ACCOUNT));
                    async(() -> account.getDataStorage().saveTable(Tables.ACCOUNT));

                });

            }
        },
        HELP(1, "help", "ajuda", "comandos", "comando") {
            @Override
            public void execute(Context<ProxiedPlayer> context) {
                context.sendMessage("§6Uso do §a/" + context.getLabel() + "§6:");
                context.sendMessage("§e/" + context.getLabel() + " <jogador> §e- §bAdicione um amigo.");
                context.sendMessage("§e/" + context.getLabel() + " remover <jogador> §e- §bRemove um amigo.");
                context.sendMessage("§e/" + context.getLabel() + " recusar <jogador> §e- §bRecusar pedido.");
                context.sendMessage("§e/" + context.getLabel() + " aceitar <jogador> §e- §bAceitar pedido.");
                context.sendMessage("§e/" + context.getLabel() + " status <status> §e- §bAltera o status de online.");
                context.sendMessage("§e/" + context.getLabel() + " list [página] §e- §bVer amigos.");
                context.sendMessage("§e/" + context.getLabel() + " pedidos [página] §e- §bVer pedidos.");
                context.sendMessage("§e/" + context.getLabel() + " cancelar <jogador> §e- §bCancela um pedido.");
            }
        };

        private final int minimumArgs;
        private final String[] field;

        Argument(int minimumArgs, java.lang.String... strings) {
            this.minimumArgs = minimumArgs;
            this.field = strings;
        }

        public static Argument fetch(String s) {
            for (Argument arg : values()) {
                for (String key : arg.getField()) {
                    if (key.equalsIgnoreCase(s))
                        return arg;
                }
            }
            return null;
        }

        public abstract void execute(Context<ProxiedPlayer> context);
    }

}
