package com.songhub.android.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: classes3.dex */
public final class BgvSettings {
    private static final String KEY_CAMERA_LENS = "bgv_camera_lens";
    private static final String KEY_CATEGORY = "bgv_category";
    private static final String KEY_CONTINUOUS_VIDEO = "bgv_continuous_video";
    private static final String KEY_CUSTOM_PATH = "bgv_custom_path";
    private static final String KEY_MODE = "bgv_mode";
    private static final String KEY_PHOTO_ANIMATION = "bgv_photo_animation";
    private static final String KEY_PHOTO_INTERVAL = "bgv_photo_interval";
    private static final String KEY_USE_CUSTOM_PATH = "bgv_use_custom";
    public static final int LENS_BACK = 0;
    public static final int LENS_EXTERNAL = 2;
    public static final int LENS_FRONT = 1;
    public static final int MODE_CAMERA = 2;
    public static final int MODE_PHOTOS = 1;
    public static final int MODE_VIDEOS = 0;
    public static final int PHOTO_ANIM_FADE = 1;
    public static final int PHOTO_ANIM_NONE = 0;
    public static final int PHOTO_ANIM_SLIDE = 2;
    public static final int PHOTO_ANIM_ZOOM = 3;
    private static final String PREF_NAME = "bgv_settings";

    public static void setMode(Context context, int i) {
        prefs(context).edit().putInt(KEY_MODE, i).apply();
    }

    public static int getMode(Context context) {
        return prefs(context).getInt(KEY_MODE, 0);
    }

    public static void setCameraLens(Context context, int i) {
        prefs(context).edit().putInt(KEY_CAMERA_LENS, i).apply();
    }

    public static int getCameraLens(Context context) {
        return prefs(context).getInt(KEY_CAMERA_LENS, 0);
    }

    public static void setCategory(Context context, String str) {
        prefs(context).edit().putString(KEY_CATEGORY, str).apply();
    }

    public static String getCategory(Context context) {
        return prefs(context).getString(KEY_CATEGORY, "AUTO");
    }

    public static void setPhotoAnimation(Context context, int i) {
        prefs(context).edit().putInt(KEY_PHOTO_ANIMATION, i).apply();
    }

    public static int getPhotoAnimation(Context context) {
        return prefs(context).getInt(KEY_PHOTO_ANIMATION, 1);
    }

    public static void setPhotoInterval(Context context, int i) {
        prefs(context).edit().putInt(KEY_PHOTO_INTERVAL, i).apply();
    }

    public static int getPhotoInterval(Context context) {
        return prefs(context).getInt(KEY_PHOTO_INTERVAL, 5);
    }

