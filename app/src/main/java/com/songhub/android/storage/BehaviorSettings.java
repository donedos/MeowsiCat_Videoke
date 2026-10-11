package com.songhub.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes3.dex */
public final class BehaviorSettings {
    private static final String KEY_HIDE_CONTROLS_LANDSCAPE = "hide_controls_landscape";
    private static final String KEY_SHOW_DIALOG_LANDSCAPE = "show_dialog_landscape";
    private static final String KEY_SKIP_DISCLAIMER = "skip_disclaimer";
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

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, 0);
    }
}
