/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */


package com.minecraft.core.bukkit.util.scoreboard;

import java.util.ArrayList;
import java.util.List;

public class AnimatedString {

    private final String text;
    private final List<String> frames;
    private int position;
    private boolean finished;

    public AnimatedString(String text, String c1, String c2, String c3) {
        this(text, c1, c2, c3, 15);
    }

    public AnimatedString(String text, String c1, String c2, String c3, int p) {
        this.text = text;
        this.frames = new ArrayList<>();
        createFrames(c1, c2, c3, p);
    }

    private void createFrames(String c1, String c2, String c3, int p) {
        if (this.text != null && !this.text.isEmpty()) {
            int i;
            for (i = 0; i < this.text.length(); i++) {
                if (this.text.charAt(i) != ' ')
                    this.frames.add(c1 + this.text.substring(0, i) + c2 + this.text.charAt(i) + c3 + this.text.substring(i + 1));
            }
            for (i = 0; i < p; i++) {
                this.frames.add(c1 + this.text);
            }

            for (i = 0; i < 7; i++) {
                this.frames.add(c1 + this.text);
                this.frames.add(c1 + this.text);
                this.frames.add(c2 + this.text);
                this.frames.add(c2 + this.text);
            }

            for (i = 0; i < this.text.length(); i++) {
                if (this.text.charAt(i) != ' ')
                    this.frames.add(c3 + this.text.substring(0, i) + c2 + this.text.charAt(i) + c1 + this.text.substring(i + 1));
            }
            for (i = 0; i < p; i++)
                this.frames.add(c3 + this.text);
        }
    }

    public String next() {
        if (this.frames.isEmpty()) {
            return "";
        }
        if (this.finished) {
            this.position--;
            if (this.position <= 0)
                this.finished = false;
        } else {
            this.position++;
            if (this.position >= this.frames.size()) {
                this.finished = true;
                return next();
            }
        }

        return this.frames.get(this.position);
    }

    public boolean isFinished() {
        return this.finished;
    }
}