package com.minecraft.pvp.game.types;

import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.translation.Language;
import com.minecraft.pvp.event.UserDiedEvent;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.game.GameType;
import com.minecraft.pvp.game.selectors.damage.Challenge;
import com.minecraft.pvp.game.selectors.damage.Editor;
import com.minecraft.pvp.game.tasks.DamagerTask;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.util.DamageSettings;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

public class Damage extends Game {

    private final Location spawn;

    public Damage() {
        setType(GameType.DAMAGE);
        setWorld(Bukkit.getWorld("damage"));

        setSpawn(new Location(getWorld(), 0.5, 70, 0.5, 0, 0));
        setLobby(new Location(getWorld(), 0.5, 70, 0.5, 0, 0));

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(265);

        setLimit(20);

        addColumn(Columns.PVP_DAMAGE_EASY, Columns.PVP_DAMAGE_MEDIUM, Columns.PVP_DAMAGE_HARD, Columns.PVP_DAMAGE_EXTREME, Columns.PVP_DAMAGE_SETTINGS, Columns.PVP_COINS);
        setValidDamages(Sets.immutableEnumSet(CONTACT, ENTITY_ATTACK, PROJECTILE, SUFFOCATION, FALL, FIRE, FIRE_TICK, MELTING, LAVA, DROWNING, BLOCK_EXPLOSION, ENTITY_EXPLOSION, VOID, LIGHTNING, SUICIDE, STARVATION, POISON, MAGIC, WITHER, FALLING_BLOCK, THORNS, CUSTOM));

        new DamagerTask(this).start(getPlugin());

        spawn = new Location(getWorld(), 0.5, 67, 11.5);

        ((CraftWorld) getWorld()).getHandle().spigotConfig.itemDespawnRate = 30;
    }

