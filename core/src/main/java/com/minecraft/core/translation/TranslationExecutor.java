/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.translation;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class TranslationExecutor {

    private static TranslationExecutor instance;

    private final Map<Language, Map<String, MessageFormat>> languageTranslations;

    public TranslationExecutor() {
        this.languageTranslations = new HashMap<>();
        instance = this;
    }

    public void onDisable() {
        instance = null;
        this.languageTranslations.clear();
    }

    public static void addTranslation(TranslationInterface dataTranslation) {
        final Map<Language, Map<String, MessageFormat>> map = dataTranslation.loadTranslations();
        map.forEach((language, messagesMap) -> {
            if (instance.languageTranslations.containsKey(language)) {
                final Map<String, MessageFormat> messages = instance.languageTranslations.get(language);
                messages.putAll(messagesMap);
            } else {
                instance.languageTranslations.put(language, messagesMap);
            }
        });
    }

    public String translate(Language language, final String tag, final Object... format) {
        Map<String, MessageFormat> map = languageTranslations.computeIfAbsent(language, v -> new HashMap<>());
        MessageFormat messageFormat = map.computeIfAbsent(tag, v -> new MessageFormat(tag));
        return messageFormat.format(format);
    }

    public static String tl(Language language, String tag, final Object... format) {
        if (instance == null) {
            return tag;
        }
        return instance.translate(language, tag, format);
    }

}