package com.un4seen.bass;

import android.os.ParcelFileDescriptor;
import java.nio.ByteBuffer;

public class BASSMIDI {
    public static final int BASS_ATTRIB_MIDI_CHANS = 73730;
    public static final int BASS_ATTRIB_MIDI_CPU = 73729;
    public static final int BASS_ATTRIB_MIDI_EXCKEYS = 73745;
    public static final int BASS_ATTRIB_MIDI_KILL = 73735;
    public static final int BASS_ATTRIB_MIDI_PPQN = 73728;
    public static final int BASS_ATTRIB_MIDI_QUEUED_ASYNC = 73744;
    public static final int BASS_ATTRIB_MIDI_QUEUED_BYTE = 73743;
    public static final int BASS_ATTRIB_MIDI_QUEUED_TICK = 73742;
    public static final int BASS_ATTRIB_MIDI_QUEUE_ASYNC = 73741;
    public static final int BASS_ATTRIB_MIDI_QUEUE_BYTE = 73740;
    public static final int BASS_ATTRIB_MIDI_QUEUE_TICK = 73739;
    public static final int BASS_ATTRIB_MIDI_REVERB = 73737;
    public static final int BASS_ATTRIB_MIDI_SPEED = 73736;
    public static final int BASS_ATTRIB_MIDI_SRC = 73734;
    public static final int BASS_ATTRIB_MIDI_STATE = 73733;
    public static final int BASS_ATTRIB_MIDI_TRACK_VOL = 73984;
    public static final int BASS_ATTRIB_MIDI_VOICES = 73731;
    public static final int BASS_ATTRIB_MIDI_VOICES_ACTIVE = 73732;
    public static final int BASS_ATTRIB_MIDI_VOL = 73738;
    public static final int BASS_CONFIG_MIDI_AUTOFONT = 66562;
    public static final int BASS_CONFIG_MIDI_COMPACT = 66560;
    public static final int BASS_CONFIG_MIDI_DEFFONT = 66563;
    public static final int BASS_CONFIG_MIDI_SAMPLELOADING = 66570;
    public static final int BASS_CONFIG_MIDI_SAMPLEMEM = 66567;
    public static final int BASS_CONFIG_MIDI_SAMPLEREAD = 66568;
    public static final int BASS_CONFIG_MIDI_SAMPLETHREADS = 66566;
    public static final int BASS_CONFIG_MIDI_SFZHEAD = 66569;
    public static final int BASS_CONFIG_MIDI_VOICES = 66561;
    public static final int BASS_CTYPE_STREAM_MIDI = 68864;
    public static final int BASS_ERROR_MIDI_INCLUDE = 7000;
    public static final int BASS_MIDI_ASYNC = 4194304;
    public static final int BASS_MIDI_CHAN_CHORUS = -1;
    public static final int BASS_MIDI_CHAN_REVERB = -2;
    public static final int BASS_MIDI_CHAN_USERFX = -3;
    public static final int BASS_MIDI_DECAYEND = 4096;
    public static final int BASS_MIDI_DECAYSEEK = 16384;
    public static final int BASS_MIDI_EVENTS_ABSTIME = 268435456;
    public static final int BASS_MIDI_EVENTS_ASYNC = 536870912;
    public static final int BASS_MIDI_EVENTS_CANCEL = 67108864;
    public static final int BASS_MIDI_EVENTS_FILTER = 1073741824;
    public static final int BASS_MIDI_EVENTS_FLUSH = Integer.MIN_VALUE;
    public static final int BASS_MIDI_EVENTS_NORSTATUS = 33554432;
    public static final int BASS_MIDI_EVENTS_RAW = 65536;
    public static final int BASS_MIDI_EVENTS_STRUCT = 0;
    public static final int BASS_MIDI_EVENTS_SYNC = 16777216;
    public static final int BASS_MIDI_EVENTS_TIME = 134217728;
    public static final int BASS_MIDI_FONTLOAD_COMPACT = 2;
    public static final int BASS_MIDI_FONTLOAD_KEEPDEC = 16;
    public static final int BASS_MIDI_FONTLOAD_NOLOAD = 4;
    public static final int BASS_MIDI_FONTLOAD_NOWAIT = 1;
    public static final int BASS_MIDI_FONTLOAD_TIME = 8;
    public static final int BASS_MIDI_FONT_EX = 16777216;
    public static final int BASS_MIDI_FONT_EX2 = 33554432;
    public static final int BASS_MIDI_FONT_LINATTMOD = 1048576;
    public static final int BASS_MIDI_FONT_LINDECVOL = 2097152;
    public static final int BASS_MIDI_FONT_MINFX = 16777216;
    public static final int BASS_MIDI_FONT_MMAP = 131072;
    public static final int BASS_MIDI_FONT_NOFX = 524288;
    public static final int BASS_MIDI_FONT_NOLIMITS = 8388608;
    public static final int BASS_MIDI_FONT_NORAMPIN = 4194304;
    public static final int BASS_MIDI_FONT_NOSBLIMITS = 8388608;
    public static final int BASS_MIDI_FONT_SBLIMITS = 33554432;
    public static final int BASS_MIDI_FONT_STEREO = 67108864;
    public static final int BASS_MIDI_FONT_XGDRUMS = 262144;
    public static final int BASS_MIDI_MARK_COPY = 6;
    public static final int BASS_MIDI_MARK_CUE = 1;
    public static final int BASS_MIDI_MARK_INST = 8;
    public static final int BASS_MIDI_MARK_KEYSIG = 5;
    public static final int BASS_MIDI_MARK_LYRIC = 2;
    public static final int BASS_MIDI_MARK_MARKER = 0;
    public static final int BASS_MIDI_MARK_SEQSPEC = 10;
    public static final int BASS_MIDI_MARK_TEXT = 3;
    public static final int BASS_MIDI_MARK_TICK = 65536;
    public static final int BASS_MIDI_MARK_TIMESIG = 4;
    public static final int BASS_MIDI_MARK_TRACK = 7;
    public static final int BASS_MIDI_MARK_TRACKSTART = 9;
    public static final int BASS_MIDI_NOCROP = 32768;
    public static final int BASS_MIDI_NODRUMPARAM = 1024;
    public static final int BASS_MIDI_NODRUMPARAMUSER = 512;
    public static final int BASS_MIDI_NOFX = 8192;
    public static final int BASS_MIDI_NOSYSRESET = 2048;
    public static final int BASS_MIDI_NOTEOFF1 = 65536;
    public static final int BASS_MIDI_SINCINTER = 8388608;
    public static final int BASS_POS_MIDI_TICK = 2;
    public static final int BASS_SYNC_MIDI_EVENT = 65540;
    public static final int BASS_SYNC_MIDI_MARK = 65536;
    public static final int BASS_SYNC_MIDI_TICK = 65541;
    public static final int BASS_TAG_MIDI_TRACK = 69632;
    public static final int MIDI_EVENT_ATTACK = 28;
    public static final int MIDI_EVENT_BANK = 10;
    public static final int MIDI_EVENT_BANK_LSB = 70;
    public static final int MIDI_EVENT_CHANPRES = 3;
    public static final int MIDI_EVENT_CHANPRES_FILTER = 67;
    public static final int MIDI_EVENT_CHANPRES_PITCH = 66;
    public static final int MIDI_EVENT_CHANPRES_VIBRATO = 65;
    public static final int MIDI_EVENT_CHANPRES_VOLUME = 68;
    public static final int MIDI_EVENT_CHORUS = 24;
    public static final int MIDI_EVENT_CHORUS_DELAY = 37;
    public static final int MIDI_EVENT_CHORUS_DEPTH = 38;
    public static final int MIDI_EVENT_CHORUS_FEEDBACK = 40;
    public static final int MIDI_EVENT_CHORUS_LEVEL = 41;
    public static final int MIDI_EVENT_CHORUS_MACRO = 31;
    public static final int MIDI_EVENT_CHORUS_RATE = 39;
    public static final int MIDI_EVENT_CHORUS_REVERB = 42;
    public static final int MIDI_EVENT_COARSETUNE = 8;
    public static final int MIDI_EVENT_CONTROL = 64;
    public static final int MIDI_EVENT_CUTOFF = 25;
    public static final int MIDI_EVENT_DECAY = 29;
    public static final int MIDI_EVENT_DEFDRUMS = 65542;
    public static final int MIDI_EVENT_DRUMS = 6;
    public static final int MIDI_EVENT_DRUM_CHORUS = 54;
    public static final int MIDI_EVENT_DRUM_COARSETUNE = 51;
    public static final int MIDI_EVENT_DRUM_CUTOFF = 55;
    public static final int MIDI_EVENT_DRUM_FINETUNE = 50;
    public static final int MIDI_EVENT_DRUM_LEVEL = 57;
    public static final int MIDI_EVENT_DRUM_PAN = 52;
    public static final int MIDI_EVENT_DRUM_RESONANCE = 56;
    public static final int MIDI_EVENT_DRUM_REVERB = 53;
    public static final int MIDI_EVENT_DRUM_USERFX = 58;
    public static final int MIDI_EVENT_END = 0;
    public static final int MIDI_EVENT_END_TRACK = 65539;
    public static final int MIDI_EVENT_EXPRESSION = 14;
    public static final int MIDI_EVENT_FINETUNE = 7;
    public static final int MIDI_EVENT_KEYPRES = 71;
    public static final int MIDI_EVENT_KEYPRES_FILTER = 74;
    public static final int MIDI_EVENT_KEYPRES_PITCH = 73;
    public static final int MIDI_EVENT_KEYPRES_VIBRATO = 72;
    public static final int MIDI_EVENT_KEYPRES_VOLUME = 75;
    public static final int MIDI_EVENT_MASTERVOL = 9;
    public static final int MIDI_EVENT_MASTER_COARSETUNE = 84;
    public static final int MIDI_EVENT_MASTER_FINETUNE = 83;
    public static final int MIDI_EVENT_MIXLEVEL = 65536;
    public static final int MIDI_EVENT_MODE = 22;
    public static final int MIDI_EVENT_MODRANGE = 69;
    public static final int MIDI_EVENT_MODULATION = 11;
    public static final int MIDI_EVENT_MOD_FILTER = 78;
    public static final int MIDI_EVENT_MOD_PITCH = 77;
    public static final int MIDI_EVENT_MOD_VIBRATO = 69;
    public static final int MIDI_EVENT_MOD_VOLUME = 79;
    public static final int MIDI_EVENT_NOTE = 1;
    public static final int MIDI_EVENT_NOTES = 131072;
    public static final int MIDI_EVENT_NOTESOFF = 18;
    public static final int MIDI_EVENT_PAN = 13;
    public static final int MIDI_EVENT_PAN_LSB = 85;
    public static final int MIDI_EVENT_PITCH = 4;
    public static final int MIDI_EVENT_PITCHRANGE = 5;
    public static final int MIDI_EVENT_PORTAMENTO = 19;
    public static final int MIDI_EVENT_PORTANOTE = 21;
    public static final int MIDI_EVENT_PORTATIME = 20;
    public static final int MIDI_EVENT_PROGRAM = 2;
    public static final int MIDI_EVENT_RELEASE = 27;
    public static final int MIDI_EVENT_RESET = 17;
    public static final int MIDI_EVENT_RESONANCE = 26;
    public static final int MIDI_EVENT_REVERB = 23;
    public static final int MIDI_EVENT_REVERB_DELAY = 33;
    public static final int MIDI_EVENT_REVERB_HICUTOFF = 35;
    public static final int MIDI_EVENT_REVERB_LEVEL = 36;
    public static final int MIDI_EVENT_REVERB_LOCUTOFF = 34;
    public static final int MIDI_EVENT_REVERB_MACRO = 30;
    public static final int MIDI_EVENT_REVERB_TIME = 32;
    public static final int MIDI_EVENT_SCALETUNING = 63;
    public static final int MIDI_EVENT_SOFT = 60;
    public static final int MIDI_EVENT_SOSTENUTO = 76;
    public static final int MIDI_EVENT_SOUNDOFF = 16;
    public static final int MIDI_EVENT_SPEED = 65540;
    public static final int MIDI_EVENT_SUSTAIN = 15;
    public static final int MIDI_EVENT_SYSTEM = 61;
    public static final int MIDI_EVENT_SYSTEMEX = 65538;
    public static final int MIDI_EVENT_TEMPO = 62;
    public static final int MIDI_EVENT_TRANSPOSE = 65537;
    public static final int MIDI_EVENT_USERFX = 43;
    public static final int MIDI_EVENT_USERFX_CHORUS = 46;
    public static final int MIDI_EVENT_USERFX_LEVEL = 44;
    public static final int MIDI_EVENT_USERFX_REVERB = 45;
    public static final int MIDI_EVENT_VIBRATO_DELAY = 82;
    public static final int MIDI_EVENT_VIBRATO_DEPTH = 81;
    public static final int MIDI_EVENT_VIBRATO_RATE = 80;
    public static final int MIDI_EVENT_VOICES = 131073;
    public static final int MIDI_EVENT_VOLUME = 12;
    public static final int MIDI_SYSTEM_DEFAULT = 0;
    public static final int MIDI_SYSTEM_GM1 = 1;
    public static final int MIDI_SYSTEM_GM2 = 2;
    public static final int MIDI_SYSTEM_GS = 4;
    public static final int MIDI_SYSTEM_GS_88 = 5;
    public static final int MIDI_SYSTEM_XG = 3;

