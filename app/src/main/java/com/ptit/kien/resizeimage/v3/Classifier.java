package com.ptit.kien.resizeimage.v3;

public interface Classifier {
    String name();

    Classification recognize(final int[] wordids);
}
