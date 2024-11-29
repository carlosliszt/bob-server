/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.hall;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.command.ProfileCommand;
import com.minecraft.core.bukkit.event.player.PlayerShowEvent;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.scheduler.GameRunnable;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.bossbar.Bossbar;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.accessory.AccessoryInventory;
import com.minecraft.lobby.feature.chair.ChairStairs;
import com.minecraft.lobby.user.User;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@Getter
@Setter
public abstract class Hall extends GameRunnable implements Listener, VariableStorage, BukkitInterface {

    private final Lobby lobby;
    private final String name;
    private final String displayName;
    private final String connectionName;
    private final String bossbar;

    private final Set<User> users;

    private World world;
    private Location spawn;

    @Getter
    private Hologram bestTime;

    private int room;


    public Hall(Lobby lobby, String displayName, String connectionName, String bossbar) {
        this.lobby = lobby;
        this.displayName = displayName;
        this.connectionName = connectionName;
        this.bossbar = bossbar;

        this.name = getClass().getSimpleName();
        this.users = new HashSet<>();
        this.room = lobby.getConfig().getInt("hall.identifier");

        this.world = Bukkit.getWorlds().get(0);
        this.world.setAutoSave(false);

        this.spawn = new Location(this.world, 0, 62, -17, 0, 0);

        this.findSlimeBlocks();

        Lobby.getEngine().getServerStorage().listen(ServerType.values());

        Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

        while (iterator.hasNext()) {

            Recipe recipe = iterator.next();

            if (recipe.getResult() != null) {
                if (recipe.getResult().getType() == Material.BLAZE_POWDER)
                    iterator.remove();
            }
        }


        getWorld().setTime(6000);
        getWorld().setGameRuleValue("doDaylightCycle ", "false");

        new ChairStairs(lobby);
    }

    protected int tick;

    @Override
    public void run() {
        tick++;

        if (isPeriodic(20)) {
            getUsers().forEach(this::handleSidebar);
            sync(this::updateLobbiesSelectors);

            for (User user : users) {
                if (user.isParkourMode()) {
                    user.setParkourTime(user.getParkourTime() + 1);
                }
                if (user.isPlateDelay())
                    user.setPlateDelay(false);
            }

        }
    }

    private final Set<UUID> vanisheds = new HashSet<>();

