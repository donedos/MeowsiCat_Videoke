package com.meowsicat.android.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* JADX INFO: loaded from: classes3.dex */
public final class TempoUtils {
    public static final double DEFAULT_BPM = 120.0d;
    public static final int DEFAULT_MPQN = 500000;
    public static final int DEFAULT_PPQN = 480;

    public static double bpmToMpqn(double d) {
        return 6.0E7d / d;
    }

    public static double mpqnToBpm(double d) {
        return 6.0E7d / d;
    }

    private TempoUtils() {
    }

    public static final class TempoEntry {
        public final long mpqn;
        public final long tick;
        public final double timeMs;

        public TempoEntry(long j, long j2, double d) {
            this.tick = j;
            this.mpqn = j2;
            this.timeMs = d;
        }
    }

    public static final class TempoEvent {
        public final long mpqn;
        public final long tick;

        public TempoEvent(long j, long j2) {
            this.tick = j;
            this.mpqn = j2;
        }
    }

    public static List<TempoEntry> buildTempoMap(List<TempoEvent> list, int i) {
        ArrayList<TempoEvent> arrayList = new ArrayList(list);
        Collections.sort(arrayList, new Comparator() { // from class: com.meowsicat.android.parser.TempoUtils$$ExternalSyntheticLambda0
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return Long.compare(((TempoUtils.TempoEvent) obj).tick, ((TempoUtils.TempoEvent) obj2).tick);
            }
        });
        long j = 500000;
        if (arrayList.isEmpty() || ((TempoEvent) arrayList.get(0)).tick != 0) {
            arrayList.add(0, new TempoEvent(0L, 500000L));
        }
        ArrayList arrayList2 = new ArrayList();
        double d = 0.0d;
        long j2 = 0;
        for (TempoEvent tempoEvent : arrayList) {
            long j3 = tempoEvent.tick - j2;
            if (j3 > 0) {
                d += (j3 * j) / (((double) i) * 1000.0d);
            }
            long j4 = tempoEvent.tick;
            long j5 = tempoEvent.mpqn;
            arrayList2.add(new TempoEntry(j4, j5, d));
            j2 = j4;
            j = j5;
        }
        return arrayList2;
    }

    public static TempoEntry getActiveTempoEntry(List<TempoEntry> list, long j) {
        if (list.isEmpty()) {
            return null;
        }
        TempoEntry tempoEntry = list.get(0);
        for (TempoEntry tempoEntry2 : list) {
            if (tempoEntry2.tick > j) {
                break;
            }
            tempoEntry = tempoEntry2;
        }
        return tempoEntry;
    }

    public static TempoEntry getActiveTempoEntryByTime(List<TempoEntry> list, double d) {
        if (list.isEmpty()) {
            return null;
        }
        TempoEntry tempoEntry = list.get(0);
        for (TempoEntry tempoEntry2 : list) {
            if (tempoEntry2.timeMs > d) {
                break;
            }
            tempoEntry = tempoEntry2;
        }
        return tempoEntry;
    }

    public static double getCurrentBpm(List<TempoEntry> list, long j, double d) {
        TempoEntry activeTempoEntry = getActiveTempoEntry(list, j);
        return ((activeTempoEntry == null || activeTempoEntry.mpqn == 0) ? 120.0d : mpqnToBpm(activeTempoEntry.mpqn)) * d;
    }

    public static double tickToMs(List<TempoEntry> list, long j, int i) {
        TempoEntry activeTempoEntry = getActiveTempoEntry(list, j);
        if (activeTempoEntry == null) {
            return 0.0d;
        }
        return activeTempoEntry.timeMs + (((j - activeTempoEntry.tick) * activeTempoEntry.mpqn) / (((double) i) * 1000.0d));
    }

    public static double msToTick(List<TempoEntry> list, double d, int i) {
        TempoEntry activeTempoEntryByTime = getActiveTempoEntryByTime(list, d);
        if (activeTempoEntryByTime == null) {
            return 0.0d;
        }
        return activeTempoEntryByTime.tick + ((((d - activeTempoEntryByTime.timeMs) * 1000.0d) * ((double) i)) / activeTempoEntryByTime.mpqn);
    }

    public static double durationTicksToMs(List<TempoEntry> list, double d, long j, int i) {
        if (d <= 0.0d) {
            return 0.0d;
        }
        TempoEntry activeTempoEntry = getActiveTempoEntry(list, j);
        return (d * (activeTempoEntry != null ? activeTempoEntry.mpqn : 500000L)) / (((double) i) * 1000.0d);
    }
}
