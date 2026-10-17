package com.meowsicat.android.storage;

import android.content.Context;
import android.util.Log;
import com.meowsicat.android.database.MegSong;
import com.meowsicat.android.song.SongLibrary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes3.dex */
public class UserContentManager {
    private static final String EXT_REGEX = "\\.(mid|midi|kar|mp3)$";
    private static final String JSON_FILENAME = "user_content.json";
    private static final String TAG = "UserContentManager";
    private static final Pattern PATTERN_TITLE_SINGER = Pattern.compile("^(.+?)\\s*-\\s*(.+)\\.(mid|midi|kar|mp3)$", 2);
    private static final Pattern PATTERN_TITLE_ONLY = Pattern.compile("^(.+)\\.(mid|midi|kar|mp3)$", 2);

    public static void scanAndSave(Context context, StorageScanner.MgokLocation mgokLocation, Set<Integer> set) {
        int i;
        File fileUserDir = mgokLocation.userDir();
        if (!fileUserDir.exists() || !fileUserDir.isDirectory()) {
            Log.w(TAG, "User directory not found: " + fileUserDir.getAbsolutePath());
            return;
        }
        File[] fileArrListFiles = fileUserDir.listFiles();
        if (fileArrListFiles == null) {
            return;
        }
        ArrayList<C1PendingSong> arrayList = new ArrayList();
        int length = fileArrListFiles.length;
        int iIntValue = 0;
        int i2 = 0;
        while (true) {
            i = 1;
            if (i2 >= length) {
                break;
            }
            File file = fileArrListFiles[i2];
            String name = file.getName();
            String lowerCase = name.toLowerCase(Locale.US);
            if (lowerCase.endsWith(".mid") || lowerCase.endsWith(".midi") || lowerCase.endsWith(".kar") || lowerCase.endsWith(".mp3")) {
                String strReplaceFirst = name.replaceFirst("^\\d+\\s*-\\s*", "");
                C1PendingSong c1PendingSong = new C1PendingSong();
                c1PendingSong.file = file;
                Matcher matcher = PATTERN_TITLE_SINGER.matcher(strReplaceFirst);
                Matcher matcher2 = PATTERN_TITLE_ONLY.matcher(strReplaceFirst);
                if (matcher.matches()) {
                    c1PendingSong.title = matcher.group(1).trim();
                    c1PendingSong.singer = matcher.group(2).trim();
                    c1PendingSong.ext = matcher.group(3).toLowerCase(Locale.US);
                } else if (matcher2.matches()) {
                    c1PendingSong.title = matcher2.group(1).trim();
                    c1PendingSong.singer = "Unknown";
                    c1PendingSong.ext = matcher2.group(2).toLowerCase(Locale.US);
                }
                arrayList.add(c1PendingSong);
            }
            i2++;
        }
        HashSet hashSet = new HashSet();
        if (set != null && !set.isEmpty()) {
            hashSet.addAll(set);
            for (Integer num : set) {
                if (num != null && num.intValue() > iIntValue) {
                    iIntValue = num.intValue();
                }
            }
            i = iIntValue >= 100000 ? 1 + (((iIntValue / 100000) + 1) * 100000) : 100001;
        }
        JSONArray jSONArray = new JSONArray();
        for (C1PendingSong c1PendingSong2 : arrayList) {
            while (hashSet.contains(Integer.valueOf(i))) {
                i++;
            }
            int i3 = i + 1;
            hashSet.add(Integer.valueOf(i));
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("code", i);
                jSONObject.put("title", c1PendingSong2.title);
                jSONObject.put("singer", c1PendingSong2.singer);
                jSONObject.put("path", c1PendingSong2.file.getAbsolutePath());
                jSONObject.put("ext", c1PendingSong2.ext);
                jSONObject.put("size", c1PendingSong2.file.length());
                jSONArray.put(jSONObject);
            } catch (Exception e) {
                Log.e(TAG, "Error adding song to JSON: " + c1PendingSong2.file.getName(), e);
            }
            i = i3;
        }
        File file2 = new File(mgokLocation.root, JSON_FILENAME);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            try {
                fileOutputStream.write(jSONArray.toString().getBytes(StandardCharsets.UTF_8));
                Log.i(TAG, "Saved user content to " + file2.getAbsolutePath());
                fileOutputStream.close();
            } catch (Throwable th) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (IOException e2) {
            Log.w(TAG, "Failed to save to media root (Permission denied?). Falling back to internal storage: " + e2.getMessage());
            saveToInternalFallback(context, mgokLocation, jSONArray);
        } catch (Exception e3) {
            Log.e(TAG, "JSON error during save", e3);
        }
    }

    /* JADX INFO: renamed from: com.meowsicat.android.storage.UserContentManager$1PendingSong, reason: invalid class name */
    static class C1PendingSong {
        String ext;
        File file;
        String singer;
        String title;

        C1PendingSong() {
        }
    }

    private static void saveToInternalFallback(Context context, StorageScanner.MgokLocation mgokLocation, JSONArray jSONArray) {
        File internalFallbackFile = getInternalFallbackFile(context, mgokLocation);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(internalFallbackFile);
            try {
                fileOutputStream.write(jSONArray.toString().getBytes(StandardCharsets.UTF_8));
                Log.i(TAG, "Saved user content to internal fallback: " + internalFallbackFile.getAbsolutePath());
                fileOutputStream.close();
            } catch (Throwable th) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save to internal fallback too", e);
        }
    }

    private static File getInternalFallbackFile(Context context, StorageScanner.MgokLocation mgokLocation) {
        return new File(context.getFilesDir(), "user_songs_" + Integer.toHexString(mgokLocation.root.getAbsolutePath().hashCode()) + ".json");
    }

    public static List<SongLibrary.LibrarySong> load(Context context, StorageScanner.MgokLocation mgokLocation) {
        File file;
        long j;
        long j2;
        File file2;
        ArrayList arrayList = new ArrayList();
        File effectiveJsonFile = getEffectiveJsonFile(context, mgokLocation);
        if (!effectiveJsonFile.exists()) {
            return arrayList;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(effectiveJsonFile);
            try {
                int length = (int) effectiveJsonFile.length();
                byte[] bArr = new byte[length];
                int i = 0;
                while (i < length) {
                    int i2 = fileInputStream.read(bArr, i, length - i);
                    if (i2 == -1) {
                        break;
                    }
                    i += i2;
                }
                JSONArray jSONArray = new JSONArray(new String(bArr, 0, i, StandardCharsets.UTF_8));
                for (int i3 = 0; i3 < jSONArray.length(); i3++) {
                    JSONObject jSONObject = jSONArray.getJSONObject(i3);
                    int i4 = jSONObject.getInt("code");
                    String string = jSONObject.getString("title");
                    String string2 = jSONObject.getString("singer");
                    String string3 = jSONObject.getString("path");
                    String string4 = jSONObject.getString("ext");
                    long jOptLong = jSONObject.optLong("size", 0L);
                    MegSong megSong = new MegSong();
                    megSong.code = i4;
                    megSong.name = string;
                    megSong.isMp3 = string4.equalsIgnoreCase("mp3");
                    File file3 = new File(string3);
                    if (megSong.isMp3) {
                        file = file3;
                        j2 = 0;
                        j = jOptLong;
                        file2 = null;
                    } else {
                        file = null;
                        j = 0;
                        j2 = jOptLong;
                        file2 = file3;
                    }
                    arrayList.add(new SongLibrary.LibrarySong(megSong, string2, "User Content", "English", file2, 0L, j2, file, 0L, j, "USER"));
                }
                fileInputStream.close();
            } catch (Throwable th) {
                try {
                    fileInputStream.close();
                    throw th;
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                    throw th;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load user content JSON", e);
        }
        return arrayList;
    }

    public static boolean hasUserFiles(StorageScanner.MgokLocation mgokLocation) {
        String[] list;
        File fileUserDir = mgokLocation.userDir();
        if (!fileUserDir.exists() || !fileUserDir.isDirectory() || (list = fileUserDir.list()) == null) {
            return false;
        }
        for (String str : list) {
            String lowerCase = str.toLowerCase(Locale.US);
            if (lowerCase.endsWith(".mid") || lowerCase.endsWith(".midi") || lowerCase.endsWith(".kar") || lowerCase.endsWith(".mp3")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isJsonGenerated(Context context, StorageScanner.MgokLocation mgokLocation) {
        return new File(mgokLocation.root, JSON_FILENAME).exists() || getInternalFallbackFile(context, mgokLocation).exists();
    }

    public static File getEffectiveJsonFile(Context context, StorageScanner.MgokLocation mgokLocation) {
        File file = new File(mgokLocation.root, JSON_FILENAME);
        return file.exists() ? file : getInternalFallbackFile(context, mgokLocation);
    }
}
