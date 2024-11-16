/*
 * Copyright (C) YoloMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.player;

import lombok.Getter;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.Property;

import java.lang.reflect.Field;

public class SkinChanger {

    @Getter
    private static final SkinChanger instance = new SkinChanger();

    public void changeTexture(PendingConnection connection, String value, String signature) {
        InitialHandler initialHandler = (InitialHandler) connection;
        LoginResult loginProfile = initialHandler.getLoginProfile();

        if (loginProfile == null) {
            setLoginProfile(initialHandler, connection);
            loginProfile = initialHandler.getLoginProfile();
        }

        Property property = new Property("textures", value, signature);
        loginProfile.setProperties(new Property[]{property});
    }

    public Property getSkin(PendingConnection pendingConnection) {
        InitialHandler initialHandler = (InitialHandler) pendingConnection;
        return initialHandler.getLoginProfile().getProperties()[0];
    }

    private void setLoginProfile(InitialHandler initialHandler, PendingConnection pendingConnection) {
        try {
            Field field = initialHandler.getClass().getDeclaredField("loginProfile");
            field.setAccessible(true);
            field.set(initialHandler, new LoginResult(pendingConnection.getUUID(), pendingConnection.getName(), new Property[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
