/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.bossbar.Bossbar;
import com.minecraft.core.bukkit.util.reflection.Info;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.team.Team;
import com.minecraft.hungergames.user.celebrations.Celebration;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.Nenhum;
import com.minecraft.hungergames.user.kits.pattern.DailyKit;
import com.minecraft.hungergames.user.object.AwaySession;
import com.minecraft.hungergames.user.object.CombatTag;
import com.minecraft.hungergames.user.object.ScrimSettings;
import com.minecraft.hungergames.user.pattern.Condition;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class User {

    private static final transient HungerGames hungerGames = HungerGames.getInstance();

    private transient final Set<UUID> victims;
    private transient Player player;
    private transient Account account;
    private transient Bossbar bossbar;
    private transient GameScoreboard scoreboard;
    @Info(fancyName = "Estado")
    private transient Condition condition;
    private transient final CombatTag combatTag;
    private transient AwaySession awaySession;
    @Info(fancyName = "Time")
    private transient Team team;

    private transient final ScrimSettings scrimSettings = new ScrimSettings();
    private transient DailyKit dailyKit = new DailyKit();
    private Kit[] kits;
    private Celebration celebration;
    private boolean online;
    @Info(fancyName = "Kills")
    private int kills;
    private boolean specs = true;

    private transient final List<Kit> accountKits;

    public User(Account account) {
        this.account = account;
        this.condition = Condition.LOADING;
        this.combatTag = new CombatTag();
        this.victims = new HashSet<>();
        this.accountKits = new ArrayList<>();
        this.celebration = hungerGames.getCelebrationStorage().getCelebrations().get(0);
        this.kits = new Kit[hungerGames.getGame().getType().getMaxKits()];
        Kit kit = hungerGames.getKitStorage().getDefaultKit();
        Arrays.fill(kits, kit);
    }

    public void loadKits() {
        accountKits.clear();

        JsonArray jsonArray = getAccount().getData(Columns.HG_KITS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            long expiration = object.get("expiration").getAsLong();

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                iterator.remove();
                continue;
            }

            Kit kit = hungerGames.getKitStorage().getKit(object.get("kit").getAsString());

            if (kit == null) {
                iterator.remove();
                continue;
            }

            accountKits.add(kit);
        }

        getAccount().getData(Columns.HG_KITS).setData(jsonArray);
        getAccount().getData(Columns.HG_KITS).setChanged(true);
    }

    public void giveKit(Kit kit, long expiration) {
        JsonArray jsonArray = getAccount().getData(Columns.HG_KITS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("kit", kit.getName());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("boughtAt", System.currentTimeMillis());

        jsonArray.add(jsonObject);

        getAccount().getData(Columns.HG_KITS).setData(jsonArray);
        loadKits();
    }

    public UUID getUniqueId() {
        return getAccount().getUniqueId();
    }

    public String getName() {
        return getAccount().getDisplayName();
    }

    public User setAccount(Account account) {
        this.account = account;
        return this;
    }

    public static User fetch(UUID uuid) {
        return hungerGames.getUserStorage().getUser(uuid);
    }

    public static User getUser(String name) {
        return hungerGames.getUserStorage().getUsers().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void setKit(int slot, Kit kit) {
        this.kits[slot] = kit;
    }

    public Kit getKit(int slot) {
        return this.kits[slot];
    }

    public String getKitContainer(boolean brackets) {
        StringBuilder stringBuilder = new StringBuilder();

        if (brackets)
            stringBuilder.append("(");

        List<Kit> kits = new ArrayList<>(getKitList());
        kits.removeIf(Kit::isNone);
        Iterator<Kit> iterator = kits.iterator();

        while (iterator.hasNext()) {
            Kit kit = iterator.next();

            stringBuilder.append(kit.getDisplayName());
            if (iterator.hasNext())
                stringBuilder.append(", ");
        }

        if (brackets)
            stringBuilder.append(")");

        String result = stringBuilder.toString();
        return (result.equals(brackets ? "()" : "") ? "" : result);
    }

    @Info(fancyName = "Kits")
    private String formattedKits() {
        return getKitContainer(false);
    }

    @Info(fancyName = "Combate")
    private String combat() {
        return getCombatTag() != null && getCombatTag().isTagged() ? "Sim" : "Não";
    }

    public List<Kit> getKitList() {
        return Arrays.asList(kits);
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isAlive() {
        return getCondition() == Condition.ALIVE;
    }

    public boolean isVanish() {
        return Vanish.getInstance().isVanished(getUniqueId());
    }

    public boolean hasKit(Kit kit, int slot) {
        return hungerGames.getKitStorage().isFreeKits() || getAccount().hasPermission(kit.getPermission()) || slot == 0 && hungerGames.getGame().getType().getMaxKits() > 1 || kit instanceof Nenhum || accountKits.contains(kit);
    }

    public boolean hasCelebration(Celebration celebration) {
        return celebration.isFree || getAccount().hasPermission(celebration.getRank());
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
        getAccount().addInt(value, Columns.HG_COINS);
        player.sendMessage("§6+" + value + " coins " + (value != initial ? "(" + Constants.DECIMAL_FORMAT.format((value / initial)) + "x)" : ""));
    }

    public boolean hasTeam() {
        return team != null;
    }

    public void addKill() {
        this.kills++;
    }

    public void addVictim(UUID uuid) {
        victims.add(uuid);
    }


}
