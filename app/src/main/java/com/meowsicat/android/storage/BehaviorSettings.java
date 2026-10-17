package com.meowsicat.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

public final class BehaviorSettings {
    private static final String KEY_HIDE_CONTROLS_LANDSCAPE = "hide_controls_landscape";
    private static final String KEY_SHOW_DIALOG_LANDSCAPE = "show_dialog_landscape";
    private static final String KEY_SKIP_DISCLAIMER = "skip_disclaimer";
    private static final String KEY_FLOATING_CAT_ENABLED = "floating_cat_enabled";
    private static final String KEY_REPEAT_MODE = "repeat_mode";
    private static final String KEY_SHUFFLE_ENABLED = "shuffle_enabled";
    private static final String KEY_WEB_PORT = "web_remote_port";
    private static final String PREF_NAME = "behavior_settings";

    public static void setShowDialogLandscape(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_SHOW_DIALOG_LANDSCAPE, z).apply();
    }

    public static boolean getShowDialogLandscape(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_DIALOG_LANDSCAPE, true);
    }

    public static void setHideControlsLandscape(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_HIDE_CONTROLS_LANDSCAPE, z).apply();
    }

    public static boolean getHideControlsLandscape(Context context) {
        return prefs(context).getBoolean(KEY_HIDE_CONTROLS_LANDSCAPE, false);
    }

    public static void setSkipDisclaimer(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_SKIP_DISCLAIMER, z).apply();
    }

    public static boolean getSkipDisclaimer(Context context) {
        return prefs(context).getBoolean(KEY_SKIP_DISCLAIMER, false);
    }

    public static void setFloatingCatEnabled(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_FLOATING_CAT_ENABLED, z).apply();
    }

    public static boolean isFloatingCatEnabled(Context context) {
        return prefs(context).getBoolean(KEY_FLOATING_CAT_ENABLED, true);
    }

    public static void setRepeatMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_REPEAT_MODE, mode).apply();
    }

    public static int getRepeatMode(Context context) {
        return prefs(context).getInt(KEY_REPEAT_MODE, 0);
    }

    public static void setShuffleEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_SHUFFLE_ENABLED, enabled).apply();
    }

    public static boolean isShuffleEnabled(Context context) {
        return prefs(context).getBoolean(KEY_SHUFFLE_ENABLED, false);
    }

    public static void setWebPort(Context context, int port) {
        prefs(context).edit().putInt(KEY_WEB_PORT, port).apply();
    }

    public static int getWebPort(Context context) {
        return prefs(context).getInt(KEY_WEB_PORT, 5678);
    }

    private static final String KEY_APP_THEME = "app_theme";
    public static final int THEME_DEFAULT = 0;
    public static final int THEME_IOS_BLUE = 1;
    public static final int THEME_IOS_PURPLE = 2;
    public static final int THEME_IOS_INDIGO = 3;
    public static final int THEME_IOS_PINK = 4;
    public static final int THEME_IOS_ORANGE = 5;
    public static final int THEME_IOS_GREEN = 6;

    public static void setAppTheme(Context context, int theme) {
        prefs(context).edit().putInt(KEY_APP_THEME, theme).apply();
    }

    public static int getAppTheme(Context context) {
        return prefs(context).getInt(KEY_APP_THEME, THEME_DEFAULT);
    }

    private static final String KEY_WELCOME_SHOWN = "welcome_shown";

    public static void setWelcomeShown(Context context, boolean shown) {
        prefs(context).edit().putBoolean(KEY_WELCOME_SHOWN, shown).apply();
    }

    public static boolean isWelcomeShown(Context context) {
        return prefs(context).getBoolean(KEY_WELCOME_SHOWN, false);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, 0);
    }
}
