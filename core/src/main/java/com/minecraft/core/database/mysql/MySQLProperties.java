/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.database.mysql;

import com.minecraft.core.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@AllArgsConstructor
@Getter
public class MySQLProperties {

    private final String host;
    private final int port;
    private final String username, password, database;

    public static MySQLProperties load(File file) {
        MySQLProperties properties = new MySQLProperties("localhost", 3306, "server", "yumrWaCYdYINenasgjxeos5xJKJ1xxx", "minecraft");
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(Constants.GSON.toJson(properties));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JSONParser jsonParser = new JSONParser();
            try {
                properties = Constants.GSON.fromJson(((JSONObject) jsonParser.parse(new FileReader(file))).toJSONString(), MySQLProperties.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return properties;
    }
}
