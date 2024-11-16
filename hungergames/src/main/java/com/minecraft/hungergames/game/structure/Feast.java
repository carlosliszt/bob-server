/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.structure;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.game.list.ClanxClan;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.Expel;
import com.minecraft.hungergames.user.kits.pattern.CooldownType;
import com.minecraft.hungergames.util.arena.FileArena;
import com.minecraft.hungergames.util.bo3.BO3;
import com.minecraft.hungergames.util.bo3.EmptyBO3;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Feast implements BukkitInterface {

    private final HungerGames hungerGames;
    private Pattern[] pattern;
    private boolean spawned, filled;
    private int time;
    private BO3 bo3;
    private Location location;
    private List<Chest> chests = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();

    public Feast(HungerGames hungerGames, int time) {
        this.hungerGames = hungerGames;
        this.spawned = false;
        this.filled = false;
        this.time = time;
        FileArena fileArena = FileArena.getArena("feast");

        if (fileArena == null) {
            FileArena.load(new File("/home/ubuntu/misc/hg/structures/feast"));
            fileArena = FileArena.getArena("feast");
        }

        this.bo3 = fileArena.getBO3();
    }

    public Feast countdown() {
        build();
        new BukkitRunnable() {
            public void run() {

                if (getHungerGames().getGame().getRecoveryMode().isEnabled())
                    return;

                if (getTime() > 0) {
                    if (time != 0 && time % 5 == 0 && time <= 15 || time != 0 && time % 30 == 0 || time != 0 && time <= 5)
                        broadcastTime();
                    if (time == 10) {
                        final Kit kit = HungerGames.getInstance().getKitStorage().getKit(Expel.class);

                        for (final Player player : Bukkit.getOnlinePlayers()) {
                            if (player == null)
                                continue;

                            final User user = User.fetch(player.getUniqueId());

                            if (kit.isUser(user)) {
                                kit.addCooldown(player.getUniqueId(), CooldownType.DEFAULT, kit.getCooldown() + 5);
                            }
                        }
                    }
                    setTime(getTime() - 1);
                } else {
                    spawn(false);
                    cancel();
                }
            }
        }.runTaskTimer(getHungerGames(), 0L, 20L);
        return this;
    }

    public Feast findLocation(int range) {
        int x = randomize(range), z = randomize(range);

        if (absolute(x) < 80) {
            if (x > 0)
                x += 40;
            else
                x -= 40;
        }

        if (absolute(z) < 80) {
            if (z > 0)
                z += 40;
            else
                z -= 40;
        }

        int y = HungerGames.getInstance().getGame().getWorld().getHighestBlockYAt(x, z);

        Location location = new Location(HungerGames.getInstance().getGame().getWorld(), x, y + 1, z);
        if (location.getY() >= 90)
            location.setY(72);

        setLocation(location);
        if (!getLocation().getChunk().isLoaded())
            getLocation().getChunk().load();
        return this;
    }

    public void spawn(boolean silent) {
        getLocation().clone().add(0, 1, 0).getBlock().setType(Material.ENCHANTMENT_TABLE);
        getLocation().getWorld().strikeLightningEffect(getLocation());

        makeChests().forEach(c -> fill(c, 18));

        getBlocks().forEach(block -> {
            block.removeMetadata("unbreakable", getHungerGames());
            block.setMetadata("kit.lumberjack.ignore", new GameMetadata(true));
        });

        if (!silent)
            broadcast("hg.feast.spawned_broadcast");

        this.filled = true;

        Game game = hungerGames.getGame();

        if (game instanceof ClanxClan && !game.isDamage())
            game.setDamage(true);
    }

    public void fill(Chest chest, int frequency) {
        for (ItemStack stack : getStacks()) {
            if (Constants.RANDOM.nextInt(100) < frequency)
                chest.getInventory().setItem(Constants.RANDOM.nextInt(27), stack);
        }
        chest.update();
    }

    public Feast build() {
        if (location == null)
            throw new IllegalStateException("Feast location can not be null.");
        if (bo3 instanceof EmptyBO3)
            throw new IllegalStateException("BO3 can not be empty.");

        bo3.spawn(location, (location, pattern) -> {

            Block block = location.getBlock();

            if (pattern.getMaterial() == Material.LAPIS_BLOCK) {

                block.setType(Material.AIR);

                for (int i = 0; i < 100; i++) {
                    block = block.getRelative(BlockFace.UP);

                    if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK)
                        WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0);
                }

                return false;
            } else if (pattern.getMaterial() == Material.GRASS && block.getBiome() == Biome.DESERT) {
                pattern.setMaterial(Material.SAND);
            }

            if (pattern.getMaterial() != Material.AIR) {
                blocks.add(block);
                block.setMetadata("unbreakable", new GameMetadata(0));
            }

            return true;
        });

        this.bo3 = new EmptyBO3();
        this.spawned = true;
        return this;
    }

    protected void broadcastTime() {
        String location = "(" + (int) getLocation().getX() + ", " + (int) getLocation().getY() + ", " + (int) getLocation().getZ() + ")";
        String br = DateUtils.formatTime(Language.PORTUGUESE, getTime()), us = DateUtils.formatTime(Language.ENGLISH, getTime());
        for (Player player : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(player.getUniqueId());
            if (account == null)
                continue;
            player.sendMessage(account.getLanguage().translate("hg.feast.spawn_broadcast", location, (account.getLanguage() == Language.PORTUGUESE ? br : us)));
        }
    }

    protected List<Chest> makeChests() {
        List<Chest> chests = new ArrayList<>();
        Location[] locations = {getLocation().clone().add(1, 1, 1), getLocation().clone().add(-1, 1, -1),
                getLocation().clone().add(1, 1, -1), getLocation().clone().add(-1, 1, 1), getLocation().clone().add(-2, 1, 2),
                getLocation().clone().add(-2, 1, -2), getLocation().clone().add(-2, 1, 0), getLocation().clone().add(2, 1, 0),
                getLocation().clone().add(0, 1, -2), getLocation().clone().add(0, 1, 2), getLocation().clone().add(+2, 1, -2),
                getLocation().clone().add(2, 1, 2)};
        for (Location location : locations) {
            location.getBlock().setType(Material.CHEST);
            chests.add((Chest) location.getBlock().getState());
        }
        return chests;
    }

    int random(int i) {
        return Math.max(1, Constants.RANDOM.nextInt(i + 1));
    }

    private Pattern getPattern() {
        return pattern[Constants.RANDOM.nextInt(pattern.length)];
    }

    public ItemStack[] getStacks() {
        return new ItemStack[]{
                new ItemFactory(Material.ENCHANTED_BOOK).addStoredEnchantment(Enchantment.DAMAGE_ALL, 1).getStack(),
                new ItemFactory(Material.ENCHANTED_BOOK).addStoredEnchantment(Enchantment.FIRE_ASPECT, 1).getStack(),
                new ItemStack(Material.DIAMOND_HELMET), new ItemFactory(Material.ANVIL).setDurability(2).getStack(),
                new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_SWORD),
                new ItemStack(Material.DIAMOND_PICKAXE), new ItemStack(Material.DIAMOND_AXE),
                new ItemStack(Material.COOKED_BEEF, random(37)), new ItemStack(Material.FLINT_AND_STEEL),
                new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.LAVA_BUCKET),
                new ItemStack(Material.ENDER_PEARL, random(2)), new ItemStack(Material.GOLDEN_APPLE, random(12)),
                new ItemStack(Material.EXP_BOTTLE, random(12)), new ItemStack(Material.WEB, random(9)),
                new ItemStack(Material.TNT, random(16)), new ItemStack(Material.POTION, 1, (short) 16418),
                new ItemStack(Material.POTION, 1, (short) 16424), new ItemStack(Material.POTION, 1, (short) 16420),
                new ItemStack(Material.POTION, 1, (short) 16428), new ItemStack(Material.POTION, 1, (short) 16426),
                new ItemStack(Material.POTION, 1, (short) 16417), new ItemStack(Material.POTION, 1, (short) 16419),
                new ItemStack(Material.POTION, 1, (short) 16421), new ItemStack(Material.WEB, random(3)),
                new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.COOKED_CHICKEN, random(7)), new ItemStack(Material.MUSHROOM_SOUP, random(12))};
    }
}
