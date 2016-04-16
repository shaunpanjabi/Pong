package com.shaunlp.pong;


import android.graphics.Color;

import java.util.Random;

public enum Colors {
    BLACK(255, 0, 0, 0),
    WHITE(255, 255, 255, 255),
    GREEN(255, 0 ,255, 0);

    private final int alpha;
    private final int r;
    private final int g;
    private final int b;
    private static Random generator = new Random();

    private Colors (final int alpha, final int r, final int g, final int b) {
        this.alpha = alpha;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getColor() {
        return Color.argb(alpha, r, g, b);
    }

    public static int getRandomColor() {
        return Color.argb(255,
                generator.nextInt(255),
                generator.nextInt(255),
                generator.nextInt(255));
    }
}
