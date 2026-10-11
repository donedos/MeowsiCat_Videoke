package com.songhub.android.parser;

import java.util.Locale;

/* JADX INFO: loaded from: classes3.dex */
public class LanguageEncoding {
    public static String getEncodingForLanguage(String str) {
        if (str == null || str.isEmpty()) {
            return "ISO-8859-1";
        }
        String strTrim = str.toLowerCase(Locale.ROOT).trim();
        if (containsAny(strTrim, "korean u", "china u", "japan u", "vietnam u", "taiwan u", "user midi")) {
            return "UTF-8";
        }
        if (containsAny(strTrim, "chinese", "china", "mandarin", "cantonese")) {
            return "GBK";
        }
        if (strTrim.contains("korea")) {
            return "EUC-KR";
        }
        if (strTrim.contains("japan")) {
            return "Shift_JIS";
        }
        if (containsAny(strTrim, "taiwan", "hongkong")) {
            return "Big5";
        }
        if (strTrim.contains("russian")) {
            return "CP1251";
        }
        return strTrim.contains("vietnam") ? "ISO-8859-1" : "CP1252";
    }

    private static boolean containsAny(String str, String... strArr) {
        for (String str2 : strArr) {
            if (str.contains(str2)) {
                return true;
            }
        }
        return false;
    }
}
