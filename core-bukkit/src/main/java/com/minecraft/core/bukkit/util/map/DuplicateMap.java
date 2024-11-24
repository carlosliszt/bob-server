/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DuplicateMap<K, V> {

    private final Map<K, ArrayList<V>> m = new HashMap<>();

    public void put(K k, V v) {
        if (m.containsKey(k)) {
            m.get(k).add(v);
        } else {
            ArrayList<V> arr = new ArrayList<>();
            arr.add(v);
            m.put(k, arr);
        }
    }

    public ArrayList<V> get(K k) {
        return m.get(k);
    }

    public V get(K k, int index) {
        return m.get(k).size() - 1 < index ? null : m.get(k).get(index);
    }

    public boolean containsKey(K k) {
        return m.get(k) != null;
    }

}