package com.songhub.android.song;

import android.content.Context;
import android.util.Log;
import com.songhub.android.database.MegSong;
import com.songhub.android.database.MegidxDatabase;
import com.songhub.android.storage.StorageScanner;
import com.songhub.android.storage.UserContentManager;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: classes3.dex */
public class SongLibrary {
    private static SongLibrary instance;
    private static String lastLoadedPath;
    private static long lastModifiedTimestamp;
    public final List<LibrarySong> songs = new ArrayList();
    public final List<String> loadErrors = new ArrayList();

    public static SongLibrary getInstance() {
        return instance;
    }

    public static class LibrarySong {
        private static final Map<String, String> stringPool = new HashMap();
        public final String languageName;
        public final long localByteLength;
        public final long localByteOffset;
        public final File megmidFile;
        public final File megvolFile;
        public final long mp3LocalByteLength;
        public final long mp3LocalByteOffset;
        public final String searchSinger;
        public final String searchTitle;
        public final String singerName;
        public final MegSong song;
        public final String sourceType;
        public final String styleName;

        private static String intern(String str) {
            if (str == null) {
                return null;
            }
            Map<String, String> map = stringPool;
            String str2 = map.get(str);
            if (str2 != null) {
                return str2;
            }
            map.put(str, str);
            return str;
        }

        public LibrarySong(MegSong megSong, String str, String str2, String str3, File file, long j, long j2, File file2, long j3, long j4, String str4) {
            this.song = megSong;
            this.singerName = str;
            this.styleName = intern(str2);
            this.languageName = intern(str3);
            this.megmidFile = file;
            this.localByteOffset = j;
            this.localByteLength = j2;
            this.megvolFile = file2;
            this.mp3LocalByteOffset = j3;
            this.mp3LocalByteLength = j4;
            this.sourceType = intern(str4);
            this.searchTitle = megSong.name != null ? megSong.name.toLowerCase(Locale.getDefault()) : "";
            this.searchSinger = str != null ? str.toLowerCase(Locale.getDefault()) : "";
        }

        public boolean isMp3() {
            return this.song.isMp3;
        }

        public int code() {
            return this.song.code;
        }

        public String title() {
            return this.song.name;
        }
    }

    public static SongLibrary load(Context context) {
        return load(context, StorageScanner.scan(context));
    }