    public static class BASS_MIDI_DEVICEINFO {
        public int flags;
        public int id;
        public String name;
    }

    public static class BASS_MIDI_EVENT {
        public int chan;
        public int event;
        public int param;
        public int pos;
        public int tick;
    }

    public static class BASS_MIDI_FONT {
        public int bank;
        public int font;
        public int preset;
    }

    public static class BASS_MIDI_FONTEX {
        public int dbank;
        public int dbanklsb;
        public int dpreset;
        public int font;
        public int sbank;
        public int spreset;
    }

    public static class BASS_MIDI_FONTEX2 extends BASS_MIDI_FONTEX {
        public int minchan;
        public int numchan;
    }

    public static class BASS_MIDI_FONTINFO {
        public String comment;
        public String copyright;
        public String name;
        public int presets;
        public int samload;
        public int samsize;
        public int samtype;
    }

    public static class BASS_MIDI_MARK {
        public int pos;
        public String text;
        public int track;
    }

    public static class BASS_MIDI_MARKB {
        public int pos;
        public byte[] text;
        public int track;
    }

    public interface MIDIFILTERPROC {
        boolean MIDIFILTERPROC(int i, int i2, BASS_MIDI_EVENT bass_midi_event, boolean z, Object obj);
    }

    static {
        System.loadLibrary("bassmidi");
    }

