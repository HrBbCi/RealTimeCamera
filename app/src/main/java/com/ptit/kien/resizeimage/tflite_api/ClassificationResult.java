package com.ptit.kien.resizeimage.tflite_api;

import android.graphics.Rect;
import android.graphics.RectF;

public class ClassificationResult {
    public final String title;
    public final int labelIndex;
    public final float confidence;
    public ClassificationResult(String title, int labelIndex, float confidence) {
        this.title = title;
        this.labelIndex = labelIndex;
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return title + " " + String.format("(%.1f%%) ", confidence * 100.0f);
    }
}
