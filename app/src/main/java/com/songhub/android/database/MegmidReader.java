package com.songhub.android.database;

import android.content.Context;
import com.songhub.android.song.SongLibrary;
import java.io.IOException;
import java.io.RandomAccessFile;

/* JADX INFO: loaded from: classes3.dex */
public final class MegmidReader {
    private MegmidReader() {
    }

    public static byte[] readMidi(Context context, String str, long j, long j2) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(str, "r");
        if (j >= 0) {
            try {
                if (j + j2 <= randomAccessFile.length()) {
                    byte[] bArr = new byte[(int) j2];
                    randomAccessFile.seek(j);
                    randomAccessFile.readFully(bArr);
                    randomAccessFile.close();
                    byte[] bArrProcessMidiData = NativeLib.processMidiData(bArr, false);
                    if (bArrProcessMidiData != null) {
                        return bArrProcessMidiData;
                    }
                    throw new IOException("This song is not supported in this app.");
                }
            } catch (Throwable th) {
                try {
                    randomAccessFile.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        throw new IOException("Song data out of bounds: offset=" + j + " length=" + j2 + " but megmid file size=" + randomAccessFile.length());
    }

    public static byte[] readMidi(Context context, SongLibrary.LibrarySong librarySong) throws IOException {
        if ("USER".equals(librarySong.sourceType)) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(librarySong.megmidFile, "r");
            try {
                byte[] bArr = new byte[(int) librarySong.localByteLength];
                randomAccessFile.seek(librarySong.localByteOffset);
                randomAccessFile.readFully(bArr);
                randomAccessFile.close();
                return bArr;
            } catch (Throwable th) {
                try {
                    randomAccessFile.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        return readMidi(context, librarySong.megmidFile.getAbsolutePath(), librarySong.localByteOffset, librarySong.localByteLength);
    }

    public static byte[] readMidi(SongLibrary.LibrarySong librarySong) throws IOException {
        return readMidi(null, librarySong);
    }
}
