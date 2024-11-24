/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.CommandFrame;
import com.minecraft.core.command.argument.TypeAdapter;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import com.minecraft.core.translation.TranslationExecutor;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.UUID;

/**
 * The context is where all information from the command dispatcher
 * is stored, such as the sender, arguments and label
 */
public interface Context<S> {

    /**
     * Contains the label sent by the command
     *
     * @return String
     */
    String getLabel();

    /**
     * Return if the sender is a player.
     *
     * @return Boolean
     */
    boolean isPlayer();

    /**
     * The generic value can be either
     * a Console or Player
     *
     * @return S
     */
    S getSender();

    /**
     * @return the executor type
     */
    Platform getPlatform();

    /**
     * Contains all arguments sent by the command
     *
     * @return String[] of arguments
     */
    String[] getArgs();

    /**
     * Return command sender's uniqueid.
     *
     * @return UUID
     */
    UUID getUniqueId();

    /**
     * @return the number of arguments
     */
    default int argsCount() {
        return getArgs().length;
    }

    /**
     * Return command sender's account.
     *
     * @return Account
     */
    default Account getAccount() {
        return Account.fetch(getUniqueId());
    }

    /**
     * @param index the index of the argument
     * @return the argument - null if the index is out of bounds
     */
    default String getArg(int index) {
        try {
            return getArgs()[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T getArg(int index, Class<T> type) {
        return (T) getCommandFrame().getAdapterMap().get(type).convertNonNull(getArg(index));
    }

    /**
     * Gets all args between indexes from and to
     *
     * @param from defines the start of the array relative to the arguments, inclusive
     * @param to   defines the end of the array relative to the arguments, exclusive
     * @return the arguments array - null if the indexes are out of bounds
     */
    default String[] getArgs(int from, int to) {
        try {
            return Arrays.copyOfRange(getArgs(), from, to);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T[] getArgs(int from, int to, Class<T> type) {
        try {
            final TypeAdapter<?> adapter = getCommandFrame().getAdapterMap().get(type);
            final T[] instance = (T[]) Array.newInstance(type, to - from);

            for (int i = from; i <= to; i++) {
                instance[i - from] = (T) adapter.convertNonNull(getArg(i));
            }

            return instance;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Sends a message to the executor
     *
     * @param message the message to be sent
     */
    void sendMessage(String message);

    /**
     * Sends multiple messages to the executor
     *
     * @param messages the messages to be sent
     */
    void sendMessage(String[] messages);

    /**
     * Sends a message formatting it with the String#format() method
     *
     * @param message the message to be sent
     * @param objects the objects to be inserted
     */
    default void sendMessage(String message, Object... objects) {
        sendMessage(String.format(message, objects));
    }

    default void info(String key, Object... objects) {
        sendMessage(TranslationExecutor.tl(getLanguage(), key, objects));
    }

    default Language getLanguage() {
        return Constants.getAccountStorage().getAccount(getUniqueId()).getLanguage();
    }

    /**
     * @return this command's frame
     */
    CommandFrame<?, ?, ?> getCommandFrame();

    /**
     * @return this command's holder
     */
    CommandHolder<?, ?> getCommandHolder();

    default boolean isDeveloper() {
        return getAccount().hasPermission(Rank.DEVELOPER_ADMIN);
    }

}
