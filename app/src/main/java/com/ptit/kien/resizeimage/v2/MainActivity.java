package com.ptit.kien.resizeimage.v2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ptit.kien.resizeimage.R;
import com.ptit.kien.resizeimage.tflite_api.ModelClassificator;
import com.ptit.kien.resizeimage.tflite_mtcnn.TestActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Define View
    ImageView ivInput, ivResult;
    Button btnLoadImage, btnProcess, btnCreate;

    //Code
    final int REQUEST_CODE_FOLDER = 123;
    final int REQUEST_CODE_CAMERA = 456;
    //Image Classifier
    private Classifier detector;
    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mtcnn.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap_mtcnn.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    //Paint
    private static final float TEXT_SIZE_DIP = 5;
    private float textSizePx;
    private Paint fgPaint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListener();
        loadModel();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLoadImage:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_FOLDER);
                break;
            case R.id.btnCreate:
                startActivity(new Intent(MainActivity.this, TestActivity.class));
        }
    }

    private void scaleImage() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ivInput.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ivResult.setImageBitmap(bitmap);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 600, 800, false);
        List<Classifier.Recognition> results;
        results = detector.recognizeImage(resizedBitmap);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CODE_CAMERA);
                } else {
                    Toast.makeText(this, "Ko dc phep", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_FOLDER:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_FOLDER);
                } else {
                    Toast.makeText(this, "Ko dc phep", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ivResult.setImageBitmap(bitmap);
        }
        if (requestCode == REQUEST_CODE_FOLDER && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivInput.setImageBitmap(bitmap);
                scaleImage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap performBW(Bitmap inp) {
        Bitmap bmOut = Bitmap.createBitmap(inp.getWidth(), inp.getHeight(),
                inp.getConfig());
        int A, R, G, B;
        int w = inp.getWidth();
        int h = inp.getHeight();
        int[] colors = new int[w * h];
        inp.getPixels(colors, 0, w, 0, 0, w, h);
        int i = 0;
        int j = 0;
        int pos;
        int val;
        for (i = 0; i < h; i++) {
            for (j = 0; j < w; j++) {
                pos = i * w + j;
                A = (colors[pos] >> 24) & 0xFF;
                R = (colors[pos] >> 16) & 0xFF;
                G = (colors[pos] >> 8) & 0xFF;
                B = colors[pos] & 0xFF;
                //Thuật toán xử lý grayscale
                R = G = B = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                colors[pos] = Color.argb(A, R, G, B);
            }
        }
        bmOut.setPixels(colors, 0, w, 0, 0, w, h);
        return bmOut;
    }

    private enum DetectorMode {
        TF_OD_API;
    }
//    public void CamClick() {
//        ActivityCompat.requestPermissions(
//                MainActivity.this,
//                new String[]{Manifest.permission.CAMERA},
//                REQUEST_CODE_CAMERA
//        );
//    }


    private void loadModel() {
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void initListener() {
        btnLoadImage.setOnClickListener(this);
        btnProcess.setOnClickListener(this);
        ivResult.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
    }

    private void initViews() {
        ivInput = findViewById(R.id.ivInput);
        ivResult = findViewById(R.id.ivResult);
        btnLoadImage = findViewById(R.id.btnLoadImage);
        btnProcess = findViewById(R.id.btnProcess);
        btnCreate = findViewById(R.id.btnCreate);
    }

}
