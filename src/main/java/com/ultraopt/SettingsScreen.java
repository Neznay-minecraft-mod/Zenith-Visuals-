package com.ultraopt;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {
    private static final String[] TABS = {"Оптимизация", "Остров", "Панель", "Оформление"};
    private static final String[] HINTS = {
            "Скрытие лишнего и ускорение",
            "Динамический остров",
            "Панель статистики и клавиши",
            "Цвета и стиль"};
    private static final int SIDE_W = 104, ROW_H = 30;

    private final Screen parent;
    private final List<Setting> current = new ArrayList<>();
    private int tab = 0, page = 0, pages = 1;
    private int px, py, pw, ph, cx, cw, perPage;
    private boolean bgDone;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Zenith Visuals"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        pw = Math.min(450, this.width - 16);
        ph = Math.min(260, this.height - 16);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;
        cx = px + SIDE_W + 16;
        cw = pw - SIDE_W - 24;
        perPage = Math.max(3, (ph - 44 - 34) / ROW_H);

        for (int t = 0; t < TABS.length; t++) {
            final int tt = t;
            this.addRenderableWidget(Button.builder(
                    Component.literal((t == tab ? "» " : "") + TABS[t]),
                    b -> {
                        tab = tt;
                        page = 0;
                        rebuild();
                    }).bounds(px + 8, py + 46 + t * 24, SIDE_W, 20).build());
        }

        current.clear();
        for (Setting s : Cfg.ALL) if (s.tab == tab) current.add(s);
        pages = Math.max(1, (current.size() + perPage - 1) / perPage);
        if (page >= pages) page = pages - 1;

        for (int i = 0; i < perPage; i++) {
            int idx = page * perPage + i;
            if (idx >= current.size()) break;
            final Setting s = current.get(idx);
            int ry = py + 44 + i * ROW_H;
            this.addRenderableWidget(Button.builder(s.display(), b -> {
                s.next();
                b.setMessage(s.display());
            }).bounds(cx + cw - 100, ry + 3, 96, 20).build());
        }

        int by = py + ph - 28;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (page > 0) {
                page--;
                rebuild();
            }
        }).bounds(cx, by, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if (page < pages - 1) {
                page++;
                rebuild();
            }
        }).bounds(cx + 24, by, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Сброс"), b -> {
            for (Setting s : current) s.reset();
            rebuild();
        }).bounds(cx + cw - 170, by, 64, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(cx + cw - 100, by, 96, 20).build());
    }

    private void rebuild() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void onClose() {
        Cfg.save();
        Island.notify("Zenith", "Настройки сохранены", 0x3DDC84, 1800);
        this.minecraft.gui.setScreen(this.parent);
    }

    // Без @Override намеренно: если имя метода в версии совпадает — фон будет нашим.
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        bgDone = true;
        g.fill(0, 0, this.width, this.height, 0xA0000000);
        Draw.rect(g, px, py, pw, ph, 10, 0xF0121419);
        Draw.rect(g, px + 6, py + 40, SIDE_W + 4, ph - 52, 8, 0x22FFFFFF);
        drawTexts(g, true);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        bgDone = false;
        super.extractRenderState(g, mouseX, mouseY, delta);
        if (!bgDone) drawTexts(g, false);
    }

    private void drawTexts(GuiGraphicsExtractor g, boolean withRects) {
        int acc = Cfg.accent();
        g.text(this.font, Component.literal("ZENITH VISUALS").withStyle(ChatFormatting.BOLD), px + 14, py + 12, acc, true);
        g.text(this.font, HINTS[tab], px + 14, py + 26, 0xFF8A909C, false);
        if (withRects) {
            g.fill(px + 8, py + 38, px + pw - 8, py + 39, 0x30FFFFFF);
            g.fill(px + 10, py + 46 + tab * 24 + 3, px + 12, py + 46 + tab * 24 + 17, acc);
        }
        for (int i = 0; i < perPage; i++) {
            int idx = page * perPage + i;
            if (idx >= current.size()) break;
            Setting s = current.get(idx);
            int ry = py + 44 + i * ROW_H;
            if (withRects) Draw.rect(g, cx, ry, cw, ROW_H - 4, 6, 0x28FFFFFF);
            g.text(this.font, Draw.fit(this.font, s.name, cw - 116), cx + 8, ry + 4, 0xFFFFFFFF, false);
            g.text(this.font, Draw.fit(this.font, s.desc, cw - 116), cx + 8, ry + 15, 0xFF8A909C, false);
        }
        int by = py + ph - 28;
        g.text(this.font, (page + 1) + " / " + pages, cx + 52, by + 6, 0xFF9AA0AA, false);
        g.text(this.font, "Правый Shift — меню", px + 12, py + ph - 14, 0xFF6B717C, false);
    }
}