    public static void setContinuousVideo(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_CONTINUOUS_VIDEO, z).apply();
    }

    public static boolean isContinuousVideo(Context context) {
        return prefs(context).getBoolean(KEY_CONTINUOUS_VIDEO, false);
    }

    public static List<String> getAvailableCategories(Context context, String str) {
        File[] fileArrListFiles;
        ArrayList arrayList = new ArrayList();
        File bgvDirectory = getBgvDirectory(context, str);
        if (bgvDirectory != null && bgvDirectory.exists() && (fileArrListFiles = bgvDirectory.listFiles()) != null) {
            for (File file : fileArrListFiles) {
                if (file.isDirectory()) {
                    arrayList.add(file.getName());
                }
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public static void setCustomPath(Context context, String str) {
        prefs(context).edit().putString(KEY_CUSTOM_PATH, str).apply();
    }

    public static String getCustomPath(Context context) {
        return prefs(context).getString(KEY_CUSTOM_PATH, "");
    }

    public static void setUseCustomPath(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_USE_CUSTOM_PATH, z).apply();
    }

    public static boolean isUseCustomPath(Context context) {
        return prefs(context).getBoolean(KEY_USE_CUSTOM_PATH, false);
    }

    public static boolean hasVideos(File file) {
        File[] fileArrListFiles;
        if (file == null || !file.exists() || !file.isDirectory() || (fileArrListFiles = file.listFiles()) == null) {
            return false;
        }
        for (File file2 : fileArrListFiles) {
            if (file2.isFile()) {
                String lowerCase = file2.getName().toLowerCase();
                if (lowerCase.endsWith(".mp4") || lowerCase.endsWith(".mkv") || lowerCase.endsWith(".avi")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasPhotos(File file) {
        File[] fileArrListFiles;
        if (file == null || !file.exists() || !file.isDirectory() || (fileArrListFiles = file.listFiles()) == null) {
            return false;
        }
        for (File file2 : fileArrListFiles) {
            if (file2.isDirectory()) {
                if (hasPhotos(file2)) {
                    return true;
                }
            } else {
                String lowerCase = file2.getName().toLowerCase();
                if (lowerCase.endsWith(".jpg") || lowerCase.endsWith(".png")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static File getBgPhotoDirectory(Context context, String str) {
        ArrayList<File> arrayList = new ArrayList();
        if (str != null) {
            arrayList.add(new File(str));
        }
        for (StorageScanner.MgokLocation mgokLocation : StorageScanner.scan(context)) {
            if (str == null || !mgokLocation.root.getAbsolutePath().equals(str)) {
                arrayList.add(mgokLocation.root);
            }
        }
        for (File file : arrayList) {
            File file2 = new File(file, StorageScanner.BGPHOTO_SUBFOLDER);
            if (file2.exists() && file2.isDirectory()) {
                return file2;
            }
            File file3 = new File(file, StorageScanner.BGPHOTO_SUBFOLDER.toUpperCase());
            if (file3.exists() && file3.isDirectory()) {
                return file3;
            }
            File file4 = new File(file, "photos");
            if (file4.exists() && file4.isDirectory()) {
                return file4;
            }
        }
        return null;
    }

    public static File getBgvDirectory(Context context, String str) {
        File[] fileArrListFiles;
        File[] fileArrListFiles2;
        if (isUseCustomPath(context)) {
            String customPath = getCustomPath(context);
            if (!customPath.isEmpty()) {
                File file = new File(customPath);
                if (file.exists() && file.isDirectory()) {
                    return file;
                }
            }
        }
        ArrayList<File> arrayList = new ArrayList();
        if (str != null) {
            arrayList.add(new File(str));
        }
        for (StorageScanner.MgokLocation mgokLocation : StorageScanner.scan(context)) {
            if (str == null || !mgokLocation.root.getAbsolutePath().equals(str)) {
                arrayList.add(mgokLocation.root);
            }
        }
        for (File file2 : arrayList) {
            File file3 = new File(file2, StorageScanner.BGV_SUBFOLDER);
            if (file3.exists() && file3.isDirectory() && (fileArrListFiles2 = file3.listFiles()) != null && fileArrListFiles2.length > 0) {
                return file3;
            }
            File file4 = new File(file2, StorageScanner.BGV_SUBFOLDER.toUpperCase());
            if (file4.exists() && file4.isDirectory() && (fileArrListFiles = file4.listFiles()) != null && fileArrListFiles.length > 0) {
                return file4;
            }
            File file5 = new File(file2, StorageScanner.VIDEOS_SUBFOLDER);
            if (file5.exists() && file5.isDirectory()) {
                return file5;
            }
            File file6 = new File(file2, "Videos");
            if (file6.exists() && file6.isDirectory()) {
                return file6;
            }
        }
        return null;
    }

    public static boolean hasAnyCamera(Context context) {
        String[] cameraIdList;
        if (context == null) {
            return false;
        }
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService("camera");
            return (cameraManager == null || (cameraIdList = cameraManager.getCameraIdList()) == null || cameraIdList.length <= 0) ? false : true;
        } catch (Exception unused) {
            return false;
        }
    }

    public static boolean hasCameraLens(Context context, int i) {
        String[] cameraIdList;
        int i2;
        if (context == null) {
            return false;
        }
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService("camera");
            if (cameraManager != null && (cameraIdList = cameraManager.getCameraIdList()) != null && cameraIdList.length != 0) {
                if (i == 1) {
                    i2 = 0;
                } else if (i == 0) {
                    i2 = 1;
                } else {
                    i2 = 2;
                    if (i == 2 && Build.VERSION.SDK_INT >= 28) {
                    }
                }
                for (String str : cameraIdList) {
                    Integer num = (Integer) cameraManager.getCameraCharacteristics(str).get(CameraCharacteristics.LENS_FACING);
                    if (num != null && num.intValue() == i2) {
                        return true;
                    }
                }
            }
        } catch (Exception unused) {
        }
        return false;
    }

    public static List<Integer> getAvailableCameraLenses(Context context) {
        boolean z;
        ArrayList arrayList = new ArrayList();
        if (!hasAnyCamera(context)) {
            return arrayList;
        }
        boolean z2 = true;
        if (hasCameraLens(context, 1)) {
            arrayList.add(1);
            z = true;
        } else {
            z = false;
        }
        if (hasCameraLens(context, 0)) {
            arrayList.add(0);
        } else {
            z2 = z;
        }
        if (hasCameraLens(context, 2)) {
            arrayList.add(2);
        } else if (!z2 && arrayList.isEmpty()) {
            arrayList.add(0);
        }
        return arrayList;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, 0);
    }
}
