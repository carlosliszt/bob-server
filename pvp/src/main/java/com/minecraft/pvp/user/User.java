/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.reflection.Info;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.event.PlayerProtectionRemoveEvent;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.util.DamageSettings;
import com.minecraft.pvp.util.Type;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class User {

    private final UUID uniqueId;
    private final String name;

    private final Account account;

    private Game game;
    private Player player;

    private GameScoreboard scoreboard;

    private UUID lastCombat = null;
    private long lastCombatTime = 0L;

    @Setter
    private Kit kit1, kit2;

    private boolean kept, canFirstFall;

    private int aliveSeconds;
    private final DamageSettings damageSettings;

    private transient final List<Kit> kits;

    public User(Account account) {
        this.account = account;

        this.uniqueId = account.getUniqueId();
        this.name = account.getUsername();

        this.kept = true;
        this.canFirstFall = true;
        this.damageSettings = new DamageSettings();

        this.kits = new ArrayList<>();
    }

    public void loadKits() {
        kits.clear();

        JsonArray jsonArray = getAccount().getData(Columns.PVP_KITS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            long expiration = object.get("expiration").getAsLong();

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                iterator.remove();
                continue;
            }

            Kit kit = PvP.getPvP().getKitStorage().getKit(object.get("kit").getAsString());

            if (kit == null) {
                iterator.remove();
                continue;
            }

            kits.add(kit);
        }

        getAccount().getData(Columns.PVP_KITS).setData(jsonArray);
        getAccount().getData(Columns.PVP_KITS).setChanged(true);
    }

    public void giveKit(Kit kit, long expiration) {
        JsonArray jsonArray = getAccount().getData(Columns.PVP_KITS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("kit", kit.getName());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("boughtAt", System.currentTimeMillis());

        jsonArray.add(jsonObject);

        getAccount().getData(Columns.PVP_KITS).setData(jsonArray);
        loadKits();
    }

    public void restoreCombat() {
        lastCombatTime = 0L;
        lastCombat = null;
    }

    public void addCombat(UUID uuid) {
        lastCombatTime = 15000L + System.currentTimeMillis();
        lastCombat = uuid;
    }

    public void incrementSecondsAlive() {
        this.aliveSeconds++;
    }

    public void resetSecondsAlive() {
        this.aliveSeconds = 0;
    }

    public boolean inCombat() {
        return lastCombatTime >= System.currentTimeMillis();
    }

    public boolean hasKit(Kit kit, Type type) {
        return kit.isNone() || type == Type.PRIMARY || getAccount().hasPermission(kit.getDefaultRank()) || kits.contains(kit);
    }

    private int boost(int value) {
        Rank rank = getAccount().getRank();

        if (rank.getId() >= Rank.PRO.getId()) {
            return value * 2;
        } else if (rank.getId() == Rank.VIP.getId()) {
            return (int) (value * 1.5);
        } else if (getAccount().hasMedal(Medal.SUPPORTER)) {
            return (int) (value * 1.25);
        }

        return value;
    }

    public void giveCoins(double initial) {
        int value = boost((int) initial);
        getAccount().addInt(value, Columns.PVP_COINS);
        player.sendMessage("§6+" + value + " coins " + (value != initial ? "(" + Constants.DECIMAL_FORMAT.format((value / initial)) + "x)" : ""));
    }

    public List<ItemStack> getInventoryContents() {
        List<ItemStack> list = Stream.concat(Arrays.stream(getPlayer().getInventory().getContents()), Arrays.stream(getPlayer().getInventory().getArmorContents())).collect(Collectors.toList());

        Player player = this.player;

        if (player.getOpenInventory().getBottomInventory() == player.getInventory())
            Collections.addAll(list, player.getOpenInventory().getTopInventory().getContents());

        if (player.getItemOnCursor() != null && player.getItemOnCursor().getType() != Material.AIR)
            list.add(player.getItemOnCursor());

        return list;
    }

    public void vanish() {
        if (getAccount().hasPermission(Rank.PARTNER_PLUS))
            Vanish.getInstance().setVanished(player, getAccount().getRank());
    }

    public Player getPlayer() {
        if (this.player == null)
            this.player = Bukkit.getPlayer(getUniqueId());
        return player;
    }

    public boolean isVanish() {
        return Vanish.getInstance().isVanished(getUniqueId());
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setCanFirstFall(boolean canFirstFall) {
        this.canFirstFall = canFirstFall;
    }

    public boolean isUsing(Kit kit) {
        return getKit1().getName().equals(kit.getName()) || getKit2().getName().equals(kit.getName());
    }

    public void setScoreboard(GameScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public void handleSidebar() {
        getGame().handleSidebar(this);
    }

    public void join(boolean teleport) {
        getGame().join(this, teleport);
    }

    public void quit() {
        getGame().quit(this);
    }

    public void setKept(boolean kept) {
        this.kept = kept;
        if (!kept)
            Bukkit.getPluginManager().callEvent(new PlayerProtectionRemoveEvent(player));
    }

    @Info(fancyName = "Kits")
    private String formattedKits() {
        return getKit1().getName() + ", " + getKit2().getName();
    }

    @Info(fancyName = "Combate")
    private String combat() {
        return inCombat() ? "Sim" : "Não";
    }

    public static User fetch(UUID uuid) {
        return PvP.getPvP().getUserStorage().getUser(uuid);
    }

}