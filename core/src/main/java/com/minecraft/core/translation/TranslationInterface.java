/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.translation;

import java.text.MessageFormat;
import java.util.Map;

public interface TranslationInterface {

    Map<Language, Map<String, MessageFormat>> loadTranslations();

}