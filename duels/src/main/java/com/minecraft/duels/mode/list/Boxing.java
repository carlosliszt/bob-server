package com.minecraft.duels.mode.list;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.duels.event.player.UserDeathEvent;
import com.minecraft.duels.mode.Mode;
import com.minecraft.duels.room.Room;
import com.minecraft.duels.user.User;
import com.minecraft.duels.util.enums.RoomStage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Boxing extends Mode {

    public Boxing() {
        super(10, DuelType.BOXING_1V1, DuelType.BOXING_2V2);
        setWins(Columns.DUELS_BOXING_WINS);
        setLoses(Columns.DUELS_BOXING_LOSSES);
        setWinstreak(Columns.DUELS_BOXING_WINSTREAK);
        setWinstreakRecord(Columns.DUELS_BOXING_MAX_WINSTREAK);
        setGames(Columns.DUELS_BOXING_GAMES);
        setRating(Columns.DUELS_BOXING_RATING);
    }

    @Override
    public void start(Room room) {
        super.start(room);

        room.getAlivePlayers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            playerInventory.clear();

            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 255), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);

            player.getInventory().setItem(0, new ItemFactory(Material.DIAMOND_SWORD).setName("§aCombo").setDescription("§7Boxing Mode").addEnchantment(Enchantment.DAMAGE_ALL, 1).setUnbreakable().addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());

            player.updateInventory();
        });
    }

    @EventHandler
    public void onBoxingDamage(EntityDamageByEntityEvent event) {
        if (!event.isBothPlayers())
            return;

        final Player entity = (Player) event.getEntity(), damager = (Player) event.getDamager();

        final User user = User.fetch(entity.getUniqueId()), user1 = User.fetch(damager.getUniqueId());

        final Room room = user.getRoom();

        if (room == null)
            return;

        if (room.getMode() != this)
            return;

        if (room.getStage() != RoomStage.PLAYING)
            return;

        if (!user.isPlaying() || !user1.isPlaying())
            return;

        user1.addBoxingHit();

        if (user1.getBoxingHits() >= 100) {
            new UserDeathEvent(user, false).fire();
        }
    }

    @Override
    public void handleSidebar(User user) {

        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            user.setScoreboard(gameScoreboard = new GameScoreboard(user.getPlayer()));

        Room room = user.getRoom();

        if (room == null)
            return;

        RoomStage stage = room.getStage();

        List<String> scores = new ArrayList<>();

        int time = room.getTime();
        int teamLimit = room.getMaxPlayers() / 2;

        gameScoreboard.updateTitle("§b§lDUELS");
        scores.add("§8" + room.getCode());
        scores.add(" ");
        scores.add("Modo: §a" + getName() + " " + teamLimit + "v" + teamLimit);

        if (stage == RoomStage.STARTING || stage == RoomStage.WAITING) {
            scores.add("Jogadores: §a" + room.getAlivePlayers().size() + "/" + room.getMaxPlayers());
            scores.add(" ");
            scores.add(time == -1 ? "Aguardando..." : "Iniciando em §a" + time + "s");
        } else {
            scores.add("Tempo: §a" + format(time));
            scores.add(" ");
            scores.add("§f§lHits totais:");
            room.getRed().getMembers().forEach(red -> scores.add("§7" + red.getName() + "§f: §a" + red.getBoxingHits()));
            room.getBlue().getMembers().forEach(blue -> scores.add("§7" + blue.getName() + "§f: §a" + blue.getBoxingHits()));
        }

        scores.add(" ");
        scores.add("Rating: §a" + user.getAccount().getData(getRating()).getAsInteger());
        scores.add("Winstreak: §7" + user.getAccount().getData(getWinstreak()).getAsInteger());

        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

}