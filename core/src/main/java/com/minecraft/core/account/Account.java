/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.account.datas.*;
import com.minecraft.core.account.fields.*;
import com.minecraft.core.account.friend.Friend;
import com.minecraft.core.account.friend.FriendRequest;
import com.minecraft.core.account.system.UnloadTask;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.*;
import com.minecraft.core.party.Party;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.packet.ServerPayload;
import com.minecraft.core.translation.Language;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@lombok.Data
public class Account {

    private Party party;
    private final UUID uniqueId;
    private String username;

    private transient TagList tagList = new TagList(this);
    private transient MedalList medalList = new MedalList(this);
    private transient ClanTagList clanTagList = new ClanTagList(this);
    private final transient List<PlusColorData> plusColors = new ArrayList<>();

    private transient UnloadTask unloadTask;

    private transient SkinData skinData = new SkinData();

    private transient AccountExecutor accountExecutor;

    private final transient Set<RankData> ranks = new HashSet<>();
    private final transient Set<Punish> punishments = new HashSet<>();
    private final transient Set<NickData> nicks = new HashSet<>();

    private final transient List<TagData> tags = new ArrayList<>();
    private final transient List<ClanTagData> clanTags = new ArrayList<>();
    private final transient List<MedalData> medals = new ArrayList<>();
    private final transient List<PermissionData> permissions = new ArrayList<>();
    private transient PlusColorList plusColorList = new PlusColorList(this);

    private final transient HashMap<String, Property> properties = new HashMap<>();

    private final List<Friend> friends = new ArrayList<>();
    private final List<FriendRequest> sentRequests = new ArrayList<>();
    private final List<FriendRequest> receivedRequests = new ArrayList<>();

    @Getter
    @Setter
    private double alpha = 0.0D; //cosmetic

    private transient int flags = 0, preferences = 0;

    private final DataStorage dataStorage;

    public Account(UUID uniqueId, String username) {
        this.dataStorage = new DataStorage(this.uniqueId = uniqueId, this.username = username);
    }

    public void loadSentFriendRequests() {
        sentRequests.clear();
        JsonArray jsonArray = getData(Columns.SENT_FRIEND_REQUESTS).getAsJsonArray();

        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            String friendName = object.get("name").getAsString();
            UUID friendUniqueId = UUID.fromString(object.get("uniqueId").getAsString());
            FriendRequest.Status status = FriendRequest.Status.valueOf(object.get("status").getAsString());
            String senderName = object.get("senderName").getAsString();
            UUID senderUniqueId = UUID.fromString(object.get("senderUniqueId").getAsString());

            sentRequests.add(new FriendRequest(friendName, friendUniqueId, status, senderName, senderUniqueId));
        }