    public void join(User user) {
        getUsers().add(user);
        user.setHall(this);

        Player player = user.getPlayer();

        if (player.getPassenger() != null)
            player.getPassenger().leaveVehicle();

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setLevel(0);
        player.setExp(0);

        if (!Vanish.getInstance().isVanished(player.getUniqueId()))
            player.setGameMode(GameMode.SURVIVAL);

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        for (UUID uuid : getVanisheds()) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null) {
                vanished.hidePlayer(player);
            }
        }

        Account account = user.getAccount();

        Bossbar bossbar = BukkitGame.getEngine().getBossbarProvider().getBossbar(player);

        bossbar.setMessage("§b§l" + this.bossbar);
        bossbar.setPercentage(1F);

        BukkitGame.getEngine().getBossbarProvider().updateBossbar(player);

        boolean vip = account.hasPermission(Rank.VIP);

        account.setProperty("lobby.fly", vip);
        player.setAllowFlight(vip);
        player.setFlying(vip);

        ((CraftPlayer) player).getHandle().updateAbilities();

        InteractableItem games = new InteractableItem(new ItemFactory().setType(Material.COMPASS).setName("§aJogos").getStack(), new InteractableItem.Interact() {
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                gameSelector(player);
                return false;
            }
        });

        InteractableItem profile = new InteractableItem(new ItemFactory().setType(Material.SKULL_ITEM).setDurability(3).setSkull(player.getName()).setName("§aPerfil").getStack(), new InteractableItem.Interact() {
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                new ProfileCommand.ProfileMenu(player, account).open();
                return false;
            }
        });

        InteractableItem collectibles = new InteractableItem(new ItemFactory().setType(Material.CHEST).setDurability(3).setName("§aAcessórios §7(Clique Direito)")
                .setDescription(
                        "§eClique para ver os cosméticos!"
                )
                .getStack(), new InteractableItem.Interact() {
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                if (Account.fetch(player.getUniqueId()).hasRank(Rank.DEVELOPER_ADMIN)) {
                    new AccessoryInventory(player, account, AccessoryType.NULL).openInventory();
                } else {
                    player.sendMessage("§cEm desenvolvimento.");
                }
                return false;
            }
        });

        InteractableItem lobbies = new InteractableItem(new ItemFactory().setType(Material.NETHER_STAR).setName("§aLobbies").getStack(), new InteractableItem.Interact() {
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                Selector selector = account.getLanguage() == Language.PORTUGUESE ? ptLobbiesSelector : enLobbiesSelector;
                selector.open(player);
                return false;
            }
        });

        player.getInventory().setItem(0, games.getItemStack());
        player.getInventory().setItem(1, profile.getItemStack());

        player.getInventory().setItem(4, collectibles.getItemStack());

        player.getInventory().setItem(7, new ItemFactory().setType(Material.INK_SACK).setDurability(10).setName("§fPlayers: §aVisíveis").getStack());
        player.getInventory().setItem(8, lobbies.getItemStack());
    }

    public void quit(User user) {
        getUsers().remove(user);
    }

    public final List<Location> slimeLocationList = new ArrayList<>();

    public void findSlimeBlocks() {
        Location spawn = this.getSpawn();
        for (int x = -60; x < 60; x++) {
            for (int z = -60; z < 60; z++) {
                for (int y = -60; y < 60; y++) {
                    final Block block = spawn.clone().add(x, y, z).getBlock();
                    if (block.getType() == Material.SLIME_BLOCK) {
                        slimeLocationList.add(block.getLocation());
                    } else if (block.getType() == Material.SPONGE) {
                        Lobby.getLobby().blockSet.add(block);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockVisibility(PlayerShowEvent event) {
        if (getVanisheds().contains(event.getReceiver().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractListener(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT"))
            return;
        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() != Material.INK_SACK)
            return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;
        Player player = event.getPlayer();
        Account account = Account.fetch(player.getUniqueId());
        if (itemMeta.getDisplayName().equals("§fPlayers: §cInvisíveis")) {
            player.setItemInHand(new ItemFactory().setType(Material.INK_SACK).setDurability(10).setName("§fPlayers: §aVisíveis").getStack());
            getVanisheds().remove(player.getUniqueId());

            for (Player o : Bukkit.getOnlinePlayers()) {
                if (o.getEntityId() == player.getEntityId())
                    continue;
                if (Vanish.getInstance().isVanished(o.getUniqueId())) {
                    if (account.getRank().getCategory().getImportance() < Vanish.getInstance().getRank(o.getUniqueId()).getCategory().getImportance())
                        continue;
                }
                player.showPlayer(o);
            }

            player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("lobby.players_enabled"));
        } else if (itemMeta.getDisplayName().equals("§fPlayers: §aVisíveis")) {
            player.setItemInHand(new ItemFactory().setType(Material.INK_SACK).setDurability(8).setName("§fPlayers: §cInvisíveis").getStack());
            getVanisheds().add(player.getUniqueId());

            for (Player o : Bukkit.getOnlinePlayers()) {
                if (o.getEntityId() == player.getEntityId())
                    continue;
                player.hidePlayer(o);
            }

            player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("lobby.players_disabled"));
        }
        player.updateInventory();
    }

    private final Random random = Constants.RANDOM;

    @EventHandler
    public void handleSlimeParticles(ServerHeartbeatEvent event) {
        if (!event.isPeriodic(10))
            return;
        if (Bukkit.getOnlinePlayers().size() == 0)
            return;
        for (Location locations : getSlimeLocationList()) {
            if (random.nextBoolean())
                continue;
            Location location = locations.clone().add(0.5, 0.6, 0.5);
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.VILLAGER_HAPPY, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), 0.25f, 0.25f, 0.25f, 1, 1);
            Bukkit.getOnlinePlayers().forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));
        }
    }

    public abstract void handleSidebar(User user);

    public abstract void handleNPCs(User user);

    protected boolean isPeriodic(int x) {
        return tick % x == 0;
    }

    public boolean isConnectionCooldown(UUID uuid) {
        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(uuid, "connect-cooldown");
        return cooldown != null && !cooldown.expired();
    }

    public Cooldown getCooldown(UUID uuid) {
        return CooldownProvider.getGenericInstance().getCooldown(uuid, "connect-cooldown");
    }

    public void addCooldown(UUID uuid) {
        CooldownProvider.getGenericInstance().addCooldown(uuid, "connect-cooldown", "connect-cooldown", 3, false);
    }

    public void gameSelector(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Selecionar jogo");

        InteractableItem hg = new InteractableItem(new ItemFactory().setType(Material.MUSHROOM_SOUP).setName("§aHardcore Games").setDescription("§7" + Constants.getServerStorage().count(ServerType.HG_LOBBY, ServerType.HGMIX, ServerType.SCRIM, ServerType.EVENT) + " jogando").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                if (isConnectionCooldown(player.getUniqueId())) {
                    Account account = Account.fetch(player.getUniqueId());
                    Cooldown cooldown = getCooldown(player.getUniqueId());
                    player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                    return true;
                }

                addCooldown(player.getUniqueId());

                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.HG_LOBBY);

                Account account = Account.fetch(player.getUniqueId());

                if (server == null) {
                    player.sendMessage(account.getLanguage().translate("no_server_available", "hg"));
                    return true;
                }

                player.closeInventory();
                account.connect(server);
                return true;
            }
        });


        InteractableItem duels = new InteractableItem(new ItemFactory().setType(Material.DIAMOND_SWORD).setName("§aDuels").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDescription("§7" + Constants.getServerStorage().count(ServerType.DUELS_LOBBY, ServerType.DUELS) + " jogando").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                if (isConnectionCooldown(player.getUniqueId())) {
                    Account account = Account.fetch(player.getUniqueId());
                    Cooldown cooldown = getCooldown(player.getUniqueId());
                    player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                    return true;
                }

                addCooldown(player.getUniqueId());

                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.DUELS_LOBBY);

                Account account = Account.fetch(player.getUniqueId());

                if (server == null) {
                    player.sendMessage(account.getLanguage().translate("no_server_available", "duels"));
                    return true;
                }

                player.closeInventory();
                account.connect(server);
                return true;
            }
        });

        InteractableItem pvp = new InteractableItem(new ItemFactory().setType(Material.IRON_CHESTPLATE).setName("§aPvP").setDescription("§7" + Constants.getServerStorage().count(ServerType.PVP_LOBBY, ServerType.PVP) + " jogando").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                if (isConnectionCooldown(player.getUniqueId())) {
                    Account account = Account.fetch(player.getUniqueId());
                    Cooldown cooldown = getCooldown(player.getUniqueId());
                    player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                    return true;
                }

                addCooldown(player.getUniqueId());

                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.PVP_LOBBY);

                Account account = Account.fetch(player.getUniqueId());

                if (server == null) {
                    player.sendMessage(account.getLanguage().translate("no_server_available", "pvp"));
                    return true;
                }

                player.closeInventory();
                account.connect(server);
                return true;
            }
        });

        InteractableItem the_bridge = new InteractableItem(new ItemFactory().setType(Material.STAINED_CLAY).setName("§aThe Bridge").setDescription("§7" + Constants.getServerStorage().count(ServerType.THE_BRIDGE_LOBBY, ServerType.THE_BRIDGE) + " jogando").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                if (isConnectionCooldown(player.getUniqueId())) {
                    Account account = Account.fetch(player.getUniqueId());
                    Cooldown cooldown = getCooldown(player.getUniqueId());
                    player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                    return true;
                }

                addCooldown(player.getUniqueId());

                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.THE_BRIDGE_LOBBY);

                Account account = Account.fetch(player.getUniqueId());

                if (server == null) {
                    player.sendMessage(account.getLanguage().translate("no_server_available", "the_bridge"));
                    return true;
                }

                player.closeInventory();
                account.connect(server);
                return true;
            }
        });

        InteractableItem prototype = new InteractableItem(new ItemFactory().setType(Material.STICK).setName("§aProtótipos").setDescription("§7" + Constants.getServerStorage().count(ServerType.TIOGERSON) + " jogando").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                Account account = Account.fetch(player.getUniqueId());

                if (isConnectionCooldown(player.getUniqueId())) {
                    Cooldown cooldown = getCooldown(player.getUniqueId());

                    player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                    return true;
                }

                addCooldown(player.getUniqueId());
                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.PROTOTYPE);

                if (server == null) {
                    player.sendMessage(account.getLanguage().translate("no_server_available", "prototype"));
                    return true;
                }

                player.closeInventory();
                account.connect(server);
                return true;
            }
        });

        inventory.setItem(10, hg.getItemStack());
        inventory.setItem(11, duels.getItemStack());
        inventory.setItem(12, pvp.getItemStack());
        inventory.setItem(13, the_bridge.getItemStack());
        inventory.setItem(14, prototype.getItemStack());

        player.openInventory(inventory);
    }

    private Selector ptLobbiesSelector = Selector.builder().build();
    private Selector enLobbiesSelector = Selector.builder().build();

    public Selector buildLobbiesSelector(Language language, Selector other) {
        List<ItemStack> stacks = new ArrayList<>();

        for (Server server : Constants.getServerStorage().getServers(Constants.getServerType())) {

            if (server.isDead())
                continue;

            stacks.add(new ItemFactory(Material.STAINED_GLASS_PANE).setDurability(Bukkit.getPort() == server.getPort() ? 5 : 7).setName("§a" + getDisplayName() + " #" + server.getName().charAt(server.getName().length() - 1)).setDescription("§fPlayers §a" + server.getBreath().getOnlinePlayers() + "/" + server.getBreath().getMaxPlayers()).getStack());
        }

        Selector selector = Selector.builder().build();

        selector.setName(language.translate("lobby.container.select", getDisplayName()));
        selector.setItems(stacks);
        selector.setSize((getSize(stacks.size()) * 9) + 18);
        selector.setPreviousPageSlot(0);
        selector.setNextPageSlot(8);
        selector.setAllowedSlots(Arrays.asList(10, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24));
        selector.build();

        other.getPlayers().forEach(selector::open);

        return selector;
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {

            if (event.getClickedInventory().getName().contains("Lobby") && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {

                if (event.getCurrentItem().getDurability() == 14)
                    return;

                HumanEntity entityHuman = event.getWhoClicked();

                entityHuman.closeInventory();

                Account account = Account.fetch(entityHuman.getUniqueId());

                if (isConnectionCooldown(entityHuman.getUniqueId())) {
                    Cooldown cooldown = this.getCooldown(entityHuman.getUniqueId());

                    entityHuman.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                    return;
                }

                Server server = Constants.getServerStorage().getServer(getConnectionName() + event.getCurrentItem().getItemMeta().getDisplayName().split("#")[1]);

                addCooldown(entityHuman.getUniqueId());
                account.connect(server);
            }
        }
    }

    public void updateLobbiesSelectors() {
        this.ptLobbiesSelector = buildLobbiesSelector(Language.PORTUGUESE, this.ptLobbiesSelector);
        this.enLobbiesSelector = buildLobbiesSelector(Language.ENGLISH, this.enLobbiesSelector);
    }

    public int getSize(int rooms) {

        int number = 0;

        for (int i = 0; i < rooms; i++) {
            if (i % 5 == 0)
                number++;
        }
        return number;
    }

    public final Hologram.Interact interact = (player, hologram, line, type) -> {
        List<NPC> npcs = new ArrayList<>(BukkitGame.getEngine().getNPCProvider().getPlayerHumans(player));
        npcs.removeIf(NPC::isHidden);

        if (npcs.isEmpty())
            return;

        npcs.sort(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(hologram.getLocation())));
        NPC.Interact interact = npcs.get(0).getInteractExecutor();
        if (interact != null)
            interact.handle(player, npcs.get(0), NPC.Interact.ClickType.RIGHT);
    };

}