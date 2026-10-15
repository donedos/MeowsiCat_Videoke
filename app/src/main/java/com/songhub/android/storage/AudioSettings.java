package com.songhub.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes3.dex */
public class AudioSettings {
    public static final int CHORUS_DEFAULT = 4;
    public static final int CHORUS_HIGH = 3;
    public static final int CHORUS_LOW = 1;
    public static final int CHORUS_MEDIUM = 2;
    public static final int CHORUS_OFF = 0;
    public static final String KEY_BYPASS_MEWTWO = "audio_stream_sync_mode";
    public static final String KEY_CHORUS = "midi_chorus";
    public static final String KEY_EQ_GAINS = "eq_gains_10band";
    public static final String KEY_EXPERIMENTAL_CHORUS = "experimental_chorus_mp3";
    public static final String KEY_MELODY_CHANNEL = "melody_channel";
    public static final String KEY_WIPE_MELODY_CHANNEL = "wipe_melody_channel";
    public static final String KEY_MP3_VOLUME = "mp3_master_volume";
    public static final String KEY_MPX_MODE = "mpx_mode";
    public static final String KEY_POLYPHONY = "midi_polyphony";
    public static final String KEY_REVERB = "midi_reverb";
    public static final String KEY_SAMPLE_RATE = "audio_sample_rate";
    public static final String KEY_TYPING_VOLUME = "typing_sound_volume";
    public static final String KEY_VOCAL_ENABLED = "vocal_enabled";
    public static final String KEY_VOLUME = "master_volume";
    public static final int MPX_LEFT = 1;
    public static final int MPX_MIXED = 3;
    public static final int MPX_RIGHT = 2;
    public static final int MPX_STEREO = 0;
    private static final String PREF_NAME = "audio_settings";
    public static final int REVERB_DEFAULT = 4;
    public static final int REVERB_HIGH = 3;
    public static final int REVERB_LOW = 1;
    public static final int REVERB_MEDIUM = 2;
    public static final int REVERB_OFF = 0;
    public static final int VOLUME_HIGH = 3;
    public static final int VOLUME_LOW = 1;
    public static final int VOLUME_MEDIUM = 2;
    public static final int VOLUME_OFF = 0;
    public static final String[] MPX_NAMES = {"STEREO", "LEFT", "RIGHT", "MIXED"};
    public static final EqPreset[] PRESETS = {new EqPreset("Default", new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}), new EqPreset("Classic", new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.5f, -1.5f, -1.5f, -2.5f}), new EqPreset("Flat", new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}), new EqPreset("Rock", new float[]{4.5f, 3.5f, 1.5f, 0.0f, -1.5f, -1.5f, 0.0f, 1.5f, 3.5f, 4.5f}), new EqPreset("Pop", new float[]{-1.5f, -1.0f, 0.0f, 2.0f, 4.0f, 4.0f, 2.0f, 0.0f, -1.0f, -1.5f}), new EqPreset("Electric", new float[]{4.0f, 2.0f, 0.0f, -2.0f, -4.0f, 0.0f, 2.0f, 4.0f, 5.0f, 5.0f}), new EqPreset("Jazz", new float[]{3.0f, 2.0f, 0.0f, 1.5f, -1.5f, -1.5f, 0.0f, 1.0f, 2.0f, 3.0f}), new EqPreset("Bass High", new float[]{6.0f, 5.0f, 4.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}), new EqPreset("Bass Low", new float[]{-6.0f, -5.0f, -4.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}), new EqPreset("Treble High", new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f, 6.0f}), new EqPreset("Treble Low", new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -2.0f, -4.0f, -5.0f, -6.0f}), new EqPreset("Music", new float[]{3.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 3.0f}), new EqPreset("Movie", new float[]{3.0f, 2.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 2.0f, 3.0f, 4.0f})};
    public static final float[] EQ_FREQUENCIES = {31.25f, 62.5f, 125.0f, 250.0f, 500.0f, 1000.0f, 2000.0f, 4000.0f, 8000.0f, 16000.0f};

    public static class EqPreset {
        public final float[] gains;
        public final String name;

        public EqPreset(String str, float[] fArr) {
            this.name = str;
            this.gains = fArr;
        }
    }

    public static int getMidiMasterVolume(Context context) {
        return getPrefs(context).getInt(KEY_VOLUME, 100);
    }

    public static void setMidiMasterVolume(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_VOLUME, i).apply();
    }

    public static int getMp3MasterVolume(Context context) {
        return getPrefs(context).getInt(KEY_MP3_VOLUME, 100);
    }

    public static void setMp3MasterVolume(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_MP3_VOLUME, i).apply();
    }

    public static int getReverb(Context context) {
        return getPrefs(context).getInt(KEY_REVERB, 4);
    }

    public static void setReverb(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_REVERB, i).apply();
    }

    public static int getChorus(Context context) {
        return getPrefs(context).getInt(KEY_CHORUS, 4);
    }

    public static void setChorus(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_CHORUS, i).apply();
    }

    public static int getPolyphony(Context context) {
        return getPrefs(context).getInt(KEY_POLYPHONY, 512);
    }

    public static void setPolyphony(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_POLYPHONY, i).apply();
    }

    public static int getMelodyChannel(Context context) {
        return getPrefs(context).getInt(KEY_MELODY_CHANNEL, 1);
    }

    public static void setMelodyChannel(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_MELODY_CHANNEL, i).apply();
    }

    /**
     * MIDI channel (1-16) that drives the karaoke lyric wipe, or 0 for automatic detection.
     */
    public static int getWipeMelodyChannel(Context context) {
        return getPrefs(context).getInt(KEY_WIPE_MELODY_CHANNEL, 0);
    }

    public static void setWipeMelodyChannel(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_WIPE_MELODY_CHANNEL, i).apply();
    }

    public static boolean isExperimentalChorusEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_EXPERIMENTAL_CHORUS, true);
    }

    public static void setExperimentalChorusEnabled(Context context, boolean z) {
        getPrefs(context).edit().putBoolean(KEY_EXPERIMENTAL_CHORUS, z).apply();
    }

    public static int getSampleRate(Context context) {
        return getPrefs(context).getInt(KEY_SAMPLE_RATE, 44100);
    }

    public static void setSampleRate(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_SAMPLE_RATE, i).apply();
    }

    public static float[] getEqualizerGains(Context context) {
        String string = getPrefs(context).getString(KEY_EQ_GAINS, null);
        float[] fArr = new float[10];
        if (string == null) {
            return fArr;
        }
        String[] strArrSplit = string.split(",");
        for (int i = 0; i < Math.min(strArrSplit.length, 10); i++) {
            try {
                fArr[i] = Float.parseFloat(strArrSplit[i]);
            } catch (NumberFormatException unused) {
                fArr[i] = 0.0f;
            }
        }
        return fArr;
    }

    public static void setEqualizerGains(Context context, float[] fArr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fArr.length; i++) {
            sb.append(fArr[i]);
            if (i < fArr.length - 1) {
                sb.append(",");
            }
        }
        getPrefs(context).edit().putString(KEY_EQ_GAINS, sb.toString()).apply();
    }

    public static int getTypingVolumeLevel(Context context) {
        return getPrefs(context).getInt(KEY_TYPING_VOLUME, 2);
    }

    public static void setTypingVolumeLevel(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_TYPING_VOLUME, i).apply();
    }

    public static float getTypingVolumeFactor(Context context) {
        int typingVolumeLevel = getTypingVolumeLevel(context);
        if (typingVolumeLevel == 0) {
            return 0.0f;
        }
        if (typingVolumeLevel != 1) {
            return typingVolumeLevel != 3 ? 0.6f : 1.0f;
        }
        return 0.3f;
    }

    public static int getMpxMode(Context context) {
        return getPrefs(context).getInt(KEY_MPX_MODE, 0);
    }

    public static void setMpxMode(Context context, int i) {
        getPrefs(context).edit().putInt(KEY_MPX_MODE, ((i % 4) + 4) % 4).apply();
    }

    public static boolean isVocalEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_VOCAL_ENABLED, true);
    }

    public static void setVocalEnabled(Context context, boolean z) {
        getPrefs(context).edit().putBoolean(KEY_VOCAL_ENABLED, z).apply();
    }

    public static boolean isBypassMewTwo(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs.contains(KEY_BYPASS_MEWTWO)) {
            try {
                try {
                    return prefs.getInt(KEY_BYPASS_MEWTWO, 0) == 849201;
                } catch (Exception unused) {
                    return false;
                }
            } catch (ClassCastException unused2) {
                return prefs.getBoolean(KEY_BYPASS_MEWTWO, false);
            }
        }
        if (prefs.contains("bypass_mewtwo")) {
            return prefs.getBoolean("bypass_mewtwo", false);
        }
        return false;
    }

    public static void setBypassMewTwo(Context context, boolean z) {
        int i = z ? 849201 : 0;
        SharedPreferences.Editor editorEdit = getPrefs(context).edit();
        editorEdit.putInt(KEY_BYPASS_MEWTWO, i);
        editorEdit.remove("bypass_mewtwo");
        editorEdit.apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, 0);
    }
}