    @Override
    public void join(User user, boolean teleport) {
        super.join(user, teleport);

        Player p = user.getPlayer();

        p.getInventory().setItem(0, new ItemStack(Material.STONE_SWORD));

        p.getInventory().setItem(13, new ItemStack(Material.BOWL, 64));
        p.getInventory().setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
        p.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));

        for (int i = 0; i < 36; i++)
            p.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));

        p.updateInventory();

        user.resetSecondsAlive();
        user.handleSidebar();
    }

    private final NPC EDITOR = NPC.builder().location(new Location(Bukkit.getWorld("damage"), 4.5, 66.0, 19.5, 150, 0)).property(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1ODU2NjYxNjc3NTQsInByb2ZpbGVJZCI6ImIwZDczMmZlMDBmNzQwN2U5ZTdmNzQ2MzAxY2Q5OGNhIiwicHJvZmlsZU5hbWUiOiJPUHBscyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDUyNWJiNjBmODdmY2JhYzIzYTdhN2E5Mzk3ZDAyZDhkYjk2OGYxZWM3YTM3NjA3OWY1NmI3NDk0NzAyMDE4YyJ9fX0=", "rdmLzu7IrYSF86YQlHFs986IvTwJ9AZupTqUTMEsknpE6+QRyb69L/T2jR8jRFJXDFB+RGqN/gOyYW+jfBp0THmBbmImyuib8mgZvVBjK+wjVnRGSh5SXq4I+9tR0sVDb8l75X7Pp3aU7RuBlPJsgyZ0CHD/QBRVnT2zQX/zKWHsbyA9CnHnsjNsLPU+qJF+YeXvUkWD/vXQ6SZkRuP9jElySZ39G3FBRVr16dKrvY4maqqaO1nnbkMgN70D7RFFACa+4becRdXoH8hpvOQxn/KxnZWVfE42/GZUg37C9mRsUu5qdAdM5C5xFzZ2ddqApHtMLGM94WRkLX/G0CVto21oFSNyO2WzA+WHaWgeRW2F5CiVVJm3LMRZ1rmatwaJQRNRkshBQmRsqqAJB0gZHhUtR2u2BA42D+D/WVI5nk7Zx8m1xyeND8G3Jox25gVrUMZTHyKFWTMTBxtZjOV/v9gQ1wjacG4U1AtRYQKo5VflbDocHv4SnAPoCo8ArSy0BiIJBBE+qS6MG51MP8/JCtw9HDniUhl8/71C5gWhGvpcJkbFeTHE+OMt9QmuQuiP2uM4iHajBbhbvNJaiQw5s1jaMy4WlfUKBJcPgHLP8EZV5s3nwUbL58kYVWET/CozGnyszOR0JyOrWf5umX7jsHZTDya41Mmruzq/FkXuK50=")).interactExecutor((player, npc, type) -> new Editor(User.fetch(player.getUniqueId())).build()).build();
    private final NPC CHALLENGES = NPC.builder().location(new Location(Bukkit.getWorld("damage"), -3.5, 66.0, 19.5, -150, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxNTY0ODQyNjQyNiwKICAicHJvZmlsZUlkIiA6ICJhYTZhNDA5NjU4YTk0MDIwYmU3OGQwN2JkMzVlNTg5MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiejE0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIzYjQ1MzkwMThkYWYxZjI5MGMxZjQyYTcxMjU2MTJhYjc1YjBmNjIzNDQ2MTI3YzY0MDAzM2U4NjY1NzhhOTQiCiAgICB9CiAgfQp9", "wiuAbBgRGkpwknuaPoGRQS2/K39uktJQWeRN2YvgBOE593rxeE7DZYMxLRXGATAQ0eLBuAPmUQDjuQWpXYzCPZOthQsXJvXxH5Oea/mlwESdMAHxiB53gezYPqxK1f3SKi48Bah5vQoF4c8SRb1b4LUAVjDO2bm3iFmBhgNFbihVrYW+1+UY5bs9/G36M3zDRRFOEpoE/wCHHRM+FzIRBBHhr5xmgX16c01Q80cQEH7TKalyVi6Y09qmii+817MmoZBIrnkrNrXCPHf0NSlI1rOtSxYOHeWMNl6XZ+Rdi+zJryZ865WyhM3ue4G1YDNG3lJDPT/YPEr7TlCQY+mhsz+EI4DwTmbhh/NM/Emwao6Fw8cCTV71lvhPlf+kX2ttJbxHIS3RBuvVzuFnKAQGt5J0i0pCI9HJkTeiVM5nTKV8lZNfZ0pWFreSzK3MMsx3m5osklmkKZzmf4V/FhIVyrxGfMjcOY35uzN0ZSJtyxHLMWbZCwjv3lth7ux31j9fW/HR4LUC0ItpMurwgVgHgRAKKgKN6IfftQWNzLdZi4ND9bC0yOfgmZLpKSBBslY6gJ/4NN3p7EvRvpeZhBtdWE58s8VvETaj2psBGZuVB2ALHWp3ntHXmljW+2Tu5aSdHLmBgwvGOCRBpKYc/mGJjtqMEocD4POF840Z5cJQT8s=")).interactExecutor((player, npc, type) -> new Challenge(User.fetch(player.getUniqueId())).build()).build();

    @Override
    public void rejoin(User user, Rejoin rejoin) {
        super.rejoin(user, rejoin);
    }

    @Override
    public void quit(User user) {
        super.quit(user);
        user.resetSecondsAlive();
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        gameScoreboard.updateTitle("§b§lPVP: DAMAGE");
        List<String> scores = new ArrayList<>();
        scores.add(" ");

        DamageSettings settings = user.getDamageSettings();

        boolean inChallenge = settings.isInChallenge();
        Language language = user.getAccount().getLanguage();

        if (!user.isKept()) {
            scores.add(inChallenge ? "Desafio: " + settings.getChallenge().getName(language, true) : "Modo: §aCustom");
            scores.add("Tempo: §7" + format(user.getAliveSeconds()));

            if (!inChallenge) {
                scores.add(" ");
                scores.add("Wither: " + (settings.isWither() ? "§a✔" : "§c✘"));
                scores.add("Drops: " + (settings.isDrops() ? "§a✔" : "§c✘"));
            }

        } else {
            scores.add(inChallenge ? "Desafio: " + settings.getChallenge().getName(language, true) : "Modo: §aCustom");
            if (inChallenge)
                scores.add("Recorde: §7" + format(user.getAccount().getData(settings.getChallenge().getColumns()).getAsInt()));
        }

        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void onLogin(User user) {
        Player p = user.getPlayer();

        EDITOR.clone(p).spawn(true);
        CHALLENGES.clone(p).spawn(true);

        Hologram hologram = new Hologram(p, EDITOR.getLocation().clone().add(0, 2.1, 0), "§b§lEDITOR", "§7(Clique aqui)");
        hologram.show();

        Hologram hologram2 = new Hologram(p, CHALLENGES.getLocation().clone().add(0, 2.1, 0), "§b§lDESAFIOS", "§7(Clique aqui)");
        hologram2.show();
    }

    @EventHandler
    public void onUserDied(UserDiedEvent event) {
        if (!event.getGame().getUniqueId().equals(getUniqueId()))
            return;

        User killed = event.getKilled();

        Player player = killed.getPlayer();

        player.sendMessage(killed.getAccount().getLanguage().translate("pvp.arena.death_to_anyone"));
        killed.getGame().join(killed, false);
        player.teleport(spawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == WITHER)
            return;
        if (!(event.getEntity() instanceof Player))
            return;
        User user = User.fetch(event.getEntity().getUniqueId());
        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;
        event.setCancelled(true);
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

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (user.isKept()) {
            event.setCancelled(true);
            return;
        }

        Item item = event.getItem();

        if (item.hasMetadata("owner") && item.getMetadata("owner").get(0).asInt() != player.getEntityId()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        double Y = to.getY();

        if (Y > 63 || Y < 58)
            return;

        User user = User.fetch(player.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (!user.isKept())
            return;

        if (player.getGameMode() != GameMode.SURVIVAL)
            return;

        if (Vanish.getInstance().isVanished(player.getUniqueId()))
            return;

        Block block = to.getBlock().getRelative(BlockFace.DOWN);

        if (block.getType() != Material.STAINED_CLAY)
            return;

        if (block.getData() != 4)
            return;

        user.setKept(false);

        DamageSettings settings = user.getDamageSettings();

        if (settings.isWither()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 3), true);
        }

        player.setNoDamageTicks(15);
    }

    @Getter
    public enum DamageType {

        EASY(4, "Fácil", "Easy", "§a", "§c2.0 §4❤", 5, Columns.PVP_DAMAGE_EASY),
        MEDIUM(5, "Médio", "Medium", "§e", "§c2.5 §4❤", 4, Columns.PVP_DAMAGE_MEDIUM),
        HARD(7, "Difícil", "Hard", "§c", "§c3.5 §4❤", 1, Columns.PVP_DAMAGE_HARD),
        EXTREME(9, "Extremo", "Extreme", "§4", "§c4.5 §4❤", 14, Columns.PVP_DAMAGE_EXTREME),
        VARIABLE(1, "Variado", "Variable", "§9", "§c2.0 §4❤ §7- §c4.5 §4❤", 11, null);

        private final double damage;
        private final String portugueseName, englishName, color, description;
        private final int durability;
        private final Columns columns;

        DamageType(double damage, String portuguese_name, String english_name, String color, String description, int durability, Columns columns) {
            this.damage = damage;
            this.portugueseName = portuguese_name;
            this.englishName = english_name;
            this.color = color;
            this.description = description;
            this.durability = durability;
            this.columns = columns;
        }

        public double getDamage() {
            if (this == VARIABLE) {
                return Math.floor(Math.random() * 8 + 1);
            } else return damage;
        }

        public String getName(Language language, boolean color) {
            return (color ? getColor() : "") + (language == Language.PORTUGUESE ? getPortugueseName() : getEnglishName());
        }

        public static DamageType fromDurability(int durability) {
            return Arrays.stream(values()).filter(damageType -> damageType.getDurability() == durability).findFirst().orElse(null);
        }

    }

    @Getter
    public enum DamageFrequency {

        FIRST("0.25s", "Receba dano a cada 0.25s", new ItemStack(Material.IRON_SWORD, 1), 5),
        SECOND("0.50s", "Receba dano a cada 0.50s", new ItemStack(Material.IRON_SWORD, 2), 10),
        THIRD("0.75s", "Receba dano a cada 0.75s", new ItemStack(Material.IRON_SWORD, 3), 15),
        FOURTH("1.00s", "Receba dano a cada 1.00s", new ItemStack(Material.IRON_SWORD, 4), 20),
        FIFTH("1.50s", "Receba dano a cada 1.50s", new ItemStack(Material.IRON_SWORD, 5), 30);

        private final String description, explanation;
        private final ItemStack icon;
        private final int frequency;

        DamageFrequency(String description, String explanation, ItemStack icon, int frequency) {
            this.description = description;
            this.explanation = explanation;
            this.icon = icon;
            this.frequency = frequency;
        }

        public static DamageFrequency fromItemStack(ItemStack itemStack) {
            return Arrays.stream(values()).filter(damageFrequency -> damageFrequency.getIcon().getAmount() == itemStack.getAmount()).findFirst().orElse(null);
        }

    }

}