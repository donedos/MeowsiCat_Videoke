package com.songhub.android.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes3.dex */
public class LyricLine {
    public double endMs;
    public int highlightColor;
    public double startMs;
    public List<Fragment> fragments = new ArrayList();
    public float totalWidthPx = 0.0f;
    public float emojiWidthPx = 0.0f;
    public String speakerTag = "";
    public boolean isSpeakerChange = false;
    public String romanized = "";
    public List cachedSegments = null;
    public float cachedMaxWidth = -1.0f;
    private String cachedFullText = null;

    public LyricLine(double d) {
        this.startMs = d;
        this.endMs = d;
    }

    public String fullText() {
        String str = this.cachedFullText;
        if (str != null) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Fragment> it = this.fragments.iterator();
        while (it.hasNext()) {
            sb.append(it.next().text);
        }
        String string = sb.toString();
        this.cachedFullText = string;
        return string;
    }
}
