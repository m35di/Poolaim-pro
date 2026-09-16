package com.onyx.poolaim;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {
    private static final String PREF = "poolaim_prefs";

    public static SharedPreferences get(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static int getColor(Context ctx) {
        return get(ctx).getInt("color", 0);
    }

    public static void setColor(Context ctx, int c) {
        get(ctx).edit().putInt("color", c).apply();
    }

    public static int getThickness(Context ctx) {
        return get(ctx).getInt("thickness", 0);
    }

    public static void setThickness(Context ctx, int t) {
        get(ctx).edit().putInt("thickness", t).apply();
    }

    public static int getMode(Context ctx) {
        return get(ctx).getInt("mode", 0);
    }

    public static void setMode(Context ctx, int m) {
        get(ctx).edit().putInt("mode", m).apply();
    }
}
