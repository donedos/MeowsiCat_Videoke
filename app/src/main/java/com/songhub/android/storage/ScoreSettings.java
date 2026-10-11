package com.songhub.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes3.dex */
public final class ScoreSettings {
    public static final int FANFARE_HIGH = 3;
    public static final int FANFARE_LOW = 1;
    public static final int FANFARE_MID = 2;
    public static final int FANFARE_OFF = 0;
    private static final String KEY_DIFFICULTY = "score_difficulty";
    private static final String KEY_FANFARE_LEVEL = "score_fanfare_level";
    private static final String KEY_MODE = "score_mode";
    private static final String PREF_NAME = "score_settings";

    public enum ScoreMode {
        OFF,
        RANDOM,
        MICROPHONE;

        public static ScoreMode fromInt(int i) {
            return (i < 0 || i >= values().length) ? OFF : values()[i];
        }
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD;

        public static Difficulty fromInt(int i) {
            return (i < 0 || i >= values().length) ? EASY : values()[i];
        }
    }

    public static void setScoreMode(Context context, ScoreMode scoreMode) {
        prefs(context).edit().putInt(KEY_MODE, scoreMode.ordinal()).apply();
    }

    public static ScoreMode getScoreMode(Context context) {
        return ScoreMode.fromInt(prefs(context).getInt(KEY_MODE, ScoreMode.RANDOM.ordinal()));
    }

    public static void setDifficulty(Context context, Difficulty difficulty) {
        prefs(context).edit().putInt(KEY_DIFFICULTY, difficulty.ordinal()).apply();
    }

    public static Difficulty getDifficulty(Context context) {
        return Difficulty.fromInt(prefs(context).getInt(KEY_DIFFICULTY, Difficulty.MEDIUM.ordinal()));
    }

    public static void setFanfareLevel(Context context, int i) {
        prefs(context).edit().putInt(KEY_FANFARE_LEVEL, i).apply();
    }

    public static int getFanfareLevel(Context context) {
        if (!prefs(context).contains(KEY_FANFARE_LEVEL) && prefs(context).contains("score_fanfare")) {
            int i = prefs(context).getBoolean("score_fanfare", true) ? 2 : 0;
            setFanfareLevel(context, i);
            return i;
        }
        return prefs(context).getInt(KEY_FANFARE_LEVEL, 2);
    }

    public static boolean isFanfareEnabled(Context context) {
        return getFanfareLevel(context) > 0;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, 0);
    }
}
