package com.minecraft.core.proxy.util.reward.storage;

import com.minecraft.core.Constants;
import com.minecraft.core.database.mysql.MySQL;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.reward.GiftCode;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class GiftCodeStorage {

    private final List<GiftCode> giftCodes = new ArrayList<>();
    private final Set<UUID> notExists = new HashSet<>();

    //queries
    private static final String GET_CODE_QUERY = "SELECT * FROM codes WHERE `key` = ?";
    private static final String INSERT_CODE_QUERY = "INSERT INTO codes (`key`, `name`, `rank`, `duration`, `creator`, `creation`) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String DELETE_CODE_QUERY = "DELETE FROM codes WHERE `key` = ?";

    public GiftCode get(String key) {
        return giftCodes.stream().filter(c -> c.getKey().equals(key)).findFirst().orElse(load(key));
    }

    public boolean delete(GiftCode giftCode) {
        giftCodes.remove(giftCode);

        try (Connection connection = Constants.getMySQL().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_CODE_QUERY)) {

            statement.setString(1, giftCode.getKey());
            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean push(GiftCode giftCode) {

        try (Connection connection = Constants.getMySQL().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_CODE_QUERY)) {

            statement.setString(1, giftCode.getKey());
            statement.setString(2, giftCode.getName());
            statement.setString(3, giftCode.getRank().name());
            statement.setString(4, giftCode.getDuration());
            statement.setString(5, giftCode.getCreator().toString());
            statement.setLong(6, giftCode.getCreation());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                giftCodes.add(giftCode);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private GiftCode load(String key) {

        UUID uuid = UUID.nameUUIDFromBytes((key.toLowerCase()).getBytes(StandardCharsets.UTF_8));

        if (notExists.contains(uuid)) {
            return null;
        }

        try (Connection connection = Constants.getMySQL().getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_CODE_QUERY)) {

            statement.setString(1, key);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                GiftCode giftCode = new GiftCode();
                giftCode.setKey(resultSet.getString("key"));
                giftCode.setName(resultSet.getString("name"));
                giftCode.setRank(Rank.valueOf(resultSet.getString("rank")));
                giftCode.setDuration(resultSet.getString("duration"));
                giftCode.setCreator(UUID.fromString(resultSet.getString("creator")));
                giftCode.setCreation(resultSet.getLong("creation"));

                String redeemer = resultSet.getString("redeemer");
                if (redeemer != null) {
                    giftCode.setRedeemer(UUID.fromString(redeemer));
                }

                giftCode.setRedeem(resultSet.getLong("redeem"));

                giftCodes.add(giftCode);
                return giftCode;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        notExists.add(uuid);
        return null;
    }
}
