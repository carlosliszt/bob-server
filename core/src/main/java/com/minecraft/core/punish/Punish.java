/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.punish;

import com.google.gson.JsonObject;
import com.minecraft.core.account.Account;
import com.minecraft.core.database.enums.Columns;
import lombok.Data;

@Data
public class Punish {

    private String applier;
    private long applyDate, time, unpunishDate;
    private boolean active, automatic;
    private String reason, unpunisher;
    private PunishType type;
    private PunishCategory category;
    private String address;
    private String code;

    public boolean isExpired() {
        return !isPermanent() && this.time <= System.currentTimeMillis();
    }

    public boolean isPermanent() {
        return this.time == -1L;
    }

    public JsonObject object() {
        JsonObject object = new JsonObject();
        object.addProperty("applier", getApplier());
        object.addProperty("applyDate", getApplyDate());
        object.addProperty("time", getTime());

        object.addProperty("active", isActive());
        object.addProperty("reason", getReason());

        object.addProperty("code", getCode());

        object.addProperty("type", getType().name().toLowerCase());
        object.addProperty("category", getCategory().name().toLowerCase());

        if (isAutomatic())
            object.addProperty("automatic", true);

        if (!isActive()) {
            object.addProperty("unpunisher", getUnpunisher());
            object.addProperty("unpunishDate", getUnpunishDate());
        } else
            object.addProperty("address", getAddress());
        return object;
    }

    public boolean isInexcusable() {
        return getCategory().isInexcusable();
    }

    public static Punish resolve(JsonObject entry) {
        Punish punish = new Punish();
        punish.setActive(entry.get("active").getAsBoolean());
        punish.setApplier(entry.get("applier").getAsString());
        punish.setApplyDate(entry.get("applyDate").getAsLong());
        punish.setTime(entry.get("time").getAsLong());

        if (entry.has("automatic"))
            punish.setAutomatic(true);

        punish.setReason(entry.get("reason").getAsString());
        punish.setType(PunishType.valueOf(entry.get("type").getAsString().toUpperCase()));
        punish.setCategory(PunishCategory.valueOf(entry.get("category").getAsString().toUpperCase()));
        punish.setCode(entry.get("code").getAsString());

        if (punish.isActive()) {
            punish.setUnpunisher("...");
            punish.setUnpunishDate(-1L);
            punish.setAddress(entry.get("address").getAsString());
        } else {
            punish.setUnpunisher(entry.get("unpunisher").getAsString());
            punish.setUnpunishDate(entry.get("unpunishDate").getAsLong());
        }
        return punish;
    }

    public void assign(Account account) {
        account.getDataStorage().loadColumns(true, Columns.BANNED, Columns.MUTED);
        account.getPunishments().add(this);
        account.savePunishments();
        if (getType() == PunishType.BAN) {
            account.getData(Columns.BANNED).setData(true);
            account.getDataStorage().saveColumn(Columns.BANNED);
        } else if (getType() == PunishType.MUTE && getCategory() == PunishCategory.COMMUNITY) {
            account.getData(Columns.MUTED).setData(true);
            account.getDataStorage().saveColumn(Columns.MUTED);
        }
    }
}