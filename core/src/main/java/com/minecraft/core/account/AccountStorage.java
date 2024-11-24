/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.account;

import com.minecraft.core.Constants;
import com.minecraft.core.account.root.RemoteAccount;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccountStorage {

    private final Map<UUID, Account> accountMap = new ConcurrentHashMap<>();
    /*  private final Set<Account> pendingUnload = new HashSet<>();

    public Account queueUnload(UUID uuid, long unloadAfter, Consumer<Account> consumer) {

        Account account = accountMap.get(uuid);

        if (account == null)
            return null;

        UnloadTask unloadTask = new UnloadTask((System.currentTimeMillis() + unloadAfter), consumer);
        account.setUnloadTask(unloadTask);
        pendingUnload.add(account);

        return account;
    }  */

    public AccountStorage() {
        accountMap.put(Constants.CONSOLE_UUID, new RemoteAccount());
    }

    public void store(UUID uniqueId, Account account) {
        accountMap.put(uniqueId, account);
    }

    public Account forget(UUID uniqueId) {
        return accountMap.remove(uniqueId);
    }

    public Account forget(Account account) {
        return accountMap.remove(account.getUniqueId());
    }

    public Account getAccount(UUID uniqueId) {
        return accountMap.get(uniqueId);
    }

    public static Account getAccountByName(String name, boolean filterOriginalNickname) {
        return Constants.getAccountStorage().getAccounts().stream().filter(account -> {

            if (account.getUniqueId().equals(Constants.CONSOLE_UUID))
                return false;

            if (account.getDisplayName().equalsIgnoreCase(name))
                return true;

            return filterOriginalNickname && account.hasCustomName() && account.getUsername().equalsIgnoreCase(name);
        }).findFirst().orElse(null);
    }

    public Collection<Account> getAccounts() {
        return accountMap.values();
    }
}