    public static native int BASS_MIDI_ConvertEvents(ByteBuffer byteBuffer, int i, BASS_MIDI_EVENT[] bass_midi_eventArr, int i2, int i3);

    public static native boolean BASS_MIDI_FontCompact(int i);

    public static native int BASS_MIDI_FontFlags(int i, int i2, int i3);

    public static native boolean BASS_MIDI_FontFree(int i);

    public static native boolean BASS_MIDI_FontGetInfo(int i, BASS_MIDI_FONTINFO bass_midi_fontinfo);

    public static native String BASS_MIDI_FontGetPreset(int i, int i2, int i3);

    public static native boolean BASS_MIDI_FontGetPresets(int i, int[] iArr);

    public static native float BASS_MIDI_FontGetVolume(int i);

    public static native int BASS_MIDI_FontInit(ParcelFileDescriptor parcelFileDescriptor, int i);

    public static native int BASS_MIDI_FontInit(BASS.Asset asset, int i);

    public static native int BASS_MIDI_FontInit(String str, int i);

    public static native int BASS_MIDI_FontInit(ByteBuffer byteBuffer, int i);

    public static native int BASS_MIDI_FontInitUser(BASS.BASS_FILEPROCS bass_fileprocs, Object obj, int i);

    public static native boolean BASS_MIDI_FontLoad(int i, int i2, int i3);

