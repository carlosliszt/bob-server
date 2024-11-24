/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.translation;

import com.minecraft.core.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;

public enum Language {

    MEOW("meow_meow.properties", "Miau:3", "M3w02", new SimpleDateFormat("MM/dd/yyyy HH:mm"), new SimpleDateFormat("MM/dd/yyyy")),
    PORTUGUESE("pt_br.properties", "Português", "VIxPa", new SimpleDateFormat("dd/MM/yyyy HH:mm"), new SimpleDateFormat("dd/MM/yyyy")),
    ENGLISH("en_us.properties", "English", "WaAYu", new SimpleDateFormat("MM/dd/yy',' hh:mm aa"), new SimpleDateFormat("MM/dd/yy"));

    private final String fileName;
    private final String name;
    private final String uniqueCode;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat dayFormat;

    Language(String fileName, String name, String uniqueCode, SimpleDateFormat simpleDateFormat, SimpleDateFormat dayFormat) {
        this.fileName = fileName;
        this.name = name;
        this.uniqueCode = uniqueCode;
        this.dateFormat = simpleDateFormat;
        this.dayFormat = dayFormat;
    }

    public static Language fromString(String name) {
        return Arrays.stream(values()).filter(language -> language.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Language fromUniqueCode(String code) {
        return Arrays.stream(values()).filter(language -> language.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public SimpleDateFormat getDayFormat() {
        return dayFormat;
    }

    public String getLower() {
        return name.toLowerCase();
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public String translate(String tag, Object... format) {
        return TranslationExecutor.tl(this, tag, format);
    }

    public String translateTime(int time) {
        return DateUtils.formatTime(this, time);
    }

}