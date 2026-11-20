package com.ultraopt;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Draw {
    private static final int[][] INSETS = new int[32][];

    private Draw() {}

    private static int[] insets(int r) {
        int[] a = INSETS[r];
        if (a != null) return a;
        a = new int[r];
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            double dx = Math.sqrt(Math.max(0, r * r - dy * dy));
            a[i] = Math.max(0, r - (int) Math.ceil(dx));
        }
        INSETS[r] = a;
        return a;
    }

    /** Скруглённый прямоугольник (быстро, на основе fill). */
    public static void rect(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r > 31) r = 31;
        if (r <= 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        int[] ins = insets(r);
        g.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int in = ins[i];
            g.fill(x + in, y + i, x + w - in, y + i + 1, color);
            g.fill(x + in, y + h - 1 - i, x + w - in, y + h - i, color);
        }
    }

    /** Стеклянная панель с фоном и бликом. */
    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int r) {
        rect(g, x, y, w, h, r, Cfg.bg());
        if (Cfg.HIGHLIGHT.b) {
            int rr = Math.min(r, Math.min(w, h) / 2);
            g.fill(x + rr, y, x + w - rr, y + 1, 0x30FFFFFF);
        }
    }

    public static void text(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) {
        g.text(f, s, x, y, color, Cfg.TEXT_SHADOW.b);
    }

    public static String fit(Font f, String s, int maxW) {
        if (maxW <= 0) return "";
        if (f.width(s) <= maxW) return s;
        while (s.length() > 1 && f.width(s + "…") > maxW) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    public static int alpha(int rgb, int a) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    public static int hsv(float h, float s, float v) {
        float r, g, b;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        return 0xFF000000 | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
