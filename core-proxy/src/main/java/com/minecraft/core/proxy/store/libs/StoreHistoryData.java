/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.store.libs;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreHistoryData {

    private int id, transactionId;
    private String target;
    private String command;
    private long writtenDate;
    private boolean executed;
    private Type type;
    private boolean deleted;

    public static StoreHistoryData queue(JsonObject jsonObject) {

        StoreHistoryData data = new StoreHistoryData();

        data.setTransactionId(jsonObject.get("transaction_id").getAsInt());
        data.setId(jsonObject.get("id").getAsInt());
        data.setTarget(jsonObject.get("player").getAsString());
        data.setType(Type.valueOf(jsonObject.get("type").getAsString().toUpperCase()));
        data.setCommand(jsonObject.get("command").getAsString());
        data.setExecuted(false);
        data.setDeleted(false);
        data.setWrittenDate(System.currentTimeMillis());

        return data;
    }

    @Override
    public String toString() {
        return "StoreHistoryData{" +
                "transactionId=" + transactionId +
                ", target='" + target + '\'' +
                ", command='" + command + '\'' +
                ", writtenDate=" + writtenDate +
                ", executed=" + executed +
                ", type=" + type +
                '}';
    }

    public void apply() {
        BungeeCord.getInstance().getPluginManager().dispatchCommand(BungeeCord.getInstance().getConsole(), getCommand().replace("{player}", getTarget()));
        setExecuted(true);
    }

    public enum Type {

        ONLINE, OFFLINE

    }

}
