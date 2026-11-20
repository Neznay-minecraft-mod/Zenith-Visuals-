package com.ultraopt;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Cfg {
    public static final List<Setting> ALL = new ArrayList<>();
    public static int version = 0;

    public static final int T_OPT = 0, T_ISL = 1, T_HUD = 2, T_LOOK = 3;

    private static Setting bool(int tab, String id, String name, String desc, boolean def) {
        Setting s = new Setting(Setting.Type.BOOL, tab, id, name, desc, def ? 1 : 0, 0, 1, 1, "", null);
        ALL.add(s);
        return s;
    }

    private static Setting num(int tab, String id, String name, String desc, int def, int min, int max, int step, String suffix) {
        Setting s = new Setting(Setting.Type.INT, tab, id, name, desc, def, min, max, step, suffix, null);
        ALL.add(s);
        return s;
    }

    private static Setting choice(int tab, String id, String name, String desc, int def, String... opts) {
        Setting s = new Setting(Setting.Type.CHOICE, tab, id, name, desc, def, 0, opts.length - 1, 1, "", opts);
        ALL.add(s);
        return s;
    }

    // ===================== ОПТИМИЗАЦИЯ =====================
    public static final Setting ENTITY_CULL = bool(T_OPT, "entity_cull", "Скрытие далёких существ", "Не рисует мобов и предметы вдали", true);
    public static final Setting OCCLUSION = bool(T_OPT, "occlusion", "Скрытие за стенами", "Не рисует то, что закрыто блоками", true);
    public static final Setting DIST_MOB = num(T_OPT, "dist_mob", "Дальность: мобы", "Дальше этого мобы не рисуются", 56, 16, 128, 8, " бл.");
    public static final Setting DIST_ITEM = num(T_OPT, "dist_item", "Дальность: предметы", "Предметы и опыт", 28, 8, 64, 4, " бл.");
    public static final Setting DIST_PLAYER = num(T_OPT, "dist_player", "Дальность: игроки", "Другие игроки", 128, 32, 256, 16, " бл.");
    public static final Setting DIST_OTHER = num(T_OPT, "dist_other", "Дальность: прочее", "Рамки, стойки, лодки, вагонетки", 40, 16, 96, 8, " бл.");
    public static final Setting OCC_RECHECK = num(T_OPT, "occ_recheck", "Проверка стен", "Как часто проверять (в тиках)", 3, 1, 10, 1, " т.");
    public static final Setting OCC_MIN = num(T_OPT, "occ_min", "Не скрывать ближе", "Рядом всегда рисуем", 6, 2, 16, 2, " бл.");
    public static final Setting PARTICLE_CULL = bool(T_OPT, "particle_cull", "Отсечение частиц", "Не создаёт далёкие частицы", true);
    public static final Setting PARTICLE_DIST = num(T_OPT, "particle_dist", "Дальность частиц", "Дальше частицы не создаются", 24, 8, 64, 4, " бл.");
    public static final Setting PARTICLE_THIN = num(T_OPT, "particle_thin", "Прореживание частиц", "Часть далёких частиц пропускается", 30, 0, 90, 10, "%");
    public static final Setting TICK_SKIP = bool(T_OPT, "tick_skip", "Пропуск тиков вдали", "Далёкие существа не обновляются", true);
    public static final Setting TICK_DIST = num(T_OPT, "tick_dist", "Дальность тиков", "С какого расстояния пропускать", 96, 32, 192, 16, " бл.");
    public static final Setting HUD_INTERVAL = num(T_OPT, "hud_interval", "Обновление панели", "Реже = меньше нагрузка", 100, 50, 1000, 50, " мс");
    public static final Setting OPT_STATS = bool(T_OPT, "opt_stats", "Статистика мода", "Показывает сколько скрыто", true);

    // ===================== ОСТРОВ =====================
    public static final Setting ISLAND = bool(T_ISL, "island", "Динамический остров", "Главный переключатель", true);
    public static final Setting ISLAND_CHAT = bool(T_ISL, "island_chat", "Чат в острове", "Сообщения всплывают в острове", true);
    public static final Setting ISLAND_CHAT_TIME = num(T_ISL, "island_chat_time", "Время сообщения", "Сколько висит чат", 8, 2, 20, 1, " с");
    public static final Setting ISLAND_DAMAGE = bool(T_ISL, "island_damage", "Урон и низкое HP", "Показывает получение урона", true);
    public static final Setting ISLAND_IDLE = choice(T_ISL, "island_idle", "В покое показывает", "Что на острове без событий", 0,
            "FPS и пинг", "Только FPS", "Время", "Ник и HP", "Ничего");
    public static final Setting ISLAND_Y = num(T_ISL, "island_y", "Отступ сверху", "Положение острова", 6, 0, 60, 2, " px");
    public static final Setting ISLAND_W = num(T_ISL, "island_w", "Ширина в покое", "Размер маленького острова", 140, 80, 220, 10, " px");
    public static final Setting ISLAND_SPEED = num(T_ISL, "island_speed", "Скорость анимации", "Выше = быстрее", 6, 1, 12, 1, "");
    public static final Setting ISLAND_AVATAR = bool(T_ISL, "island_avatar", "Аватар в чате", "Цветной значок отправителя", true);
    public static final Setting ISLAND_EXPAND = bool(T_ISL, "island_expand", "Раскрытие по клавише", "Клавиша J раскрывает остров", true);

    // ===================== ПАНЕЛЬ =====================
    public static final Setting PANEL = bool(T_HUD, "panel", "Панель статистики", "Главный переключатель", true);
    public static final Setting SHOW_FPS = bool(T_HUD, "show_fps", "FPS", "Кадры в секунду", true);
    public static final Setting SHOW_PING = bool(T_HUD, "show_ping", "Пинг", "Задержка до сервера", true);
    public static final Setting SHOW_COORDS = bool(T_HUD, "show_coords", "Координаты", "XYZ", true);
    public static final Setting SHOW_DIR = bool(T_HUD, "show_dir", "Направление", "Куда вы смотрите", true);
    public static final Setting SHOW_SPEED = bool(T_HUD, "show_speed", "Скорость", "Блоков в секунду", false);
    public static final Setting SHOW_CLOCK = bool(T_HUD, "show_clock", "Время", "Реальное время", true);
    public static final Setting SHOW_MEM = bool(T_HUD, "show_mem", "Память", "Использование ОЗУ игрой", true);
    public static final Setting SHOW_CPS = bool(T_HUD, "show_cps", "CPS", "Клики в секунду", true);
    public static final Setting SHOW_HELD = bool(T_HUD, "show_held", "Предмет в руке", "Название и прочность", true);
    public static final Setting KEYS = bool(T_HUD, "keys", "Клавиши (WASD)", "Показ нажатых клавиш", true);
    public static final Setting PANEL_ANCHOR = choice(T_HUD, "panel_anchor", "Угол панели", "Где расположена панель", 0,
            "Слева сверху", "Справа сверху", "Слева снизу", "Справа снизу");
    public static final Setting KEYS_ANCHOR = choice(T_HUD, "keys_anchor", "Угол клавиш", "Где расположены клавиши", 3,
            "Слева сверху", "Справа сверху", "Слева снизу", "Справа снизу");
    public static final Setting PANEL_X = num(T_HUD, "panel_x", "Отступ по X", "От края экрана", 6, 0, 80, 2, " px");
    public static final Setting PANEL_Y = num(T_HUD, "panel_y", "Отступ по Y", "От края экрана", 6, 0, 80, 2, " px");

    // ===================== ОФОРМЛЕНИЕ =====================
    public static final Setting ACCENT = choice(T_LOOK, "accent", "Акцентный цвет", "Цвет линий и значков", 0,
            "Оранжевый", "Голубой", "Зелёный", "Розовый", "Фиолетовый", "Красный", "Жёлтый", "Белый", "Радуга");
    public static final Setting BG_ALPHA = num(T_LOOK, "bg_alpha", "Прозрачность фона", "Фон панелей", 70, 20, 95, 5, "%");
    public static final Setting RADIUS = num(T_LOOK, "radius", "Скругление углов", "Радиус панелей", 8, 0, 14, 2, " px");
    public static final Setting TEXT_SHADOW = bool(T_LOOK, "text_shadow", "Тень текста", "Лучше читается", true);
    public static final Setting HIGHLIGHT = bool(T_LOOK, "highlight", "Стеклянный блик", "Светлая линия сверху", true);
    public static final Setting ACCENT_BAR = bool(T_LOOK, "accent_bar", "Акцентная полоса", "Цветная полоса на панелях", true);
    public static final Setting ANIMS = bool(T_LOOK, "anims", "Анимации", "Плавное раскрытие острова", true);

    private static final int[] ACCENT_RGB = {
            0xFF7A1A, 0x3FB8FF, 0x3DDC84, 0xFF5FA2, 0xA66BFF, 0xFF4D4D, 0xFFD23F, 0xFFFFFF
    };

    private Cfg() {}

    public static int accent() {
        int idx = ACCENT.i;
        if (idx >= ACCENT_RGB.length) {
            float h = (System.currentTimeMillis() % 6000L) / 6000f;
            return Draw.hsv(h, 0.65f, 1f);
        }
        return 0xFF000000 | ACCENT_RGB[idx];
    }

    public static int bg() {
        int a = BG_ALPHA.i * 255 / 100;
        return (a << 24) | 0x14161C;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("turbine.properties");
    }

    public static void load() {
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file(), StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return;
        }
        for (Setting s : ALL) {
            String v = p.getProperty(s.id);
            if (v != null) s.deserialize(v);
        }
        version++;
    }

    public static void save() {
        Properties p = new Properties();
        for (Setting s : ALL) p.setProperty(s.id, s.serialize());
        try (Writer w = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) {
            p.store(w, "Turbine settings");
        } catch (IOException ignored) {
        }
    }
}