    public static native boolean BASS_MIDI_FontLoadEx(int i, int i2, int i3, int i4, int i5);

    public static native boolean BASS_MIDI_FontSetVolume(int i, float f);

    public static native boolean BASS_MIDI_FontUnload(int i, int i2, int i3);

    public static native boolean BASS_MIDI_FontUnpack(int i, String str, int i2);

    public static native int BASS_MIDI_GetVersion();

    public static native int BASS_MIDI_StreamCreate(int i, int i2, int i3);

    public static native int BASS_MIDI_StreamCreateEvents(BASS_MIDI_EVENT[] bass_midi_eventArr, int i, int i2, int i3);

    public static native int BASS_MIDI_StreamCreateFile(ParcelFileDescriptor parcelFileDescriptor, long j, long j2, int i, int i2);

    public static native int BASS_MIDI_StreamCreateFile(BASS.Asset asset, long j, long j2, int i, int i2);

    public static native int BASS_MIDI_StreamCreateFile(String str, long j, long j2, int i, int i2);

    public static native int BASS_MIDI_StreamCreateFile(ByteBuffer byteBuffer, long j, long j2, int i, int i2);

    public static native int BASS_MIDI_StreamCreateFileUser(int i, int i2, BASS.BASS_FILEPROCS bass_fileprocs, Object obj, int i3);

