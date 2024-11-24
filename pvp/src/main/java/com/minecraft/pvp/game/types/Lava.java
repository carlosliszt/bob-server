/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.game.types;

import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.pvp.event.UserDiedEvent;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.game.GameType;
import com.minecraft.pvp.user.User;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

public class Lava extends Game {

    public Lava() {
        setType(GameType.LAVA);
        setWorld(Bukkit.getWorld("lava"));

        setSpawn(new Location(getWorld(), 0.5, 70, 0.5, 0, 0));
        setLobby(new Location(getWorld(), 0.5, 70, 0.5, 0, 0));

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(150);

        setLimit(20);

        addColumn(Columns.PVP_COINS);
        setValidDamages(Sets.immutableEnumSet(FIRE, FIRE_TICK, MELTING, LAVA));
    }

    @Override
    public void join(User user, boolean teleport) {
        super.join(user, teleport);

        Player player = user.getPlayer();

        player.spigot().setCollidesWithEntities(false);

        player.getInventory().setItem(0, new ItemStack(Material.STONE_SWORD));

        player.getInventory().setItem(13, new ItemStack(Material.BOWL, 64));
        player.getInventory().setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
        player.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));

        for (int i = 0; i < 36; i++)
            player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));

        player.updateInventory();

        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            player.setFireTicks(0);
            player.setVelocity(new Vector());
        });

        user.handleSidebar();
    }

    @Override
    public void rejoin(User user, Rejoin rejoin) {
        super.rejoin(user, rejoin);
    }

    @Override
    public void quit(User user) {
        super.quit(user);

        Player player = user.getPlayer();
        player.spigot().setCollidesWithEntities(true);
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lPVP: LAVA");
        scores.add(" ");
        scores.add("§fCoins: §6" + user.getAccount().getData(Columns.PVP_COINS).getAsInteger());
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void onLogin(User user) {

    }

    @EventHandler
    public void onUserDied(UserDiedEvent event) {
        if (!event.getGame().getUniqueId().equals(getUniqueId()))
            return;

        User killed = event.getKilled();

        killed.getPlayer().sendMessage(killed.getAccount().getLanguage().translate("pvp.arena.death_to_anyone"));
        killed.getGame().join(killed, true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null)
            return;
        User user = User.fetch(player.getUniqueId());
        if (player.getWorld().getUID().equals(Bukkit.getWorlds().get(0).getUID()) || !user.getGame().getUniqueId().equals(getUniqueId()))
            return;
        event.getItemDrop().remove();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        User user = User.fetch(event.getEntity().getUniqueId());
        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;
        event.setCancelled(!getValidDamages().contains(event.getCause()));
    }

    private final NPC EASY = NPC.builder().location(new Location(Bukkit.getWorld("lava"), -10.5, 65.5, 39.5, -140, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyMTE4ODQyNzEzNiwKICAicHJvZmlsZUlkIiA6ICI2MWFmMjZkZmQ3YzI0MjAxOGE0ODFmOGM3ZjgyMTI1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5dWtpcml0b0ZMQU1FIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNmNDY1MmE5YzM5NTZjMmY5ODhkOWQ0ZDU5OGExOTE0ODRkN2M5YTJmMDllZTQyYmJmODhkOGE2MjE4ZmZkNWEiCiAgICB9CiAgfQp9", "d+iAOde3XUvhGiRCis5CVNH8PwP8ptXIisjjFJyJ6PxaTpMc+PLivLUUr19CtETKP4fLmy1i5hxPhuDnEWYrQqqTdd544w745d+o0Hnb3Vdp5/anQNlWb8YfHMOTmr6RAG2STfASkWR4W9PEOZqDIGNa/IE30I2hrrkQcrbQbY5V90Ud3fM8cM/oes4KLllnefDuHjaZr/28odC8HW6GUq8YvANppRD5+PSqSS8XoG1ifZK/CVfcSKKDtd5rfjzG8mEhSFh7j7PZRhUEyj0TUMpMKZhJPM6YJxXTmn6vs2HTkH0KOJFlXRVgjDnghUKJ+17qUotYCaS1pG9mKzrFuZGjs30Mhfzsg/iNlFoMOF3OMSJDR1AoZ7WBvNXZ98xkuglqXnph1P4GR59zv5pR0zTZ/t6AyzMZmrbGsSr6TwIzIhkCaUTFewGM6b2BE+7kTpN2xMXiUgAq252dZ0uJCJqaR9uO98/dJBD/YfQaz8zbHj+9vkA+Q2REJ3iJTQ1b+mOLkQ/ayXH9MpS7dF4jjt0WsBxacMc/bthdwcKtr2gEL2ixVtLd0j1X7peEHNSUdGfGc8n8LPonuNA2CDg36bEfwOyrtM/RbCmtgbKs/Hbccw4F52DcB3JyB8jRv5EYCMD6Ih0fFEaM7IosJp2O2KEg2jGPtOuqyWuS4VeJE/E=")).interactExecutor((player, npc, type) -> {
        player.sendMessage("§aVocê passou o lava dif. I.");
        User.fetch(player.getUniqueId()).join(true);
    }).build();

    private final NPC MEDIUM = NPC.builder().location(new Location(Bukkit.getWorld("lava"), -16.5, 53.5, 37.5, 178, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyMTE4ODQyNzEzNiwKICAicHJvZmlsZUlkIiA6ICI2MWFmMjZkZmQ3YzI0MjAxOGE0ODFmOGM3ZjgyMTI1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5dWtpcml0b0ZMQU1FIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNmNDY1MmE5YzM5NTZjMmY5ODhkOWQ0ZDU5OGExOTE0ODRkN2M5YTJmMDllZTQyYmJmODhkOGE2MjE4ZmZkNWEiCiAgICB9CiAgfQp9", "d+iAOde3XUvhGiRCis5CVNH8PwP8ptXIisjjFJyJ6PxaTpMc+PLivLUUr19CtETKP4fLmy1i5hxPhuDnEWYrQqqTdd544w745d+o0Hnb3Vdp5/anQNlWb8YfHMOTmr6RAG2STfASkWR4W9PEOZqDIGNa/IE30I2hrrkQcrbQbY5V90Ud3fM8cM/oes4KLllnefDuHjaZr/28odC8HW6GUq8YvANppRD5+PSqSS8XoG1ifZK/CVfcSKKDtd5rfjzG8mEhSFh7j7PZRhUEyj0TUMpMKZhJPM6YJxXTmn6vs2HTkH0KOJFlXRVgjDnghUKJ+17qUotYCaS1pG9mKzrFuZGjs30Mhfzsg/iNlFoMOF3OMSJDR1AoZ7WBvNXZ98xkuglqXnph1P4GR59zv5pR0zTZ/t6AyzMZmrbGsSr6TwIzIhkCaUTFewGM6b2BE+7kTpN2xMXiUgAq252dZ0uJCJqaR9uO98/dJBD/YfQaz8zbHj+9vkA+Q2REJ3iJTQ1b+mOLkQ/ayXH9MpS7dF4jjt0WsBxacMc/bthdwcKtr2gEL2ixVtLd0j1X7peEHNSUdGfGc8n8LPonuNA2CDg36bEfwOyrtM/RbCmtgbKs/Hbccw4F52DcB3JyB8jRv5EYCMD6Ih0fFEaM7IosJp2O2KEg2jGPtOuqyWuS4VeJE/E=")).interactExecutor((player, npc, type) -> {
        player.sendMessage("§aVocê passou o lava dif. II.");
        User.fetch(player.getUniqueId()).join(true);
    }).build();

    private final NPC HARD = NPC.builder().location(new Location(Bukkit.getWorld("lava"), 7.5, 69.5, -20.5, -150, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyMTE4ODQyNzEzNiwKICAicHJvZmlsZUlkIiA6ICI2MWFmMjZkZmQ3YzI0MjAxOGE0ODFmOGM3ZjgyMTI1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5dWtpcml0b0ZMQU1FIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNmNDY1MmE5YzM5NTZjMmY5ODhkOWQ0ZDU5OGExOTE0ODRkN2M5YTJmMDllZTQyYmJmODhkOGE2MjE4ZmZkNWEiCiAgICB9CiAgfQp9", "d+iAOde3XUvhGiRCis5CVNH8PwP8ptXIisjjFJyJ6PxaTpMc+PLivLUUr19CtETKP4fLmy1i5hxPhuDnEWYrQqqTdd544w745d+o0Hnb3Vdp5/anQNlWb8YfHMOTmr6RAG2STfASkWR4W9PEOZqDIGNa/IE30I2hrrkQcrbQbY5V90Ud3fM8cM/oes4KLllnefDuHjaZr/28odC8HW6GUq8YvANppRD5+PSqSS8XoG1ifZK/CVfcSKKDtd5rfjzG8mEhSFh7j7PZRhUEyj0TUMpMKZhJPM6YJxXTmn6vs2HTkH0KOJFlXRVgjDnghUKJ+17qUotYCaS1pG9mKzrFuZGjs30Mhfzsg/iNlFoMOF3OMSJDR1AoZ7WBvNXZ98xkuglqXnph1P4GR59zv5pR0zTZ/t6AyzMZmrbGsSr6TwIzIhkCaUTFewGM6b2BE+7kTpN2xMXiUgAq252dZ0uJCJqaR9uO98/dJBD/YfQaz8zbHj+9vkA+Q2REJ3iJTQ1b+mOLkQ/ayXH9MpS7dF4jjt0WsBxacMc/bthdwcKtr2gEL2ixVtLd0j1X7peEHNSUdGfGc8n8LPonuNA2CDg36bEfwOyrtM/RbCmtgbKs/Hbccw4F52DcB3JyB8jRv5EYCMD6Ih0fFEaM7IosJp2O2KEg2jGPtOuqyWuS4VeJE/E=")).interactExecutor((player, npc, type) -> {
        player.sendMessage("§aVocê passou o lava dif. III.");
        User.fetch(player.getUniqueId()).join(true);
    }).build();

    private final NPC EXTREME = NPC.builder().location(new Location(Bukkit.getWorld("lava"), 36.5, 92.5, -12.5, -21, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyMTE4ODQyNzEzNiwKICAicHJvZmlsZUlkIiA6ICI2MWFmMjZkZmQ3YzI0MjAxOGE0ODFmOGM3ZjgyMTI1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5dWtpcml0b0ZMQU1FIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNmNDY1MmE5YzM5NTZjMmY5ODhkOWQ0ZDU5OGExOTE0ODRkN2M5YTJmMDllZTQyYmJmODhkOGE2MjE4ZmZkNWEiCiAgICB9CiAgfQp9", "d+iAOde3XUvhGiRCis5CVNH8PwP8ptXIisjjFJyJ6PxaTpMc+PLivLUUr19CtETKP4fLmy1i5hxPhuDnEWYrQqqTdd544w745d+o0Hnb3Vdp5/anQNlWb8YfHMOTmr6RAG2STfASkWR4W9PEOZqDIGNa/IE30I2hrrkQcrbQbY5V90Ud3fM8cM/oes4KLllnefDuHjaZr/28odC8HW6GUq8YvANppRD5+PSqSS8XoG1ifZK/CVfcSKKDtd5rfjzG8mEhSFh7j7PZRhUEyj0TUMpMKZhJPM6YJxXTmn6vs2HTkH0KOJFlXRVgjDnghUKJ+17qUotYCaS1pG9mKzrFuZGjs30Mhfzsg/iNlFoMOF3OMSJDR1AoZ7WBvNXZ98xkuglqXnph1P4GR59zv5pR0zTZ/t6AyzMZmrbGsSr6TwIzIhkCaUTFewGM6b2BE+7kTpN2xMXiUgAq252dZ0uJCJqaR9uO98/dJBD/YfQaz8zbHj+9vkA+Q2REJ3iJTQ1b+mOLkQ/ayXH9MpS7dF4jjt0WsBxacMc/bthdwcKtr2gEL2ixVtLd0j1X7peEHNSUdGfGc8n8LPonuNA2CDg36bEfwOyrtM/RbCmtgbKs/Hbccw4F52DcB3JyB8jRv5EYCMD6Ih0fFEaM7IosJp2O2KEg2jGPtOuqyWuS4VeJE/E=")).interactExecutor((player, npc, type) -> {
        player.sendMessage("§aVocê passou o lava dif. IV.");
        User.fetch(player.getUniqueId()).join(true);
    }).build();

}