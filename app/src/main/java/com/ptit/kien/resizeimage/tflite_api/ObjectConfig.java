package com.ptit.kien.resizeimage.tflite_api;

public class ObjectConfig extends ModelConfig {
    @Override
    public String getModelFilename() {
        return "mobile.tflite";
    }

    @Override
    public String getLabelsFilename() {
        return "labelmap_mobile.txt";
    }

    @Override
    public int getInputWidth() {
        return 224;
    }

    @Override
    public int getInputHeight() {
        return 224;
    }

    @Override
    public int getInputSize() {
        return getInputWidth() * getInputHeight() * getChannelsCount() * FLOAT_BYTES_COUNT;
    }

    @Override
    public int getChannelsCount() {
        return 3;
    }

    @Override
    public float getStd() {
        return 255.f;
    }

    @Override
    public float getMean() {
        return 0.f;
    }
}
