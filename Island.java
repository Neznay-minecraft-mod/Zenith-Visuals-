package com.ultraopt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;

public final class Island {
    private static final String[] INFO_LABELS = {"Сервер", "Пинг", "FPS", "XYZ", "Курс", "Скорость", "ОЗУ", "Предмет"};
    private static final int INFO_ROWS = INFO_LABELS.length;

    private static float cw = 0, ch = 0;
    private static long lastT = 0;
    public static boolean expanded = false;

    private static String nTitle = "", nText = "";
    private static int nColor = 0xFF7A1A;
    private static boolean nChat;
    private static long nUntil = 0;
    private static String idle = "";
    private static float lastHp = -1;

    private Island() {}

    public static void toggle() {
        expanded = !expanded;
    }

    public static void refresh() {
        switch (Cfg.ISLAND_IDLE.i) {
            case 0 -> idle = "FPS " + Stats.fpsS + (Stats.ping >= 0 ? "  ·  " + Stats.pingS + " мс" : "");
            case 1 -> idle = "FPS " + Stats.fpsS;
            case 2 -> idle = Stats.clock;
            case 3 -> idle = Stats.nick + "  ·  HP " + (int) Math.ceil(Stats.hp);
            default -> idle = "";
        }
    }

    public static void notify(String title, String text, int rgb, int ms) {
        if (!Cfg.ISLAND.b) return;
        nTitle = title;
        nText = text;
        nColor = 0xFF000000 | rgb;
        nChat = false;
        nUntil = System.currentTimeMillis() + ms;
    }

    public static void chat(String raw) {
        if (!Cfg.ISLAND.b || !Cfg.ISLAND_CHAT.b || raw == null) return;
        String s = raw.trim();
        if (s.isEmpty()) return;
        String name = "Сервер", text = s;
        if (s.startsWith("<")) {
            int e = s.indexOf('>');
            if (e > 1 && e < 28) {
                name = s.substring(1, e);
                text = s.substring(e + 1).trim();
            }
        } else {
            int lim = Math.min(s.length(), 28);
            for (int i = 1; i < lim; i++) {
                char c = s.charAt(i);
                if (c == ':' || c == '»' || c == '>') {
                    name = s.substring(0, i).trim();
                    text = s.substring(i + 1).trim();
                    break;
                }
            }
        }
        if (text.isEmpty()) return;
        if (name.isEmpty()) name = "Сервер";
        nTitle = name;
        nText = text;
        nChat = true;
        nUntil = System.currentTimeMillis() + Cfg.ISLAND_CHAT_TIME.i * 1000L;
    }

    /** Вызывается каждый клиентский тик: следит за здоровьем. */
    public static void tick(Minecraft mc) {
        if (mc.player == null) {
            lastHp = -1;
            return;
        }
        float hp = mc.player.getHealth();
        if (lastHp >= 0 && Cfg.ISLAND_DAMAGE.b && hp < lastHp - 0.01f) {
            boolean low = hp <= 6f;
            notify(low ? "Мало здоровья" : "Урон",
                    "-" + fmt(lastHp - hp) + " HP, осталось " + fmt(hp),
                    low ? 0xFF4D4D : 0xFFB84D, 2500);
        }
        lastHp = hp;
    }