        getData(Columns.SENT_FRIEND_REQUESTS).setData(jsonArray);
        getData(Columns.SENT_FRIEND_REQUESTS).setChanged(true);
    }

    public void addSentFriendRequest(FriendRequest request) {
        JsonArray jsonArray = getData(Columns.SENT_FRIEND_REQUESTS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", request.getReceiverName());
        jsonObject.addProperty("uniqueId", request.getReceiver().toString());
        jsonObject.addProperty("status", request.getStatus().name());
        jsonObject.addProperty("senderName", request.getSenderName());
        jsonObject.addProperty("senderUniqueId", request.getSenderUniqueId().toString());

        jsonArray.add(jsonObject);

        getData(Columns.SENT_FRIEND_REQUESTS).setData(jsonArray);
        loadSentFriendRequests();
    }

    public void cancelFriendRequest(FriendRequest request) {
        JsonArray jsonArray = getData(Columns.SENT_FRIEND_REQUESTS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            if (object.get("uniqueId").getAsString().equalsIgnoreCase(request.getReceiver().toString())) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.SENT_FRIEND_REQUESTS).setData(jsonArray);
        loadSentFriendRequests();
    }

    public void loadReceivedFriendRequests() {
        receivedRequests.clear();
        JsonArray jsonArray = getData(Columns.RECEIVED_FRIEND_REQUESTS).getAsJsonArray();

        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            String friendName = object.get("name").getAsString();
            UUID friendUniqueId = UUID.fromString(object.get("uniqueId").getAsString());
            FriendRequest.Status status = FriendRequest.Status.valueOf(object.get("status").getAsString());
            String senderName = object.get("senderName").getAsString();
            UUID senderUniqueId = UUID.fromString(object.get("senderUniqueId").getAsString());

            receivedRequests.add(new FriendRequest(friendName, friendUniqueId, status, senderName, senderUniqueId));
        }

        getData(Columns.RECEIVED_FRIEND_REQUESTS).setData(jsonArray);
        getData(Columns.RECEIVED_FRIEND_REQUESTS).setChanged(true);
    }

    public void addReceivedFriendRequest(FriendRequest request) {
        JsonArray jsonArray = getData(Columns.RECEIVED_FRIEND_REQUESTS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", request.getReceiverName());
        jsonObject.addProperty("uniqueId", request.getReceiver().toString());
        jsonObject.addProperty("status", request.getStatus().name());
        jsonObject.addProperty("senderName", request.getSenderName());
        jsonObject.addProperty("senderUniqueId", request.getSenderUniqueId().toString());

        jsonArray.add(jsonObject);

        getData(Columns.RECEIVED_FRIEND_REQUESTS).setData(jsonArray);
        loadReceivedFriendRequests();
    }

    public void removeReceivedFriendRequest(FriendRequest request) {
        JsonArray jsonArray = getData(Columns.RECEIVED_FRIEND_REQUESTS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            if (object.get("uniqueId").getAsString().equalsIgnoreCase(request.getReceiver().toString())) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.RECEIVED_FRIEND_REQUESTS).setData(jsonArray);
        loadReceivedFriendRequests();
    }

    public void loadFriends() {
        friends.clear();
        JsonArray jsonArray = getData(Columns.FRIENDS).getAsJsonArray();

        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            String friendName = object.get("name").getAsString();
            UUID friendUniqueId = UUID.fromString(object.get("uniqueId").getAsString());
            long addedAt = object.get("added_at").getAsLong();

            friends.add(new Friend(friendName, friendUniqueId, addedAt));
        }

        getData(Columns.FRIENDS).setData(jsonArray);
        getData(Columns.FRIENDS).setChanged(true);

    }

    public void addFriend(Friend friend) {
        JsonArray jsonArray = getData(Columns.FRIENDS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", friend.getName());
        jsonObject.addProperty("uniqueId", friend.getUniqueId().toString());
        jsonObject.addProperty("added_at", System.currentTimeMillis());

        jsonArray.add(jsonObject);

        getData(Columns.FRIENDS).setData(jsonArray);
        loadFriends();
    }

    public void removeFriend(Friend friend) {
        JsonArray jsonArray = getData(Columns.FRIENDS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            if (object.get("uniqueId").getAsString().equalsIgnoreCase(friend.getUniqueId().toString())) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.FRIENDS).setData(jsonArray);
        loadFriends();
    }

    public void loadPlusColors() {
        plusColors.clear();

        JsonArray jsonArray = getData(Columns.PLUSCOLORS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            String code = object.get("plusColor").getAsString();
            String addedBy = object.get("added_by").getAsString();
            long expiration = object.get("expiration").getAsLong();
            long addedAt = object.get("added_at").getAsLong();
            long updatedAt = object.has("updated_at") ? object.get("updated_at").getAsLong() : object.get("added_at").getAsLong();

            PlusColor plusColor = PlusColor.fromUniqueCode(code);

            if (plusColor == null) {
                System.out.println("PlusColor '" + code + "' not found, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                System.out.println("PlusColor '" + plusColor.getName() + "' expired, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            plusColors.add(new PlusColorData(plusColor, addedBy, addedAt, expiration));
        }

        getData(Columns.PLUSCOLORS).setData(jsonArray);
        getData(Columns.PLUSCOLORS).setChanged(true);
    }

    public void givePlusColors(PlusColor plusColor, long expiration, String added_by) {
        JsonArray jsonArray = getData(Columns.PLUSCOLORS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("plusColor", plusColor.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", System.currentTimeMillis());
        jsonArray.add(jsonObject);

        getData(Columns.PLUSCOLORS).setData(jsonArray);
        loadPlusColors();
    }

    public void removePlusColor(PlusColor plusColor) {
        JsonArray jsonArray = getData(Columns.PLUSCOLORS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            PlusColor comparator = PlusColor.fromUniqueCode(object.get("plusColor").getAsString());

            if (comparator == plusColor) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.PLUSCOLORS).setData(jsonArray);
        loadPlusColors();
    }


    public void loadPermissions() {
        permissions.clear();

        JsonArray jsonArray = getData(Columns.PERMISSIONS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            String permission = object.get("permission").getAsString();
            long expiration = object.get("expiration").getAsLong();

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                System.out.println("Permission '" + permission + "' expired, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            String addedBy = object.get("added_by").getAsString();
            long addedAt = object.get("added_at").getAsLong();

            getPermissions().add(new PermissionData(getDisplayName(), addedBy, addedAt, expiration));
        }

        getData(Columns.PERMISSIONS).setData(jsonArray);
        getData(Columns.PERMISSIONS).setChanged(true);
    }

    public void givePermission(String permission, long expiration, String added_by) {
        JsonArray jsonArray = getData(Columns.PERMISSIONS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("permission", permission);
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", System.currentTimeMillis());

        jsonArray.add(jsonObject);

        getData(Columns.PERMISSIONS).setData(jsonArray);
        loadPermissions();
    }

    public void removePermission(String permission) {
        JsonArray jsonArray = getData(Columns.PERMISSIONS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            if (object.get("permission").getAsString().equalsIgnoreCase(permission)) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.PERMISSIONS).setData(jsonArray);
        loadPermissions();
    }

    public PermissionData getPermission(String perm) {
        return permissions.stream().filter(p -> p.getName().equalsIgnoreCase(perm)).findFirst().orElse(null);
    }

    public boolean hasPermission(String perm) {
        return getPermission(perm) != null;
    }

    public void loadTags() {
        getTags().clear();

        JsonArray jsonArray = getData(Columns.TAGS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {

            JsonObject object = iterator.next().getAsJsonObject();

            String code = object.get("tag").getAsString();
            String addedBy = object.get("added_by").getAsString();
            long expiration = object.get("expiration").getAsLong();
            long addedAt = object.get("added_at").getAsLong();

            Tag tag = Tag.fromUniqueCode(code);

            if (tag == null) {
                System.out.println("Tag '" + code + "' not found, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                System.out.println("Tag '" + tag.getName() + "' expired, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            getTags().add(new TagData(tag, addedBy, addedAt, expiration));
        }
        getData(Columns.TAGS).setData(jsonArray);
        getData(Columns.TAGS).setChanged(true);
    }

    public void giveTag(Tag tag, long expiration, String added_by) {
        JsonArray jsonArray = getData(Columns.TAGS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("tag", tag.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", System.currentTimeMillis());
        jsonArray.add(jsonObject);

        getData(Columns.TAGS).setData(jsonArray);
        loadTags();
    }

    public void removeTag(Tag tag) {
        JsonArray jsonArray = getData(Columns.TAGS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            Tag comparator = Tag.fromUniqueCode(object.get("tag").getAsString());

            if (comparator == tag) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.TAGS).setData(jsonArray);
        loadTags();
    }

    public void loadMedals() {
        getMedals().clear();

        JsonArray jsonArray = getData(Columns.MEDALS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {

            JsonObject object = iterator.next().getAsJsonObject();

            String code = object.get("medal").getAsString();
            String addedBy = object.get("added_by").getAsString();
            long expiration = object.get("expiration").getAsLong();
            long addedAt = object.get("added_at").getAsLong();

            Medal medal = Medal.fromUniqueCode(code);

            if (medal == null) {
                System.out.println("Medal '" + code + "' not found, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                System.out.println("Medal '" + medal.getName() + "' expired, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            getMedals().add(new MedalData(medal, addedBy, addedAt, expiration));
        }
        getData(Columns.MEDALS).setData(jsonArray);
        getData(Columns.MEDALS).setChanged(true);
    }

    public void giveMedal(Medal medal, long expiration, String added_by) {
        JsonArray jsonArray = getData(Columns.MEDALS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("medal", medal.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", System.currentTimeMillis());
        jsonArray.add(jsonObject);

        getData(Columns.MEDALS).setData(jsonArray);
        loadMedals();
    }

    public void giveMedal(Medal medal, long expiration, String added_by, long added_at, long updated_at) {
        JsonArray jsonArray = getData(Columns.MEDALS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("medal", medal.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", added_at);
        jsonObject.addProperty("updated_at", updated_at);
        jsonArray.add(jsonObject);

        getData(Columns.MEDALS).setData(jsonArray);
        loadMedals();
    }

    public void removeMedal(Medal medal) {
        JsonArray jsonArray = getData(Columns.MEDALS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            Medal comparator = Medal.fromUniqueCode(object.get("medal").getAsString());

            if (comparator == medal) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.MEDALS).setData(jsonArray);
        loadMedals();
    }

    public void loadClanTags() {
        getClanTags().clear();

        JsonArray jsonArray = getData(Columns.CLANTAGS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {

            JsonObject object = iterator.next().getAsJsonObject();

            String code = object.get("clanTag").getAsString();
            String addedBy = object.get("added_by").getAsString();
            long expiration = object.get("expiration").getAsLong();
            long addedAt = object.get("added_at").getAsLong();

            Clantag clantag = Clantag.fromUniqueCode(code);

            if (clantag == null) {
                System.out.println("ClanTag '" + code + "' not found, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                System.out.println("ClanTag '" + clantag.getName() + "' expired, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            getClanTags().add(new ClanTagData(clantag, addedBy, addedAt, expiration));
        }
        getData(Columns.CLANTAGS).setData(jsonArray);
        getData(Columns.CLANTAGS).setChanged(true);
    }

    public void giveClanTag(Clantag clantag, long expiration, String added_by) {
        JsonArray jsonArray = getData(Columns.CLANTAGS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("clanTag", clantag.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", System.currentTimeMillis());
        jsonArray.add(jsonObject);

        getData(Columns.CLANTAGS).setData(jsonArray);
        loadClanTags();
    }

    public void giveClanTag(Clantag clantag, long expiration, String added_by, long added_at, long updated_at) {
        JsonArray jsonArray = getData(Columns.CLANTAGS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("clanTag", clantag.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", added_at);
        jsonObject.addProperty("updated_at", updated_at);
        jsonArray.add(jsonObject);

        getData(Columns.CLANTAGS).setData(jsonArray);
        loadClanTags();
    }

    public void removeClanTag(Clantag clantag) {
        JsonArray jsonArray = getData(Columns.CLANTAGS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            Clantag comparator = Clantag.fromUniqueCode(object.get("clanTag").getAsString());

            if (comparator == clantag) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.CLANTAGS).setData(jsonArray);
        loadClanTags();
    }

    public boolean hasSentFriendRequest(UUID uniqueId) {
        return getPendingFriendRequests().stream().anyMatch(request -> request.getReceiver().equals(uniqueId));
    }

    public boolean hasReceivedFriendRequest(UUID uniqueId) {
        return getReceivedPendingRequests().stream().anyMatch(request -> request.getSenderUniqueId().equals(uniqueId));
    }

    public List<FriendRequest> getReceivedPendingRequests() {
        return receivedRequests.stream().filter(request -> request.getStatus() == FriendRequest.Status.PENDING).collect(Collectors.toList());
    }

    public List<FriendRequest> getPendingFriendRequests() {
        return sentRequests.stream().filter(request -> request.getStatus() == FriendRequest.Status.PENDING).collect(Collectors.toList());
    }

    public FriendRequest getSentFriendRequest(UUID uniqueId) {
        return sentRequests.stream().filter(request -> request.getReceiver().equals(uniqueId)).findFirst().orElse(null);
    }

    public FriendRequest getReceivedFriendRequest(UUID uniqueId) {
        return receivedRequests.stream().filter(request -> request.getSenderUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public boolean hasFriend(UUID uniqueId) {
        return friends.stream().anyMatch(friend -> friend.getUniqueId().equals(uniqueId));
    }

    public Friend getFriend(UUID uniqueId) {
        return friends.stream().filter(friend -> friend.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public boolean hasTag(Tag tag) {
        return getTag(tag) != null;
    }

    private TagData getTag(Tag tag) {
        return tags.stream().filter(tagData -> tagData.getTag().getUniqueCode().equalsIgnoreCase(tag.getUniqueCode())).findFirst().orElse(null);
    }

    public boolean hasPlusColor(PlusColor plusColor) {
        return getPlusColor(plusColor) != null;
    }

    private PlusColorData getPlusColor(PlusColor plusColor) {
        return plusColors.stream().filter(plusColorData -> plusColorData.getPlusColor().getUniqueCode().equalsIgnoreCase(plusColor.getUniqueCode())).findFirst().orElse(null);
    }

    public boolean hasMedal(Medal medal) {
        return getMedal(medal) != null;
    }

    private MedalData getMedal(Medal medal) {
        return medals.stream().filter(medalData -> medalData.getMedal().getUniqueCode().equalsIgnoreCase(medal.getUniqueCode())).findFirst().orElse(null);
    }

    public boolean hasClanTag(Clantag clantag) {
        return getClanTag(clantag) != null;
    }

    private ClanTagData getClanTag(Clantag clantag) {
        return clanTags.stream().filter(clanTagData -> clanTagData.getClantag().getUniqueCode().equalsIgnoreCase(clantag.getUniqueCode())).findFirst().orElse(null);
    }

    public void savePunishments() {
        if (getPunishments().isEmpty())
            return;
        JsonArray jsonElements = new JsonArray();
        for (Punish punish : getPunishments()) {
            jsonElements.add(punish.object());
        }
        getData(Columns.PUNISHMENTS).setData(jsonElements);
        getData(Columns.PUNISHMENTS).setChanged(true);
        getDataStorage().saveTable(Tables.ACCOUNT);
    }

    public void loadPunishments() {
        getPunishments().clear();

        JsonArray punishments = getData(Columns.PUNISHMENTS).getAsJsonArray();

        for (JsonElement punishment : punishments) {
            Punish punish = Punish.resolve(punishment.getAsJsonObject());
            if (punish.isActive() && punish.isExpired()) {
                unpunish(punish, "Sistema");

                if (punish.getType() == PunishType.BAN)
                    getData(Columns.BANNED).setData(false);
                else if (punish.getType() == PunishType.MUTE)
                    getData(Columns.MUTED).setData(false);
            }
            getPunishments().add(punish);
        }
        savePunishments();
    }

    public void unpunish(Punish punish, String author) {
        punish.setActive(false);
        punish.setUnpunisher(author);
        punish.setUnpunishDate(System.currentTimeMillis());
    }

    public Property getProperty(String key) {
        return getProperties().get(key.toLowerCase());
    }

    public Property getProperty(String key, Object def) {
        return getProperties().computeIfAbsent(key.toLowerCase(), p -> Property.build(key, def));
    }

    public void removeProperty(String key) {
        getProperties().remove(key);
    }

    public boolean hasProperty(String key) {
        return getProperties().get(key) != null;
    }

    public void removeRank(Rank rank) {
        JsonArray jsonArray = getData(Columns.RANKS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject object = iterator.next().getAsJsonObject();

            Rank comparator = Rank.fromUniqueCode(object.get("rank").getAsString());

            if (comparator == rank) {
                iterator.remove();
                break;
            }
        }

        getData(Columns.RANKS).setData(jsonArray);
        loadRanks();
    }

    public void loadNicks() {
        getNicks().clear();

        JsonArray jsonArray = getData(Columns.NICK_OBJECTS).getAsJsonArray();

        for (JsonElement jsonElement : jsonArray) {

            JsonObject object = jsonElement.getAsJsonObject();

            String nickname = object.get("nickname").getAsString();
            long expiration = object.get("expiry").getAsLong();
            long addedAt = object.get("dateChanged").getAsLong();

            getNicks().add(new NickData(nickname, addedAt, expiration));
        }

        getData(Columns.NICK_OBJECTS).setData(jsonArray);
        getData(Columns.NICK_OBJECTS).setChanged(true);
        getDataStorage().saveColumn(Columns.NICK_OBJECTS);
        getDataStorage().saveTable(Tables.ACCOUNT);
    }

    public void addNick(String nickname, long changedAt, long expiry) {
        JsonArray array = getData(Columns.NICK_OBJECTS).getAsJsonArray();
        JsonObject object = new JsonObject();

        object.addProperty("nickname", nickname);
        object.addProperty("dateChanged", changedAt);
        object.addProperty("expiry", expiry);

        array.add(object);
        getData(Columns.NICK_OBJECTS).setData(array);
        loadNicks();
    }

    public void giveRank(Rank rank, long expiration, String added_by) {
        JsonArray jsonArray = getData(Columns.RANKS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("rank", rank.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", System.currentTimeMillis());
        jsonArray.add(jsonObject);

        getData(Columns.RANKS).setData(jsonArray);
        loadRanks();
    }

    public void giveRank(Rank rank, long expiration, String added_by, long added_at, long updated_at) {
        JsonArray jsonArray = getData(Columns.RANKS).getAsJsonArray();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("rank", rank.getUniqueCode());
        jsonObject.addProperty("expiration", expiration);
        jsonObject.addProperty("added_by", added_by);
        jsonObject.addProperty("added_at", added_at);
        jsonObject.addProperty("updated_at", updated_at);
        jsonArray.add(jsonObject);

        getData(Columns.RANKS).setData(jsonArray);
        loadRanks();
    }

    public RankData getRankData(Rank rank) {
        return getRanks().stream().filter(rankData -> rankData.getRank() == rank).findFirst().orElse(null);
    }

    public NickData getNickData(String nickname) {
        return nicks.stream().filter(nickData -> nickData.getNickname().equalsIgnoreCase(nickname)).findFirst().orElse(null);
    }

    public List<NickData> getUnexpiredNicks() {
        return nicks.stream().filter(nickData -> !nickData.hasExpired()).collect(Collectors.toList());
    }

    public PlusColorData getPlusColorData(PlusColor plusColor) {
        return getPlusColors().stream().filter(plusColorData -> plusColorData.getPlusColor() == plusColor).findFirst().orElse(null);
    }

    public TagData getTagData(Tag tag) {
        return getTags().stream().filter(tagData -> tagData.getTag() == tag).findFirst().orElse(null);
    }

    public MedalData getMedalData(Medal medal) {
        return getMedals().stream().filter(medalData -> medalData.getMedal() == medal).findFirst().orElse(null);
    }

    public ClanTagData getClanTaData(Clantag clantag) {
        return getClanTags().stream().filter(clanTagData -> clanTagData.getClantag() == clantag).findFirst().orElse(null);
    }

    public boolean hasRank(Rank rank) {
        return getRanks().stream().anyMatch(rankData -> rankData.getRank() == rank);
    }

    public Language getLanguage() {
        return getProperty("account_language").getAs(Language.class);
    }

    public void setLanguage(Language language) {
        setProperty("account_language", language);
    }

    public void loadRanks() {
        getRanks().clear();

        JsonArray jsonArray = getData(Columns.RANKS).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();

        setProperty("account_rank", Rank.MEMBER);
        setProperty("isAdmin", false);

        Rank currentRank = Rank.MEMBER;

        while (iterator.hasNext()) {

            JsonObject object = iterator.next().getAsJsonObject();

            String code = object.get("rank").getAsString();
            String addedBy = object.get("added_by").getAsString();
            long expiration = object.get("expiration").getAsLong();
            long addedAt = object.get("added_at").getAsLong();
            long updatedAt = object.has("updated_at") ? object.get("updated_at").getAsLong() : object.get("added_at").getAsLong();

            Rank rank = Rank.fromUniqueCode(code);

            if (rank == null) {
                System.out.println("Rank '" + code + "' not found, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            if (expiration != -1 && expiration < System.currentTimeMillis()) {
                System.out.println("Rank '" + rank.getName() + "' expired, removing from " + username + "'s account.");
                iterator.remove();
                continue;
            }

            if (rank.getId() > currentRank.getId()) {
                getProperty("account_rank").setValue(rank);
                setProperty("isAdmin", rank.getCategory().getImportance() >= Rank.Category.ADMINISTRATION.getImportance());
                currentRank = rank;
            }

            getRanks().add(new RankData(rank, addedBy, addedAt, updatedAt, expiration));
        }
        getData(Columns.RANKS).setData(jsonArray);
        getData(Columns.RANKS).setChanged(true);
    }

    public void setProperty(String key, Object value) {
        getProperties().computeIfAbsent(key.toLowerCase(), p -> Property.build(key, value)).setValue(value);
    }

    public Rank getRank() {
        return getProperty("account_rank", Rank.MEMBER).getAs(Rank.class);
    }

    public String getDisplayName() {
        if (hasProperty("nickname"))
            return getProperty("nickname").getAsString();
        return getUsername();
    }

    public Data getData(Columns column) {
        return getDataStorage().getData(column);
    }

    public void setDisplayName(String name) {
        setProperty("nickname", name);
    }

    public boolean hasCustomName() {
        return hasProperty("nickname");
    }

    public boolean getFlag(Flag flag) {
        return (flags & (1 << flag.getBitIndex())) != 0;
    }

    public void setFlag(Flag flag, boolean value) {
        if (value)
            flags |= (1 << flag.getBitIndex());
        else
            flags &= ~(1 << flag.getBitIndex());
    }

    public boolean getPreference(Preference preference) {
        return (preferences & (1 << preference.getBitIndex())) == 0;
    }

    public void setPreference(Preference preference, boolean value) { //Inverse values!
        if (value)
            preferences &= ~(1 << preference.getBitIndex());
        else
            preferences |= (1 << preference.getBitIndex());
    }

    public static Account fetch(UUID uuid) {
        return Constants.getAccountStorage().getAccount(uuid);
    }

    public boolean hasPermission(Rank rank) {
        return rank.getId() <= getRank().getId();
    }

    public boolean isPunished(PunishType type) {
        return getPunishments().stream().anyMatch(punish -> punish.isActive() && punish.getType() == type && !punish.isExpired());
    }

    public boolean isPunished(PunishType type, PunishCategory punishCategory) {
        return getPunishments().stream().anyMatch(punish -> punish.getCategory() == punishCategory && punish.isActive() && punish.getType() == type && !punish.isExpired());
    }

    public int unpunish(Punish punish, String author, boolean force) {
        if (punish.isInexcusable() && !force)
            return 0;
        unpunish(punish, author);
        savePunishments();
        return 1;
    }

    public Punish getPunish(PunishType type) {
        return getPunishments().stream().filter(punish -> punish.isActive() && punish.getType() == type).findFirst().orElse(null);
    }

    public Punish getPunish(PunishType type, PunishCategory punishCategory) {
        return getPunishments().stream().filter(punish -> punish.isActive() && punish.getType() == type && punish.getCategory() == punishCategory).findFirst().orElse(null);
    }

    public Punish getPunish(String code) {
        return getPunishments().stream().filter(punish -> {

            if (code.startsWith("#"))
                return ("#" + punish.getCode()).equalsIgnoreCase(code);
            return punish.getCode().equalsIgnoreCase(code);

        }).findFirst().orElse(null);
    }

    public int count(PunishType punishType, PunishCategory category) {
        return (int) getPunishments().stream().filter(punish -> punish.getType() == punishType && punish.getCategory() == category).count();
    }

    public int count(PunishType punishType) {
        return (int) getPunishments().stream().filter(punish -> punish.getType() == punishType).count();
    }

    public Collection<Punish> getActivePunishments() {
        return getPunishments().stream().filter(Punish::isActive).collect(Collectors.toList());
    }

    public boolean isSessionValid(String address) {
        return System.currentTimeMillis() < getData(Columns.SESSION_EXPIRES_AT).getAsLong() && address.equals(getData(Columns.SESSION_ADDRESS).getAsString());
    }

    public void removeInt(int a, Columns column) {
        int b = getData(column).getAsInt();
        b -= a;
        if (b < 0)
            b = 0;
        getData(column).setData(b);
    }

    public void addInt(int a, Columns column) {
        int b = getData(column).getAsInt();
        b += a;
        getData(column).setData(b);
    }

    public boolean hasClan() {
        return getData(Columns.CLAN).getAsInt() != -1;
    }

    public boolean isInParty() {
        return Constants.getPartyStorage().getAllParties().stream().anyMatch(party -> party.getMembers().contains(getUniqueId())) || Constants.getPartyStorage().getAllParties().stream().anyMatch(party -> party.getOwner() == getUniqueId());
    }

    public String getRatio(int kills, int deaths) {
        if (deaths == 0)
            return kills + "";
        return Constants.SIMPLE_DECIMAL_FORMAT.format((double) kills / deaths);
    }

    public void setVersion(int i) {
        setProperty("version", i);
    }

    public int getVersion() {
        return getProperty("version", -1).getAsInt();
    }

    public void setRanking(Ranking ranking) {
        setProperty("ranking", ranking);
    }

    public Ranking getRanking() {
        return getProperty("ranking", Ranking.BRONZE_I).getAs(Ranking.class);
    }

    public void loadSkinData() {
        this.skinData = SkinData.fromJson(getData(Columns.SKIN).getAsJsonObject());
    }

    public void connect(Server server) {
        connect(new ServerRedirect(getUniqueId(), new ServerRedirect.Route(server, null)));
    }

    public void connect(ServerRedirect route) {

        ServerPayload payload = route.getRoute().getServer().getBreath();

        if (payload.getPort() == Constants.getServerStorage().myPort()) {
            accountExecutor.sendMessage(getLanguage().translate("already_connected"));
            return;
        }

        if (!hasPermission(Rank.VIP) && payload.getOnlinePlayers() >= payload.getMaxPlayers()) {
            accountExecutor.sendMessage(getLanguage().translate("server_is_full", Constants.SERVER_STORE));
            return;
        }

        try {
            String message = Constants.GSON.toJson(route);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF(message);
            accountExecutor.sendPluginMessage("Redirection", b.toByteArray());
            b.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Clan getClan() {

        if (!hasClan())
            return null;

        return Constants.getClanService().getClan(getData(Columns.CLAN).getAsInt());
    }

    public Party getParty() {
        if (!isInParty())
            return null;

        return Constants.getPartyStorage().getAllParties().stream().filter(party -> party.getMembers().contains(getUniqueId()) || party.getOwner() == getUniqueId()).findFirst().orElse(null);
    }

    public void resetNicks() {
        JsonArray array = new JsonArray();
        getData(Columns.NICK_OBJECTS).setData(array);
    }

}