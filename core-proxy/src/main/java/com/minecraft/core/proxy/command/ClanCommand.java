/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.AccountStorage;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.clan.communication.ClanIntegrationMessage;
import com.minecraft.core.clan.invite.Invite;
import com.minecraft.core.clan.member.Member;
import com.minecraft.core.clan.member.role.Role;
import com.minecraft.core.clan.service.ClanService;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.*;
import com.minecraft.core.proxy.util.chat.ChatType;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClanCommand implements ProxyInterface {

    private static final ClanService clanService = Constants.getClanService();

    @Command(name = "clan", platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context) {

        try {
            if (context.argsCount() == 0) {
                Argument.HELP.execute(context);
            } else {

                Argument argument = Argument.fetch(context.getArg(0));

                if (argument == null) {
                    Argument.HELP.execute(context);
                    return;
                }

                if (argument.getMinimumArgs() > context.argsCount()) {
                    Argument.HELP.execute(context);
                    return;
                }
                argument.execute(context);
            }
        } catch (SQLException e) {
            context.info("unexpected_error");
            e.printStackTrace();
        }
    }

    @Command(name = "cc", platform = Platform.PLAYER)
    public void clanChatCommand(Context<ProxiedPlayer> context) {
        try {
            Argument.CHAT.execute(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Completer(name = "clan")
    public List<String> completer(Context<CommandSender> context) {
        String[] args = context.getArgs();
        if (args.length == 1) {
            List<String> stringList = new ArrayList<>();
            for (Argument arg : Argument.values()) {

                if (arg == Argument.HELP)
                    continue;

                for (String str : arg.getField()) {
                    if (startsWith(str, args[0])) {
                        stringList.add(str);
                    }
                }
            }
            return stringList;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("cortag") || args[0].equalsIgnoreCase("tagcolor"))) {
            return context.getAccount().getClanTagList().getClanTags().stream().map(clantag -> clantag.getName().toLowerCase()).filter(s -> startsWith(s, context.getArg(1))).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Getter
    public enum Argument implements ProxyInterface {

        HELP(0, "help") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {
                context.sendMessage("§6Uso do §a/clan§6:");
                context.sendMessage("§e/clan ver [clan] - §bVeja as informações de um clan.");
                context.sendMessage("§e/clan membros [clan] - §bVeja os membros de um clan.");
                context.sendMessage("§e/clan criar <nome> <tag> - §bCrie um clan.");
                context.sendMessage("§e/clan apagar - §bAcabe com o seu clan.");
                context.sendMessage("§e/clan sair - §bSaia do seu clan.");
                context.sendMessage("§e/clan convidar <alvo> - §bConvide jogadores para seu clan.");
                context.sendMessage("§e/clan transferir <alvo> - §bTransfira a posse do clan para outro membro.");
                context.sendMessage("§e/clan promover <alvo> - §bPromova um membro do clan.");
                context.sendMessage("§e/clan rebaixar <alvo> - §bRebaixe um administrador do clan.");
                context.sendMessage("§e/clan expulsar <alvo> - §bExpulse um membro do clan.");
                context.sendMessage("§e/clan convites - §bVisualize os convites recentes.");
                context.sendMessage("§e/clan aceitar <clan> - §bAceite um convite de clan recebido.");
                context.sendMessage("§e/clan negar <clan> - §bNegue um convite de clan recebido.");
                context.sendMessage("§e/clan chat - §bEntre em um bate-papo particular com seu clan.");
            }
        },

        CHAT(1, "chat") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();
                ChatType chatType = account.getProperty("chat_type", ChatType.NORMAL).getAs(ChatType.class);

                if (chatType != ChatType.CLAN) {
                    account.setProperty("chat_type", ChatType.CLAN);
                    account.setProperty("old_chat_type", chatType);
                    context.sendMessage("§aVocê entrou no bate-papo do clan.");
                } else {

                    ChatType type = account.getProperty("old_chat_type", ChatType.NORMAL).getAs(ChatType.class);

                    account.setProperty("chat_type", type == ChatType.CLAN ? ChatType.NORMAL : type);
                    context.sendMessage("§cVocê saiu do bate-papo do clan.");
                }
            }
        },

        CREATE(3, "criar", "create") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account creator = context.getAccount();

                if (creator.hasClan()) {
                    context.sendMessage("§cVocê já faz parte de um clan.");
                    return;
                }

                String name = context.getArg(1);
                String tag = context.getArg(2);

                if (name.length() < 3 || name.length() > 16) {
                    context.sendMessage("§cO nome do clan deve ter entre 3 e 16 caracteres.");
                    return;
                }

                if (tag.length() < 3 || tag.length() > 8) {
                    context.sendMessage("§cA tag do clan deve ter entre 3 e 8 caracteres.");
                    return;
                }

                if (!Constants.isValid(name)) {
                    context.sendMessage("§cO nome do clan contém caracteres não alfa-numéricos.");
                    return;
                }

                if (!Constants.isValid(tag)) {
                    context.sendMessage("§cA tag do clan contém caracteres não alfa-numéricos.");
                    return;
                }

                if (clanService.isClanExists(name, tag)) {
                    context.sendMessage("§cJá existe um clan com esse nome ou tag.");
                    return;
                }

                int cost = 8000;

                if (creator.getRank().getId() < 1) {
                    creator.getDataStorage().loadColumns(Collections.singletonList(Columns.HG_COINS));

                    if (creator.getData(Columns.HG_COINS).getAsInt() < cost) {
                        context.sendMessage("§cVocê precisa de no mínimo " + cost + " coins no HG. VIPs podem criar clans de graça.");
                        return;
                    }
                } else cost = 0;

                Clan clan = new Clan(0, name, tag, 18, System.currentTimeMillis(), 0, "GRAY");

                try {
                    clanService.register(clan);
                } catch (SQLException e) {
                    context.info("unexpected_error");
                    e.printStackTrace();
                    return;
                }

                clan.join(creator, Role.OWNER);
                creator.getData(Columns.CLAN).setData(clan.getIndex());
                creator.getDataStorage().saveColumn(Columns.CLAN);
                clanService.pushClan(clan);
                context.sendMessage("§aO clan " + clan.getName() + " foi criado com sucesso.");

                ClanIntegrationMessage message = ClanIntegrationMessage.builder()
                        .messageCause(ClanIntegrationMessage.MessageCause.CREATION)
                        .clanTag(clan.getTag())
                        .index(clan.getIndex()).clanName(clan.getName())
                        .cost(cost).target(clan.getMember(creator.getUniqueId())).color(clan.getColor()).build();

                Constants.getRedis().publish(Redis.CLAN_INTEGRATION_CHANNEL, Constants.GSON.toJson(message));
            }
        },

        DELETE(1, "apagar", "delete") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (context.argsCount() > 1 && account.getRank().getId() >= Rank.DEVELOPER_ADMIN.getId()) {

                    String clanName = context.getArg(1);

                    if (!clanService.isClanExists(clanName, clanName)) {
                        context.info("object.not_found", "Clan");
                        return;
                    }

                    Clan clan = clanService.getClan(clanName);

                    if (clan == null) {
                        context.info("object.not_found", "Clan");
                        return;
                    }

                    try {
                        if (clanService.delete(clan)) {

                            for (Member clanMember : clan.getMembers()) {

                                Account memberAccount = Account.fetch(clanMember.getUniqueId());

                                if (memberAccount != null) {
                                    memberAccount.getData(Columns.CLAN).setData(-1);
                                    memberAccount.getDataStorage().saveColumn(Columns.CLAN);
                                }
                            }

                            ClanIntegrationMessage message = ClanIntegrationMessage.builder()
                                    .messageCause(ClanIntegrationMessage.MessageCause.DISBAND)
                                    .clanTag(clan.getTag())
                                    .index(clan.getIndex())
                                    .clanName(clan.getName()).color(clan.getColor())
                                    .build();

                            Constants.getRedis().publish(Redis.CLAN_INTEGRATION_CHANNEL, Constants.GSON.toJson(message));
                            context.sendMessage("§cClan '" + clan.getName() + "' apagada com sucesso.");
                        }
                    } catch (Exception e) {
                        context.info("unexpected_error");
                        e.printStackTrace();
                    }

                } else {
                    if (!account.hasClan()) {
                        context.sendMessage("§cVocê não faz parte de nenhum clan.");
                        return;
                    }

                    Clan clan = account.getClan();
                    Member member = clan.getMember(account.getUniqueId());

                    if (member.getRole() != Role.OWNER) {
                        context.sendMessage("§cApenas o dono do clan pode desfazê-lo.");
                        return;
                    }

                    try {
                        if (clanService.delete(clan)) {
                            search(context, member.getName(), memberAccount -> {
                                memberAccount.loadRanks();
                                sendMessage(clan, memberAccount.getRank().getDefaultTag().getFormattedColor() + memberAccount.getUsername() + " §eacabou com o clan.");
                            });

                            for (Member clanMember : clan.getMembers()) {

                                Account memberAccount = Account.fetch(clanMember.getUniqueId());

                                if (memberAccount != null) {
                                    memberAccount.getData(Columns.CLAN).setData(-1);
                                    memberAccount.getDataStorage().saveColumn(Columns.CLAN);
                                }
                            }

                            ClanIntegrationMessage message = ClanIntegrationMessage.builder()
                                    .messageCause(ClanIntegrationMessage.MessageCause.DISBAND)
                                    .clanTag(clan.getTag())
                                    .index(clan.getIndex())
                                    .clanName(clan.getName()).color(clan.getColor()).build();

                            Constants.getRedis().publish(Redis.CLAN_INTEGRATION_CHANNEL, Constants.GSON.toJson(message));
                        }
                    } catch (Exception e) {
                        context.info("unexpected_error");
                        e.printStackTrace();
                    }
                }


            }
        },

        TAGCOLOR(1, "cortag", "tagcolor") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {
                String[] args = context.getArgs();
                Account account = context.getAccount();

                Clan clan = Constants.getClanService().fetch(account.getData(Columns.CLAN).getAsInt());

                if (clan == null || !account.hasClan()) {
                    context.sendMessage("§cVocê precisa fazer parte de um clan para alterar sua clan tag.");
                    return;
                }

                account.getClanTagList().loadClanTags();

                if (args.length == 1) {
                    int max = account.getClanTagList().getClanTags().size() * 2;

                    TextComponent[] textComponents = new TextComponent[max];
                    textComponents[0] = new TextComponent("§aSuas Clan Tags: ");

                    int i = max - 1;

                    final Tag tag = Tag.fromUniqueCode(account.getData(Columns.TAG).getAsString());
                    final PrefixType prefixType = PrefixType.fromUniqueCode(account.getData(Columns.PREFIXTYPE).getAsString());

                    for (Clantag clantag : account.getClanTagList().getClanTags()) {
                        if (i < max - 1) {
                            textComponents[i] = new TextComponent("§f, ");
                            i -= 1;
                        }

                        String hoverDisplay = "§fExemplo: " + (tag == Tag.MEMBER ? tag.getMemberSetting(prefixType) : prefixType.getFormatter().format(tag)).replace("#", PlusColor.fromUniqueCode(account.getData(Columns.PLUSCOLOR).getAsString()).getColor() + "+") + account.getDisplayName() + " " + clantag.getColor() + "[" + clan.getTag().toUpperCase() + "]" + "\n\n§eClique para selecionar!";

                        TextComponent component = createTextComponent(clantag.getColor() + clantag.getName(), HoverEvent.Action.SHOW_TEXT, hoverDisplay, ClickEvent.Action.RUN_COMMAND, "/clantag " + clantag.getName());
                        textComponents[i] = component;
                        i -= 1;
                    }

                    context.getSender().sendMessage(textComponents);
                } else {
                    Clantag clantag = Clantag.fromName(args[1]);

                    if (clantag == null) {
                        context.info("command.clantag.generic_error");
                        return;
                    }

                    if (!account.getClanTagList().hasTag(clantag)) {
                        context.info("command.clantag.generic_error");
                        return;
                    }

                    if (clantag.getChatColor().equals(clan.getColor())) {
                        context.info("command.clantag.clantag_already_in_use");
                        return;
                    }

                    if (!clan.getMember(account.getUniqueId()).isAdmin()) {
                        context.sendMessage("§cVocê precisa ser um gerente do clan para alterar a clan tag.");
                        return;
                    }

                    context.info("command.clantag.clantag_change", clantag.getColor() + clantag.getName());

                    clan.setColor(clantag.getChatColor());
                    try {
                        Constants.getClanService().pushClan(clan);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    try (Jedis jedis = Constants.getRedis().getResource()) {
                        jedis.publish(Redis.CLAN_TAG_UPDATE, clan.getIndex() + ":" + clan.getColor());
                    }

                }
            }

            TextComponent createTextComponent(String name, HoverEvent.Action hoverAction, String hoverDisplay, ClickEvent.Action clickAction, String clickValue) {
                TextComponent textComponent = new TextComponent(name);
                textComponent.setHoverEvent(new HoverEvent(hoverAction, new TextComponent[]{new TextComponent(hoverDisplay)}));
                textComponent.setClickEvent(new ClickEvent(clickAction, clickValue));
                return textComponent;
            }

        },

        INVITE(2, "convidar", "invite") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (!account.hasClan()) {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                Clan clan = account.getClan();
                Member member = clan.getMember(account.getUniqueId());

                if (!member.isAdmin()) {
                    context.sendMessage("§cApenas administradores do clan podem convidar novos membros.");
                    return;
                }

                Account target = AccountStorage.getAccountByName(context.getArg(1), false);

                if (target == null) {
                    context.info("target.not_found");
                    return;
                }

                ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(target.getUniqueId());

                if (targetPlayer == null) {
                    context.info("target.not_found");
                    return;
                }

                if (target.hasClan()) {
                    context.sendMessage("§cEste jogador já faz parte de um clan.");
                    return;
                }

                if (clan.hasPendingInvite(target.getUniqueId())) {
                    context.sendMessage("§cJá há um convite pendente para este jogador.");
                    return;
                }

                if (clan.hasRecentInvite(target.getUniqueId())) {
                    context.sendMessage("§cEste jogador foi convidado recentemente, aguarde alguns minutos para convidá-lo novamente.");
                    return;
                }

                if (clan.isFull()) {
                    context.sendMessage("§cO clan está lotado.");
                    return;
                }

                Invite invite = new Invite(target.getUsername(), target.getUniqueId(), Invite.Status.PENDING, member);
                clan.getInvites().add(invite);
                search(context, target.getUsername(), memberAccount -> {
                    memberAccount.loadRanks();
                    sendMessage(clan, memberAccount.getRank().getDefaultTag().getFormattedColor() + memberAccount.getUsername() + " §efoi convidado para o clan.");
                });

                TextComponent interactable = new TextComponent(TextComponent
                        .fromLegacyText("§ePara aceitar o convite, §b§lCLIQUE AQUI"));
                interactable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/clan accept " + clan.getName().toLowerCase()));

                targetPlayer.sendMessage("");
                targetPlayer.sendMessage(TextComponent.fromLegacyText("§eVocê foi convidado para participar do clan §b" + clan.getName()));
                targetPlayer.sendMessage(interactable);
                targetPlayer.sendMessage("");
            }
        },

        TRANSFER(2, "transferir", "transfer") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (!account.hasClan()) {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                Clan clan = account.getClan();
                Member member = clan.getMember(account.getUniqueId());

                if (member.getRole() != Role.OWNER) {
                    context.sendMessage("§cApenas donos podem transferir a posse do clan.");
                    return;
                }

                Member target = clan.getMember(context.getArg(1));

                if (target == null) {
                    context.sendMessage("§cEste jogador não faz parte do clan.");
                    return;
                }

                target.setRole(Role.OWNER);
                member.setRole(Role.MEMBER);

                search(context, target.getName(), memberAccount -> {
                    memberAccount.loadRanks();
                    sendMessage(clan, memberAccount.getRank().getDefaultTag().getFormattedColor() + target.getName() + " §eagora é dono do clan.");

                });

                clanService.pushClan(clan);
            }
        },

        PROMOTE(2, "promover", "promote") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (!account.hasClan()) {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                Clan clan = account.getClan();
                Member member = clan.getMember(account.getUniqueId());

                if (member.getRole() != Role.OWNER) {
                    context.sendMessage("§cApenas o líder do clan pode promover membros.");
                    return;
                }

                Member target = clan.getMember(context.getArg(1));

                if (target == null) {
                    context.sendMessage("§cEste jogador não faz parte do clan.");
                    return;
                }

                if (target.isAdmin()) {
                    context.sendMessage("§cEste jogador já foi promovido.");
                    return;
                }

                Account targetAccount = Account.fetch(target.getUniqueId());
                target.setRole(Role.ADMINISTRATOR);
                search(context, target.getName(), memberAccount -> {
                    memberAccount.loadRanks();
                    sendMessage(clan, memberAccount.getRank().getDefaultTag().getFormattedColor() + target.getName() + " §efoi promovido para gerente.");
                });

                try {
                    clanService.pushClan(clan);
                } catch (SQLException e) {
                    context.info("§cHouve um erro inesperado, o jogador voltou ao seu cargo anterior por segurança.");
                    target.setRole(Role.MEMBER);
                    e.printStackTrace();
                }
            }
        },

        DEMOTE(2, "rebaixar", "demote") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (!account.hasClan()) {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                Clan clan = account.getClan();
                Member member = clan.getMember(account.getUniqueId());

                if (member.getRole() != Role.OWNER) {
                    context.sendMessage("§cApenas líderes podem rebaixar gerentes.");
                    return;
                }

                Member target = clan.getMember(context.getArg(1));

                if (target == null) {
                    context.sendMessage("§cEste jogador não faz parte do clan.");
                    return;
                }

                if (target.getRole() != Role.ADMINISTRATOR) {
                    context.sendMessage("§cEste jogador não é gerente do clan.");
                    return;
                }

                target.setRole(Role.MEMBER);
                search(context, target.getName(), memberAccount -> {
                    memberAccount.loadRanks();
                    sendMessage(clan, memberAccount.getRank().getDefaultTag().getFormattedColor() + target.getName() + " §enão é mais um Gerente.");
                });
                clanService.pushClan(clan);
            }
        },

        KICK(2, "expulsar", "kick") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (!account.hasClan()) {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                Clan clan = account.getClan();
                Member member = clan.getMember(account.getUniqueId());

                if (!member.isAdmin()) {
                    context.sendMessage("§cApenas gerentes podem expulsar jogadores.");
                    return;
                }

                Member target = clan.getMember(context.getArg(1));

                if (target == null) {
                    context.sendMessage("§cEste jogador não faz parte do clan.");
                    return;
                }

                if (member.getRole().getId() <= target.getRole().getId()) {
                    context.sendMessage("§cVocê não pode expulsar este membro.");
                    return;
                }

                search(context, target.getName(), memberAccount -> {
                    memberAccount.loadRanks();
                    sendMessage(clan, memberAccount.getRank().getDefaultTag().getFormattedColor() + target.getName() + " §efoi expulso do clan.");
                });

                clan.quit(target.getUniqueId());

                Account accountTarget = Account.fetch(target.getUniqueId());

                if (accountTarget != null) {
                    accountTarget.getDataStorage().getData(Columns.CLAN).setData(-1);
                    accountTarget.getDataStorage().saveColumn(Columns.CLAN);
                }

                clanService.pushClan(clan);

                ClanIntegrationMessage message = ClanIntegrationMessage.builder()
                        .messageCause(ClanIntegrationMessage.MessageCause.MEMBER_LEFT)
                        .clanTag(clan.getTag())
                        .index(clan.getIndex()).clanName(clan.getName())
                        .target(target).color(clan.getColor()).color(clan.getColor()).build();

                Constants.getRedis().publish(Redis.CLAN_INTEGRATION_CHANNEL, Constants.GSON.toJson(message));
            }
        },

        QUIT(1, "sair", "quit") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (!account.hasClan()) {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                Clan clan = account.getClan();
                Member member = clan.getMember(account.getUniqueId());

                if (member.getRole() == Role.OWNER) {
                    context.sendMessage("§cVocê não pode sair do seu próprio clan.");
                    return;
                }

                clan.quit(account.getUniqueId());
                account.getData(Columns.CLAN).setData(-1);
                account.getDataStorage().saveColumn(Columns.CLAN);
                sendMessage(clan, account.getRank().getDefaultTag().getFormattedColor() + account.getUsername() + " §esaiu do clan.");
                context.sendMessage("§aVocê saiu do clan " + clan.getName() + " com sucesso.");
                clanService.pushClan(clan);

                ClanIntegrationMessage message = ClanIntegrationMessage.builder()
                        .messageCause(ClanIntegrationMessage.MessageCause.MEMBER_LEFT)
                        .clanTag(clan.getTag()).index(clan.getIndex())
                        .clanName(clan.getName())
                        .target(member).color(clan.getColor()).build();

                Constants.getRedis().publish(Redis.CLAN_INTEGRATION_CHANNEL, Constants.GSON.toJson(message));

            }
        },

        ACCEPT(2, "aceitar", "accept") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (account.hasClan()) {
                    context.sendMessage("§cVocê já faz parte de um clan.");
                    return;
                }

                String clanName = context.getArg(1);
                Clan clan = clanService.getClan(clanName);

                if (clan == null) {
                    context.info("object.not_found", "Clan");
                    return;
                }

                if (!clan.hasPendingInvite(account.getUniqueId())) {
                    context.sendMessage("§cNenhum convite pendente encontrado.");
                    return;
                }

                if (clan.isFull()) {
                    context.sendMessage("§cO clan " + clan.getName() + " está lotado.");
                    return;
                }

                Invite invite = clan.getPendingInvite(account.getUniqueId());
                invite.setStatus(Invite.Status.ACCEPTED);

                clan.join(account);
                account.getData(Columns.CLAN).setData(clan.getIndex());
                account.getDataStorage().saveColumn(Columns.CLAN);

                ClanIntegrationMessage message = ClanIntegrationMessage.builder()
                        .messageCause(ClanIntegrationMessage.MessageCause.MEMBER_JOIN)
                        .clanTag(clan.getTag()).index(clan.getIndex())
                        .clanName(clan.getName())
                        .target(clan.getMember(account.getUniqueId())).color(clan.getColor()).build();

                Constants.getRedis().publish(Redis.CLAN_INTEGRATION_CHANNEL, Constants.GSON.toJson(message));

                sendMessage(clan, account.getRank().getDefaultTag().getFormattedColor() + account.getUsername() + " §eentrou no clan.");
                clanService.pushClan(clan);
            }
        },

        DECLINE(2, "negar", "decline") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                String clanName = context.getArg(1);
                Clan clan = clanService.getClan(clanName);

                if (clan == null) {
                    context.info("object.not_found", "Clan");
                    return;
                }

                if (!clan.hasPendingInvite(account.getUniqueId())) {
                    context.sendMessage("§cNenhum convite pendente foi encontrado.");
                    return;
                }

                Invite invite = clan.getPendingInvite(account.getUniqueId());
                invite.setStatus(Invite.Status.DECLINED);
                context.sendMessage("§cVocê negou o convite do clan " + clan.getName());
            }
        },

        INFO(1, "ver", "info") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();
                Clan clan;

                if (context.argsCount() == 1 && account.hasClan())
                    clan = account.getClan();
                else if (context.argsCount() > 1) {

                    String clanName = context.getArg(1);

                    if (!clanService.isClanExists(clanName, clanName)) {
                        context.info("object.not_found", "Clan");
                        return;
                    }

                    clan = clanService.getClan(clanName);
                } else {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                if (clan == null) {
                    context.info("object.not_found", "Clan");
                    return;
                }

                context.sendMessage("§aInformações de " + clan.getName() + ":");
                context.sendMessage(" §aTag: " + ChatColor.valueOf(clan.getColor()) + "[" + clan.getTag().toUpperCase() + "]");
                context.sendMessage(" §aMembros: §f" + clan.getMembers().size() + "/" + clan.getSlots());
                context.sendMessage(" §aPontos: §f" + clan.getPoints());
                context.sendMessage(" §aCriado em: §f" + account.getLanguage().getDateFormat().format(clan.getCreation()));

            }
        },

        INVITES(1, "convites", "invites") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();

                if (account.hasClan()) {
                    Clan clan = account.getClan();
                    Member member = clan.getMember(account.getUniqueId());

                    if (member.isAdmin()) {

                        if (clan.getInvites().isEmpty()) {
                            context.sendMessage("§cNenhum convite recente encontrado.");
                            return;
                        }

                        for (Invite invite : clan.getInvites()) {
                            context.sendMessage("§aConvite: §f" + invite.getInviteName());
                            context.sendMessage("  §7Emitido em: " + account.getLanguage().getDateFormat().format(invite.getRelease()));
                            context.sendMessage("  §7Emitido por: " + invite.getInvitor().getName());
                            context.sendMessage("  §7Status: " + invite.getStatus().getName());
                        }
                        return;
                    }

                    context.sendMessage("§cApenas administradores podem visualizar convites recentes.");
                } else {

                    int count = 0;

                    for (Clan clan : clanService.getClans()) {

                        if (!clan.hasPendingInvite(account.getUniqueId()))
                            continue;

                        count++;

                        TextComponent interactable = new TextComponent(TextComponent
                                .fromLegacyText("§b§lCLIQUE AQUI §epara entrar no clan §6" + clan.getName()));
                        interactable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/clan accept " + clan.getName().toLowerCase()));
                        ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(account.getUniqueId());
                        proxiedPlayer.sendMessage(interactable);
                    }

                    if (count == 0)
                        context.sendMessage("§cNenhum convite pendente encontrado.");
                }
            }
        },

        MEMBERS(1, "membros", "members") {
            @Override
            public void execute(Context<ProxiedPlayer> context) throws SQLException {

                Account account = context.getAccount();
                Clan clan;

                if (context.argsCount() == 1 && account.hasClan())
                    clan = account.getClan();
                else if (context.argsCount() > 1) {

                    String clanName = context.getArg(1);

                    if (!clanService.isClanExists(clanName, clanName)) {
                        context.info("object.not_found", "Clan");
                        return;
                    }

                    clan = clanService.getClan(clanName);
                } else {
                    context.sendMessage("§cVocê não faz parte de nenhum clan.");
                    return;
                }

                if (clan == null) {
                    context.info("object.not_found", "Clan");
                    return;
                }

                context.sendMessage("");
                context.sendMessage("§aMembros de " + ChatColor.valueOf(clan.getColor()) + clan.getName() + "§a:");

                List<Member> ordered = new ArrayList<>(clan.getMembers());
                ordered.sort((a, b) -> Integer.compare(b.getRole().getId(), a.getRole().getId()));

                for (Member member : ordered) {

                    search(context, member.getName(), memberAccount -> {
                        memberAccount.loadRanks();
                        context.sendMessage(" " + memberAccount.getRank().getDefaultTag().getFormattedColor() + member.getName() + " §e- " + member.getRole().getDisplay());
                    });

                }
            }
        };

        private final int minimumArgs;
        private final String[] field;

        public abstract void execute(Context<ProxiedPlayer> context) throws SQLException;

        Argument(int minimumArgs, java.lang.String... strings) {
            this.minimumArgs = minimumArgs;
            this.field = strings;
        }

        private final String MESSAGE_PREFIX = "§9[CLAN] ";

        public void sendMessage(Clan clan, String msg) {
            clan.getMembers().forEach(c -> {
                ProxiedPlayer player = BungeeCord.getInstance().getPlayer(c.getUniqueId());

                if (player != null)
                    player.sendMessage(TextComponent.fromLegacyText(MESSAGE_PREFIX + msg));
            });
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
    }
}