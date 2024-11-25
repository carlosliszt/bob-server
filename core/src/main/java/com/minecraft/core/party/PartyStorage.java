package com.minecraft.core.party;

import com.minecraft.core.Constants;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * CREATE TABLE `parties` (
 * `unique_id` VARCHAR(36) PRIMARY KEY,
 * `members` TEXT NOT NULL,
 * `owner` VARCHAR(36) NOT NULL,
 * `open` BOOLEAN NOT NULL
 * );
 */

@Getter
public class PartyStorage {

    private final Map<UUID, Party> partyMap = new ConcurrentHashMap<>();

    public Party getParty(UUID uniqueId) {
        return partyMap.computeIfAbsent(uniqueId, this::loadParty);
    }

    public Party fetch(UUID uniqueId) {
        return partyMap.get(uniqueId);
    }

    public Party getPartyByOwner(UUID owner) {
        return partyMap.values().stream()
                .filter(p -> p.getOwner().equals(owner))
                .findFirst()
                .orElse(null);
    }

    private Party loadParty(UUID uniqueId) {
        String query = "SELECT * FROM `parties` WHERE `unique_id` = ? LIMIT 1;";

        try (PreparedStatement statement = Constants.getMySQL().getConnection().prepareStatement(query)) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? parseParty(resultSet) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while loading party '" + uniqueId + "'.", e);
        }
    }

    private Party parseParty(ResultSet resultSet) throws SQLException {
        UUID uniqueId = UUID.fromString(resultSet.getString("unique_id"));
        UUID owner = UUID.fromString(resultSet.getString("owner"));
        boolean open = resultSet.getBoolean("open");
        Set<UUID> members = new HashSet<>(Arrays.asList(
                Constants.GSON.fromJson(resultSet.getString("members"), UUID[].class)
        ));
        return new Party(uniqueId, members, owner, open);
    }

    public void saveParty(Party party) throws SQLException {
        String query = "UPDATE `parties` SET `members` = ?, `owner` = ?, `open` = ? WHERE `unique_id` = ? LIMIT 1;";

        try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement(query)) {
            ps.setString(1, Constants.GSON.toJson(party.getMembers()));
            ps.setString(2, party.getOwner().toString());
            ps.setBoolean(3, party.isOpen());
            ps.setString(4, party.getUniqueId().toString());
            ps.executeUpdate();
        }
    }

    public void registerParty(Party party) throws SQLException {
        String query = "INSERT INTO `parties`(`unique_id`, `members`, `owner`, `open`) VALUES (?, ?, ?, ?);";

        try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement(query)) {
            ps.setString(1, party.getUniqueId().toString());
            ps.setString(2, Constants.GSON.toJson(party.getMembers()));
            ps.setString(3, party.getOwner().toString());
            ps.setBoolean(4, party.isOpen());
            ps.executeUpdate();

            partyMap.put(party.getUniqueId(), party);
        }
    }

    public boolean isPartyExists(UUID uniqueId) {
        if (partyMap.containsKey(uniqueId)) {
            return true;
        }

        String query = "SELECT `unique_id` FROM `parties` WHERE `unique_id` = ? LIMIT 1;";
        try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement(query)) {
            ps.setString(1, uniqueId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteParty(UUID uniqueId) throws SQLException {
        String query = "DELETE FROM `parties` WHERE `unique_id` = ? LIMIT 1;";

        try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement(query)) {
            ps.setString(1, uniqueId.toString());
            boolean success = ps.executeUpdate() > 0;

            if (success) {
                partyMap.remove(uniqueId);
            }

            return success;
        }
    }

    public void forgetParty(UUID uniqueId) {
        partyMap.remove(uniqueId);
    }

    public void addParty(Party party) {
        partyMap.put(party.getUniqueId(), party);
    }

    public List<Party> getAllParties() {
        return new ArrayList<>(partyMap.values());
    }
}
