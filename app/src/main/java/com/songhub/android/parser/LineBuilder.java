package com.songhub.android.parser;

import com.songhub.android.model.Fragment;
import com.songhub.android.model.LyricLine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes3.dex */
public final class LineBuilder {
    private static final Pattern SPEAKER_TAG_AT = Pattern.compile("(?i)^\\s*@([mwc])\\s*(.*)", 32);
    private static final Pattern SPEAKER_TAG_ALT = Pattern.compile("(?i)^\\s*([MFBD]):\\s*(.*)", 32);
    private static final Pattern RUBY_PATTERN = Pattern.compile("([^\\[]*)\\[([^\\]]+)\\]");

    private LineBuilder() {
    }

    /* JADX WARN: Code duplicated, block: B:102:0x01d8  */
    /* JADX WARN: Code duplicated, block: B:104:0x01dc  */
    /* JADX WARN: Code duplicated, block: B:112:0x021f  */
    /* JADX WARN: Code duplicated, block: B:113:0x0221  */
    /* JADX WARN: Code duplicated, block: B:115:0x0229  */
    /* JADX WARN: Code duplicated, block: B:118:0x0230  */
    /* JADX WARN: Code duplicated, block: B:120:0x023a  */
    /* JADX WARN: Code duplicated, block: B:186:0x022d A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:45:0x00e2  */
    /* JADX WARN: Code duplicated, block: B:48:0x00eb  */
    /* JADX WARN: Code duplicated, block: B:49:0x00ed  */
    /* JADX WARN: Code duplicated, block: B:54:0x00fa  */
    /* JADX WARN: Code duplicated, block: B:65:0x0125  */
    /* JADX WARN: Code duplicated, block: B:87:0x016e  */
    /* JADX WARN: Code duplicated, block: B:89:0x0177  */
    /* JADX WARN: Code duplicated, block: B:91:0x0199  */
    /* JADX WARN: Code duplicated, block: B:92:0x01c0  */
    /* JADX WARN: Code duplicated, block: B:95:0x01c8  */
    /* JADX WARN: Code duplicated, block: B:97:0x01d0  */
    public static List<LyricLine> build(List<MidiParser.TickMsText> list, String str, int i) {
        boolean z = false;
        String str2 = null;
        boolean z2;
        boolean z3;
        boolean z4;
        String[] strArrSplit;
        long j;
        int i2 = 0;
        String str3;
        ArrayList<LyricLine> arrayList = new ArrayList();
        boolean z5 = str != null && (str.toUpperCase().contains("JAPAN") || str.toUpperCase().contains("JP") || str.toUpperCase().contains("JAP"));
        String str4 = "";
        String str5 = "";
        LyricLine lyricLine = null;
        boolean z6 = false;
        for (Iterator<MidiParser.TickMsText> it = list.iterator(); it.hasNext(); it = it) {
            if (lyricLine != null && !lyricLine.fragments.isEmpty()) {
                arrayList.add(lyricLine);
                lyricLine = null;
            }
            MidiParser.TickMsText next = it.next();
            long j2 = next.tick;
            boolean z7 = z5;
            double d = next.ms;
            String strReplace = next.text.replace("^", "").replace("<", " ");
            Matcher matcher = SPEAKER_TAG_AT.matcher(strReplace);
            boolean zFind = matcher.find();
            Matcher matcher2 = !zFind ? SPEAKER_TAG_ALT.matcher(strReplace) : null;
            if (zFind) {
                str5 = "@" + matcher.group(1).toLowerCase();
                strReplace = matcher.group(2);
            } else {
                if (matcher2 == null || !matcher2.find()) {
                    z = false;
                } else {
                    String upperCase = matcher2.group(1).toUpperCase();
                    if (upperCase.equals("M")) {
                        str2 = "@m";
                    } else if (upperCase.equals("F")) {
                        str2 = "@w";
                    } else {
                        if (upperCase.equals("B") || upperCase.equals("D")) {
                            str2 = "@c";
                        }
                        strReplace = matcher2.group(2);
                    }
                    str5 = str2;
                    strReplace = matcher2.group(2);
                }
                if (z && lyricLine != null) {
                    lyricLine.speakerTag = str5;
                }
                if (strReplace.contains("(")) {
                    z6 = true;
                }
                if (strReplace.contains(")")) {
                    z2 = false;
                } else {
                    z2 = z6;
                }
                if (!strReplace.contains("(") || strReplace.contains(")")) {
                    strReplace = strReplace.replace("(", "").replace(")", "");
                }
                if (strReplace.indexOf(13) < 0 || strReplace.indexOf(10) >= 0 || strReplace.indexOf(47) >= 0 || strReplace.indexOf(92) >= 0) {
                    z3 = true;
                } else {
                    z3 = false;
                }
                if ((strReplace.isEmpty() || z3) && ((!strReplace.trim().isEmpty() || z3 || strReplace.equals(" ")) && (z || d >= 1000.0d || !isAllUpper(strReplace.trim()) || strReplace.trim().length() >= 30 || z3))) {
                    if (lyricLine == null) {
                        lyricLine = new LyricLine(d);
                        lyricLine.speakerTag = str5;
                    }
                    if (z3) {
                        strArrSplit = strReplace.replace('\r', '\n').replace('/', '\n').replace('\\', '\n').split("\n", -1);
                        if (strArrSplit[0].isEmpty()) {
                            z4 = z7;
                            j = j2;
                        } else {
                            z4 = z7;
                            ProcessedText processedTextProcessFragmentText = processFragmentText(strArrSplit[0], true, z4);
                            j = j2;
                            Fragment fragment = new Fragment(j, d, processedTextProcessFragmentText.cleanText);
                            fragment.rubyData = processedTextProcessFragmentText.rubies;
                            fragment.isParenthesized = z6;
                            lyricLine.fragments.add(fragment);
                            lyricLine.endMs = d;
                        }
                        if (strArrSplit.length > 1) {
                            if (!lyricLine.fragments.isEmpty()) {
                                arrayList.add(lyricLine);
                            }
                            lyricLine = null;
                        }
                        for (i2 = 1; i2 < strArrSplit.length; i2++) {
                            str3 = strArrSplit[i2];
                            if (str3 != null && !str3.trim().isEmpty()) {
                                lyricLine = new LyricLine(d);
                                lyricLine.speakerTag = str5;
                                ProcessedText processedTextProcessFragmentText2 = processFragmentText(str3, true, z4);
                                Fragment fragment2 = new Fragment(j, d, processedTextProcessFragmentText2.cleanText);
                                fragment2.rubyData = processedTextProcessFragmentText2.rubies;
                                fragment2.isParenthesized = z6;
                                lyricLine.fragments.add(fragment2);
                                lyricLine.endMs = d;
                                if (i2 != strArrSplit.length - 1) {
                                    if (!lyricLine.fragments.isEmpty()) {
                                        arrayList.add(lyricLine);
                                    }
                                    lyricLine = null;
                                }
                            } else if (lyricLine != null) {
                                if (!lyricLine.fragments.isEmpty()) {
                                    arrayList.add(lyricLine);
                                }
                                lyricLine = null;
                            }
                        }
                    } else {
                        z4 = z7;
                        if (!strReplace.isEmpty()) {
                            ProcessedText processedTextProcessFragmentText3 = processFragmentText(strReplace, true, z4);
                            Fragment fragment3 = new Fragment(j2, d, processedTextProcessFragmentText3.cleanText);
                            fragment3.rubyData = processedTextProcessFragmentText3.rubies;
                            fragment3.isParenthesized = z6;
                            lyricLine.fragments.add(fragment3);
                            lyricLine.endMs = d;
                        }
                    }
                    z5 = z4;
                } else {
                    z5 = z7;
                }
                z6 = z2;
            }
            z = true;
            if (z && lyricLine != null) {
                lyricLine.speakerTag = str5;
            }
            if (strReplace.contains("(")) {
                z6 = true;
            }
            if (strReplace.contains(")")) {
                z2 = false;
            } else {
                z2 = z6;
            }
            if (!strReplace.contains("(")) {
                strReplace = strReplace.replace("(", "").replace(")", "");
            } else {
                strReplace = strReplace.replace("(", "").replace(")", "");
            }
            if (strReplace.indexOf(13) < 0) {
                z3 = true;
            } else {
                z3 = true;
            }
            if (strReplace.isEmpty()) {
                if (lyricLine == null) {
                    lyricLine = new LyricLine(d);
                    lyricLine.speakerTag = str5;
                }
                if (z3) {
                    strArrSplit = strReplace.replace('\r', '\n').replace('/', '\n').replace('\\', '\n').split("\n", -1);
                    if (strArrSplit[0].isEmpty()) {
                        z4 = z7;
                        ProcessedText processedTextProcessFragmentText4 = processFragmentText(strArrSplit[0], true, z4);
                        j = j2;
                        Fragment fragment4 = new Fragment(j, d, processedTextProcessFragmentText4.cleanText);
                        fragment4.rubyData = processedTextProcessFragmentText4.rubies;
                        fragment4.isParenthesized = z6;
                        lyricLine.fragments.add(fragment4);
                        lyricLine.endMs = d;
                    } else {
                        z4 = z7;
                        j = j2;
                    }
                    if (strArrSplit.length > 1) {
                        if (!lyricLine.fragments.isEmpty()) {
                            arrayList.add(lyricLine);
                        }
                        lyricLine = null;
                    }
                    for (i2 = 1; i2 < strArrSplit.length; i2++) {
                        str3 = strArrSplit[i2];
                        if (str3 != null && !str3.trim().isEmpty()) {
                            lyricLine = new LyricLine(d);
                            lyricLine.speakerTag = str5;
                            ProcessedText processedTextInner = processFragmentText(str3, true, z4);
                            Fragment fragmentInner = new Fragment(j, d, processedTextInner.cleanText);
                            fragmentInner.rubyData = processedTextInner.rubies;
                            fragmentInner.isParenthesized = z6;
                            lyricLine.fragments.add(fragmentInner);
                            lyricLine.endMs = d;
                            if (i2 != strArrSplit.length - 1) {
                                if (!lyricLine.fragments.isEmpty()) {
                                    arrayList.add(lyricLine);
                                }
                                lyricLine = null;
                            }
                        } else if (lyricLine != null) {
                            if (!lyricLine.fragments.isEmpty()) {
                                arrayList.add(lyricLine);
                            }
                            lyricLine = null;
                        }
                    }
                } else {
                    z4 = z7;
                    if (!strReplace.isEmpty()) {
                        ProcessedText processedTextProcessFragmentText5 = processFragmentText(strReplace, true, z4);
                        Fragment fragment5 = new Fragment(j2, d, processedTextProcessFragmentText5.cleanText);
                        fragment5.rubyData = processedTextProcessFragmentText5.rubies;
                        fragment5.isParenthesized = z6;
                        lyricLine.fragments.add(fragment5);
                        lyricLine.endMs = d;
                    }
                }
                z5 = z4;
            } else {
                if (lyricLine == null) {
                    lyricLine = new LyricLine(d);
                    lyricLine.speakerTag = str5;
                }
                if (z3) {
                    strArrSplit = strReplace.replace('\r', '\n').replace('/', '\n').replace('\\', '\n').split("\n", -1);
                    if (strArrSplit[0].isEmpty()) {
                        z4 = z7;
                        ProcessedText processedTextProcessFragmentText6 = processFragmentText(strArrSplit[0], true, z4);
                        j = j2;
                        Fragment fragment6 = new Fragment(j, d, processedTextProcessFragmentText6.cleanText);
                        fragment6.rubyData = processedTextProcessFragmentText6.rubies;
                        fragment6.isParenthesized = z6;
                        lyricLine.fragments.add(fragment6);
                        lyricLine.endMs = d;
                    } else {
                        z4 = z7;
                        j = j2;
                    }
                    if (strArrSplit.length > 1) {
                        if (!lyricLine.fragments.isEmpty()) {
                            arrayList.add(lyricLine);
                        }
                        lyricLine = null;
                    }
                    for (i2 = 1; i2 < strArrSplit.length; i2++) {
                        str3 = strArrSplit[i2];
                        if (str3 != null && !str3.trim().isEmpty()) {
                            lyricLine = new LyricLine(d);
                            lyricLine.speakerTag = str5;
                            ProcessedText processedTextInner = processFragmentText(str3, true, z4);
                            Fragment fragmentInner = new Fragment(j, d, processedTextInner.cleanText);
                            fragmentInner.rubyData = processedTextInner.rubies;
                            fragmentInner.isParenthesized = z6;
                            lyricLine.fragments.add(fragmentInner);
                            lyricLine.endMs = d;
                            if (i2 != strArrSplit.length - 1) {
                                if (!lyricLine.fragments.isEmpty()) {
                                    arrayList.add(lyricLine);
                                }
                                lyricLine = null;
                            }
                        } else if (lyricLine != null) {
                            if (!lyricLine.fragments.isEmpty()) {
                                arrayList.add(lyricLine);
                            }
                            lyricLine = null;
                        }
                    }
                } else {
                    z4 = z7;
                    if (!strReplace.isEmpty()) {
                        ProcessedText processedTextProcessFragmentText7 = processFragmentText(strReplace, true, z4);
                        Fragment fragment7 = new Fragment(j2, d, processedTextProcessFragmentText7.cleanText);
                        fragment7.rubyData = processedTextProcessFragmentText7.rubies;
                        fragment7.isParenthesized = z6;
                        lyricLine.fragments.add(fragment7);
                        lyricLine.endMs = d;
                    }
                }
                z5 = z4;
            }
            z6 = z2;
        }
        if (lyricLine != null && !lyricLine.fragments.isEmpty()) {
            arrayList.add(lyricLine);
        }
        double d2 = -10000.0d;
        for (LyricLine lyricLine2 : arrayList) {
            boolean z8 = (lyricLine2.speakerTag.isEmpty() || lyricLine2.speakerTag.equals(str4)) ? false : true;
            boolean z9 = lyricLine2.startMs - d2 > 5000.0d;
            if (!lyricLine2.speakerTag.isEmpty() && (z8 || z9)) {
                lyricLine2.isSpeakerChange = true;
                str4 = lyricLine2.speakerTag;
            }
            d2 = lyricLine2.endMs;
        }
        double MAX_HOLD_MS = 5000.0d;
        for (int lineIdx = 0; lineIdx < arrayList.size(); lineIdx++) {
            LyricLine lyricLine3 = (LyricLine) arrayList.get(lineIdx);
            if (lyricLine3.fragments.isEmpty()) {
                continue;
            }
            int fragmentCount = lyricLine3.fragments.size();
            for (int fragmentIdx = 0; fragmentIdx < fragmentCount; fragmentIdx++) {
                Fragment fragment8 = (Fragment) lyricLine3.fragments.get(fragmentIdx);
                int nextIdx = fragmentIdx + 1;
                Fragment fragment9 = nextIdx < fragmentCount
                        ? (Fragment) lyricLine3.fragments.get(nextIdx)
                        : null;
                if (fragment9 != null) {
                    long j3 = fragment9.absTick - fragment8.absTick;
                    long j4 = i;
                    if (j3 > 4 * j4) {
                        j3 = j4 * 2;
                    }
                    fragment8.durationTicks = j3;
                    fragment8.invDuration = j3 > 0 ? 1.0d / j3 : 0.0d;
                    fragment8.endTick = fragment8.absTick + j3;
                    fragment8.endMs = fragment9.absMs > fragment8.absMs
                            ? fragment9.absMs
                            : fragment8.absMs + 1000.0d;
                } else {
                    Fragment nextLineFirst = lineIdx + 1 < arrayList.size()
                            ? (Fragment) ((LyricLine) arrayList.get(lineIdx + 1)).fragments.get(0)
                            : null;
                    long j3;
                    if (nextLineFirst != null) {
                        j3 = nextLineFirst.absTick - fragment8.absTick;
                        if (j3 > 4L * i) {
                            j3 = i * 2L;
                        }
                    } else {
                        j3 = ((long) i) * 2;
                    }
                    fragment8.durationTicks = j3;
                    fragment8.invDuration = j3 > 0 ? 1.0d / j3 : 0.0d;
                    fragment8.endTick = fragment8.absTick + j3;
                    double nextLineStart = lineIdx + 1 < arrayList.size()
                            ? ((LyricLine) arrayList.get(lineIdx + 1)).startMs
                            : fragment8.absMs + MAX_HOLD_MS;
                    fragment8.endMs = Math.min(nextLineStart, fragment8.absMs + MAX_HOLD_MS);
                    if (fragment8.endMs <= fragment8.absMs) {
                        fragment8.endMs = fragment8.absMs + 1000.0d;
                    }
                }
            }
            lyricLine3.startMs = ((Fragment) lyricLine3.fragments.get(0)).absMs;
            lyricLine3.endMs = ((Fragment) lyricLine3.fragments.get(fragmentCount - 1)).endMs;
        }
        return arrayList;
    }

