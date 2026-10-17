package com.meowsicat.android.storage;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes3.dex */
public class RankingManager {
    private static final String FILE_NAME = "rankings.json";

    public static class RankingEntry {
        public String date;
        public String difficulty;
        public String mode;
        public int score;
        public String singerName;
        public String songCode;
        public String songTitle;

        public RankingEntry() {
            this.songCode = "";
            this.songTitle = "";
            this.singerName = "";
            this.mode = "";
            this.difficulty = "";
            this.date = "";
        }

        public RankingEntry(String str, String str2, String str3, int i, String str4, String str5, String str6) {
            this.songCode = str == null ? "" : str;
            this.songTitle = str2 == null ? "" : str2;
            this.singerName = str3 == null ? "" : str3;
            this.score = i;
            this.mode = str4 == null ? "" : str4;
            this.difficulty = str5 == null ? "" : str5;
            this.date = str6 == null ? "" : str6;
        }
    }

    public static void saveScore(Context context, RankingEntry rankingEntry) {
        List<RankingEntry> listLoadRankings = loadRankings(context);
        listLoadRankings.add(rankingEntry);
        Collections.sort(listLoadRankings, new Comparator<RankingEntry>() { // from class: com.meowsicat.android.storage.RankingManager.1
            @Override // java.util.Comparator
            public int compare(RankingEntry rankingEntry2, RankingEntry rankingEntry3) {
                return Integer.compare(rankingEntry3.score, rankingEntry2.score);
            }
        });
        if (listLoadRankings.size() > 100) {
            listLoadRankings = listLoadRankings.subList(0, 100);
        }
        saveToFile(context, listLoadRankings);
    }

    public static void clearRankings(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists() || file.delete()) {
            return;
        }
        Log.w("RankingManager", "Could not delete rankings file");
    }

    /* JADX WARN: Code duplicated, block: B:18:0x0075  */
    public static List<RankingEntry> loadRankings(Context context) {
        String str;
        ArrayList arrayList = new ArrayList();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return arrayList;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                byte[] bArr = new byte[(int) file.length()];
                fileInputStream.read(bArr);
                JSONArray jSONArray = new JSONArray(new String(bArr, StandardCharsets.UTF_8));
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    String strOptString = jSONObject.optString("songCode", "");
                    String strOptString2 = jSONObject.optString("songTitle", "");
                    String strOptString3 = jSONObject.optString("singerName", "");
                    if (strOptString3.isEmpty() && jSONObject.has("playerName")) {
                        String strOptString4 = jSONObject.optString("playerName", "");
                        if (strOptString4.equalsIgnoreCase("Player 1") || strOptString4.equalsIgnoreCase("TV User")) {
                            str = strOptString3;
                        } else {
                            str = strOptString4;
                        }
                    } else {
                        str = strOptString3;
                    }
                    arrayList.add(new RankingEntry(strOptString, strOptString2, str, jSONObject.optInt("score"), jSONObject.optString("mode"), jSONObject.optString("difficulty"), jSONObject.optString("date")));
                }
                fileInputStream.close();
            } catch (Throwable th) {
                try {
                    fileInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Exception e) {
            Log.e("RankingManager", "Failed to load rankings", e);
        }
        return arrayList;
    }

    private static void saveToFile(Context context, List<RankingEntry> list) {
        try {
            JSONArray jSONArray = new JSONArray();
            for (RankingEntry rankingEntry : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("songCode", rankingEntry.songCode);
                jSONObject.put("songTitle", rankingEntry.songTitle);
                jSONObject.put("singerName", rankingEntry.singerName);
                jSONObject.put("score", rankingEntry.score);
                jSONObject.put("mode", rankingEntry.mode);
                jSONObject.put("difficulty", rankingEntry.difficulty);
                jSONObject.put("date", rankingEntry.date);
                jSONArray.put(jSONObject);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(new File(context.getFilesDir(), FILE_NAME));
            try {
                fileOutputStream.write(jSONArray.toString().getBytes(StandardCharsets.UTF_8));
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
            Log.e("RankingManager", "Failed to save rankings", e);
        }
    }
}
