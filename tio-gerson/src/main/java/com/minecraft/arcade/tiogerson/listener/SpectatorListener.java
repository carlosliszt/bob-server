package com.minecraft.arcade.tiogerson.listener;

import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.translation.Language;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class SpectatorListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            User user = User.fetch(event.getDamager().getUniqueId());
            Room room = user.getRoom();

            if (room.isSpectator(user) && !user.getAccount().hasPermission(Rank.ADMINISTRATOR)) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        User user = User.fetch(player.getUniqueId());
        Room room = user.getRoom();

        if (user.isPlaying())
            return;

        if (user.getAccount().hasPermission(Rank.PARTNER_PLUS))
            return;

        event.setCancelled(true);

        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(event.getPlayer().getUniqueId(), "chat.cooldown");

        Set<Player> recipients = event.getRecipients();

        recipients.removeIf(p -> !p.getWorld().getUID().equals(player.getWorld().getUID()) || !room.isSpectator(User.fetch(p.getUniqueId())));

        Account account = Account.fetch(player.getUniqueId());

        if (cooldown != null && !cooldown.expired()) {
            event.getPlayer().sendMessage(account.getLanguage().translate("wait_to_chat", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        } else {
            CooldownProvider.getGenericInstance().addCooldown(event.getPlayer().getUniqueId(), "chat.cooldown", 3, false);
        }

        Tag tag = account.getProperty("account_tag").getAs(Tag.class);

        recipients.forEach(recipient -> {
            Account account_recipient = Account.fetch(recipient.getUniqueId());
            PrefixType prefixType = account_recipient.getProperty("account_prefix_type").getAs(PrefixType.class);
            String prefix = account_recipient.getLanguage() == Language.PORTUGUESE ? "§7[ESPECTADOR] " : "§7[SPECTATOR] ";
            recipient.sendRawMessage(prefix + (tag == Tag.MEMBER ? tag.getMemberSetting(prefixType) : prefixType.getFormatter().format(tag).replace("#", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+")) + account.getDisplayName() + " §7»§r " + event.getMessage());
        });
    }

}
