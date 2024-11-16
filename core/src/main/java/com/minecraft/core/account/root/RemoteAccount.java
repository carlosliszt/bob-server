/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.root;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.RankData;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;

import java.util.Collections;
import java.util.Set;

public class RemoteAccount extends Account {

    public RemoteAccount() {
        super(Constants.CONSOLE_UUID, "[SERVER]");
        getData(Columns.PREMIUM).setData(true);
        setProperty("isAdmin", true);
    }

    @Override
    public Language getLanguage() {
        return Language.ENGLISH;
    }

    @Override
    public boolean hasPermission(Rank rank) {
        return true;
    }

    @Override
    public void loadMedals() {
    }

    @Override
    public void loadPunishments() {
    }

    @Override
    public void loadRanks() {
    }

    @Override
    public void loadSkinData() {
    }

    @Override
    public void loadTags() {
    }

    @Override
    public Set<RankData> getRanks() {
        return Collections.singleton(new RankData(Rank.ADMINISTRATOR, "root", System.currentTimeMillis(), System.currentTimeMillis(), -1));
    }

    @Override
    public boolean getFlag(Flag flag) {
        return false;
    }

    @Override
    public int getVersion() {
        return -1;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
}
