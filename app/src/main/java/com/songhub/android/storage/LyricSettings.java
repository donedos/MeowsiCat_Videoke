package com.songhub.android.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/* JADX INFO: loaded from: classes3.dex */
public final class LyricSettings {
    public static final int ALIGN_CENTER = 0;
    public static final int ALIGN_SPLIT = 1;
    public static final int ALIGN_SPLIT_CLOSE = 2;
    public static final int ANIM_CROSSFADE = 3;
    public static final int ANIM_FADE = 1;
    public static final int ANIM_FLIP = 4;
    public static final int ANIM_NONE = 0;
    public static final int ANIM_POP_BOUNCE = 5;
    public static final int ANIM_WIPE = 2;
    public static final int COUNTDOWN_CUSTOM_IMAGE_INDIVIDUAL = 6;
    public static final int COUNTDOWN_CUSTOM_IMAGE_STACKED = 5;
    public static final int COUNTDOWN_IMAGE_INDIVIDUAL = 4;
    public static final int COUNTDOWN_IMAGE_STACKED = 3;
    public static final int COUNTDOWN_STAR_STACKED = 2;
    public static final int COUNTDOWN_TEXT_ANIM = 0;
    public static final int COUNTDOWN_TEXT_STATIC = 1;
    public static final float DEFAULT_FONT_SIZE_LANDSCAPE = 63.0f;
    public static final float DEFAULT_FONT_SIZE_PORTRAIT = 42.0f;
    public static final float DEFAULT_OUTLINE = 6.0f;
    public static final int FONT_ARIAL = 4;
    public static final int FONT_BASIC = 16;
    public static final int FONT_CALIBRI = 6;
    public static final int FONT_COMIC_NEUE = 9;
    public static final int FONT_CUSTOM = 19;
    public static final int FONT_DEFAULT = 0;
    public static final int FONT_DYNA_PUFF = 14;
    public static final int FONT_FREDOKA = 10;
    public static final int FONT_GENSHIN = 11;
    public static final int FONT_MERIENDA = 15;
    public static final int FONT_NOTO_CJK = 2;
    public static final int FONT_RADIO_CANADA = 13;
    public static final int FONT_ROBOTO = 1;
    public static final int FONT_SAN_FRANCISCO = 7;
    public static final int FONT_SERIF = 3;
    public static final int FONT_TAHOMA = 5;
    public static final int FONT_THE_ALIEN = 12;
    public static final int FONT_THE_CUTE_RABBIT = 17;
    public static final int FONT_THE_NORMAL = 18;
    public static final int FONT_WHITE_CHALK = 8;
    private static final String KEY_ALIGNMENT = "lyric_alignment";
    private static final String KEY_COUNTDOWN_IMAGE_SIZE = "countdown_image_size";
    private static final String KEY_COUNTDOWN_IMAGE_SIZE_LANDSCAPE = "countdown_image_size_landscape";
    private static final String KEY_COUNTDOWN_IMAGE_SIZE_PORTRAIT = "countdown_image_size_portrait";
    private static final String KEY_COUNTDOWN_STYLE = "countdown_style";
    private static final String KEY_CUSTOM_FONT_PATH = "lyric_custom_font_path";
    private static final String KEY_FONT_SIZE_LANDSCAPE = "lyric_font_size_landscape";
    private static final String KEY_FONT_SIZE_PORTRAIT = "lyric_font_size_portrait";
    private static final String KEY_FONT_TYPE = "lyric_font_type";
    private static final String KEY_FOUR_LINE_PORTRAIT = "lyric_four_line_portrait";
    private static final String KEY_HIGHLIGHT_COLOR = "lyric_highlight_color";
    private static final String KEY_LYRIC_ANIMATION = "lyric_animation";
    public static final int STYLE_MODERN = 0;
    public static final int STYLE_KARAOKE = 1;
    private static final String KEY_LYRIC_STYLE = "lyric_style";
    private static final String KEY_OUTLINE_THICKNESS = "lyric_outline_thickness";
    private static final String KEY_RANDOMIZE_INTRO_STYLE = "lyric_randomize_intro_style";
    private static final String KEY_SHOW_SHADOW = "lyric_show_shadow";
    private static final String KEY_USE_RANDOM_COLOR = "lyric_use_random_color";
    private static final String PREF_NAME = "lyric_settings";
    public static final int[] PRESET_COLORS = {Color.parseColor("#0000FF"), Color.parseColor("#FF0000"), Color.parseColor("#4CAF50"), Color.parseColor("#FDD835"), Color.parseColor("#00BCD4"), Color.parseColor("#FF00FF"), Color.parseColor("#FF5722"), Color.parseColor("#795548"), Color.parseColor("#607D8B"), Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"), Color.parseColor("#3F51B5"), Color.parseColor("#FFC107"), Color.parseColor("#8BC34A"), Color.parseColor("#4682B4"), Color.parseColor("#D2691E"), Color.parseColor("#008080"), Color.parseColor("#FF4500"), Color.parseColor("#2E8B57"), Color.parseColor("#8A2BE2")};
    public static final int DEFAULT_HIGHLIGHT = Color.parseColor("#4F46E5");

    public static void setAlignment(Context context, int i) {
        prefs(context).edit().putInt(KEY_ALIGNMENT, i).apply();
    }

    public static int getAlignment(Context context) {
        return prefs(context).getInt(KEY_ALIGNMENT, 0);
    }

    public static void setHighlightColor(Context context, int i) {
        prefs(context).edit().putInt(KEY_HIGHLIGHT_COLOR, i).apply();
    }

    public static int getHighlightColor(Context context) {
        return prefs(context).getInt(KEY_HIGHLIGHT_COLOR, DEFAULT_HIGHLIGHT);
    }

    public static void setUseRandomColor(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_USE_RANDOM_COLOR, z).apply();
    }

