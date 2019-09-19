package com.ptit.kien.resizeimage.custom;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
public class ImageUtils {
    private static Socket mSocket;

    public static void sendLog(String log) {
        if (mSocket == null) {
            try {
                mSocket = IO.socket("http://192.168.1.49:3000");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            mSocket.connect();
        }
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        mSocket.emit("client-send-log", currentTime + ":" + log);
    }
}
