/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.translation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

public class PropertiesStorageDataTranslation implements TranslationInterface {

    @Override
    public Map<Language, Map<String, MessageFormat>> loadTranslations() {
        Map<Language, Map<String, MessageFormat>> languageMaps = new HashMap<>();
        for (Language language : Language.values()) {
            try (InputStream inputStream = getClass().getResourceAsStream("/" + language.getFileName())) {
                if (inputStream == null) {
                    System.err.println("Translation file not found: " + language.getFileName());
                    continue;
                }
                SortedProperties properties = new SortedProperties();

                properties.load(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

                Map<String, MessageFormat> map = new HashMap<>();
                properties.forEach((key, message) -> map.put(String.valueOf(key), new MessageFormat((String) message)));
                languageMaps.put(language, map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return languageMaps;
    }

    static class SortedProperties extends Properties {
        public Enumeration keys() {
            Enumeration keysEnum = super.keys();
            Vector<String> keyList = new Vector<String>();
            while (keysEnum.hasMoreElements()) {
                keyList.add((String) keysEnum.nextElement());
            }
            Collections.sort(keyList);
            return keyList.elements();
        }

    }
}