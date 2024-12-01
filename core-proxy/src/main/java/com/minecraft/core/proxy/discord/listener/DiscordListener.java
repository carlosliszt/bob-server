package com.minecraft.core.proxy.discord.listener;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.discord.Discord;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter implements ProxyInterface {

    private final Discord discord;
    private final Map<String, WebhookClient> webHooks = Collections.synchronizedMap(new HashMap<>());
    private ArrayList<Member> discordMembers = new ArrayList<>();

    public DiscordListener(Discord discord) {
        this.discord = discord;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        discord.log(new EmbedBuilder()
                .setTitle("Proxy")
                .setDescription("O servidor foi iniciado com sucesso!")
                .setColor(0x00FF00)
                .setTimestamp(LocalDateTime.now()));
    }

    public WebhookClient getWebhook(TextChannel textChannel, Account account) {

        if (textChannel == null)
            return null;

        WebhookClient webhook = webHooks.get(account.getUsername());

        if (webhook == null) {

            List<Webhook> retrievedWebhooks = textChannel.retrieveWebhooks().complete();
            Webhook cachedWebhook = retrievedWebhooks.stream().filter(c -> c.getName().equalsIgnoreCase(account.getUsername())).findFirst().orElse(null);

            if (cachedWebhook == null) {
                if (retrievedWebhooks.size() == 10) {

                    Iterator<Webhook> iterator = retrievedWebhooks.iterator();

                    while (iterator.hasNext()) {

                        Webhook w = iterator.next();

                        if (!webHooks.containsKey(w.getName()) || BungeeCord.getInstance().getPlayer(w.getName()) == null) {
                            textChannel.deleteWebhookById(w.getId()).queue();
                            webHooks.remove(w.getName());
                            iterator.remove();
                        }
                    }

                    if (retrievedWebhooks.size() == 10) { // If all webhooks are in use, remove first
                        Webhook first = retrievedWebhooks.get(0);
                        textChannel.deleteWebhookById(first.getId()).queue();
                        webHooks.remove(first.getName());
                        retrievedWebhooks.remove(first);
                    }
                }
                cachedWebhook = textChannel.createWebhook(account.getUsername()).complete();
            }

            WebhookClientBuilder builder = WebhookClientBuilder.fromJDA(cachedWebhook);
            builder.setThreadFactory((job) -> {
                Thread thread = new Thread(job);
                thread.setName("discord.webhook.staffchat." + account.getUniqueId());
                thread.setDaemon(true);
                return thread;
            });
            builder.setWait(true);
            webhook = builder.build();
            webHooks.put(account.getUsername(), webhook);
        }
        return webhook;
    }

    public void hook(Account account, String message) {
        async(() -> {
            try {
                WebhookClient webhookClient = getWebhook(discord.getJDA().getTextChannelById("1310230206732304446"), account);
                String avatarUrl = "https://mineskin.eu/helm/" + account.getUniqueId() + "/256";

                if (webhookClient == null)
                    return;

                Pattern pattern = Pattern.compile("@(\\w+)");
                Matcher matcher = pattern.matcher(message);
                StringBuffer sb = new StringBuffer();

                while (matcher.find()) {
                    String username = matcher.group(1);

                    Optional<Member> member = discordMembers.stream().filter(m -> m.getUser().getName().equalsIgnoreCase(username)).findFirst();

                    if (member.isPresent()) {
                        String userId = "<@" + member.get().getId() + ">";
                        matcher.appendReplacement(sb, userId);
                    } else {
                        matcher.appendReplacement(sb, "@" + username);
                    }
                }
                matcher.appendTail(sb);

                String content = sb.toString();

                webhookClient.send(new WebhookMessageBuilder()
                        .setAvatarUrl(avatarUrl)
                        .setAllowedMentions(new AllowedMentions().withParseEveryone(false).withParseRoles(false).withParseUsers(true))
                        .setContent(content)
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Falha ao enviar a mensagem " + message + " de " + account.getUsername() + ". (thread=" + Thread.currentThread().getName() + ", error=" + e.getMessage() + ")");
            }
        });
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.getAuthor().isBot() || event.isWebhookMessage())
            return;
        if (event.getMessage().isSuppressedEmbeds() || event.getMessage().isTTS())
            return;
        if (event.getMessage().getContentRaw().isEmpty())
            return;

        String ID = "1310230206732304446";
        if (event.getChannel().getId().equals(ID)) {

            String msg = fixMessage(event);
            Constants.getAccountStorage().getAccounts().stream().filter(account -> account.hasPermission(Rank.PARTNER_PLUS) && account.getPreference(Preference.STAFFCHAT)).forEach(acc -> {

                ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(acc.getUniqueId());

                if (proxiedPlayer == null)
                    return;

                if (!discordMembers.contains(event.getMember())) {
                    discordMembers.add(event.getMember());
                }
                proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§e[STAFF] §7@" + event.getAuthor().getName()  + "§f: " + msg));
            });
        }

        if (event.getMessage().getContentRaw().contains("!moveall")) {

            String[] args = event.getMessage().getContentRaw().split(" ");

            VoiceChannel to = event.getMessage().getGuild().getVoiceChannelById(args[1]);
            VoiceChannel from = event.getMessage().getGuild().getVoiceChannelById(args[2]);

            if (to == null || from == null)
                return;

            to.getMembers().forEach(c -> {
                if (c.getVoiceState() == null || c.getVoiceState().inAudioChannel())
                    from.getGuild().moveVoiceMember(c, from).queue();
            });
        }

    }

    public String fixMessage(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if (message.length() > 300)
            message = message.substring(0, 300) + " §8[...]";

        List<Member> members = event.getMessage().getMentions().getMembers();
        List<GuildChannel> channels = event.getMessage().getMentions().getChannels();
        List<Role> roles = event.getMessage().getMentions().getRoles();

//        if (event.getMessage().getReferenced != null) {
//            Message message1 = event.getMessage();
//            User member = message1.getAuthor();
//            message = "§9> " + member.getName() + "#" + member.getDiscriminator() + "§r " + message;
//        }

        String[] args = message.split(" ");

        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (!members.isEmpty() && s.contains("<@!")) {
                String id = StringUtils.replace(s.split("<@!")[1], ">", "");
                Member member = members.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
                if (member != null) {
                    User user = member.getUser();
                    args[i] = "§6@" + user.getName() + "§f";
                } else
                    args[i] = "§6@unknown#0000§f";
            } else if (!members.isEmpty() && s.contains("<@")) {
                String id = StringUtils.replace(s.split("<@")[1], ">", "");
                Member member = members.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
                if (member != null) {
                    User user = member.getUser();
                    args[i] = "§6@" + user.getName() + "§f";
                } else
                    args[i] = "§6@unknown#0000§f";
            } else if (s.equalsIgnoreCase("@everyone")) {
                args[i] = "§c@everyone§f";
            } else if (s.equalsIgnoreCase("@here")) {
                args[i] = "§c@here§f";
            } else if (!channels.isEmpty() && s.contains("<#")) {
                String id = StringUtils.replace(s.split("<#")[1], ">", "");
                GuildChannel channel = channels.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
                if (channel != null) {
                    args[i] = "§e#" + channel.getName() + "§f";
                } else
                    args[i] = "§4#unknown-channel§f";
            } else if (!roles.isEmpty() && s.contains("<@&")) {
                String id = StringUtils.replace(s.split("<@&")[1], ">", "");
                Role channel = roles.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
                if (channel != null) {
                    args[i] = "§b@" + channel.getName() + "§f";
                } else
                    args[i] = "§4@unknown-role§f";
            }
        }
        return String.join(" ", args);
    }
}