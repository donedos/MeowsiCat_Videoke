package com.meowsicat.android.model;

import java.util.List;

/* JADX INFO: loaded from: classes3.dex */
public class CountdownGroup {
    public double anchorMs;
    public int lineIndex;
    public List<Double> pipMs;

    public int size() {
        List<Double> list = this.pipMs;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public double firstPipMs() {
        return this.pipMs.get(0).doubleValue();
    }

    public double lastPipMs() {
        List<Double> list = this.pipMs;
        return list.get(list.size() - 1).doubleValue();
    }

    public int numberAt(double d) {
        int i = 0;
        for (int i2 = 0; i2 < this.pipMs.size(); i2++) {
            if (d >= this.pipMs.get(i2).doubleValue()) {
                i = i2;
            }
        }
        return size() - i;
    }

    public double beatProgress(double d) {
        int i = 0;
        for (int i2 = 0; i2 < this.pipMs.size(); i2++) {
            if (d >= this.pipMs.get(i2).doubleValue()) {
                i = i2;
            }
        }
        double dDoubleValue = this.pipMs.get(i).doubleValue();
        int i3 = i + 1;
        double dMax = (d - dDoubleValue) / Math.max(1.0d, (i3 < this.pipMs.size() ? this.pipMs.get(i3).doubleValue() : this.anchorMs) - dDoubleValue);
        if (dMax < 0.0d) {
            dMax = 0.0d;
        }
        if (dMax > 1.0d) {
            return 1.0d;
        }
        return dMax;
    }

    public double getGoDurationMs() {
        List<Double> list = this.pipMs;
        if (list == null || list.isEmpty()) {
            return 500.0d;
        }
        double dLastPipMs = this.anchorMs - lastPipMs();
        if (dLastPipMs > 100.0d) {
            return dLastPipMs;
        }
        return 500.0d;
    }
}
