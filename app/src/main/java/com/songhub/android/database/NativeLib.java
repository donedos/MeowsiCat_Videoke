package com.songhub.android.database;

/* JADX INFO: loaded from: classes3.dex */
public final class NativeLib {
    public static native byte[] processMidiData(byte[] bArr, boolean z);

    public static native boolean verifyDiagnosticPassword(String str);

    static {
        System.loadLibrary("Mewtwo");
    }
}
