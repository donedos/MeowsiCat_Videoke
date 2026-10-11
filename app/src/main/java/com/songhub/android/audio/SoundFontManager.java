package com.songhub.android.audio;

import android.content.Context;
import android.content.SharedPreferences;
import com.songhub.android.storage.StorageScanner;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/* JADX INFO: loaded from: classes3.dex */
public final class SoundFontManager {
    private static final String KEY_SELECTED_SF2_PATH = "selected_sf2_path";
    private static final String PREFS_NAME = "songhub_settings";

    private SoundFontManager() {
    }

    public static List<File> listSoundFonts(Context context) {
        ArrayList<File> arrayList = new ArrayList();
        Iterator<StorageScanner.MgokLocation> it = StorageScanner.scan(context).iterator();
        while (it.hasNext()) {
            arrayList.addAll(listSoundFonts(it.next()));
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (File file : arrayList) {
            if (!linkedHashMap.containsKey(file.getName())) {
                linkedHashMap.put(file.getName(), file);
            }
        }
        ArrayList arrayList2 = new ArrayList(linkedHashMap.values());
        Collections.sort(arrayList2, new Comparator() { // from class: com.songhub.android.audio.SoundFontManager$$ExternalSyntheticLambda2
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return ((File) obj).getName().compareToIgnoreCase(((File) obj2).getName());
            }
        });
        return arrayList2;
    }

    public static List<File> listSoundFonts(StorageScanner.MgokLocation mgokLocation) {
        File[] fileArrListFiles;
        ArrayList arrayList = new ArrayList();
        File fileSoundDir = mgokLocation.soundDir();
        if (fileSoundDir.exists() && fileSoundDir.isDirectory() && (fileArrListFiles = fileSoundDir.listFiles(new FileFilter() { // from class: com.songhub.android.audio.SoundFontManager$$ExternalSyntheticLambda0
            @Override // java.io.FileFilter
            public final boolean accept(File file) {
                return SoundFontManager.lambda$listSoundFonts$1(file);
            }
        })) != null) {
            Collections.addAll(arrayList, fileArrListFiles);
            Collections.sort(arrayList, new Comparator() { // from class: com.songhub.android.audio.SoundFontManager$$ExternalSyntheticLambda1
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return ((File) obj).getName().compareToIgnoreCase(((File) obj2).getName());
                }
            });
        }
        return arrayList;
    }

    static /* synthetic */ boolean lambda$listSoundFonts$1(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(".sf2");
    }

    public static void setSelectedSoundFont(Context context, String str) {
        getPrefs(context).edit().putString(KEY_SELECTED_SF2_PATH, str).apply();
    }

    public static String getSelectedSoundFontPath(Context context) {
        return getPrefs(context).getString(KEY_SELECTED_SF2_PATH, null);
    }

    public static String getOrAutoPickSoundFont(Context context) {
        return getOrAutoPickSoundFont(context, StorageScanner.scan(context));
    }

    public static String getOrAutoPickSoundFont(Context context, List<StorageScanner.MgokLocation> locations) {
        String selectedSoundFontPath = getSelectedSoundFontPath(context);
        if (selectedSoundFontPath != null && new File(selectedSoundFontPath).exists()) {
            return selectedSoundFontPath;
        }
        for (StorageScanner.MgokLocation mgokLocation : locations) {
            List<File> listListSoundFonts = listSoundFonts(mgokLocation);
            if (!listListSoundFonts.isEmpty()) {
                String absolutePath = listListSoundFonts.get(0).getAbsolutePath();
                setSelectedSoundFont(context, absolutePath);
                return absolutePath;
            }
        }
        List<File> listListSoundFonts2 = listSoundFonts(context);
        if (listListSoundFonts2.isEmpty()) {
            return null;
        }
        String absolutePath2 = listListSoundFonts2.get(0).getAbsolutePath();
        setSelectedSoundFont(context, absolutePath2);
        return absolutePath2;
    }

    public static String getOrAutoPickSoundFont(Context context, StorageScanner.MgokLocation mgokLocation) {
        String selectedSoundFontPath = getSelectedSoundFontPath(context);
        if (selectedSoundFontPath != null && new File(selectedSoundFontPath).exists()) {
            return selectedSoundFontPath;
        }
        List<File> listListSoundFonts = listSoundFonts(mgokLocation);
        if (!listListSoundFonts.isEmpty()) {
            String absolutePath = listListSoundFonts.get(0).getAbsolutePath();
            setSelectedSoundFont(context, absolutePath);
            return absolutePath;
        }
        List<File> listListSoundFonts2 = listSoundFonts(context);
        if (listListSoundFonts2.isEmpty()) {
            return null;
        }
        String absolutePath2 = listListSoundFonts2.get(0).getAbsolutePath();
        setSelectedSoundFont(context, absolutePath2);
        return absolutePath2;
    }

    public static void clearSelectedSoundFont(Context context) {
        getPrefs(context).edit().remove(KEY_SELECTED_SF2_PATH).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, 0);
    }
}
