/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.exception;

/**
 * The NoSuchConverterException is thrown when there
 * isn't a converter for the type provided.
 */
public class NoSuchConverterException extends CommandException {

    public NoSuchConverterException(Class<?> type) {
        super("No converter found for type " + type.getTypeName());
    }

}
