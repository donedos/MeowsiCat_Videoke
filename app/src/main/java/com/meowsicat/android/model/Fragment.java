package com.meowsicat.android.model;

import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes3.dex */
public class Fragment {
    public double absMs;
    public long absTick;
    public String text;
    public List<Ruby> rubyData = new ArrayList();
    public float widthPx = 0.0f;
    public boolean isParenthesized = false;
    public long durationTicks = 0;
    public long endTick = 0;
    public double endMs = 0.0d;
    public double invDuration = 0.0d;

    public static class Ruby {
        public final int index;
        public final String kanji;
        public final String text;

        public Ruby(int i, String str, String str2) {
            this.index = i;
            this.text = str;
            this.kanji = str2;
        }
    }

    public Fragment(long j, double d, String str) {
        this.absTick = j;
        this.absMs = d;
        this.text = str;
    }
}
