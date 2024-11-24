/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.platform;

/**
 * The PlatformValidator validates if the Target
 * is a correct and usable {@link Platform}
 */
public interface PlatformValidator {

    /**
     * Tries to validate the Command platform and Sender object.
     * <p> Returns false if it wasn't validated</p>
     *
     * @param target Platform
     * @param object Object
     * @return Boolean
     */
    boolean validate(Platform target, Object object);

    /**
     * Returns the Platform by the Sender object
     * <p>Example: The Player object returns a {@link Platform} of PLAYER</p>
     *
     * @param object Object
     * @return Platform
     */
    Platform fromSender(Object object);

}