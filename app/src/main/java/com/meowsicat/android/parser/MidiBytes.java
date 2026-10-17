package com.meowsicat.android.parser;

/* JADX INFO: loaded from: classes3.dex */
public final class MidiBytes {
    private MidiBytes() {
    }

    public static int readBe16(byte[] bArr, int i) {
        return (bArr[i + 1] & 255) | ((bArr[i] & 255) << 8);
    }

    public static long readBe32(byte[] bArr, int i) {
        return ((long) (bArr[i + 3] & 255)) | (((long) (bArr[i] & 255)) << 24) | ((long) ((bArr[i + 1] & 255) << 16)) | ((long) ((bArr[i + 2] & 255) << 8));
    }

    public static final class VlqResult {
        public final int nextIndex;
        public final long value;

        public VlqResult(long j, int i) {
            this.value = j;
            this.nextIndex = i;
        }
    }

    public static VlqResult readVlq(byte[] bArr, int i) {
        byte b;
        long j = 0;
        do {
            b = bArr[i];
            i++;
            j = (j << 7) | ((long) (b & 127));
        } while ((b & 128) != 0);
        return new VlqResult(j, i);
    }
}
