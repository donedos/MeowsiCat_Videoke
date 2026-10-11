package com.songhub.android.database;

/* JADX INFO: loaded from: classes3.dex */
public class MegSong {
    public int code;
    public boolean isMp3;
    public int langId;
    public int langType;
    public int midiPosSectors;
    public int midiSizeSectors;
    public int mp3PosSectors;
    public int mp3SizeSectors;
    public int mp3Vol;
    public String name = "";
    public int singerIdx;
    public int style;

    public long midiByteLength() {
        return ((long) this.midiSizeSectors) * 2048;
    }

    public long midiByteOffset() {
        return ((long) this.midiPosSectors) * 2048;
    }
}
