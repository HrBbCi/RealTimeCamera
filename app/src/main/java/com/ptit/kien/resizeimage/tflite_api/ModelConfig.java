package com.ptit.kien.resizeimage.tflite_api;

public abstract  class ModelConfig {
    static final int FLOAT_BYTES_COUNT = 4;

    public abstract String getModelFilename();

    public abstract String getLabelsFilename();

    public abstract int getInputWidth();

    public abstract int getInputHeight();

    public abstract int getInputSize();

    public abstract int getChannelsCount();

    public abstract float getMean();

    public abstract float getStd();

}
