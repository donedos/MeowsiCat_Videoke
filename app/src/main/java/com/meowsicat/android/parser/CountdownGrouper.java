package com.meowsicat.android.parser;

import com.meowsicat.android.model.CountdownGroup;
import com.meowsicat.android.model.LyricLine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/* JADX INFO: loaded from: classes3.dex */
public final class CountdownGrouper {
    private static final double AUTO_INTERLUDE_GAP_MS = 8000.0d;
    private static final double AUTO_INTRO_MIN_START_MS = 3000.0d;
    private static final double MAX_PIP_GAP_MS = 4000.0d;

    private CountdownGrouper() {
    }

    /* JADX WARN: Code duplicated, block: B:41:0x00bc  */
    /* JADX WARN: Code duplicated, block: B:44:0x00d3 A[LOOP:2: B:42:0x00d0->B:44:0x00d3, LOOP_END] */
    public static List<CountdownGroup> build(List<MidiParser.TickMsText> list, List<LyricLine> list2, List<TempoUtils.TempoEntry> list3, int i) {
        double dDurationTicksToMs;
        ArrayList arrayList;
        int i2;
        CountdownGroup countdownGroupFinishGroup;
        ArrayList arrayList2 = new ArrayList();
        if (list2 != null && !list2.isEmpty()) {
            HashSet hashSet = new HashSet();
            if (list != null && !list.isEmpty()) {
                ArrayList arrayList3 = new ArrayList();
                double d = Double.NEGATIVE_INFINITY;
                for (MidiParser.TickMsText tickMsText : list) {
                    if (!arrayList3.isEmpty() && tickMsText.ms - d > MAX_PIP_GAP_MS) {
                        CountdownGroup countdownGroupFinishGroup2 = finishGroup(arrayList3, list2);
                        if (countdownGroupFinishGroup2 != null) {
                            arrayList2.add(countdownGroupFinishGroup2);
                            hashSet.add(Integer.valueOf(countdownGroupFinishGroup2.lineIndex));
                        }
                        arrayList3 = new ArrayList();
                    }
                    arrayList3.add(Double.valueOf(tickMsText.ms));
                    d = tickMsText.ms;
                }
                if (!arrayList3.isEmpty() && (countdownGroupFinishGroup = finishGroup(arrayList3, list2)) != null) {
                    arrayList2.add(countdownGroupFinishGroup);
                    hashSet.add(Integer.valueOf(countdownGroupFinishGroup.lineIndex));
                }
            }
            for (int i3 = 0; i3 < list2.size(); i3++) {
                if (!hashSet.contains(Integer.valueOf(i3))) {
                    LyricLine lyricLine = list2.get(i3);
                    if (i3 == 0) {
                        if (lyricLine.startMs > AUTO_INTRO_MIN_START_MS) {
                            dDurationTicksToMs = TempoUtils.durationTicksToMs(list3, i, (long) TempoUtils.msToTick(list3, lyricLine.startMs, i), i);
                            arrayList = new ArrayList();
                            for (i2 = 4; i2 >= 1; i2--) {
                                arrayList.add(Double.valueOf(lyricLine.startMs - (((double) i2) * dDurationTicksToMs)));
                            }
                            CountdownGroup countdownGroup = new CountdownGroup();
                            countdownGroup.pipMs = arrayList;
                            countdownGroup.anchorMs = lyricLine.startMs;
                            countdownGroup.lineIndex = i3;
                            arrayList2.add(countdownGroup);
                        }
                    } else if (lyricLine.startMs - list2.get(i3 - 1).endMs > AUTO_INTERLUDE_GAP_MS) {
                        dDurationTicksToMs = TempoUtils.durationTicksToMs(list3, i, (long) TempoUtils.msToTick(list3, lyricLine.startMs, i), i);
                        arrayList = new ArrayList();
                        for (i2 = 4; i2 >= 1; i2--) {
                            arrayList.add(Double.valueOf(lyricLine.startMs - (((double) i2) * dDurationTicksToMs)));
                        }
                        CountdownGroup countdownGroup2 = new CountdownGroup();
                        countdownGroup2.pipMs = arrayList;
                        countdownGroup2.anchorMs = lyricLine.startMs;
                        countdownGroup2.lineIndex = i3;
                        arrayList2.add(countdownGroup2);
                    }
                }
            }
            Collections.sort(arrayList2, new Comparator() { // from class: com.meowsicat.android.parser.CountdownGrouper$$ExternalSyntheticLambda0
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return Double.compare(((CountdownGroup) obj).anchorMs, ((CountdownGroup) obj2).anchorMs);
                }
            });
        }
        return arrayList2;
    }

    private static CountdownGroup finishGroup(List<Double> list, List<LyricLine> list2) {
        if (list.isEmpty()) {
            return null;
        }
        double dDoubleValue = list.get(list.size() - 1).doubleValue();
        int size = list2.size() - 1;
        for (int i = 0; i < list2.size(); i++) {
            if (list2.get(i).startMs >= dDoubleValue - 1.0d) {
                size = i;
                break;
            }
        }
        double d = list2.get(size).startMs;
        if (d < dDoubleValue) {
            return null;
        }
        CountdownGroup countdownGroup = new CountdownGroup();
        countdownGroup.pipMs = list;
        countdownGroup.anchorMs = d;
        countdownGroup.lineIndex = size;
        return countdownGroup;
    }
}
