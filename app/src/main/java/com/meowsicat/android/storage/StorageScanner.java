package com.meowsicat.android.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/* JADX INFO: loaded from: classes3.dex */
public final class StorageScanner {
    public static final String ASSETS_SUBFOLDER = "assets";
    public static final String BGPHOTO_SUBFOLDER = "bgphoto";
    public static final String BGV_SUBFOLDER = "bgv";
    public static final String IDX_SUBFOLDER = "idx";
    private static final Set<String> IGNORED_MOUNT_NAMES = new HashSet(Arrays.asList("self", "emulated", "runtime", "appfuse", "expand", "user", "media_rw", "obb", "asec", "secure", "vendor", "knox", "tmp", "sdcard", "cd-rom", "cdrom", "rescue", "androidwritable", "installer", "pass_through", "user_de", "vendor_de", "apex", "product", "system", "mirror", "f2fs", "ext4", "vold", "data"));
    public static final String MGOK_FOLDER = "MeowsiCat";
    public static final String MP3S_SUBFOLDER = "mp3s";
    public static final String RECORDS_SUBFOLDER = "records";
    public static final String SFX_SUBFOLDER = "sfx";
    public static final String SOUND_SUBFOLDER = "sound";
    public static final String USER_SUBFOLDER = "songs";
    public static final String VIDEOS_SUBFOLDER = "videos";

    private StorageScanner() {
    }

    public static final class MgokLocation {
        public final File root;
        public final String volumeLabel;

        public MgokLocation(File file, String str) {
            this.root = file;
            this.volumeLabel = str;
        }

        public File idxDir() {
            return findSubFolder(this.root, StorageScanner.IDX_SUBFOLDER);
        }

        public File bgvDir() {
            return findSubFolder(this.root, StorageScanner.BGV_SUBFOLDER);
        }

        public File bgphotoDir() {
            return findSubFolder(this.root, StorageScanner.BGPHOTO_SUBFOLDER);
        }

        public File userDir() {
            return findSubFolder(this.root, StorageScanner.USER_SUBFOLDER);
        }

        public File sfxDir() {
            return findSubFolder(this.root, StorageScanner.SFX_SUBFOLDER);
        }

        public File soundDir() {
            return findSubFolder(this.root, StorageScanner.SOUND_SUBFOLDER);
        }

        public File videosDir() {
            return findSubFolder(this.root, StorageScanner.VIDEOS_SUBFOLDER);
        }

        public File mp3sDir() {
            return findSubFolder(this.root, StorageScanner.MP3S_SUBFOLDER);
        }

        public File recordsDir() {
            return findSubFolder(this.root, StorageScanner.RECORDS_SUBFOLDER);
        }

        public File assetsDir() {
            return findSubFolder(this.root, StorageScanner.ASSETS_SUBFOLDER);
        }

        private File findSubFolder(File file, String str) {
            File[] fileArrListFiles = file.listFiles();
            if (fileArrListFiles != null) {
                for (File file2 : fileArrListFiles) {
                    if (file2.isDirectory() && file2.getName().equalsIgnoreCase(str)) {
                        return file2;
                    }
                }
            }
            return new File(file, str);
        }
    }

