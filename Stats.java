package com.ultraopt;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Stats {
    public static int version = 0;
    public static int fps;
    public static int ping = -1;
    public static float hp;
    public static String fpsS = "0", pingS = "—", coords = "", dir = "", speedS = "", clock = "",
            mem = "", held = "", server = "", cpsS = "0", nick = "", entS = "0", partS = "0", tickS = "0";

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] DIRS = {"Ю", "ЮЗ", "З", "СЗ", "С", "СВ", "В", "ЮВ"};
    private static final long[] CLICKS = new long[64];

    private static int frames, ci, entFrame, partSec, tickSec;
    private static long lastFpsT, lastRefresh, lastSecond, lastSpeedT;
    private static boolean wasDown;
    private static double lx, lz;

    private Stats() {}

    public static void frame(Minecraft mc, long now) {
        frames++;
        if (now - lastFpsT >= 500) {
            if (lastFpsT != 0) fps = (int) (frames * 1000L / (now - lastFpsT));
            frames = 0;
            lastFpsT = now;
        }

        entFrame = CullLogic.entCulled;
        CullLogic.entCulled = 0;

        boolean down = mc.options.keyAttack.isDown();
        if (down && !wasDown) CLICKS[ci++ & 63] = now;
        wasDown = down;

        if (now - lastSecond >= 1000) {
            lastSecond = now;
            partSec = CullLogic.partCulled;
            tickSec = CullLogic.tickSkipped;
            CullLogic.partCulled = 0;
            CullLogic.tickSkipped = 0;
            ping = readPing(mc);
        }

        if (now - lastRefresh >= Cfg.HUD_INTERVAL.i) {
            lastRefresh = now;
            refresh(mc, now);
        }
    }

    private static void refresh(Minecraft mc, long now) {
        if (mc.player == null) return;
        var p = mc.player;

        fpsS = Integer.toString(fps);
        pingS = ping >= 0 ? Integer.toString(ping) : "—";
        coords = String.format(Locale.ROOT, "%.1f  %.1f  %.1f", p.getX(), p.getY(), p.getZ());

        float yaw = ((p.getYRot() % 360f) + 360f) % 360f;
        dir = DIRS[(int) Math.floor(yaw / 45f + 0.5f) % 8];

        double dt = (now - lastSpeedT) / 1000.0;
        if (lastSpeedT != 0 && dt > 0) {
            double v = Math.hypot(p.getX() - lx, p.getZ() - lz) / dt;
            speedS = String.format(Locale.ROOT, "%.1f бл/с", v);
        }
        lx = p.getX();
        lz = p.getZ();
        lastSpeedT = now;

        clock = LocalTime.now().format(CLOCK);

        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) >> 20;
        mem = used + " / " + (rt.maxMemory() >> 20) + " МБ";

        int c = 0;
        for (long t : CLICKS) if (t != 0 && now - t <= 1000) c++;
        cpsS = Integer.toString(c);

        ItemStack st = p.getMainHandItem();
        if (st.isEmpty()) {
            held = "—";
        } else {
            String n = st.getHoverName().getString();
            if (st.isDamageableItem()) n += "  " + (st.getMaxDamage() - st.getDamageValue()) + "/" + st.getMaxDamage();
            else if (st.getCount() > 1) n += "  x" + st.getCount();
            held = n;
        }

        nick = p.getName().getString();
        hp = p.getHealth();
        server = readServer(mc);

        entS = entFrame + " сущ.";
        partS = partSec + "/с";
        tickS = tickSec + "/с";

        version++;
    }

    private static int readPing(Minecraft mc) {
        try {
            Object conn = mc.getConnection();
            if (conn == null || mc.player == null) return -1;
            Object info = conn.getClass().getMethod("getPlayerInfo", java.util.UUID.class).invoke(conn, mc.player.getUUID());
            if (info == null) return -1;
            return (Integer) info.getClass().getMethod("getLatency").invoke(info);
        } catch (Throwable t) {
            return -1;
        }
    }

    private static String readServer(Minecraft mc) {
        try {
            Object sd = mc.getCurrentServer();
            if (sd == null) return "Одиночная игра";
            return (String) sd.getClass().getField("ip").get(sd);
        } catch (Throwable t) {
            return "—";
        }
    }
}