    public static SongLibrary load(Context context, List<StorageScanner.MgokLocation> list) {
        File[] fileArrListFiles;
        if (list == null || list.isEmpty()) {
            return new SongLibrary();
        }
        StringBuilder sb = new StringBuilder();
        long jMax = 0;
        for (StorageScanner.MgokLocation mgokLocation : list) {
            sb.append(mgokLocation.root.getAbsolutePath());
            sb.append(";");
            File fileIdxDir = mgokLocation.idxDir();
            if (fileIdxDir.exists() && fileIdxDir.isDirectory() && (fileArrListFiles = fileIdxDir.listFiles()) != null) {
                for (File file : fileArrListFiles) {
                    String lowerCase = file.getName().toLowerCase();
                    if (lowerCase.equals("shubidx") || lowerCase.endsWith(".shubidx") || lowerCase.equals("megidx") || lowerCase.endsWith(".megidx")) {
                        jMax = Math.max(jMax, file.lastModified());
                    }
                }
            }
            File file2 = new File(mgokLocation.root, "user_content.json");
            if (file2.exists()) {
                jMax = Math.max(jMax, file2.lastModified());
            }
        }
        String string = sb.toString();
        if (instance != null && string.equals(lastLoadedPath) && jMax == lastModifiedTimestamp) {
            return instance;
        }
        SongLibrary songLibrary = new SongLibrary();
        HashSet hashSet = new HashSet();
        Iterator<StorageScanner.MgokLocation> it = list.iterator();
        while (it.hasNext()) {
            loadFromLocation(context, it.next(), songLibrary, hashSet);
        }
        Collections.sort(songLibrary.songs, new Comparator() { // from class: com.songhub.android.song.SongLibrary$$ExternalSyntheticLambda0
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return Integer.compare(((SongLibrary.LibrarySong) obj).code(), ((SongLibrary.LibrarySong) obj2).code());
            }
        });
        instance = songLibrary;
        lastLoadedPath = string;
        lastModifiedTimestamp = jMax;
        return songLibrary;
    }

    public static SongLibrary load(Context context, StorageScanner.MgokLocation mgokLocation) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(mgokLocation);
        return load(context, arrayList);
    }

    private static void loadFromLocation(Context context, StorageScanner.MgokLocation mgokLocation, SongLibrary songLibrary, Set<Integer> set) {
        File[] fileArrListFiles;
        File fileIdxDir = mgokLocation.idxDir();
        try {
            List<LibrarySong> listLoad = UserContentManager.load(context, mgokLocation);
            for (LibrarySong librarySong : listLoad) {
                if (!set.contains(Integer.valueOf(librarySong.code()))) {
                    songLibrary.songs.add(librarySong);
                    set.add(Integer.valueOf(librarySong.code()));
                }
            }
            Log.d("SongLibrary", "Loaded " + listLoad.size() + " user songs from " + mgokLocation.volumeLabel);
        } catch (Exception e) {
            songLibrary.loadErrors.add("Failed to load user content from " + mgokLocation.volumeLabel + ": " + e.getMessage());
        }
        if (!fileIdxDir.exists() || !fileIdxDir.isDirectory() || (fileArrListFiles = fileIdxDir.listFiles(new FileFilter() { // from class: com.songhub.android.song.SongLibrary$$ExternalSyntheticLambda3
            @Override // java.io.FileFilter
            public final boolean accept(File file) {
                return SongLibrary.lambda$loadFromLocation$1(file);
            }
        })) == null || fileArrListFiles.length <= 0) {
            return;
        }
        for (File file : fileArrListFiles) {
            try {
                int size = songLibrary.songs.size();
                loadOneDatabase(file, songLibrary, set);
                Log.d("SongLibrary", "Loaded " + (songLibrary.songs.size() - size) + " database songs from " + file.getName());
            } catch (Throwable th) {
                String str = "Failed to load " + file.getName() + ": " + th.getMessage();
                songLibrary.loadErrors.add(str);
                Log.e("SongLibrary", str, th);
            }
        }
    }

    static /* synthetic */ boolean lambda$loadFromLocation$1(File file) {
        String lowerCase = file.getName().toLowerCase();
        return file.isFile() && (lowerCase.equals("shubidx") || lowerCase.endsWith(".shubidx") || lowerCase.equals("megidx") || lowerCase.endsWith(".megidx"));
    }

    private static void loadOneDatabase(File file, SongLibrary songLibrary, Set<Integer> set) throws Exception {
        String str;
        String str2;
        File file2;
        long j;
        long j2;
        File fileResolveVolume = null;
        long jResolveLocalOffset = 0;
        MegidxDatabase megidxDatabaseLoad = MegidxDatabase.load(file.getAbsolutePath());
        File parentFile = file.getParentFile();
        File parentFile2 = parentFile.getParentFile();
        List<File> listFindVolumes = findVolumes(parentFile, "shubblob");
        if (listFindVolumes.isEmpty()) {
            listFindVolumes = findVolumes(parentFile, "megmid");
        }
        if (listFindVolumes.isEmpty() && parentFile2 != null) {
            listFindVolumes = findVolumes(parentFile2, "shubblob");
            if (listFindVolumes.isEmpty()) {
                listFindVolumes = findVolumes(parentFile2, "megmid");
            }
        }
        List<File> listFindVolumes2 = findVolumes(parentFile, "megvol");
        if (listFindVolumes2.isEmpty() && parentFile2 != null) {
            listFindVolumes2 = findVolumes(parentFile2, "megvol");
        }
        if (listFindVolumes.isEmpty()) {
            songLibrary.loadErrors.add("No shubblob/megmid container files found for " + file.getName());
            return;
        }
        String lowerCase = file.getName().toLowerCase();
        Iterator<MegSong> it = megidxDatabaseLoad.songs.iterator();
        while (it.hasNext()) {
            MegSong next = it.next();
            if (!set.contains(Integer.valueOf(next.code))) {
                if (lowerCase.contains("user")) {
                    str = "USER";
                } else {
                    str = next.isMp3 ? "MP3" : "MIDI";
                }
                String str3 = str;
                String str4 = (next.singerIdx < 0 || next.singerIdx >= megidxDatabaseLoad.singers.size()) ? "" : megidxDatabaseLoad.singers.get(next.singerIdx).name;
                String str5 = megidxDatabaseLoad.styleMap.containsKey(Integer.valueOf(next.style)) ? megidxDatabaseLoad.styleMap.get(Integer.valueOf(next.style)) : "";
                String str6 = (next.langId < 0 || next.langId >= megidxDatabaseLoad.languages.size()) ? "" : megidxDatabaseLoad.languages.get(next.langId);
                long jMidiByteOffset = next.midiByteOffset();
                long jMidiByteLength = next.midiByteLength();
                File fileResolveVolume2 = resolveVolume(listFindVolumes, jMidiByteOffset);
                long jResolveLocalOffset2 = resolveLocalOffset(listFindVolumes, jMidiByteOffset);
                if (next.isMp3) {
                    long j3 = ((long) next.mp3SizeSectors) * 2048;
                    if (next.mp3Vol >= 0) {
                        str2 = lowerCase;
                        if (next.mp3Vol < listFindVolumes2.size()) {
                            fileResolveVolume = listFindVolumes2.get(next.mp3Vol);
                            jResolveLocalOffset = ((long) next.mp3PosSectors) * 2048;
                        }
                        file2 = fileResolveVolume;
                        j = jResolveLocalOffset;
                        j2 = j3;
                    } else {
                        str2 = lowerCase;
                    }
                    long j4 = ((long) next.mp3PosSectors) * 2048;
                    fileResolveVolume = resolveVolume(listFindVolumes2, j4);
                    jResolveLocalOffset = resolveLocalOffset(listFindVolumes2, j4);
                    file2 = fileResolveVolume;
                    j = jResolveLocalOffset;
                    j2 = j3;
                } else {
                    str2 = lowerCase;
                    it = it;
                    file2 = null;
                    j = 0;
                    j2 = 0;
                }
                songLibrary.songs.add(new LibrarySong(next, str4, str5, str6, fileResolveVolume2, jResolveLocalOffset2, jMidiByteLength, file2, j, j2, str3));
                set.add(Integer.valueOf(next.code));
                lowerCase = str2;
                megidxDatabaseLoad = megidxDatabaseLoad;
                it = it;
            }
        }
    }

    private static File resolveVolume(List<File> list, long j) {
        if (list.isEmpty()) {
            return null;
        }
        long length = 0;
        for (File file : list) {
            length += file.length();
            if (j < length) {
                return file;
            }
        }
        return list.get(list.size() - 1);
    }

    private static long resolveLocalOffset(List<File> list, long j) {
        long j2 = 0;
        if (list.isEmpty()) {
            return 0L;
        }
        Iterator<File> it = list.iterator();
        while (it.hasNext()) {
            long length = it.next().length() + j2;
            if (j < length) {
                return j - j2;
            }
            j2 = length;
        }
        return j - j2;
    }

    private static List<File> findVolumes(File file, final String str) {
        ArrayList arrayList = new ArrayList();
        File[] fileArrListFiles = file.listFiles(new FileFilter() { // from class: com.songhub.android.song.SongLibrary$$ExternalSyntheticLambda1
            @Override // java.io.FileFilter
            public final boolean accept(File file2) {
                return SongLibrary.lambda$findVolumes$2(str, file2);
            }
        });
        if (fileArrListFiles != null) {
            for (File file2 : fileArrListFiles) {
                arrayList.add(file2);
            }
            Collections.sort(arrayList, new Comparator() { // from class: com.songhub.android.song.SongLibrary$$ExternalSyntheticLambda2
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return ((File) obj).getName().compareTo(((File) obj2).getName());
                }
            });
        }
        return arrayList;
    }

    static /* synthetic */ boolean lambda$findVolumes$2(String str, File file) {
        return file.isFile() && file.getName().toLowerCase().startsWith(str);
    }
}
