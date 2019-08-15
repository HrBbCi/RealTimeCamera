package com.ptit.kien.resizeimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    //Text
    private static final float TEXT_SIZE_DIP = 5;
    private float textSizePx;
    private Paint fgPaint;

    //Camera
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListener();
        int cropSize = TF_OD_API_INPUT_SIZE;
        ivInput.setImageResource(R.drawable.face1);
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
        scaleImage();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLoadImage:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_FOLDER);
                break;
            case R.id.btnCreate:
//                CamClick();
                Intent intent1 = new Intent(MainActivity.this,DetectorActivity.class);
                startActivity(intent1);
                break;
            case R.id.btnProcess:
                scaleImage();
                break;
            case R.id.ivResult:
                showImage();
                break;

        }
    }

    private void showImage() {
        scaleImage();
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image);
        final ImageView ivDialog = (ImageView) dialog.findViewById(R.id.ivDialog);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ivResult.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ivDialog.setImageBitmap(bitmap);
        dialog.show();
    }

    private void scaleImage() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ivInput.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ivResult.setImageBitmap(bitmap);

        final int maxSize = TF_OD_API_INPUT_SIZE;
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);

        final List<Classifier.Recognition> results = detector.recognizeImage(resizedBitmap);
        final Canvas canvas = new Canvas(resizedBitmap);
        ivResult.setImageBitmap(resizedBitmap);
//        ivResult.getLayoutParams().height =INPUT_SIZE;
//        ivResult.getLayoutParams().width =INPUT_SIZE;
        //Draw picture
        final Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);

        //Draw text
        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        fgPaint = new Paint();
        fgPaint.setTextSize(textSizePx);
        fgPaint.setColor(Color.YELLOW);

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= 0.4f) {
                canvas.drawText(result.getTitle() + ": " + result.getConfidence(), (location.right)-10, (location.top + 10), fgPaint);
                canvas.drawRect(location, paint);
                result.setLocation(location);
            }
        }
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

    public void CamClick() {
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE_CAMERA
        );
    }


}