    public static List<MgokLocation> scan(Context context) {
        ArrayList arrayList = new ArrayList();
        try {
            File appExternalMeowsiCat = context.getExternalFilesDir("MeowsiCat");
            if (appExternalMeowsiCat != null) {
                if (!appExternalMeowsiCat.exists()) {
                    appExternalMeowsiCat.mkdirs();
                }
                if (appExternalMeowsiCat.exists() && appExternalMeowsiCat.isDirectory()) {
                    arrayList.add(new MgokLocation(appExternalMeowsiCat, "App Storage (MeowsiCat)"));
                }
            }
        } catch (Exception unused) {}

        for (File canonicalFile : getCandidateRoots(context)) {
            if (canonicalFile != null && canonicalFile.exists() && canonicalFile.isDirectory()) {
                try {
                    canonicalFile = canonicalFile.getCanonicalFile();
                } catch (Exception unused) {
                }
                File[] fileArrListFiles = canonicalFile.listFiles();
                if (fileArrListFiles != null) {
                    for (File f : fileArrListFiles) {
                        if (f.isDirectory() && f.getName().equalsIgnoreCase(MGOK_FOLDER)) {
                            boolean exists = false;
                            for (Object loc : arrayList) {
                                if (loc instanceof MgokLocation && ((MgokLocation) loc).root.equals(f)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                arrayList.add(new MgokLocation(f, labelForRoot(context, canonicalFile)));
                            }
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    private static void addRootIfUnique(List<File> list, File file) {
        File canonicalFile;
        File canonicalFile2;
        if (file != null && file.exists() && file.isDirectory() && file.canRead()) {
            try {
                canonicalFile = file.getCanonicalFile();
            } catch (Exception unused) {
                canonicalFile = file;
            }
            if (IGNORED_MOUNT_NAMES.contains(canonicalFile.getName().toLowerCase(Locale.US))) {
                return;
            }
            for (File file2 : list) {
                try {
                    canonicalFile2 = file2.getCanonicalFile();
                } catch (Exception unused2) {
                    canonicalFile2 = file2;
                }
                if (file2.equals(file) || canonicalFile2.equals(canonicalFile) || canonicalFile2.getAbsolutePath().equals(canonicalFile.getAbsolutePath())) {
                    return;
                }
            }
            list.add(canonicalFile);
        }
    }

    public static List<File> getCandidateRoots(Context context) {
        File fileWalkUpToVolumeRoot;
        ArrayList arrayList = new ArrayList();
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        if (externalStorageDirectory != null) {
            addRootIfUnique(arrayList, externalStorageDirectory);
        }
        File[] externalFilesDirs = context.getExternalFilesDirs(null);
        if (externalFilesDirs != null) {
            for (File file : externalFilesDirs) {
                if (file != null && (fileWalkUpToVolumeRoot = walkUpToVolumeRoot(file)) != null) {
                    addRootIfUnique(arrayList, fileWalkUpToVolumeRoot);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                if (storageManager != null) {
                    Iterator<StorageVolume> it = storageManager.getStorageVolumes().iterator();
                    while (it.hasNext()) {
                        File volumeDirCompat = getVolumeDirCompat(it.next());
                        if (volumeDirCompat != null) {
                            addRootIfUnique(arrayList, volumeDirCompat);
                        }
                    }
                }
            } catch (Exception unused) {
            }
        }
        checkCommonMountPoints(arrayList);
        return arrayList;
    }

    private static void checkCommonMountPoints(List<File> list) {
        File[] fileArrListFiles;
        String[] strArr = {"/storage", "/mnt/usb", "/mnt/media_rw", "/media"};
        for (int i = 0; i < 4; i++) {
            File file = new File(strArr[i]);
            if (file.exists() && file.isDirectory() && (fileArrListFiles = file.listFiles()) != null) {
                for (File file2 : fileArrListFiles) {
                    if (file2.isDirectory() && file2.canRead() && !IGNORED_MOUNT_NAMES.contains(file2.getName().toLowerCase(Locale.US))) {
                        addRootIfUnique(list, file2);
                    }
                }
            }
        }
    }

    private static File walkUpToVolumeRoot(File file) {
        for (int i = 0; i < 4 && file != null; i++) {
            file = file.getParentFile();
        }
        return file;
    }

    private static File getVolumeDirCompat(StorageVolume storageVolume) {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                return storageVolume.getDirectory();
            }
            Object objInvoke = storageVolume.getClass().getMethod("getPath", new Class[0]).invoke(storageVolume, new Object[0]);
            if (objInvoke instanceof String) {
                return new File((String) objInvoke);
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    public static String labelForRoot(Context context, File file) {
        File canonicalFile;
        File canonicalFile2;
        StorageManager storageManager;
        File canonicalFile3;
        try {
            canonicalFile = file.getCanonicalFile();
        } catch (Exception unused) {
            canonicalFile = file;
        }
        try {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            if (externalStorageDirectory != null) {
                try {
                    canonicalFile2 = externalStorageDirectory.getCanonicalFile();
                } catch (Exception unused2) {
                    canonicalFile2 = externalStorageDirectory;
                }
            } else {
                canonicalFile2 = externalStorageDirectory;
            }
            if (canonicalFile.equals(canonicalFile2)) {
                return "Internal Storage";
            }
            if (externalStorageDirectory != null && externalStorageDirectory.equals(file)) {
                return "Internal Storage";
            }
            if (Build.VERSION.SDK_INT >= 24 && (storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE)) != null) {
                Iterator<StorageVolume> it = storageManager.getStorageVolumes().iterator();
                while (it.hasNext()) {
                    StorageVolume storageVolumeM = it.next();
                    File volumeDirCompat = getVolumeDirCompat(storageVolumeM);
                    if (volumeDirCompat != null) {
                        try {
                            canonicalFile3 = volumeDirCompat.getCanonicalFile();
                        } catch (Exception unused3) {
                            canonicalFile3 = volumeDirCompat;
                        }
                        if (file.equals(volumeDirCompat) || canonicalFile.equals(canonicalFile3)) {
                            String description = storageVolumeM.getDescription(context);
                            return description != null ? description.toString() : file.getAbsolutePath();
                        }
                    }
                }
            }
            return file.getAbsolutePath();
        } catch (Exception unused4) {
            return file.getAbsolutePath();
        }
    }
}
