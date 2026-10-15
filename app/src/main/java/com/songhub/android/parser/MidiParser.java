package com.songhub.android.parser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes3.dex */
public final class MidiParser {
    private static final String[] METADATA_TAGS = {"lag", "lan", "sty", "t", "s", "l", "m", "x"};
    private static final Pattern CHORD_TRACK_NAME_RE = Pattern.compile("^(chord|chords|chordname|chord name)$", 2);

    public static final class ParseResult {
        public List<TempoUtils.TempoEntry> tempoMap;
        public int ticksPerBeat;
        public List<TickMsText> lyricEventsTicks = new ArrayList();
        public List<TickMsText> chordEventsTicks = new ArrayList();
        public List<TickMsText> countdownEventsTicks = new ArrayList();
        public Map<String, String> metadata = new LinkedHashMap();
        public List<NoteSpan>[] noteRunsByChannel = new ArrayList[16];
        public int[] melodyChannelRank = new int[16];
        public boolean[] isDrumChannel = new boolean[128];
        public int[] drumMapForChannel = new int[128];
        public List<byte[]> collectedSysexBlocks = new ArrayList();
        public int systemMode = 4;
    }

    private MidiParser() {
    }

    public static final class TickText {
        public final String text;
        public final long tick;

        public TickText(long j, String str) {
            this.tick = j;
            this.text = str;
        }
    }

    public static final class TickMsText {
        public final double ms;
        public final String text;
        public final long tick;

        public TickMsText(long j, double d, String str) {
            this.tick = j;
            this.ms = d;
            this.text = str;
        }
    }

    public static final class NoteSpan {
        public final long onTick;
        public final long offTick;

        public NoteSpan(long j, long j2) {
            this.onTick = j;
            this.offTick = j2;
        }
    }

