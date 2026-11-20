package com.ultraopt;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Hud {
    private static final String[] L = new String[20];
    private static final String[] V = new String[20];
    private static int n, lw, vw, lastS = -1, lastC = -1;

    private Hud() {}

    private static void add(String label, String value) {
        L[n] = label;
        V[n] = value;
        n++;
    }

    private static void rebuild(Minecraft mc) {
        Font f = mc.font;
        n = 0;
        if (Cfg.SHOW_FPS.b) add("FPS", Stats.fpsS);
        if (Cfg.SHOW_PING.b) add("Пинг", Stats.pingS + " мс");
        if (Cfg.SHOW_COORDS.b) add("XYZ", Stats.coords);
        if (Cfg.SHOW_DIR.b) add("Курс", Stats.dir);
        if (Cfg.SHOW_SPEED.b) add("Скорость", Stats.speedS);
        if (Cfg.SHOW_CLOCK.b) add("Время", Stats.clock);
        if (Cfg.SHOW_MEM.b) add("ОЗУ", Stats.mem);
        if (Cfg.SHOW_CPS.b) add("CPS", Stats.cpsS);
        if (Cfg.SHOW_HELD.b) add("Предмет", Draw.fit(f, Stats.held, 150));
        if (Cfg.OPT_STATS.b) {
            add("Скрыто", Stats.entS + ", част. " + Stats.partS);
            add("Тики пропущ.", Stats.tickS);
        }
        lw = 0;
        vw = 0;
        for (int i = 0; i < n; i++) {
            lw = Math.max(lw, f.width(L[i]));
            vw = Math.max(vw, f.width(V[i]));
        }
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        long now = System.currentTimeMillis();

        Stats.frame(mc, now);
        if (lastS != Stats.version || lastC != Cfg.version) {
            lastS = Stats.version;
            lastC = Cfg.version;
            rebuild(mc);
            Island.refresh();
        }

        Island.render(g, mc, now);
        if (Cfg.PANEL.b && n > 0) panel(g, mc);
        if (Cfg.KEYS.b) keys(g, mc);
    }

    private static void panel(GuiGraphicsExtractor g, Minecraft mc) {
        Font f = mc.font;
        int pad = 6, lh = 11;
        int w = pad * 2 + 4 + lw + 10 + vw;
        int h = pad * 2 + n * lh - 2;
        int a = Cfg.PANEL_ANCHOR.i;
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        int x = (a == 1 || a == 3) ? sw - w - Cfg.PANEL_X.i : Cfg.PANEL_X.i;
        int y = a >= 2 ? sh - h - Cfg.PANEL_Y.i : Cfg.PANEL_Y.i;

        Draw.panel(g, x, y, w, h, Cfg.RADIUS.i);
        if (Cfg.ACCENT_BAR.b) g.fill(x + 3, y + 5, x + 5, y + h - 5, Cfg.accent());
        int tx = x + pad + 4;
        for (int i = 0; i < n; i++) {
            int ty = y + pad + i * lh;
            Draw.text(g, f, L[i], tx, ty, 0xFF9AA0AA);
            Draw.text(g, f, V[i], tx + lw + 10, ty, 0xFFFFFFFF);
        }
    }

    private static void keys(GuiGraphicsExtractor g, Minecraft mc) {
        Font f = mc.font;
        int B = 20, G = 2;
        int w = B * 3 + G * 2, h = B * 4 + G * 3;
        int a = Cfg.KEYS_ANCHOR.i;
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        int x = (a == 1 || a == 3) ? sw - w - Cfg.PANEL_X.i : Cfg.PANEL_X.i;
        int y = a >= 2 ? sh - h - Cfg.PANEL_Y.i : Cfg.PANEL_Y.i;
        var o = mc.options;
        int acc = Cfg.accent();

        box(g, f, x + B + G, y, B, B, "W", o.keyUp.isDown(), acc);
        int y1 = y + B + G;
        box(g, f, x, y1, B, B, "A", o.keyLeft.isDown(), acc);
        box(g, f, x + B + G, y1, B, B, "S", o.keyDown.isDown(), acc);
        box(g, f, x + 2 * (B + G), y1, B, B, "D", o.keyRight.isDown(), acc);
        int y2 = y1 + B + G;
        int half = (w - G) / 2;
        box(g, f, x, y2, half, B, "ЛКМ", o.keyAttack.isDown(), acc);
        box(g, f, x + half + G, y2, w - half - G, B, "ПКМ", o.keyUse.isDown(), acc);
        int y3 = y2 + B + G;
        box(g, f, x, y3, w, B, "ПРОБЕЛ", o.keyJump.isDown(), acc);
    }

    private static void box(GuiGraphicsExtractor g, Font f, int x, int y, int w, int h, String s, boolean down, int acc) {
        Draw.rect(g, x, y, w, h, Math.min(Cfg.RADIUS.i, 5), down ? Draw.alpha(acc, 0xD0) : Cfg.bg());
        g.text(f, s, x + (w - f.width(s)) / 2, y + (h - 8) / 2, down ? 0xFF101114 : 0xFFFFFFFF, false);
    }
}
