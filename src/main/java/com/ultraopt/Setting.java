package com.ultraopt;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class Setting {
    public enum Type { BOOL, INT, CHOICE }

    public final Type type;
    public final int tab;
    public final String id, name, desc, suffix;
    public final int min, max, step, def;
    public final String[] choices;
    public boolean b;
    public int i;

    Setting(Type type, int tab, String id, String name, String desc, int def,
            int min, int max, int step, String suffix, String[] choices) {
        this.type = type;
        this.tab = tab;
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.def = def;
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
        this.choices = choices;
        setDefault();
    }

    private void setDefault() {
        if (type == Type.BOOL) b = def != 0;
        else i = def;
    }

    public void next() {
        switch (type) {
            case BOOL -> b = !b;
            case INT -> {
                i += step;
                if (i > max) i = min;
            }
            case CHOICE -> i = (i + 1) % choices.length;
        }
        Cfg.version++;
    }

    public void reset() {
        setDefault();
        Cfg.version++;
    }

    public Component display() {
        switch (type) {
            case BOOL:
                return Component.literal(b ? "ВКЛ" : "ВЫКЛ").withStyle(b ? ChatFormatting.GREEN : ChatFormatting.RED);
            case INT:
                return Component.literal(i + suffix);
            default:
                return Component.literal(choices[i]);
        }
    }

    public String serialize() {
        return type == Type.BOOL ? Boolean.toString(b) : Integer.toString(i);
    }

    public void deserialize(String v) {
        try {
            if (type == Type.BOOL) {
                b = Boolean.parseBoolean(v.trim());
            } else {
                int n = Integer.parseInt(v.trim());
                if (type == Type.INT) n = Math.max(min, Math.min(max, n));
                else n = Math.max(0, Math.min(choices.length - 1, n));
                i = n;
            }
        } catch (Exception ignored) {
        }
    }
}
