package com.minecraft.arcade.pvp.user;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.game.Game;
import com.minecraft.arcade.pvp.kit.Kit;
import com.minecraft.arcade.pvp.user.object.CombatTag;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.arcade.route.GameRouteContext;
import com.minecraft.core.bukkit.util.reflection.Info;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

@Data
public class User {

    private final Account account;

    private GameRouteContext routeContext;
    private GameScoreboard scoreboard;

    private final CombatTag combatTag = new CombatTag();

    private Kit[] kits;

    private Game game;
    private Player player;

    public User(final Account account) {
        this.account = account;
    }

    public List<ItemStack> getInventoryContents() { /* Spigot method */
        final List<ItemStack> itemStacks = new ArrayList<>();

        PlayerInventory inventory = getPlayer().getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getContents()[i];
            if (item != null) itemStacks.add(item);
        }

        for (int i = 0; i < 4; i++) {
            ItemStack armor = inventory.getArmorContents()[i];
            if (armor != null) itemStacks.add(armor);
        }

        return itemStacks;
    }

    public void setKit(int slot, Kit kit) {
        this.kits[slot] = kit;
    }

    public Kit getKit(int slot) {
        return this.kits[slot];
    }

    public List<Kit> getKitList() {
        return Arrays.asList(kits);
    }

    public boolean hasKit(Kit kit, int slot) {
        return getAccount().hasPermission(kit.getRank()) || slot == 0 && PvP.getInstance().getKitStorage().getGameType().getMaxKits() > 1 || kit.isNone();
    }

    @Info(fancyName = "Kits")
    public String formattedKits() {
        return getKitContainer(false);
    }

    @Info(fancyName = "Combat")
    public String formattedCombat() {
        return getCombatTag() != null && getCombatTag().isTagged() ? "Sim" : "Não";
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

    public static User fetch(UUID uuid) {
        return PvP.getInstance().getUserStorage().getUser(uuid);
    }

}