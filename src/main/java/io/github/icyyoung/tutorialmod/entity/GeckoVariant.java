package io.github.icyyoung.tutorialmod.entity;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/17
 */

public enum GeckoVariant {
    BLUE(0),
    GREEN(1),
    PINK(2),
    BROWN(3);

    private static final GeckoVariant[] BY_ID = Arrays.stream(values()).sorted(
            Comparator.comparingInt(GeckoVariant::getId)).toArray(GeckoVariant[]::new);
    private final int id;

    GeckoVariant(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public static GeckoVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