    private static String fmt(float v) {
        return v == (int) v ? Integer.toString((int) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    private static int clamp(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    public static void render(GuiGraphicsExtractor g, Minecraft mc, long now) {
        if (!Cfg.ISLAND.b) return;
        Font f = mc.font;
        float dt = lastT == 0 ? 0.016f : Math.min(0.1f, (now - lastT) / 1000f);
        lastT = now;

        boolean showInfo = expanded && Cfg.ISLAND_EXPAND.b;
        boolean notif = now < nUntil;
        int mode, tw, th;

        if (showInfo) {
            mode = 3;
            tw = 220;
            th = 26 + INFO_ROWS * 11 + 6;
        } else if (notif && nChat) {
            mode = 1;
            int inner = Math.max(f.width(nTitle), f.width(nText));
            tw = clamp(inner + (Cfg.ISLAND_AVATAR.b ? 34 : 16) + 12, 150, 300);
            th = 34;
        } else if (notif) {
            mode = 2;
            tw = clamp(f.width(nTitle) + f.width(nText) + 44, 120, 300);
            th = 24;
        } else if (Cfg.ISLAND_IDLE.i == 4 || idle.isEmpty()) {
            mode = 0;
            tw = 0;
            th = 0;
        } else {
            mode = 0;
            tw = Cfg.ISLAND_W.i;
            th = 20;
        }

        float k = Cfg.ANIMS.b ? 1f - (float) Math.exp(-dt * Cfg.ISLAND_SPEED.i * 1.5f) : 1f;
        cw += (tw - cw) * k;
        ch += (th - ch) * k;
        int w = Math.round(cw), h = Math.round(ch);
        if (w < 4 || h < 4) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int x = (sw - w) / 2, y = Cfg.ISLAND_Y.i;
        int r = Math.min(h / 2, 14);
        int acc = Cfg.accent();
        int bgA = Math.max(0xE0, Cfg.BG_ALPHA.i * 255 / 100);

        Draw.rect(g, x, y, w, h, r, (bgA << 24) | 0x08090D);
        if (Cfg.ACCENT_BAR.b) g.fill(x + r, y + h - 1, x + w - r, y + h, Draw.alpha(acc, 0x90));

        g.enableScissor(x + 2, y, x + w - 2, y + h);
        switch (mode) {
            case 0 -> {
                int gw = 12 + f.width(idle);
                int gx = x + (w - gw) / 2;
                Draw.rect(g, gx, y + h / 2 - 3, 6, 6, 3, acc);
                Draw.text(g, f, idle, gx + 12, y + (h - 8) / 2, 0xFFFFFFFF);
            }
            case 1 -> {
                int tx = x + 10;
                if (Cfg.ISLAND_AVATAR.b) {
                    int ay = y + (h - 20) / 2;
                    int col = Draw.hsv((Math.abs(nTitle.hashCode()) % 360) / 360f, 0.55f, 0.95f);
                    Draw.rect(g, x + 8, ay, 20, 20, 6, col);
                    String initial = nTitle.isEmpty() ? "?" : nTitle.substring(0, 1).toUpperCase(Locale.ROOT);
                    g.text(f, initial, x + 8 + (20 - f.width(initial)) / 2, ay + 6, 0xFF101114, false);
                    tx = x + 34;
                }
                int maxW = w - (tx - x) - 10;
                Draw.text(g, f, Draw.fit(f, nTitle, maxW), tx, y + 7, acc);
                Draw.text(g, f, Draw.fit(f, nText, maxW), tx, y + 19, 0xFFFFFFFF);
            }
            case 2 -> {
                int gw = 12 + f.width(nTitle) + 8 + f.width(nText);
                int gx = x + (w - gw) / 2;
                Draw.rect(g, gx, y + h / 2 - 3, 6, 6, 3, nColor);
                Draw.text(g, f, nTitle, gx + 12, y + (h - 8) / 2, nColor);
                Draw.text(g, f, nText, gx + 12 + f.width(nTitle) + 8, y + (h - 8) / 2, 0xFFFFFFFF);
            }
            default -> {
                Draw.text(g, f, Stats.nick, x + 12, y + 8, acc);
                g.text(f, Stats.clock, x + w - 12 - f.width(Stats.clock), y + 8, 0xFF9AA0AA, false);
                g.fill(x + 12, y + 20, x + w - 12, y + 21, 0x30FFFFFF);
                String[] vals = {Stats.server, Stats.pingS + " мс", Stats.fpsS, Stats.coords, Stats.dir, Stats.speedS, Stats.mem, Stats.held};
                for (int i = 0; i < INFO_ROWS; i++) {
                    int ry = y + 26 + i * 11;
                    Draw.text(g, f, INFO_LABELS[i], x + 12, ry, 0xFF9AA0AA);
                    Draw.text(g, f, Draw.fit(f, vals[i], w - 90 - 12), x + 78, ry, 0xFFFFFFFF);
                }
            }
        }
        g.disableScissor();
    }
}
