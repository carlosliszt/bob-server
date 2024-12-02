package com.minecraft.core.proxy.util.reward;

import com.minecraft.core.Constants;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/*
CREATE TABLE IF NOT EXISTS `codes`
 (`index` INT UNSIGNED NOT NULL, `key` VARCHAR(16) NOT NULL, `name` VARCHAR(128),
  `rank` VARCHAR(24) NOT NULL, `duration` VARCHAR(12) NOT NULL,
  `creator` VARCHAR(24) NOT NULL, `creation` BIGINT NOT NULL,
  `redeemer` VARCHAR(24), `redeem` BIGINT,
   PRIMARY KEY(`index`));
*/

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GiftCode {

    private String key, name;
    private Rank rank;
    private String duration;
    private UUID creator, redeemer;
    private long creation, redeem;

    public boolean isRedeemed() {
        return redeemer != null;
    }

    /*public void push() {
        String sql = "INSERT INTO `codes` (`key`, `name`, `rank`, `duration`, `creator`, `creation`, `redeemer`, `redeem`) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, key);
            ps.setString(2, name);
            ps.setString(3, rank.getUniqueCode());
            ps.setString(4, duration);
            ps.setString(5, creator.toString());
            ps.setLong(6, creation);
            ps.setString(7, redeemer.toString());
            ps.setLong(8, redeem);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }*/

    public void update() {
        String UPDATE_CODE_QUERY = "UPDATE codes SET `name` = ?, `rank` = ?, `duration` = ?, `creator` = ?, `creation` = ?, `redeemer` = ?, `redeem` = ? WHERE `key` = ?";

        try (Connection connection = Constants.getMySQL().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_CODE_QUERY)) {

            statement.setString(1, getName());
            statement.setString(2, getRank().name());
            statement.setString(3, getDuration());
            statement.setString(4, getCreator().toString());
            statement.setLong(5, getCreation());

            // Check if redeemer is set, otherwise set it to null
            if (getRedeemer() != null) {
                statement.setString(6, getRedeemer().toString());
            } else {
                statement.setNull(6, Types.VARCHAR);
            }

            // Set the redeem timestamp if it's greater than 0, otherwise set it to null
            if (getRedeem() > 0) {
                statement.setLong(7, getRedeem());
            } else {
                statement.setNull(7, Types.BIGINT);
            }

            statement.setString(8, getKey());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Gift code updated successfully.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
