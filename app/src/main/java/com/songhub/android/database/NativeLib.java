package com.songhub.android.database;

/*
 * JNI bridge for libMewtwo.so. The packaged native library exports its entry points as
 * Java_com_songhub_android_database_NativeLib_processMidiData /
 * Java_com_songhub_android_database_NativeLib_verifyDiagnosticPassword, so this class MUST stay
 * in the com.songhub.android.database package and MUST NOT be renamed/moved/obfuscated. Changing
 * the package or class name makes the native lookup fail (UnsatisfiedLinkError), which surfaces
 * to the UI as "Lyrics unavailable for this title".
 */
public final class NativeLib {
    public static native byte[] processMidiData(byte[] bArr, boolean z);

    public static native boolean verifyDiagnosticPassword(String str);

    static {
        System.loadLibrary("Mewtwo");
    }
}
