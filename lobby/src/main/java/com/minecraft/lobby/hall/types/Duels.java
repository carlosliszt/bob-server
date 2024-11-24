/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.hall.types;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.server.route.GameRouteContext;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import com.minecraft.core.bukkit.util.leaderboard.hologram.LeaderboardHologram;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardType;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.command.DuelCommand;
import com.minecraft.lobby.duel.DuelMenu;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Duels extends Hall {

    public Duels(Lobby lobby) {
        super(lobby, "Duels Lobby", "duelslobby", "DUELS NO BLAZEMC.COM.BR");

        setSpawn(new Location(Bukkit.getWorld("world"), 100.5, 50.0, 100.5, 0, 0));
        getLobby().getAccountLoader().addColumns(Columns.DUELS_SIMULATOR_WINS, Columns.DUELS_SIMULATOR_WINSTREAK, Columns.DUELS_GLADIATOR_WINS, Columns.DUELS_GLADIATOR_WINSTREAK);

        Constants.setServerType(ServerType.DUELS_LOBBY);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(340);

        gladiator_old = new Location(getWorld(), -31.5, 74.5, 36.5);
        simulator = new Location(getWorld(), -31.5, 74.5, 30.5);
        gladiator = new Location(getWorld(), -31.5, 74.5, 24.5);
        boxing = new Location(getWorld(), -31.5, 74.5, 18.5);

        sumo = new Location(getWorld(), 32.5, 74.5, 18.5);
        uhc = new Location(getWorld(), 32.5, 74.5, 24.5);
        scrim = new Location(getWorld(), 32.5, 74.5, 30.5);
        soup = new Location(getWorld(), 32.5, 74.5, 36.5);

        getLobby().getBukkitFrame().registerCommands(new DuelCommand());
    }

    @Override
    public void join(User user) {
        super.join(user);
    }

    @Override
    public void handleNPCs(User user) {
        Player player = user.getPlayer();

        player.getInventory().setItem(2, new ItemFactory(Material.BLAZE_ROD).setName("§aDesafiar §7(Direito no jogador)").getStack());

        Language language = user.getAccount().getLanguage();

        Bukkit.getScheduler().runTaskLater(getLobby(), () -> {

            BOXING.clone(player).spawn(true);
            SCRIM.clone(player).spawn(true);
            GLADIATOR.clone(player).spawn(true);
            GLADIATOR_OLD.clone(player).spawn(true);
            SOUP.clone(player).spawn(true);
            SIMULATOR.clone(player).spawn(true);
            UHC.clone(player).spawn(true);
            SUMO.clone(player).spawn(true);

            String click_to_play = language.translate("lobby.duels.click_to_play");

            Hologram boxing = new Hologram(player, BOXING.getLocation().clone().add(0, 2.1, 0), null, "§bBoxing", click_to_play);
            boxing.setInteract(interact);
            boxing.show();

            Hologram scrim = new Hologram(player, SCRIM.getLocation().clone().add(0, 2.1, 0), null, "§bScrim", click_to_play);
            scrim.setInteract(interact);
            scrim.show();

            Hologram gladiator = new Hologram(player, GLADIATOR.getLocation().clone().add(0, 2.1, 0), "§bGladiator", click_to_play);
            gladiator.setInteract(interact);
            gladiator.show();

            Hologram gladiator_old = new Hologram(player, GLADIATOR_OLD.getLocation().clone().add(0, 2.1, 0), "§bGladiator Old", click_to_play);
            gladiator_old.setInteract(interact);
            gladiator_old.show();

            Hologram soup = new Hologram(player, SOUP.getLocation().clone().add(0, 2.1, 0), "§bSoup", click_to_play);
            soup.setInteract(interact);
            soup.show();

            Hologram simulator = new Hologram(player, SIMULATOR.getLocation().clone().add(0, 2.1, 0), "§bSimulator", click_to_play);
            simulator.setInteract(interact);
            simulator.show();

            Hologram uhc = new Hologram(player, UHC.getLocation().clone().add(0, 2.1, 0), "§bUHC", click_to_play);
            uhc.setInteract(interact);
            uhc.show();

            Hologram sumo = new Hologram(player, SUMO.getLocation().clone().add(0, 2.1, 0), "§bSumo", click_to_play);
            sumo.setInteract(interact);
            sumo.show();

            LeaderboardHologram leaderboardHologram = new LeaderboardHologram(gladiatorBoard, "§e§lTOP 100 §b§lGLADIATOR RATING §7(%s/%s)", player, this.gladiator);
            leaderboardHologram.show();

            LeaderboardHologram leaderboardHologram1 = new LeaderboardHologram(simulatorBoard, "§e§lTOP 100 §b§lSIMULATOR RATING §7(%s/%s)", player, this.simulator);
            leaderboardHologram1.show();

            LeaderboardHologram leaderboardHologram2 = new LeaderboardHologram(soupBoard, "§e§lTOP 100 §b§lSOUP RATING §7(%s/%s)", player, this.soup);
            leaderboardHologram2.show();

            LeaderboardHologram leaderboardHologram3 = new LeaderboardHologram(uhcBoard, "§e§lTOP 100 §b§lUHC RATING §7(%s/%s)", player, this.uhc);
            leaderboardHologram3.show();

            LeaderboardHologram leaderboardHologram4 = new LeaderboardHologram(sumoBoard, "§e§lTOP 100 §b§lSUMO RATING §7(%s/%s)", player, this.sumo);
            leaderboardHologram4.show();

            LeaderboardHologram leaderboardHologram5 = new LeaderboardHologram(gladiatorOldBoard, "§e§lTOP 100 §b§lGLADIATOR OLD RATING §7(%s/%s)", player, this.gladiator_old);
            leaderboardHologram5.show();

            LeaderboardHologram leaderboardHologram6 = new LeaderboardHologram(boxingBoard, "§e§lTOP 100 §b§LBOXING RATING §7(%s/%s)", player, this.boxing);
            leaderboardHologram6.show();

            LeaderboardHologram leaderboardHologram7 = new LeaderboardHologram(scrimBoard, "§e§lTOP 100 §b§LSCRIM RATING §7(%s/%s)", player, this.scrim);
            leaderboardHologram7.show();

        }, (user.getAccount().getVersion() >= 47 ? 0 : 5));
    }

    @Override
    public void quit(User user) {
        super.quit(user);
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lDUELS");

        DataStorage storage = user.getAccount().getDataStorage();
        int count = Constants.getServerStorage().count();

        scores.add(" ");
        scores.add("§eGladiator:");
        scores.add(" §fWins: §b" + storage.getData(Columns.DUELS_GLADIATOR_WINS).getAsInteger());
        scores.add(" §fWinstreak: §b" + storage.getData(Columns.DUELS_GLADIATOR_WINSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§eSimulator:");
        scores.add(" §fWins: §b" + storage.getData(Columns.DUELS_SIMULATOR_WINS).getAsInteger());
        scores.add(" §fWinstreak: §b" + storage.getData(Columns.DUELS_SIMULATOR_WINSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§fPlayers: §a" + (count == -1 ? "..." : count));
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(scores);
    }

    private final NPC BOXING = NPC.builder().location(new Location(Bukkit.getWorld("world"), 97.5, 47.5, 134.5, 180, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYzODYyNDY1MTc0NSwKICAicHJvZmlsZUlkIiA6ICIzYTNmNzhkZmExZjQ0OTllYjE5NjlmYzlkOTEwZGYwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb19jcmVyYXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTIxMGRkNWY3NDgxNjJhMGQ0NzM1ZWJmNWE2ZjNlYTdhZDNiYWQ0N2ZkODYwNzUxYmY4NWFkYjkyYzM3ODVkYiIKICAgIH0KICB9Cn0=", "XCzosu8YWA9i0rRgMtmcwf/Mc3E65dpX4Bu8O2f7KfPZ8d59Z3J5e7D0JF3E4mzYzE674L4PPUHvb28uiRJwHDnHHJ0A6oGCveiH2WOW9TsnbO7KhHahQkWFDuxwaPtLqAMrMg85FeqbY2GfUulEWD9UMFGx6DJyCuODDW4HXSTNVY/mXEsYeaFIZ/2XJLyEcXIowHbuoGUJz+nX+CobUyP7OcG8G5UObHvvG196I06KaiOcBO4TDbs31g4na59dr5uk+ojA7v16AJLcPXpiOj+gDjnW3pfWKIzkTJqiS5VlK9uf4X6EOM4moXxNZ+NKIB4LzA0PWI+A1p08nOPk6dNELBnt/m7YxQwxDAq9irlYlOZ8H8ZOhARDt8ZxtdRC0nTFhH7FMOn2q7q2/NKbcDc3gHUbrd4wbDG/flSd6i11vYsWObfkc6Qj607ednc2bU65IVQHHy8/AjHYqNXcTBAPD/eCRsTkT7rkDiyJbbDKF1cLa/rxXFAkBjwTAdqw7FcF7sWdf1+oDxbDLl2E0vFchNcp01RWAR5r21vblEqfxNsXPTOB/VLTbyFESJh886N+27jfZh7klYTGYjuCQY80ifoZw6HIHaGpBP88oedG3zx3RJ7qTkxBpZl8gXZyRthbUjdyvRw5h+00BAWy5pag7PnPC3oHKsdo0FeXVLA=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.BOXING_1V1)).build();
    private final NPC SCRIM = NPC.builder().location(new Location(Bukkit.getWorld("world"), 100.5, 47.5, 131.5, 179, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTU4ODIzOTc2NTQ3MiwKICAicHJvZmlsZUlkIiA6ICI1MDAwZGJkNmVlMjY0ZDY4ODViM2M2YWFjOGEzNmE1YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaWN0aW1zX0ZyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFmMjFkMzllZTZiMzI4ODZhZDU4ZDU3MGU1MjdhNDAzMzYwZTI2OGE0NzVkN2E4NzE2MjdiNDVhZDE5ZmQ2YTYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", "x/pV98Kid4/nt4VxwVvsqwSOesyeUAgRCyX8D34m/k6ANAriwtzkxc+i/RV/NC7IuMnJPyHD35NOLtYOh0Z+cRazN7qmQMuz/J/b/cwhiq93LcnHPwLCAg1rJJmS0NlRJzFZArip0XBozYEvQWnPKw+0VC5DAF79cD0maOvLTC165uT0UA88rizSasITVT3xHjZ7njVrYllj/x4M5hhfLMS4e/MSKgU+uceixnv3nQD9iCnNfVZS8921/zOi7HG65nm9BlbDTPYElclwD/PDXTD7Eylja6pHJkMrvSdCttXP6ejWbGmDE075Svsjw4xRejywfkptblJbh12ZavbuID1az653ExalNREB2YBzXm1VmUTkB2c30q+oG1vtw2wYoIRJwwOGBk5UEyjSGfdYsjfKemJm9sWIw6OGltFR6h4jjxqaHBi44Nzxn6SSRKrD3KO5H2MDQlcwP+s0n0FB0x2zL8pIoOdwQXTuwn+PntsFccL1pOIc5lyIQaZFZ+HRqANwRqlk8bTAKpyB0PiJyTvg5A6yCjM9AZbQxBskpfGOUCx6wijoQKtZ8AG3PGsYVwnREOKXpifda97BvyedB57mJHGpcWpS8T7BRWhnyPI0aGXCxY6T856njU+AKl1TfbB+IMHCn/gbDPp/Y9qqr/GSK4iosWHXNUo8s5aY5EA=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.SCRIM_1V1)).build();
    private final NPC GLADIATOR = NPC.builder().location(new Location(Bukkit.getWorld("world"), 100.5, 47.5, 135.5, 180, 0)).property(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1ODY0NzY5NjIwOTcsInByb2ZpbGVJZCI6ImRlNTcxYTEwMmNiODQ4ODA4ZmU3YzlmNDQ5NmVjZGFkIiwicHJvZmlsZU5hbWUiOiJNSEZfTWluZXNraW4iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk5YWMxYTEzYzAxYWFkMzgxNzFjODYyNjBhZDFkNTMyYmZkMzRiZGE4YTg5MDcxMmU4ZGMyYzU3NDZmMzQ1MjgifX19", "F86AaNY4KP6r1pZ6TMBGCsnNJb5mcvxDQOKicHEUane/4VMMIMM/ywZ5BuoxugG5WZYRflIeJcKbacv6lJF5vTmsf5bcdHq2pY10rqVkacz55n6GrSbVzK3fDNYCXwL1ThDDmJxHnXFqpoPOSr33ovZKYtshGk9QqPVbxmYhNU44xnH/BdgrB0bD/nzF1QgO/c4vo+iH/zEK8K0+m7f1ErIh6YGXcSPb0K6CWhPdpP6ahp7/Qy8+ZfPnlX5f3dAuVyhJRT7UE4wsjutR3+VrarzKjPfpaS0s2Eg8IHIkZtV+WdPildm1unSESq1TqUGRJTX9pZPA7e7vYCktBXKQqoU4IXovnXaOTyyVtVi6+v62uIg4hggCSaH20mrSHuXCmXwnfsKlkQgZoRnElqtrxwa//s82hIIxznhPvbkoALe+WUH5o3xhIxTwb53WKnxjX4+sqsavhgQS+crkmIt6HA0glH73PRIsqy5IyrRIltsXQDeUOpWLNy2mHuNRI9XV33TJ5QyOJRTJjJ2VYystVM6L/GNbrOtTsT8AXI7J0TBehHs5YSrY1f+V1K3bnTZdkLvOSCAurkBKozIlyios+WPPtZ8cl5wwbkHXkH3r9Iua3OXVaVAQz0aLJJeglragIP/ES5Ogx0UGp0No0Zu37nsJcY+ouAoNO9ZaWFe/RDI=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.GLADIATOR_1V1)).build();
    private final NPC GLADIATOR_OLD = NPC.builder().location(new Location(Bukkit.getWorld("world"), 103.5, 47.5, 134.5, 179, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYwNDAxNTQ2NjE3OCwKICAicHJvZmlsZUlkIiA6ICI0MDQxMzU5MDk0MmY0ZGIwYTVlNzBmZDNjZWMyOGE1MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaZXBoaHlyZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83ZjdkZGRmNjg4MDM1NzgzNWQyMWViZGY0NGU3MThiMGFmYTczOTY2NTkwNDNiNDQ5MTI5MDk4NGFjOThmNTRhIgogICAgfQogIH0KfQ==", "h2U16BVMP2ZyuUVtpb3An3YNkvfMSsq3Iary3adD2D7eIL9TlaxFXk4z+p3hXFRBR6hi0NaQR1MXsjE/29HEVMcpGSbO/AR4Zv7cqdOr35DKuXaRLBQ4L2N9OEpBdSeQUFEh5SomU7yA9bU1vnRzAHvfsoOyxO+Ivjupd90gLjy1io3eXXIRgT/bw0KpvrYBfYKvRAAWnn/U9AwwZNLenYCV6bOAEqPWA8lEtfQ12/vuCUdfX+D9COwgStN7VS0Hg8gyaoJZ4dmtYGl3DL4WH6VxG6zGUVp8ZYFF7Ll1rARyNJgxcYCKnJBnxVBrz5aBXV/Ig9M/zUOOloeKtp1SH+cYhvfpbIFBBWLkFVAwSx9yzTAqtekwJFRW6CpL2/fHi5qLm/KTC12IVB33MkAiybXW9Gypq+hbtrhQcvctDsPOAVsJH8cLSZr2iHSOS4w9baYHBSp03RPY3CKWXje65wVzl8SMnN1E7rSDSjZrBSeKzsNsaeKRlP3z78NeuKYvgrnbU5dqNaYtXTSmT/smD3qPwNzjxzvJ5iAp4PZ+9KXDygJ8UaZiNWOuD1dQsrbRIwyJ2NfkGON4Tg5rnP5PXESg89kVx4t+ESeXC3OwJlO/d+TkuhJu2HedGNXuREy4+lK2FaCXhQd5pJ2Z2G54MsGFPEdwa5HjzHPNUf+nMrY=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.GLADIATOR_OLD_1V1)).build();
    private final NPC SOUP = NPC.builder().location(new Location(Bukkit.getWorld("world"), 109.5, 47.5, 130.5, 180, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyNjU3NTEyNDgxNywKICAicHJvZmlsZUlkIiA6ICIzYTNmNzhkZmExZjQ0OTllYjE5NjlmYzlkOTEwZGYwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb19jcmVyYXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTU0NTk0ZGU1NjAxMmI2MDRkNDg0ODJlY2RlY2U3OThhNjVlZDBiNjRiNGU3ZjkzY2QxNGRiNmU4YjFiMjhkOSIKICAgIH0KICB9Cn0=", "bP4Xuty6lGRVeycXZUBAfuza7nZhShpfx4IppEaFg73fqgnFdO/bsPRO6Lv70TVC+gmmRIbR4L86p83DEYaiSaS/SCq0d6f5VgJEOhlBNftnvGz94OL9733S/fMNrwceDAd9Bd8vFu5ceSOa6lDBcGHyeuJ0Upl1wHbVTptqsYN8ant5jjK3+LKt11immLjAmWvpNSTd5TTBOl5M016t3+ZXxugOUUvhf3mvsyMO2JGeLtuHAVekZ+rYgnU5jg6Vu4FQWeAbBg6qwOKhw1Mg3DyM8zpTvuuCj06VLICUsF7eQRyD3e8vs3uufsZdw43vU2E8Qy6vBnM/A+4gWu/WjLklDZhWeUsui/LGbHDBUFnTlxqP8v0nhDy4POf2blPz0SVHcgnBwo7FtYzrEmW+vZxdPwqUchpq1E9Z2gZauKFXVK82CMH/j1Coi10htnBYzWUVotrUXODxKck7lU2FZPb7qOfbbqMNKsefhcR6c4L7oU1qMFDTZ5IF4MuM8XdsnjJwYJAtQ3VDc/yWDUgVGFh9pmecXyvrHMb+fZHPg2nT1VT9r4kryKbg9ra5wIj0j1lY4Tlf/KXcFtLVYkHnyh1JtDSZ0Wj77PlE0VTMrRLW1cv0jwMNU0rzp8Y+sndzNG0P3fmkKV2FCXr5pKiOEVen5mNlFpoMiCyHVM2BQRA=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.SOUP_1V1)).build();
    private final NPC SIMULATOR = NPC.builder().location(new Location(Bukkit.getWorld("world"), 106.5, 47.5, 132.5, 180, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTU5NDk1OTEzNjcwNSwKICAicHJvZmlsZUlkIiA6ICIwYTQ3NDJjOWQ3YWY0NmM4OTllODNlNTdiYWE0MTNmYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDZWxsYml0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM4ZDEzMWQ5YmQ2ZjJhNTIwY2EyMTJjYWY1MDU0MGJhZGE5MWUyNzA0YmM1YmFhNmIzYzRkNjM3OTE4MWM1NjQiCiAgICB9CiAgfQp9", "JMgaRF67DJ7iYyrMaTWyGmYw36N7eQN+3fP7e94PxWIBX7thwvQz1OR2AkPtgUaPZAu/BoFGA4Cb9QDKkyWq/vic/H0cww79BOLt9atdm0/MUsljsLQB+r9ugIttFt1yTDSOmpUM1nObQYbC55rDB4b9rRpQZuYVXrM9Hot2mXLT7/QCqfAgdXtpz/C2aOUt6BMHDuqqMtRSq7fvxAaMeT1XKnVFukk5+PJXMM88lBIi7wmt6wkdiOO9aKur05EiYihXh6cxcWLFNv1dGYG1CLlcTILO3N4hluMNX77bSGPzADwA3kYUswxhsQGwV1M96cuY8mZJqFXBClTOKFgv8d/CqXVHUkDowNHWveaw4YQJd2cM2kcJDj0joZ4GFxmaz2/1UaNEvQV6iNzLu3w9vTcfrK50EPf0YCUTrsemQxvTy98UAUqOe8ZXzAa6BgIYPgOQEmd3QSZc4cOoRuPjwVvZ/dyxTIs55Z0i3B6iyEU6Ikp4hCe3Nhky5M8ni0mNd+Z+Drw+yTVIZhmeDMZ097223Rz4VLBNqMZHF+smG5RBkxipqVeLCOdqvdyEqSS9t0lDafuNWsmRh146s1t048/nX05MTHZACR4yUiu5IponwXgl6EZ6DpOAjv3FSOfliewRV2r4HyVe/CDSCnq5uzBXwHfiZL0CQM/IR5JTw0E=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.SIMULATOR_1V1)).build();
    private final NPC UHC = NPC.builder().location(new Location(Bukkit.getWorld("world"), 94.5, 47.5, 132.5, 180, 0)).property(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1ODc5NDMwOTg2MTgsInByb2ZpbGVJZCI6Ijc3MjdkMzU2NjlmOTQxNTE4MDIzZDYyYzY4MTc1OTE4IiwicHJvZmlsZU5hbWUiOiJsaWJyYXJ5ZnJlYWsiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y5ODc4ODUzNjU0YzNiYjI2ZmQzM2U4MGY4ZWQzY2RmMDNhYjMyNDdmNzM2Nzg0NjRlMDZkYTE0M2RiZDBjMTcifX19", "YtrQNd28PH3VzwY+xRH1GxEDMfaXgkTaaVqJXn1dHGk5s3vMATsQ8Yj2AN+LtJMxdawM8wCiG7PjklMOlp4GcKZtzbLEkNOXS71Nmji0BnKNR5hfXRr2LzEAOd7prPC3Agql0rEyncFbx39FFmwY0Io5jKHkV+tmS+NR1c5L6XrwU32SBStm+U6QzXAtC3+yloHPTAub6vjICoTmg9F5k5+8tq2wkwmOvcHepmhSYKc5T8x3LVIvEq5xjANSztMt/E7FFbiWt/V0GzU3ZBoKlcXONZUd/GduNo520OG5vCYezf/ZGxJPa3HbroLtgRTh1O2cnuqAzSPQDJX/n6vax4tchyCLCk6BpBZsVe13vCCANQ5bjIHNkdElSjImyezLM8/h11/YuD5TZtzHXYX18Hs6vFuIyUtYI8jR7g1YDnrgwLWsv2m8WyuGgo7hGTn8g84uWeJ11F5n7EB3ZjRUI3WyNJeJf6ibTbN+NspdgucyyAMeLP1MQIshVGCBUWQa1o06+qJNBFgpGaOazTZAbsOUsohPPbH5W9zVG4p8LAO00hRkuE4bdHj4U3yaVeQTwnXsHt3uG6dB60u8I185IqG+qXSQBxGepn9GLx6XK7AQxTSg+mKbrEFhYdtToqJGOJRKvceMWzYJYeoqigx04560Ygd/CfTFW487EpqfXKk=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.UHC_1V1)).build();
    private final NPC SUMO = NPC.builder().location(new Location(Bukkit.getWorld("world"), 91.5, 47.5, 130.5, 180, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyMjM4MjQzNzIyNiwKICAicHJvZmlsZUlkIiA6ICIxOTI1MjFiNGVmZGI0MjVjODkzMWYwMmE4NDk2ZTExYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXJpYWxpemFibGUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWMzNWVlMTU2NTdkY2JhMmYwNmQ0OTRjYzY2MTM0MDRjZjE0MDIzMTYxNjUwZjdmYzlmZmE4ODNlYjhiNTg5NiIKICAgIH0KICB9Cn0=", "yeCvQy4UAW9Rk0fajKpSak3LioFrXVWPfWI1CMu6wyyUcGOqHn2u4reMxq/Zg3XY7dvdAO6BjwIiaqtFSAit2wur0YCf4JoH+C9U+ghg1Rax2fjzM73ItrtJnxhOGbWwWMOwuLnJK6tDVFmY6PM0UFpx3HPWCdNZOK9yMBvchhcmUfoulEKGcj4uf2FLRowGqfNb8wYCnnBQO7MNHMA+6Zqy7/mH/jTPaq6l1ohoDFYRLZdgN7rbN3ViHBCj9Qnr7k58j+7ytn7Hn7rLoeSSaKg5ukmZxWLQhhJuFyVXZEmFfA+0u7rrK8EbufbCzl2R7shtmUvEJbv9SSRss3TG33HC39jIjvKucJg71oQ5j85eseYvIyWundWFfVZl4bc2oJyOzkTFbVNVf5rZEbV+B1ry3CB7VKwXuplV8KVvB+5qtP6H5d5dp4CIoMDg4xtM6WLedQwT08HZL7RJN5Wys9OmJJjCYHZuQeCytz7bCcWZaRaKD3snbf3x7VF5BiArsstIqfmmVBJPok2E4juimuRdlTEdyzsjcJge64EzHiU3+VrlmcaQTViOgeuiojHUc8DzwuBHYsh5Ux0Ej6H7A+lcgWmY1ULS72xgkXEfW7AJxN2zX7wIKaxcua8QT49N+vKlg1+7ew03LM/btt9TsO5k4b3arvW4ax7/JKxdAiM=")).interactExecutor((player, npc, type) -> openOption(player, DuelType.SUMO_1V1)).build();

    public void searchFor(Player player, DuelType duelType, boolean ranked) {

        Account account = Account.fetch(player.getUniqueId());

        if (ranked) {
            if (!account.getData(Columns.PREMIUM).getAsBoolean() && System.currentTimeMillis() < (account.getData(Columns.FIRST_LOGIN).getAsLong() + 604800000)) {
                player.sendMessage("§cVocê precisa de ao mínimo 7 dias de jogo para jogar partidas ranqueadas.");
                return;
            }
        }

        if (isConnectionCooldown(player.getUniqueId())) {
            Cooldown cooldown = this.getCooldown(player.getUniqueId());

            player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        }

        addCooldown(player.getUniqueId());
        Server server = ServerType.DUELS.getServerCategory().getServerFinder().getBestServer(ServerType.DUELS);

        if (server == null) {
            player.sendMessage(account.getLanguage().translate("no_server_available", "duels"));
            return;
        }

        GameRouteContext context = new GameRouteContext();

        context.setGame(duelType);
        context.setPlayMode(Vanish.getInstance().isVanished(account) ? PlayMode.VANISH : PlayMode.PLAYER);

        ServerRedirect.Route route = new ServerRedirect.Route(server, Constants.GSON.toJson(context));
        ServerRedirect serverRedirect = new ServerRedirect(account.getUniqueId(), route);
        account.connect(serverRedirect);
    }

    public void openOption(Player player, DuelType duelType) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Play duels");

        InteractableItem.Interact interact = new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                player.closeInventory();
                searchFor(player, duelType, false);
                return true;
            }
        };

        InteractableItem.Interact interact2 = new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                player.closeInventory();
                searchFor(player, duelType, true);
                return true;
            }
        };

        inventory.setItem(13, new InteractableItem(new ItemFactory(Material.IRON_SWORD).setName("§a" + duelType.getName()).setDescription("§7Busque por uma partida 1v1.\n\n§eClique para buscar!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), interact).getItemStack());

        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();

        if (entity instanceof Player) {
            event.setCancelled(true);

            final Player player = event.getPlayer();
            final ItemStack stack = player.getItemInHand();

            if (stack == null || stack.getType() != Material.BLAZE_ROD)
                return;

            new DuelMenu(User.fetch(player.getUniqueId()), User.fetch(entity.getUniqueId())).build();
        }
    }

    private final Leaderboard boxingBoard = new Leaderboard(Columns.DUELS_BOXING_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard scrimBoard = new Leaderboard(Columns.DUELS_SCRIM_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard gladiatorBoard = new Leaderboard(Columns.DUELS_GLADIATOR_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard gladiatorOldBoard = new Leaderboard(Columns.DUELS_GLADIATOR_OLD_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard simulatorBoard = new Leaderboard(Columns.DUELS_SIMULATOR_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard soupBoard = new Leaderboard(Columns.DUELS_SOUP_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard uhcBoard = new Leaderboard(Columns.DUELS_UHC_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard sumoBoard = new Leaderboard(Columns.DUELS_SUMO_RATING, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();

    private final Location gladiator, gladiator_old, simulator, soup, uhc, sumo, boxing, scrim;

}