    public static boolean getUseRandomColor(Context context) {
        return prefs(context).getBoolean(KEY_USE_RANDOM_COLOR, false);
    }

    public static void setOutlineThickness(Context context, float f) {
        prefs(context).edit().putFloat(KEY_OUTLINE_THICKNESS, f).apply();
    }

    public static float getOutlineThickness(Context context) {
        return prefs(context).getFloat(KEY_OUTLINE_THICKNESS, 6.0f);
    }

    public static void setShowShadow(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_SHOW_SHADOW, z).apply();
    }

    public static boolean getShowShadow(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_SHADOW, true);
    }

    public static void setFontType(Context context, int i) {
        prefs(context).edit().putInt(KEY_FONT_TYPE, i).apply();
    }

    public static int getFontType(Context context) {
        return prefs(context).getInt(KEY_FONT_TYPE, 13);
    }

    public static void setCustomFontPath(Context context, String str) {
        prefs(context).edit().putString(KEY_CUSTOM_FONT_PATH, str).apply();
    }

    public static String getCustomFontPath(Context context) {
        return prefs(context).getString(KEY_CUSTOM_FONT_PATH, null);
    }

    public static void setFontSizePortrait(Context context, float f) {
        prefs(context).edit().putFloat(KEY_FONT_SIZE_PORTRAIT, f).apply();
    }

    public static float getFontSizePortrait(Context context) {
        return prefs(context).getFloat(KEY_FONT_SIZE_PORTRAIT, 42.0f);
    }

    public static void setFontSizeLandscape(Context context, float f) {
        prefs(context).edit().putFloat(KEY_FONT_SIZE_LANDSCAPE, f).apply();
    }

    public static float getFontSizeLandscape(Context context) {
        return prefs(context).getFloat(KEY_FONT_SIZE_LANDSCAPE, 63.0f);
    }

    public static void setFourLinePortrait(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_FOUR_LINE_PORTRAIT, z).apply();
    }

    public static boolean getFourLinePortrait(Context context) {
        return prefs(context).getBoolean(KEY_FOUR_LINE_PORTRAIT, true);
    }

    public static void setRandomizeIntroStyle(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_RANDOMIZE_INTRO_STYLE, z).apply();
    }

    public static boolean getRandomizeIntroStyle(Context context) {
        return prefs(context).getBoolean(KEY_RANDOMIZE_INTRO_STYLE, false);
    }

    public static void setLyricAnimation(Context context, int i) {
        prefs(context).edit().putInt(KEY_LYRIC_ANIMATION, i).apply();
    }

    public static int getLyricAnimation(Context context) {
        return prefs(context).getInt(KEY_LYRIC_ANIMATION, 0);
    }

    public static void setLyricStyle(Context context, int i) {
        prefs(context).edit().putInt(KEY_LYRIC_STYLE, i).apply();
    }

    public static int getLyricStyle(Context context) {
        return prefs(context).getInt(KEY_LYRIC_STYLE, STYLE_MODERN);
    }

    public static void setCountdownStyle(Context context, int i) {
        prefs(context).edit().putInt(KEY_COUNTDOWN_STYLE, i).apply();
    }

    public static int getCountdownStyle(Context context) {
        return prefs(context).getInt(KEY_COUNTDOWN_STYLE, 0);
    }

    public static void setCountDownImageSizePortrait(Context context, float f) {
        prefs(context).edit().putFloat(KEY_COUNTDOWN_IMAGE_SIZE_PORTRAIT, f).apply();
    }

    public static float getCountDownImageSizePortrait(Context context) {
        return prefs(context).getFloat(KEY_COUNTDOWN_IMAGE_SIZE_PORTRAIT, prefs(context).getFloat(KEY_COUNTDOWN_IMAGE_SIZE, 2.2f));
    }

    public static void setCountDownImageSizeLandscape(Context context, float f) {
        prefs(context).edit().putFloat(KEY_COUNTDOWN_IMAGE_SIZE_LANDSCAPE, f).apply();
    }

    public static float getCountDownImageSizeLandscape(Context context) {
        return prefs(context).getFloat(KEY_COUNTDOWN_IMAGE_SIZE_LANDSCAPE, prefs(context).getFloat(KEY_COUNTDOWN_IMAGE_SIZE, 2.2f));
    }

    public static void setCountDownImageSize(Context context, float f) {
        setCountDownImageSizePortrait(context, f);
        setCountDownImageSizeLandscape(context, f);
        prefs(context).edit().putFloat(KEY_COUNTDOWN_IMAGE_SIZE, f).apply();
    }

    public static float getCountDownImageSize(Context context) {
        return context.getResources().getConfiguration().orientation == 2 ? getCountDownImageSizeLandscape(context) : getCountDownImageSizePortrait(context);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, 0);
    }
}
