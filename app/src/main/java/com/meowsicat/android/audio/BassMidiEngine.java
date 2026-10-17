package com.meowsicat.android.audio;

import android.content.Context;
import android.util.Log;
import com.meowsicat.android.storage.AudioSettings;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSMIDI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BassMidiEngine {
    private static final String TAG = "BassMidiEngine";
    private static final int MIDI_FLAGS =
            BASS.BASS_SAMPLE_FLOAT | BASSMIDI.BASS_MIDI_DECAYEND | BASSMIDI.BASS_MIDI_SINCINTER;
    private static boolean isBassInitialized = false;

    private final Context context;
    private int midiStream = 0;
    private int fontHandle = 0;
    private boolean hasStarted = false;

    private int currentKeyShift = 0;
    private float currentTempoPercent = 100.0f;
    private int currentMelodyVolume = 0;

    public BassMidiEngine(Context context) {
        this.context = context.getApplicationContext();
        if (BassMidiEngine.isBassInitialized) {
            BASS.BASS_Start();
            return;
        }
        int errorCode;
        if (BASS.BASS_Init(-1, AudioSettings.getSampleRate(context), 0)
                || (errorCode = BASS.BASS_ErrorGetCode()) == BASS.BASS_ERROR_ALREADY) {
            BassMidiEngine.isBassInitialized = true;
        } else {
            Log.e(TAG, "BASS_Init failed: " + errorCode);
        }
        BASS.BASS_PluginLoad("libbassmidi.so", 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_BUFFER, 200);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_ANDROID_AAUDIO, 1);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATEPERIOD, 30);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATETHREADS, 1);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOAT, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_REC_BUFFER, 20);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_DEV_BUFFER, 20);
        BASS.BASS_SetConfig(BASSMIDI.BASS_CONFIG_MIDI_VOICES, 256);
        BASS.BASS_SetConfig(BASSMIDI.BASS_CONFIG_MIDI_AUTOFONT, 1);
        BASS.BASS_Start();
    }

    public static boolean isInitialized() {
        return BassMidiEngine.isBassInitialized || BASS.BASS_GetCPU() >= 0.0f;
    }

    private void applyMapping() {
        if (this.fontHandle == 0 || this.midiStream == 0) {
            return;
        }
        BASSMIDI.BASS_MIDI_FONT[] fonts = new BASSMIDI.BASS_MIDI_FONT[2];
        fonts[0] = new BASSMIDI.BASS_MIDI_FONT();
        fonts[0].font = this.fontHandle;
        fonts[0].preset = -1;
        fonts[0].bank = 0;
        fonts[1] = new BASSMIDI.BASS_MIDI_FONT();
        fonts[1].font = this.fontHandle;
        fonts[1].preset = -1;
        fonts[1].bank = 128;
        BASSMIDI.BASS_MIDI_StreamSetFonts(this.midiStream, fonts, 2);
        BASSMIDI.BASS_MIDI_StreamSetFonts(0, fonts, 2);
    }

    private void applySettings() {
        if (this.midiStream == 0) {
            return;
        }
        setKey(this.currentKeyShift);
        setTempo(this.currentTempoPercent);
        setMelodyVolume(this.currentMelodyVolume);
    }

    public void setKey(int semitones) {
        this.currentKeyShift = semitones;
        if (this.midiStream != 0) {
            // In BASSMIDI, 100 is 0 semitones (concert pitch): 0=-100, 100=normal, 200=+100.
            int param = Math.max(-24, Math.min(24, semitones)) + 100;
            // MIDI_EVENT_TRANSPOSE is a per-channel event, so it must be applied to every
            // MIDI channel; otherwise only the channel passed here (the melody) changes key.
            int channels = 16;
            BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
            if (BASS.BASS_ChannelGetInfo(this.midiStream, info) && info.chans > 0) {
                channels = Math.max(info.chans, 16);
            }
            for (int chan = 0; chan < channels; chan++) {
                BASSMIDI.BASS_MIDI_StreamEvent(this.midiStream, chan, BASSMIDI.MIDI_EVENT_TRANSPOSE, param);
            }
        }
    }

    public int getKey() {
        return this.currentKeyShift;
    }

    public void setTempo(float percent) {
        this.currentTempoPercent = percent;
        if (this.midiStream != 0) {
            // BASS_ATTRIB_MIDI_SPEED uses a ratio where 1.0f = 100% normal speed.
            float speedRatio = percent / 100.0f;
            float speed = Math.max(0.25f, Math.min(2.0f, speedRatio));
            BASS.BASS_ChannelSetAttribute(this.midiStream, BASSMIDI.BASS_ATTRIB_MIDI_SPEED, speed);
        }
    }

    public float getTempo() {
        return this.currentTempoPercent;
    }

    public void setMelodyVolume(int volumePercent) {
        this.currentMelodyVolume = volumePercent;
        if (this.midiStream != 0) {
            int targetVol = Math.max(0, Math.min(100, volumePercent));
            int midiVol = Math.max(0, Math.min(127, (volumePercent * 127) / 100));

            // Target ONLY the guide melody channel (GM Channel 4 = 0-based index 3, avoiding bass channel 2 and drum channel 10)
            int configuredChan = AudioSettings.getMelodyChannel(this.context);
            int melodyChan = 3; // Default to GM Channel 4 (index 3)
            if (configuredChan > 0 && configuredChan <= 16 && configuredChan != 2 && configuredChan != 10) {
                melodyChan = configuredChan - 1;
            }

            BASSMIDI.BASS_MIDI_StreamEvent(this.midiStream, melodyChan, BASSMIDI.MIDI_EVENT_MIXLEVEL, targetVol);
            BASSMIDI.BASS_MIDI_StreamEvent(this.midiStream, melodyChan, BASSMIDI.MIDI_EVENT_VOLUME, midiVol);
        }
    }

    public int getMelodyVolume() {
        return this.currentMelodyVolume;
    }

    public boolean loadMidi(String path, long offset, long length) {
        unloadStream();
        int stream = BASSMIDI.BASS_MIDI_StreamCreateFile(
                path, offset, length, MIDI_FLAGS, AudioSettings.getSampleRate(this.context));
        this.midiStream = stream;
        this.hasStarted = false;
        if (stream == 0) {
            Log.e(TAG, "MIDI load failed code: " + BASS.BASS_ErrorGetCode());
            return false;
        }
        BASS.BASS_ChannelSetAttribute(stream, BASSMIDI.BASS_ATTRIB_MIDI_CHANS, 128.0f);
        applyMapping();
        BASSMIDI.BASS_MIDI_StreamEvent(stream, 0, BASSMIDI.MIDI_EVENT_SYSTEM, BASSMIDI.MIDI_SYSTEM_GS);
        applySettings();
        return true;
    }

    public boolean loadMidi(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        File file = new File(this.context.getCacheDir(), "current_song.mid");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(data);
            } finally {
                fileOutputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write MIDI cache file", e);
            return false;
        }
        return loadMidi(file.getAbsolutePath(), 0L, 0L);
    }

    public void loadSoundFont(String path) {
        if (path == null) {
            return;
        }
        if (this.fontHandle != 0) {
            BASSMIDI.BASS_MIDI_FontFree(this.fontHandle);
            this.fontHandle = 0;
        }
        this.fontHandle = BASSMIDI.BASS_MIDI_FontInit(path, BASSMIDI.BASS_MIDI_FONT_MMAP);
        if (this.fontHandle == 0) {
            Log.e(TAG, "SoundFont load failed code: " + BASS.BASS_ErrorGetCode() + " path: " + path);
        }
        applyMapping();
    }

    public void play() {
        if (this.midiStream == 0) {
            return;
        }
        BASS.BASS_ChannelPlay(this.midiStream, false);
        this.hasStarted = true;
    }

    public void pause() {
        if (this.midiStream != 0) {
            BASS.BASS_ChannelPause(this.midiStream);
        }
    }

    public void stop() {
        if (this.midiStream != 0) {
            BASS.BASS_ChannelStop(this.midiStream);
        }
    }

    public void restart() {
        if (this.midiStream != 0) {
            BASS.BASS_ChannelSetPosition(this.midiStream, 0L, BASS.BASS_POS_BYTE);
            this.hasStarted = false;
        }
    }

    public void seekMs(double ms) {
        if (this.midiStream == 0) {
            return;
        }
        long bytes = BASS.BASS_ChannelSeconds2Bytes(this.midiStream, ms / 1000.0d);
        BASS.BASS_ChannelSetPosition(this.midiStream, bytes, BASS.BASS_POS_BYTE);
    }

    public double getPositionMs() {
        if (this.midiStream == 0) {
            return 0.0d;
        }
        long position = BASS.BASS_ChannelGetPosition(this.midiStream, BASS.BASS_POS_BYTE);
        if (position < 0) {
            return 0.0d;
        }
        return BASS.BASS_ChannelBytes2Seconds(this.midiStream, position) * 1000.0d;
    }

    public double getDurationMs() {
        if (this.midiStream == 0) {
            return 0.0d;
        }
        long length = BASS.BASS_ChannelGetLength(this.midiStream, BASS.BASS_POS_BYTE);
        if (length <= 0) {
            return 0.0d;
        }
        double seconds = BASS.BASS_ChannelBytes2Seconds(this.midiStream, length);
        return seconds > 0.0d ? seconds * 1000.0d : 0.0d;
    }

    public long getTickPosition() {
        if (this.midiStream == 0) {
            return 0L;
        }
        return BASS.BASS_ChannelGetPosition(this.midiStream, BASSMIDI.BASS_POS_MIDI_TICK);
    }

    public boolean isLoaded() {
        return this.midiStream != 0;
    }

    public boolean isPlaying() {
        return this.midiStream != 0
                && BASS.BASS_ChannelIsActive(this.midiStream) != BASS.BASS_ACTIVE_STOPPED
                && BASS.BASS_ChannelIsActive(this.midiStream) != BASS.BASS_ACTIVE_PAUSED;
    }

    public boolean isPaused() {
        return this.midiStream != 0
                && BASS.BASS_ChannelIsActive(this.midiStream) == BASS.BASS_ACTIVE_PAUSED;
    }

    public boolean isFinished() {
        return this.midiStream == 0
                || BASS.BASS_ChannelIsActive(this.midiStream) == BASS.BASS_ACTIVE_STOPPED;
    }

    public boolean hasEnded() {
        return this.hasStarted
                && this.midiStream != 0
                && BASS.BASS_ChannelIsActive(this.midiStream) == BASS.BASS_ACTIVE_STOPPED;
    }

    public void unloadMidi() {
        unloadStream();
        deleteCacheFile();
    }

    private void unloadStream() {
        if (this.midiStream != 0) {
            BASS.BASS_StreamFree(this.midiStream);
            this.midiStream = 0;
        }
    }

    private void deleteCacheFile() {
        File file = new File(this.context.getCacheDir(), "current_song.mid");
        if (file.exists()) {
            file.delete();
        }
    }

    public void release() {
        unloadMidi();
        if (this.fontHandle != 0) {
            BASSMIDI.BASS_MIDI_FontFree(this.fontHandle);
            this.fontHandle = 0;
        }
    }
}