    private static boolean isAllUpper(String str) {
        if (str.isEmpty()) {
            return false;
        }
        boolean z = false;
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) {
                if (Character.isLowerCase(c)) {
                    return false;
                }
                z = true;
            }
        }
        return z;
    }

    private static final class ProcessedText {
        final String cleanText;
        final List<Fragment.Ruby> rubies;

        ProcessedText(String str, List<Fragment.Ruby> list) {
            this.cleanText = str;
            this.rubies = list;
        }
    }

    private static ProcessedText processFragmentText(String str, boolean z, boolean z2) {
        StringBuilder sb = new StringBuilder();
        ArrayList arrayList = new ArrayList();
        if (str.contains("[") && str.contains("]")) {
            Matcher matcher = RUBY_PATTERN.matcher(str);
            int iEnd = 0;
            while (matcher.find()) {
                String strGroup = matcher.group(1);
                String strGroup2 = matcher.group(2);
                sb.append(str.substring(iEnd, matcher.start()));
                int length = sb.length();
                sb.append(strGroup);
                arrayList.add(new Fragment.Ruby(length, strGroup2, strGroup));
                iEnd = matcher.end();
            }
            sb.append(str.substring(iEnd));
        } else {
            sb.append(str);
        }
        return new ProcessedText(sb.toString(), arrayList);
    }
}
