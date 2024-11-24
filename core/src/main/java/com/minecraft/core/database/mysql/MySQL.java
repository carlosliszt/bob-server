/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.database.mysql;

import java.sql.*;

public class MySQL {

    private final MySQLProperties properties;
    private Connection connection;

    public MySQL(MySQLProperties properties) {
        this.properties = properties;
    }

    public MySQL connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + properties.getHost() + ":" + properties.getPort() + "/" + properties.getDatabase() + "?user=" + properties.getUsername() + "&password=" + properties.getPassword() + "&autoReconnect=true&characterEncoding=utf8&useConfigs=maxPerformance&callableStmtCacheSize=400&useSSL=false");
        } catch (SQLException exception) {
            exception.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public int insertId(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.getGeneratedKeys();

        if (rs.next()) {
            return rs.getInt(1);
        }

        throw new IllegalStateException("Nenhuma chave auto gerada foi encontrada");
    }

    public Connection getConnection() {
        return connection;
    }

}