    public static Map<String, String> extractMetadataFromMarker(String str, boolean z) {
        String str2;
        int length;
        int i;
        String strTrim;
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        String[] strArr = (String[]) METADATA_TAGS.clone();
        Arrays.sort(strArr, new Comparator() { // from class: com.songhub.android.parser.MidiParser$$ExternalSyntheticLambda4
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return MidiParser.lambda$extractMetadataFromMarker$0((String) obj, (String) obj2);
            }
        });
        int length2 = str.length();
        int i2 = 0;
        while (true) {
            int iIndexOf = str.indexOf(64, i2);
            if (iIndexOf == -1) {
                return linkedHashMap;
            }
            i2 = iIndexOf + 1;
            int length3 = strArr.length;
            int i3 = 0;
            while (true) {
                if (i3 >= length3) {
                    str2 = null;
                    length = -1;
                    break;
                }
                str2 = strArr[i3];
                length = str2.length() + i2;
                if (length <= length2 && str.substring(i2, length).equalsIgnoreCase(str2) && (!z || (!str2.equals("m") && !str2.equals("w") && !str2.equals("c")))) {
                    break;
                }
                i3++;
            }
            if (str2 != null) {
                int iIndexOf2 = str.indexOf(64, length);
                if (iIndexOf2 == -1) {
                    strTrim = str.substring(length).trim();
                    i = length2;
                } else {
                    i = iIndexOf2;
                    strTrim = str.substring(length, iIndexOf2).trim();
                }
                linkedHashMap.put(str2, strTrim);
                i2 = i;
            }
        }
    }

    static /* synthetic */ int lambda$extractMetadataFromMarker$0(String str, String str2) {
        return str2.length() - str.length();
    }

    private static boolean isChordTrackName(String str) {
        if (str == null) {
            return false;
        }
        String lowerCase = str.trim().toLowerCase();
        if (lowerCase.isEmpty()) {
            return false;
        }
        if (CHORD_TRACK_NAME_RE.matcher(lowerCase).matches()) {
            return true;
        }
        String strReplaceAll = lowerCase.replaceAll("[^a-z]", "");
        return strReplaceAll.equals("chord") || strReplaceAll.equals("chords") || strReplaceAll.equals("chordname");
    }

    private static boolean looksLikeLyric(String str) {
        char lowerCase;
        if (str.equals(" ")) {
            return true;
        }
        String strTrim = str.trim();
        if (strTrim.isEmpty()) {
            return str.contains("\r") || str.contains("\n");
        }
        if (!strTrim.startsWith("/") && !strTrim.startsWith("\\")) {
            if (strTrim.charAt(0) == '#') {
                return false;
            }
            if (strTrim.charAt(0) == '@') {
                return strTrim.length() > 1 && ((lowerCase = Character.toLowerCase(strTrim.charAt(1))) == 'm' || lowerCase == 'w' || lowerCase == 'c');
            }
            int i = 0;
            int i2 = 0;
            for (char c : strTrim.toCharArray()) {
                if (Character.isLetter(c)) {
                    i++;
                }
                if (Character.isDigit(c)) {
                    i2++;
                }
            }
            if (i == 0 && i2 == 0) {
                for (char c2 : strTrim.toCharArray()) {
                    if (":!?%().,'`’\"-~ ".indexOf(c2) == -1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /* JADX WARN: Code duplicated, block: B:143:0x03db  */
    /* JADX WARN: Code duplicated, block: B:145:0x03de  */
    /* JADX WARN: Code duplicated, block: B:146:0x03e0  */
    /* JADX WARN: Code duplicated, block: B:165:0x0428  */
    /* JADX WARN: Code duplicated, block: B:225:0x0613  */
    /* JADX WARN: Code duplicated, block: B:228:0x0622  */
    /* JADX WARN: Code duplicated, block: B:232:0x0636  */
    /* JADX WARN: Code duplicated, block: B:236:0x065f  */
    /* JADX WARN: Code duplicated, block: B:238:0x0667  */
    /* JADX WARN: Code duplicated, block: B:243:0x069d  */
    /* JADX WARN: Code duplicated, block: B:245:0x06a8 A[LOOP:11: B:244:0x06a6->B:245:0x06a8, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:266:0x0729 A[LOOP:12: B:264:0x0723->B:266:0x0729, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:270:0x0759 A[LOOP:13: B:268:0x0753->B:270:0x0759, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:274:0x077f A[LOOP:14: B:272:0x0779->B:274:0x077f, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:306:0x0630 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:308:0x061c A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:312:0x0674 A[SYNTHETIC] */
    /* JADX WARN: Instruction removed from duplicated block: B:245:0x06a8, please report this as an issue */
    public static ParseResult parse(byte[] bArr, String str) throws Exception {
        int[] iArr;
        ArrayList arrayList;
        int i;
        long j;
        LinkedHashMap linkedHashMap;
        int i2;
        ArrayList<TickText> arrayList2;
        String[] strArr;
        Iterator it;
        ParseResult parseResult;
        TickText tickText;
        String strReplaceAll;
        int i3;
        String strTrim;
        StringBuilder sb;
        Map<String, String> mapExtractMetadataFromMarker;
        String[] strArr2;
        int i4;
        String str2;
        Iterator it2;
        int[] iArr2;
        ArrayList arrayList3;
        ArrayList arrayList4;
        ArrayList arrayList5;
        String str3;
        String str4;
        ArrayList arrayList6;
        String str5;
        long j2;
        String str6;
        ArrayList arrayList7;
        int i5;
        ArrayList arrayList8;
        ByteArrayOutputStream byteArrayOutputStream;
        ArrayList arrayList9;
        boolean[] zArr;
        int[] iArr3;
        boolean z;
        boolean z2;
        Integer num;
        String str7;
        String str8;
        byte[] bArr2 = bArr;
        if (bArr2.length >= 4) {
            byte b = 77;
            if (bArr2[0] == 77 && bArr2[1] == 84 && bArr2[2] == 104 && bArr2[3] == 100) {
                long be32 = MidiBytes.readBe32(bArr2, 4);
                int be16 = MidiBytes.readBe16(bArr2, 10);
                int be17 = MidiBytes.readBe16(bArr2, 12);
                int i6 = (int) (be32 + 8);
                ArrayList arrayList10 = new ArrayList();
                int i7 = 0;
                while (i7 < be16) {
                    if (bArr2[i6] != b || bArr2[i6 + 1] != 84 || bArr2[i6 + 2] != 114 || bArr2[i6 + 3] != 107) {
                        throw new IllegalArgumentException("Expected MTrk at offset " + i6);
                    }
                    long be33 = MidiBytes.readBe32(bArr2, i6 + 4);
                    int i8 = i6 + 8;
                    int i9 = (int) (((long) i8) + be33);
                    arrayList10.add(new int[]{i8, i9});
                    i7++;
                    i6 = i9;
                    b = 77;
                }
                ArrayList arrayList11 = new ArrayList();
                ArrayList arrayList12 = new ArrayList();
                ArrayList arrayList13 = new ArrayList();
                ArrayList arrayList14 = new ArrayList();
                ArrayList arrayList15 = new ArrayList();
                boolean[] zArr2 = new boolean[128];
                int[] iArr4 = new int[128];
                ArrayList arrayList16 = new ArrayList();
                zArr2[9] = true;
                iArr4[9] = 1;
                Iterator it3 = arrayList10.iterator();
                int i10 = 4;
                while (true) {
                    String str9 = "[^@]*";
                    String str10 = "(?i)@";
                    ArrayList arrayList17 = arrayList11;
                    String str11 = "@";
                    String str12 = "\n";
                    int i11 = be17;
                    if (it3.hasNext()) {
                        int[] iArr5 = (int[]) it3.next();
                        Iterator it4 = it3;
                        int i12 = iArr5[0];
                        boolean[] zArr3 = zArr2;
                        int i13 = iArr5[1];
                        boolean zIsChordTrackName = isChordTrackName(scanTrackName(bArr2, i12, i13, str));
                        int[] iArr6 = iArr4;
                        ByteArrayOutputStream byteArrayOutputStream2 = null;
                        Integer numValueOf = null;
                        long j3 = 0;
                        while (true) {
                            if (i12 >= i13) {
                                arrayList4 = arrayList14;
                                arrayList5 = arrayList16;
                                break;
                            }
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                            MidiBytes.VlqResult vlq = MidiBytes.readVlq(bArr2, i12);
                            ArrayList arrayList18 = arrayList16;
                            int i14 = vlq.nextIndex;
                            String str13 = str9;
                            ArrayList arrayList19 = arrayList14;
                            long j4 = j3 + vlq.value;
                            if (i14 >= i13) {
                                arrayList5 = arrayList18;
                                arrayList4 = arrayList19;
                                break;
                            }
                            ByteArrayOutputStream byteArrayOutputStream3 = byteArrayOutputStream2;
                            int iIntValue = bArr2[i14] & 255;
                            if (iIntValue >= 128) {
                                i14++;
                            } else if (numValueOf != null) {
                                iIntValue = numValueOf.intValue();
                            } else {
                                throw new IllegalArgumentException("Running status error at " + i14);
                            }
                            if (iIntValue == 255) {
                                int i15 = bArr2[i14] & 255;
                                MidiBytes.VlqResult vlq2 = MidiBytes.readVlq(bArr2, i14 + 1);
                                int i16 = vlq2.nextIndex;
                                String str14 = str10;
                                int i17 = i13;
                                int i18 = (int) vlq2.value;
                                int i19 = i16 + i18;
                                byte[] bArrCopyOfRange = Arrays.copyOfRange(bArr2, i16, i19);
                                if (i15 == 81 && i18 == 3) {
                                    arrayList13.add(new TempoUtils.TempoEvent(j4, ((((long) bArrCopyOfRange[0]) & 255) << 16) | ((((long) bArrCopyOfRange[1]) & 255) << 8) | (((long) bArrCopyOfRange[2]) & 255)));
                                }
                                if (i15 == 1 || i15 == 5 || i15 == 6) {
                                    try {
                                        str7 = new String(bArrCopyOfRange, str);
                                    } catch (Exception unused) {
                                        str7 = new String(bArrCopyOfRange, "ISO-8859-1");
                                    }
                                    String strReplace = str7.replace('<', ' ');
                                    if (i15 != 5) {
                                        arrayList7 = arrayList17;
                                        arrayList6 = arrayList19;
                                        str5 = str13;
                                        str6 = str14;
                                        str3 = str11;
                                        str4 = str12;
                                        ArrayList arrayList20 = arrayList12;
                                        if (i15 != 1) {
                                            arrayList12 = arrayList20;
                                            if (!zIsChordTrackName && !strReplace.trim().isEmpty()) {
                                                arrayList6.add(new TickText(j4, strReplace));
                                            }
                                        } else if (zIsChordTrackName) {
                                            String strTrim2 = strReplace.replace("\r", "").replace(str4, "").trim();
                                            if (!strTrim2.isEmpty()) {
                                                arrayList15.add(new TickText(j4, strTrim2));
                                            }
                                            arrayList12 = arrayList20;
                                        } else {
                                            arrayList12 = arrayList20;
                                            arrayList12.add(new TickText(j4, strReplace));
                                        }
                                    } else if (zIsChordTrackName) {
                                        str4 = str12;
                                        String strTrim3 = strReplace.replace("\r", "").replace(str4, "").trim();
                                        if (!strTrim3.isEmpty()) {
                                            arrayList15.add(new TickText(j4, strTrim3));
                                        }
                                        arrayList7 = arrayList17;
                                        arrayList6 = arrayList19;
                                        str5 = str13;
                                        str6 = str14;
                                        str3 = str11;
                                    } else {
                                        str4 = str12;
                                        Map<String, String> mapExtractMetadataFromMarker2 = extractMetadataFromMarker(strReplace, true);
                                        if (!mapExtractMetadataFromMarker2.isEmpty()) {
                                            Iterator<Map.Entry<String, String>> it5 = mapExtractMetadataFromMarker2.entrySet().iterator();
                                            while (it5.hasNext()) {
                                                Map.Entry<String, String> next = it5.next();
                                                String str15 = str11;
                                                arrayList19.add(new TickText(j4, str15 + next.getKey() + next.getValue()));
                                                it5 = it5;
                                                str11 = str15;
                                            }
                                            arrayList6 = arrayList19;
                                            str3 = str11;
                                            Iterator<String> it6 = mapExtractMetadataFromMarker2.keySet().iterator();
                                            while (it6.hasNext()) {
                                                strReplace = strReplace.replaceAll(str14 + Pattern.quote(it6.next()) + str13, "");
                                            }
                                            str8 = str13;
                                            str6 = str14;
                                            if (!strReplace.trim().isEmpty() || strReplace.contains("\r") || strReplace.contains(str4)) {
                                                arrayList7 = arrayList17;
                                                arrayList7.add(new TickText(j4, strReplace));
                                            } else {
                                                arrayList7 = arrayList17;
                                            }
                                        } else {
                                            arrayList7 = arrayList17;
                                            arrayList6 = arrayList19;
                                            str8 = str13;
                                            str6 = str14;
                                            str3 = str11;
                                            arrayList7.add(new TickText(j4, strReplace));
                                        }
                                        str5 = str8;
                                    }
                                } else {
                                    arrayList7 = arrayList17;
                                    arrayList6 = arrayList19;
                                    str5 = str13;
                                    str6 = str14;
                                    str3 = str11;
                                    str4 = str12;
                                }
                                arrayList13 = arrayList13;
                                j2 = j4;
                                arrayList8 = arrayList15;
                                i14 = i19;
                                zArr = zArr3;
                                num = null;
                                iArr3 = iArr6;
                                arrayList9 = arrayList18;
                                i5 = i17;
                            } else {
                                str3 = str11;
                                str4 = str12;
                                arrayList12 = arrayList12;
                                int i20 = i13;
                                arrayList6 = arrayList19;
                                str5 = str13;
                                j2 = j4;
                                str6 = str10;
                                arrayList7 = arrayList17;
                                if (iIntValue == 240 || iIntValue == 247) {
                                    MidiBytes.VlqResult vlq3 = MidiBytes.readVlq(bArr2, i14);
                                    int i21 = (int) vlq3.value;
                                    int i22 = vlq3.nextIndex;
                                    i5 = i20;
                                    int iMin = Math.min(i5, i21 + i22);
                                    i14 = iMin;
                                    num = null;
                                    zArr = zArr3;
                                    iArr3 = iArr6;
                                    arrayList8 = arrayList15;
                                    arrayList9 = arrayList18;
                                } else {
                                    if (iIntValue >= 128) {
                                        numValueOf = Integer.valueOf(iIntValue);
                                        int i23 = iIntValue & 240;
                                        i14 += (i23 == 192 || i23 == 208) ? 1 : 2;
                                    } else {
                                        int iIntValue2 = 240 & numValueOf.intValue();
                                        if (iIntValue2 != 192 && iIntValue2 != 208) {
                                            i14++;
                                        }
                                    }
                                    zArr = zArr3;
                                    num = numValueOf;
                                    iArr3 = iArr6;
                                    i5 = i20;
                                    arrayList8 = arrayList15;
                                    arrayList9 = arrayList18;
                                }
                            }
                            str9 = str5;
                            zArr3 = zArr;
                            iArr6 = iArr3;
                            arrayList12 = arrayList12;
                            arrayList13 = arrayList13;
                            byteArrayOutputStream2 = byteArrayOutputStream3;
                            j3 = j2;
                            i12 = i14;
                            arrayList16 = arrayList9;
                            arrayList17 = arrayList7;
                            str10 = str6;
                            arrayList15 = arrayList8;
                            numValueOf = num;
                            i13 = i5;
                            bArr2 = bArr;
                            arrayList14 = arrayList6;
                            str12 = str4;
                            str11 = str3;
                            str = str;
                        }
                        bArr2 = bArr;
                        arrayList14 = arrayList4;
                        zArr2 = zArr3;
                        arrayList16 = arrayList5;
                        arrayList11 = arrayList17;
                        arrayList12 = arrayList12;
                        arrayList13 = arrayList13;
                        be17 = i11;
                        arrayList15 = arrayList15;
                        iArr4 = iArr6;
                        it3 = it4;
                    } else {
                        ArrayList<TickText> arrayList21 = arrayList14;
                        ArrayList<TickText> arrayList22 = arrayList15;
                        int[] iArr7 = iArr4;
                        ArrayList arrayList23 = arrayList16;
                        ArrayList<TickText> arrayList24 = arrayList12;
                        String str16 = "[^@]*";
                        ArrayList arrayList25 = arrayList13;
                        boolean[] zArr4 = zArr2;
                        if (arrayList17.isEmpty() && !arrayList24.isEmpty()) {
                            Iterator it7 = arrayList24.iterator();
                            while (it7.hasNext()) {
                                TickText tickText2 = (TickText) it7.next();
                                String strReplaceAll2 = tickText2.text;
                                if (strReplaceAll2.startsWith("@")) {
                                    Map<String, String> mapExtractMetadataFromMarker3 = extractMetadataFromMarker(strReplaceAll2, true);
                                    if (!mapExtractMetadataFromMarker3.isEmpty()) {
                                        for (Map.Entry<String, String> entry : mapExtractMetadataFromMarker3.entrySet()) {
                                            int[] iArr8 = iArr7;
                                            arrayList21.add(new TickText(tickText2.tick, "@" + entry.getKey() + entry.getValue()));
                                            it7 = it7;
                                            strReplaceAll2 = strReplaceAll2;
                                            iArr7 = iArr8;
                                            arrayList23 = arrayList23;
                                        }
                                        it2 = it7;
                                        iArr2 = iArr7;
                                        arrayList3 = arrayList23;
                                        Iterator<String> it8 = mapExtractMetadataFromMarker3.keySet().iterator();
                                        while (it8.hasNext()) {
                                            strReplaceAll2 = strReplaceAll2.replaceAll("(?i)@" + Pattern.quote(it8.next()) + str16, "");
                                        }
                                        if (!strReplaceAll2.trim().isEmpty() || strReplaceAll2.contains("\r") || strReplaceAll2.contains("\n")) {
                                            arrayList17.add(new TickText(tickText2.tick, strReplaceAll2));
                                        }
                                    } else {
                                        it2 = it7;
                                        iArr2 = iArr7;
                                        arrayList3 = arrayList23;
                                        arrayList21.add(tickText2);
                                    }
                                } else {
                                    it2 = it7;
                                    iArr2 = iArr7;
                                    arrayList3 = arrayList23;
                                    arrayList17.add(tickText2);
                                }
                                it7 = it2;
                                iArr7 = iArr2;
                                arrayList23 = arrayList3;
                            }
                            iArr = iArr7;
                            arrayList = arrayList23;
                        } else {
                            iArr = iArr7;
                            arrayList = arrayList23;
                            if (!arrayList17.isEmpty() && !arrayList24.isEmpty()) {
                                for (TickText tickText3 : arrayList24) {
                                    if (!tickText3.text.trim().isEmpty()) {
                                        arrayList21.add(tickText3);
                                    }
                                }
                            }
                        }
                        Collections.sort(arrayList17, new Comparator() { // from class: com.songhub.android.parser.MidiParser$$ExternalSyntheticLambda0
                            @Override // java.util.Comparator
                            public final int compare(Object obj, Object obj2) {
                                return Long.compare(((MidiParser.TickText) obj).tick, ((MidiParser.TickText) obj2).tick);
                            }
                        });
                        Collections.sort(arrayList25, new Comparator() { // from class: com.songhub.android.parser.MidiParser$$ExternalSyntheticLambda1
                            @Override // java.util.Comparator
                            public final int compare(Object obj, Object obj2) {
                                return Long.compare(((TempoUtils.TempoEvent) obj).tick, ((TempoUtils.TempoEvent) obj2).tick);
                            }
                        });
                        Collections.sort(arrayList21, new Comparator() { // from class: com.songhub.android.parser.MidiParser$$ExternalSyntheticLambda2
                            @Override // java.util.Comparator
                            public final int compare(Object obj, Object obj2) {
                                return Long.compare(((MidiParser.TickText) obj).tick, ((MidiParser.TickText) obj2).tick);
                            }
                        });
                        if (!arrayList25.isEmpty()) {
                            if (((TempoUtils.TempoEvent) arrayList25.get(0)).tick != 0) {
                                arrayList25.add(0, new TempoUtils.TempoEvent(0L, 500000L));
                            }
                        } else {
                            arrayList25.add(0, new TempoUtils.TempoEvent(0L, 500000L));
                        }
                        linkedHashMap = new LinkedHashMap();
                        i2 = 7;
                        if (!arrayList21.isEmpty()) {
                            sb = new StringBuilder();
                            for (TickText tickText4 : arrayList21) {
                                if (!tickText4.text.contains("@")) {
                                    if (sb.length() > 0) {
                                        sb.append(' ');
                                    }
                                    sb.append(tickText4.text);
                                }
                            }
                            mapExtractMetadataFromMarker = extractMetadataFromMarker(sb.toString(), false);
                            strArr2 = new String[]{"t", "s", "l", "m", "lag", "lan", "sty"};
                            for (i4 = 0; i4 < 7; i4++) {
                                str2 = strArr2[i4];
                                if (mapExtractMetadataFromMarker.containsKey(str2)) {
                                    linkedHashMap.put(str2, mapExtractMetadataFromMarker.get(str2).trim());
                                }
                            }
                        }
                        arrayList2 = new ArrayList();
                        ArrayList<TickText> arrayList26 = new ArrayList();
                        strArr = new String[]{"lag", "lan", "sty", "t", "s", "l", "x"};
                        it = arrayList17.iterator();
                        StringBuilder lyricAccum = new StringBuilder();
                        long lyricLineTick = 0;
                        while (it.hasNext()) {
                            tickText = (TickText) it.next();
                            strReplaceAll = tickText.text;
                            for (i3 = 0; i3 < i2; i3++) {
                                strReplaceAll = strReplaceAll.replaceAll("(?i)@" + Pattern.quote(strArr[i3]) + str16, "");
                            }
                            strTrim = strReplaceAll.trim();
                            if (!strTrim.isEmpty() && strTrim.charAt(0) == '#') {
                                if (lyricAccum.length() > 0) {
                                    arrayList2.add(new TickText(lyricLineTick, lyricAccum.toString()));
                                    lyricAccum.setLength(0);
                                }
                                lyricLineTick = 0;
                                arrayList26.add(new TickText(tickText.tick, strReplaceAll));
                            } else if (looksLikeLyric(strReplaceAll)) {
                                int lyricIdxR = strReplaceAll.indexOf(13);
                                int lyricIdxN = strReplaceAll.indexOf(10);
                                int lyricCut = lyricIdxR >= 0 || lyricIdxN >= 0 ? (lyricIdxR >= 0 ? lyricIdxR : lyricIdxN) : -1;
                                if (lyricCut >= 0) {
                                    if (lyricCut > 0) {
                                        lyricAccum.append(strReplaceAll, 0, lyricCut);
                                    }
                                    if (lyricAccum.length() > 0) {
                                        arrayList2.add(new TickText(lyricLineTick, lyricAccum.toString()));
                                        lyricAccum.setLength(0);
                                    }
                                    lyricLineTick = 0;
                                } else {
                                    if (lyricLineTick == 0) {
                                        lyricLineTick = tickText.tick;
                                    }
                                    lyricAccum.append(strReplaceAll);
                                }
                            }
                            i2 = 7;
                        }
                        if (lyricAccum.length() > 0) {
                            arrayList2.add(new TickText(lyricLineTick, lyricAccum.toString()));
                        }
                        parseResult = new ParseResult();
                        parseResult.ticksPerBeat = i11;
                        for (TickText tickText5 : arrayList2) {
                            parseResult.lyricEventsTicks.add(new TickMsText(tickText5.tick, ticksToMsSegmented(arrayList25, i11, tickText5.tick), tickText5.text));
                        }
                        Collections.sort(arrayList22, new Comparator() { // from class: com.songhub.android.parser.MidiParser$$ExternalSyntheticLambda3
                            @Override // java.util.Comparator
                            public final int compare(Object obj, Object obj2) {
                                return Long.compare(((MidiParser.TickText) obj).tick, ((MidiParser.TickText) obj2).tick);
                            }
                        });
                        for (TickText tickText6 : arrayList22) {
                            parseResult.chordEventsTicks.add(new TickMsText(tickText6.tick, ticksToMsSegmented(arrayList25, i11, tickText6.tick), tickText6.text));
                        }
                        for (TickText tickText7 : arrayList26) {
                            parseResult.countdownEventsTicks.add(new TickMsText(tickText7.tick, ticksToMsSegmented(arrayList25, i11, tickText7.tick), tickText7.text));
                        }
                        parseResult.tempoMap = TempoUtils.buildTempoMap(arrayList25, i11);
                        parseResult.metadata = linkedHashMap;
                        parseResult.isDrumChannel = zArr4;
                        parseResult.drumMapForChannel = iArr;
                        parseResult.collectedSysexBlocks = arrayList;
                        parseResult.systemMode = i10;
                        parseResult.noteRunsByChannel = collectNoteRuns(bArr2);
                        parseResult.melodyChannelRank = collectMelodyChannelRank(bArr2);
                        return parseResult;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Not a valid MIDI file");
    }

    private static byte[] ensureF7Framing(byte[] bArr) {
        if (bArr.length == 0 || (bArr[bArr.length - 1] & 255) == 247) {
            return bArr;
        }
        byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + 1);
        bArrCopyOf[bArrCopyOf.length - 1] = -9;
        return bArrCopyOf;
    }

    private static void checkSysexForDrumAssignment(byte[] bArr, boolean[] zArr, int[] iArr) {
        int i;
        int i2;
        int i3;
        int i4;
        if (bArr.length < 6) {
            return;
        }
        int i5 = 0;
        while (true) {
            int i6 = i5 + 5;
            if (i6 >= bArr.length) {
                return;
            }
            int i7 = bArr[i5] & 255;
            if (i7 == 65 && (i3 = i5 + 7) < bArr.length && (bArr[i5 + 2] & 255) == 66 && (bArr[i5 + 3] & 255) == 18 && (bArr[i5 + 4] & 255) == 64 && (bArr[i5 + 6] & 255) == 21) {
                byte b = bArr[i6];
                int i8 = b & 255;
                int i9 = b & 240;
                if (i9 == 16) {
                    i4 = 0;
                } else if (i9 == 32) {
                    i4 = 1;
                } else if (i9 == 64) {
                    i4 = 2;
                } else {
                    i4 = i9 == 80 ? 3 : (i8 >> 4) - 1;
                }
                int i10 = b & 15;
                if (i10 == 0) {
                    i10 = 9;
                } else if (i10 <= 9) {
                    i10--;
                }
                int i11 = i10 + (i4 * 16);
                if (i11 >= 0 && i11 < 128) {
                    int i12 = bArr[i3] & 255;
                    zArr[i11] = i12 != 0;
                    iArr[i11] = i12;
                }
            }
            if (i7 == 67 && (i = i5 + 6) < bArr.length && (bArr[i5 + 2] & 255) == 76 && (bArr[i5 + 3] & 255) == 8 && (bArr[i6] & 255) == 7 && (i2 = bArr[i5 + 4] & 255) < 128) {
                int i13 = bArr[i] & 255;
                zArr[i2] = i13 != 0;
                iArr[i2] = i13;
            }
            i5++;
        }
    }

    private static boolean isGsResetBlock(byte[] bArr) {
        return bArr.length >= 9 && (bArr[0] & 255) == 240 && (bArr[1] & 255) == 65 && (bArr[3] & 255) == 66 && (bArr[4] & 255) == 18 && (bArr[5] & 255) == 64 && (bArr[6] & 255) == 0 && (bArr[7] & 255) == 127;
    }

    private static boolean isXgResetBlock(byte[] bArr) {
        return bArr.length >= 9 && (bArr[0] & 255) == 240 && (bArr[1] & 255) == 67 && (bArr[3] & 255) == 76 && (bArr[4] & 255) == 0 && (bArr[5] & 255) == 0 && (bArr[6] & 255) == 126;
    }

    private static boolean isGm1ResetBlock(byte[] bArr) {
        return bArr.length >= 6 && (bArr[0] & 255) == 240 && (bArr[1] & 255) == 126 && (bArr[2] & 255) == 127 && (bArr[3] & 255) == 9 && (bArr[4] & 255) == 1;
    }

    private static boolean isGm2ResetBlock(byte[] bArr) {
        return bArr.length >= 6 && (bArr[0] & 255) == 240 && (bArr[1] & 255) == 126 && (bArr[2] & 255) == 127 && (bArr[3] & 255) == 9 && (bArr[4] & 255) == 3;
    }

    private static double ticksToMsSegmented(List<TempoUtils.TempoEvent> list, int i, long j) {
        double dMin = 0.0d;
        int i2 = 0;
        while (i2 < list.size()) {
            long j2 = list.get(i2).tick;
            long j3 = list.get(i2).mpqn;
            i2++;
            long j4 = i2 < list.size() ? list.get(i2).tick : j;
            if (j <= j2) {
                break;
            }
            dMin += (Math.min(j, j4) - j2) * ((j3 / ((double) i)) / 1000.0d);
            if (j <= j4) {
                break;
            }
        }
        return dMin;
    }

    private static List<NoteSpan>[] collectNoteRuns(byte[] bArr) {
        List<NoteSpan>[] noteRunsByChannel = new ArrayList[16];
        if (bArr.length < 14 || bArr[0] != 77 || bArr[1] != 84 || bArr[2] != 104 || bArr[3] != 100) {
            return noteRunsByChannel;
        }
        int trackOffset = (int) (MidiBytes.readBe32(bArr, 4) + 8);
        int trackCount = MidiBytes.readBe16(bArr, 10);
        ArrayList<int[]> onEvents = new ArrayList();
        ArrayList<int[]> offEvents = new ArrayList();
        for (int track = 0; track < trackCount; track++) {
            if (trackOffset + 8 > bArr.length
                    || bArr[trackOffset] != 77
                    || bArr[trackOffset + 1] != 84
                    || bArr[trackOffset + 2] != 114
                    || bArr[trackOffset + 3] != 107) {
                break;
            }
            int trackEnd = (int) (((long) trackOffset) + 8L + MidiBytes.readBe32(bArr, trackOffset + 4));
            int index = trackOffset + 8;
            long tick = 0;
            int runningStatus = 0;
            while (index < trackEnd && index < bArr.length) {
                MidiBytes.VlqResult vlq = MidiBytes.readVlq(bArr, index);
                index = vlq.nextIndex;
                tick += vlq.value;
                if (index >= trackEnd || index >= bArr.length) {
                    break;
                }
                int eventByte = bArr[index] & 255;
                if (eventByte >= 128) {
                    runningStatus = eventByte;
                    index++;
                } else if (runningStatus == 0) {
                    break;
                } else {
                    eventByte = runningStatus;
                }
                int eventType = eventByte & 240;
                int channel = eventByte & 15;
                if (eventByte == 255) {
                    MidiBytes.VlqResult vlqMeta = MidiBytes.readVlq(bArr, index + 1);
                    index = Math.min(bArr.length, vlqMeta.nextIndex + ((int) vlqMeta.value));
                    runningStatus = 0;
                } else if (eventByte == 240 || eventByte == 247) {
                    MidiBytes.VlqResult vlqSysex = MidiBytes.readVlq(bArr, index);
                    index = Math.min(bArr.length, vlqSysex.nextIndex + ((int) vlqSysex.value));
                    runningStatus = 0;
                } else {
                    int data1 = index < bArr.length ? bArr[index] & 255 : 0;
                    int data2 = index + 1 < bArr.length ? bArr[index + 1] & 255 : 0;
                    index += (eventType == 192 || eventType == 208) ? 1 : 2;
                    if (eventType == 144 || eventType == 128) {
                        if (eventType == 128 || data2 == 0) {
                            offEvents.add(new int[]{channel, data1, (int) tick});
                        } else {
                            onEvents.add(new int[]{channel, data1, (int) tick});
                        }
                    }
                }
            }
            trackOffset = trackEnd;
        }
        List<int[]>[] onByChannelPitch = new ArrayList[16 * 128];
        List<int[]>[] offByChannelPitch = new ArrayList[16 * 128];
        for (int[] onEvent : onEvents) {
            int onSlot = (onEvent[0] * 128) + onEvent[1];
            if (onByChannelPitch[onSlot] == null) {
                onByChannelPitch[onSlot] = new ArrayList();
            }
            onByChannelPitch[onSlot].add(onEvent);
        }
        for (int[] offEvent : offEvents) {
            int offSlot = (offEvent[0] * 128) + offEvent[1];
            if (offByChannelPitch[offSlot] == null) {
                offByChannelPitch[offSlot] = new ArrayList();
            }
            offByChannelPitch[offSlot].add(offEvent);
        }
        for (int slot = 0; slot < 16 * 128; slot++) {
            List<int[]> ons = onByChannelPitch[slot];
            List<int[]> offs = offByChannelPitch[slot];
            if (ons == null || offs == null) {
                continue;
            }
            int offIndex = 0;
            for (int[] onEvent : ons) {
                while (offIndex < offs.size() && offs.get(offIndex)[2] <= onEvent[2]) {
                    offIndex++;
                }
                if (offIndex >= offs.size()) {
                    break;
                }
                int channel = onEvent[0];
                if (noteRunsByChannel[channel] == null) {
                    noteRunsByChannel[channel] = new ArrayList();
                }
                noteRunsByChannel[channel].add(new NoteSpan(onEvent[2], offs.get(offIndex)[2]));
                offIndex++;
            }
        }
        return noteRunsByChannel;
    }

    // Many SongHub MIDI files name their lead track "Melody" or "Key<...>" (Yamaha/PSR style
    // naming). That track carries the sung guide melody, which is what the lyric wipe should
    // follow. Rank 2 = keyboard "Key..." track, rank 1 = "Melody" track, 0 = not melodic.
    private static int classifyMelodyTrackName(String str) {
        if (str == null) {
            return 0;
        }
        String lower = str.trim().toLowerCase(java.util.Locale.ROOT);
        if (lower.isEmpty()) {
            return 0;
        }
        if (lower.startsWith("key")) {
            return 2;
        }
        if (lower.contains("melody")) {
            return 1;
        }
        return 0;
    }

    private static int[] collectMelodyChannelRank(byte[] bArr) {
        int[] rank = new int[16];
        if (bArr.length < 14 || bArr[0] != 77 || bArr[1] != 84 || bArr[2] != 104 || bArr[3] != 100) {
            return rank;
        }
        int trackOffset = (int) (MidiBytes.readBe32(bArr, 4) + 8);
        int trackCount = MidiBytes.readBe16(bArr, 10);
        for (int track = 0; track < trackCount; track++) {
            if (trackOffset + 8 > bArr.length
                    || bArr[trackOffset] != 77
                    || bArr[trackOffset + 1] != 84
                    || bArr[trackOffset + 2] != 114
                    || bArr[trackOffset + 3] != 107) {
                break;
            }
            int trackEnd = (int) (((long) trackOffset) + 8L + MidiBytes.readBe32(bArr, trackOffset + 4));
            int index = trackOffset + 8;
            long tick = 0;
            int runningStatus = 0;
            String name = "";
            boolean[] usedChannels = new boolean[16];
            while (index < trackEnd && index < bArr.length) {
                MidiBytes.VlqResult vlq = MidiBytes.readVlq(bArr, index);
                index = vlq.nextIndex;
                tick += vlq.value;
                if (index >= trackEnd || index >= bArr.length) {
                    break;
                }
                int eventByte = bArr[index] & 255;
                if (eventByte >= 128) {
                    runningStatus = eventByte;
                    index++;
                } else if (runningStatus == 0) {
                    break;
                } else {
                    eventByte = runningStatus;
                }
                int eventType = eventByte & 240;
                int channel = eventByte & 15;
                if (eventByte == 255) {
                    int metaType = index < bArr.length ? bArr[index] & 255 : 0;
                    MidiBytes.VlqResult vlqMeta = MidiBytes.readVlq(bArr, index + 1);
                    int payloadStart = vlqMeta.nextIndex;
                    int payloadLen = (int) vlqMeta.value;
                    if (metaType == 3 && name.isEmpty()) {
                        int copyEnd = Math.min(bArr.length, payloadStart + payloadLen);
                        if (payloadStart >= 0 && copyEnd >= payloadStart) {
                            name = new String(Arrays.copyOfRange(bArr, payloadStart, copyEnd), java.nio.charset.StandardCharsets.ISO_8859_1);
                        }
                    }
                    index = Math.min(bArr.length, payloadStart + payloadLen);
                    runningStatus = 0;
                } else if (eventByte == 240 || eventByte == 247) {
                    MidiBytes.VlqResult vlqSysex = MidiBytes.readVlq(bArr, index);
                    index = Math.min(bArr.length, vlqSysex.nextIndex + ((int) vlqSysex.value));
                    runningStatus = 0;
                } else {
                    int data2 = index + 1 < bArr.length ? bArr[index + 1] & 255 : 0;
                    index += (eventType == 192 || eventType == 208) ? 1 : 2;
                    if (eventType == 144 && data2 > 0) {
                        usedChannels[channel] = true;
                    }
                }
            }
            int trackRank = classifyMelodyTrackName(name);
            if (trackRank > 0) {
                for (int c = 0; c < 16; c++) {
                    if (usedChannels[c] && trackRank > rank[c]) {
                        rank[c] = trackRank;
                    }
                }
            }
            trackOffset = trackEnd;
        }
        return rank;
    }

    private static String scanTrackName(byte[] bArr, int i, int i2, String str) {
        while (i < i2) {
            try {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                int i3 = MidiBytes.readVlq(bArr, i).nextIndex;
                byte b = bArr[i3];
                int i4 = b & 255;
                int i5 = i3 + 1;
                if (i4 == 255) {
                    int i6 = bArr[i5] & 255;
                    MidiBytes.VlqResult vlq = MidiBytes.readVlq(bArr, i3 + 2);
                    int i7 = vlq.nextIndex;
                    int i8 = (int) vlq.value;
                    if (i6 == 3) {
                        byte[] bArrCopyOfRange = Arrays.copyOfRange(bArr, i7, i8 + i7);
                        try {
                            return new String(bArrCopyOfRange, str);
                        } catch (Exception unused) {
                            return new String(bArrCopyOfRange, "ISO-8859-1");
                        }
                    }
                    i = i7 + i8;
                } else if (i4 == 240 || i4 == 247) {
                    MidiBytes.VlqResult vlq2 = MidiBytes.readVlq(bArr, i5);
                    i = vlq2.nextIndex + ((int) vlq2.value);
                } else {
                    if (i4 < 128) {
                        return "";
                    }
                    int i9 = b & 240;
                    i = i5 + ((i9 == 192 || i9 == 208) ? 1 : 2);
                }
            } catch (Exception unused2) {
                return "";
            }
        }
        return "";
    }
}
