/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.database.HttpRequest;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.store.libs.EmptyStoreExecutionFeedback;
import com.minecraft.core.proxy.store.libs.StoreExecutionFeedback;
import com.minecraft.core.proxy.store.libs.StoreHistoryData;
import com.minecraft.core.proxy.store.libs.StoreProperty;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class Store {

    private final Set<StoreExecutionFeedback> executionFeedbackHistory;
    private final Configuration configuration;
    private final ProxyGame proxyGame;
    private StoreProperty storeProperty;

    public Store(ProxyGame proxyGame) {
        this.proxyGame = proxyGame;
        this.configuration = proxyGame.getConfiguration();
        this.executionFeedbackHistory = new HashSet<>();
    }

    public Store setup() {
        StoreProperty storeProperty = new StoreProperty();

        storeProperty.setServer(configuration.getInt("store.server"));
        storeProperty.setURL(configuration.getString("store.url"));
        storeProperty.setToken(configuration.getString("store.token"));
        this.storeProperty = storeProperty;
        return this;
    }

    public void disable() {
        delete();
    }

    public void execute() {
        StoreExecutionFeedback executionFeedback = query();

        if (executionFeedback != null && !executionFeedback.isEmpty())
            executionFeedbackHistory.add(executionFeedback);

        delete();
    }

    protected StoreExecutionFeedback query() {
        try {

            System.out.println("Checking for new store orders...");

            final String URL = storeProperty.getURL() + "restful/commands/" + storeProperty.getServer();

            HttpRequest request = HttpRequest.get(URL).connectTimeout(5000).readTimeout(5000).userAgent("Store/1.0.0").authorization(storeProperty.getToken()).acceptJson();

            if (request.ok()) {

                JsonObject responseJson = Constants.JSON_PARSER.parse(request.reader("UTF-8")).getAsJsonObject();

                request.disconnect();

                if (responseJson.get("status").getAsString().equals("error")) {
                    System.out.println("Error consulting the store! (" + responseJson.get("message").getAsString() + ")");
                    return null;
                }

                JsonArray jsonArray = responseJson.getAsJsonArray("commands");

                if (jsonArray == null || jsonArray.size() == 0) {
                    System.out.println("No execution commands in queue.");
                    return new EmptyStoreExecutionFeedback();
                }

                final List<StoreHistoryData> dataList = new ArrayList<>();

                for (JsonElement jsonElement : jsonArray) {
                    StoreHistoryData data = StoreHistoryData.queue(jsonElement.getAsJsonObject());
                    dataList.add(data);
                }

                StoreExecutionFeedback feedback = new StoreExecutionFeedback();

                feedback.getFeedback().addAll(dataList);

                feedback.getFeedback().removeIf(c -> getOrder(c.getId()) != null);

                dataList.forEach(data -> {

                    if (data.getType() == StoreHistoryData.Type.OFFLINE) {
                        data.apply();
                    } else if (BungeeCord.getInstance().getPlayer(data.getTarget()) != null) {
                        data.apply();
                    }
                });
                return feedback;
            } else {
                System.out.println("[store: " + request.code() + "] " + request.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void delete() {

        final JsonArray array = new JsonArray();

        for (StoreExecutionFeedback feedback : getExecutionFeedbackHistory()) {
            for (StoreHistoryData data : feedback.getFeedback()) {

                if (data.isDeleted() || !data.isExecuted())
                    continue;

                array.add(data.getId());
            }
        }

        if (array.size() == 0)
            return;

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("commands", array.getAsJsonArray());

        final String URL = storeProperty.getURL() + "restful/commands/" + storeProperty.getServer();

        try {
            System.out.println("Deleting executed commands!");
            HttpRequest request = HttpRequest.delete(URL).contentType("application/json; charset=utf-8").connectTimeout(5000).readTimeout(5000).userAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)").authorization(storeProperty.getToken());

            request.send(jsonObject.toString());

            if (request.ok()) {

                JsonObject responseJson = Constants.JSON_PARSER.parse(request.reader("UTF-8")).getAsJsonObject();

                if (responseJson.get("status").getAsString().equals("error")) {
                    System.out.println("Error deleting the executed tasks! (" + responseJson.get("message").getAsString() + ")");
                    return;
                }

                for (JsonElement element : array) {
                    int object = element.getAsInt();
                    System.out.println("Set deleted " + getOrder(object).toString());
                    getOrder(object).setDeleted(true);
                }
            }

            request.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StoreHistoryData getOrder(int id) {

        StoreHistoryData order = null;

        for (StoreExecutionFeedback feedback : getExecutionFeedbackHistory()) {
            for (StoreHistoryData data : feedback.getFeedback()) {
                if (data.getId() == id)
                    order = data;
            }
        }
        return order;
    }

    public Set<StoreHistoryData> getPendingOrders(String name) {

        Set<StoreHistoryData> pendingOrders = new HashSet<>();

        for (StoreExecutionFeedback feedback : getExecutionFeedbackHistory()) {
            for (StoreHistoryData data : feedback.getPendingCommands()) {

                if (data.isExecuted())
                    continue;

                if (data.getTarget().equalsIgnoreCase(name))
                    pendingOrders.add(data);
            }
        }
        return pendingOrders;
    }
}