package com.minecraft.hub.lobby;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.AnimatedString;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.translation.Language;
import com.minecraft.hub.Hub;
import com.minecraft.hub.listener.BlockListeners;
import com.minecraft.hub.listener.PlayerListeners;
import com.minecraft.hub.listener.ServerListeners;
import com.minecraft.hub.user.User;
import com.minecraft.hub.user.listener.UserLoader;
import com.minecraft.hub.util.Items;
import com.minecraft.hub.util.features.chair.ChairStairs;
import com.minecraft.hub.util.lobby.LobbyThread;
import com.minecraft.hub.util.vanish.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInitialSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.github.paperspigot.Title;

import java.util.Arrays;
import java.util.Iterator;

@Getter
public abstract class Lobby implements VariableStorage, Listener, BukkitInterface {

    private final Hub hub;
    private final boolean allowLegacy;

    private final Mode mode;
    private final int id;

    private final World world;

    private AnimatedString animatedString;

    @Setter
    private Location spawn;

    @Setter
    private String bossbar;

    public Lobby(final Hub hub, final boolean allowLegacy) {
        this.hub = hub;
        this.allowLegacy = allowLegacy;

        this.mode = Mode.fromString(this.hub.getConfig().getString("lobby.mode"));
        this.id = this.hub.getConfig().getInt("lobby.identifier");

        this.world = hub.getServer().getWorlds().get(0);

        this.world.setTime(6000L);
        this.world.setDifficulty(Difficulty.PEACEFUL);
        this.world.setAutoSave(false);
        this.world.setGameRuleValue("doMobSpawning", "false");
        this.world.setGameRuleValue("doFireTick", "false");
        this.world.setGameRuleValue("mobGriefing", "false");
        this.world.setGameRuleValue("doTileDrops", "false");
        this.world.setGameRuleValue("doEntityDrops", "false");
        this.world.setGameRuleValue("commandBlockOutput", "false");
        this.world.setGameRuleValue("doDaylightCycle", "false");
        this.world.setGameRuleValue("logAdminCommands", "false");
        this.world.setGameRuleValue("randomTickSpeed", "0");

        this.spawn = this.world.getSpawnLocation();

        new ChairStairs(hub);
    }

    public void startThread() {
        new LobbyThread(true);
    }