    public static native int BASS_MIDI_StreamCreateURL(String str, int i, int i2, BASS.DOWNLOADPROC downloadproc, Object obj, int i3);

    public static native boolean BASS_MIDI_StreamEvent(int i, int i2, int i3, int i4);

    public static native int BASS_MIDI_StreamEvents(int i, int i2, ByteBuffer byteBuffer, int i3);

    public static native int BASS_MIDI_StreamEvents(int i, int i2, BASS_MIDI_EVENT[] bass_midi_eventArr, int i3);

    public static native int BASS_MIDI_StreamGetChannel(int i, int i2);

    public static native int BASS_MIDI_StreamGetEvent(int i, int i2, int i3);

    public static native int BASS_MIDI_StreamGetEvents(int i, int i2, int i3, BASS_MIDI_EVENT[] bass_midi_eventArr);

    public static native int BASS_MIDI_StreamGetEventsEx(int i, int i2, int i3, BASS_MIDI_EVENT[] bass_midi_eventArr, int i4, int i5);

    public static native int BASS_MIDI_StreamGetFonts(int i, BASS_MIDI_FONT[] bass_midi_fontArr, int i2);

    public static native int BASS_MIDI_StreamGetFonts(int i, BASS_MIDI_FONTEX2[] bass_midi_fontex2Arr, int i2);

    public static native int BASS_MIDI_StreamGetFonts(int i, BASS_MIDI_FONTEX[] bass_midi_fontexArr, int i2);

    public static native boolean BASS_MIDI_StreamGetMark(int i, int i2, int i3, BASS_MIDI_MARK bass_midi_mark);

    public static native boolean BASS_MIDI_StreamGetMark(int i, int i2, int i3, BASS_MIDI_MARKB bass_midi_markb);

    public static native int BASS_MIDI_StreamGetMarks(int i, int i2, int i3, BASS_MIDI_MARK[] bass_midi_markArr);

    public static native int BASS_MIDI_StreamGetMarks(int i, int i2, int i3, BASS_MIDI_MARKB[] bass_midi_markbArr);

    public static native boolean BASS_MIDI_StreamGetPreset(int i, int i2, BASS_MIDI_FONT bass_midi_font);

    public static native boolean BASS_MIDI_StreamLoadSamples(int i);

    public static native boolean BASS_MIDI_StreamSetFilter(int i, boolean z, MIDIFILTERPROC midifilterproc, Object obj);

    public static native boolean BASS_MIDI_StreamSetFonts(int i, BASS_MIDI_FONT[] bass_midi_fontArr, int i2);

    public static native boolean BASS_MIDI_StreamSetFonts(int i, BASS_MIDI_FONTEX2[] bass_midi_fontex2Arr, int i2);

    public static native boolean BASS_MIDI_StreamSetFonts(int i, BASS_MIDI_FONTEX[] bass_midi_fontexArr, int i2);
}