    public void loadListeners() {
        getHub().getServer().getPluginManager().registerEvents(this, getHub());
        getHub().getServer().getPluginManager().registerEvents(new ServerListeners(), getHub());

        getHub().getServer().getPluginManager().registerEvents(new UserLoader(getHub()), getHub());
        getHub().getServer().getPluginManager().registerEvents(new PlayerListeners(getHub()), getHub());
        getHub().getServer().getPluginManager().registerEvents(new BlockListeners(getHub()), getHub());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(final PlayerJoinEvent event) {
        event.setJoinMessage(null);

        final Player player = event.getPlayer();
        final User user = User.fetch(player.getUniqueId());

        user.setScoreboard(new GameScoreboard(player));
        user.getBossbar().setMessage(this.getBossbar());

        final Account account = Account.fetch(player.getUniqueId());
        final boolean vanish = account.getRank().isStaffer();

        if (vanish) Vanish.getInstance().setVanished(event.getPlayer(), account.getRank());

        Visibility.INSTANCE.refresh(player);

        final Tag tag = account.getProperty("account_tag").getAs(Tag.class);

        if (!vanish && account.getPreference(Preference.LOBBY_ANNOUNCE_JOIN) && tag.isBetween(Tag.PARTNER_PLUS, Tag.MEMBER)) {
            getHub().getUserStorage().getUsers().forEach(target -> {
                final Account targetAccount = target.getAccount();

                if (targetAccount == null)
                    return;

                target.getPlayer().sendMessage(targetAccount.getProperty("account_prefix_type", PrefixType.DEFAULT).getAs(PrefixType.class).getFormatter().format(tag).replace("#", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+") + player.getName() + " " + targetAccount.getLanguage().translate("lobby.joined_message"));
            });
        }

        if (!vanish)
            player.setGameMode(GameMode.SURVIVAL);

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        final boolean isVip = account.hasPermission(Rank.VIP);

        account.setProperty("lobby.fly", isVip);

        player.setAllowFlight(isVip);
        player.setFlying(isVip);

        ((CraftPlayer) player).getHandle().updateAbilities();

        final Language language = account.getLanguage();

        player.sendTitle(new Title("§b§l" + Constants.SERVER_NAME.toUpperCase(), language.translate("lobby.welcome_title"), 8, 15, 12));

        final PlayerInventory playerInventory = player.getInventory();

        playerInventory.clear();
        playerInventory.setArmorContents(null);

        Items.find(language).build(player);

        this.handleItems(player, account);

        this.handleScoreboard(user, getSidebarName());

        this.handleJoin(player);
    }

    public void handleItems(final Player player, final Account account) {
        ItemStack profileStack = new ItemFactory().setSkull(player.getName()).setName("§a" + account.getLanguage().translate("lobby.profile_item")).getStack();

        ItemFactory visibilityStack = new ItemFactory(Material.INK_SACK);

        if (account.getPreference(Preference.LOBBY_PLAYER_VISIBILITY)) {
            visibilityStack.setDurability(10);
            visibilityStack.setName("§fPlayers: §aON");
        } else {
            visibilityStack.setDurability(8);
            visibilityStack.setName("§fPlayers: §cOFF");
        }

        player.getInventory().setItem(1, profileStack);
        player.getInventory().setItem(7, visibilityStack.getStack());
    }

    public void removeRecipes() {
        Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

        while (iterator.hasNext()) {

            Recipe recipe = iterator.next();

            if (recipe.getResult() != null) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onUpdateSidebar(ServerHeartbeatEvent event) {
        if (!event.isPeriodic(this.mode.getTickRate()))
            return;
        getHub().getUserStorage().getUsers().forEach(user -> handleScoreboard(user, animatedString == null ? getMode().getSidebarName() : animatedString.next()));
    }

    @EventHandler
    public void onDefineSpawn(final PlayerInitialSpawnEvent event) {
        event.setSpawnLocation(Account.fetch(event.getPlayer().getUniqueId()).hasPermission(Rank.VIP) ? this.spawn.clone().add(0, 3, 0) : this.spawn);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SLIME_BLOCK) {
            final Vector vector = this.spawn.clone().getDirection().multiply(2.9).setY(0.66);

            final Player player = event.getPlayer();

            player.setVelocity(vector);
            player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 2.5F, 2.5F);
        }
    }

    public String getSidebarName() {
        return this.mode.getSidebarName();
    }

    public abstract void handleScoreboard(final User user, final String displayName);

    public abstract void handleJoin(final Player player);

    @AllArgsConstructor
    @Getter
    public enum Mode {

        DEFAULT("Main Lobby", "lobby", "§b§l" + Constants.SERVER_NAME.toUpperCase(), 3),
        HG("HG Lobby", "hglobby", "§b§lHARDCORE GAMES", 20),
        DUELS("Duels Lobby", "duelslobby", "§b§lDUELS", 20),
        PVP("PvP Lobby", "pvplobby", "§b§lPVP", 20),
        GLADIATOR("Gladiator Lobby", "gladiatorlobby", "§b§lGLADIATOR", 20),
        BRIDGE("The Bridge Lobby", "bridgelobby", "§b§lTHE BRIDGE", 20);

        private final String name, connectionName;
        @Setter
        private String sidebarName;
        private final int tickRate;

        @Getter
        private static final Mode[] values;

        static {
            values = values();
        }

        public static Mode fromString(final String string) {
            return Arrays.stream(getValues()).filter(mode -> mode.name().equalsIgnoreCase(string)).findFirst().orElse(null);
        }
    }

    public void setAnimatedString(AnimatedString animatedString) {
        this.animatedString = animatedString;
    }
}