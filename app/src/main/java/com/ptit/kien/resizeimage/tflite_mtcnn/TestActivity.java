package com.ptit.kien.resizeimage.tflite_mtcnn;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptit.kien.resizeimage.R;

import java.util.Vector;

public class TestActivity extends AppCompatActivity {

    MTCNN mtcnn;
    Bitmap bitmap;
    Button btnCreate;
    TextView txtHello;
    ImageView ivInput;

    // Configuration values for the prepackaged SSD model.
    private static final int INPUT_SIZE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mtcnn = new MTCNN(getAssets());
        btnCreate = findViewById(R.id.btnCreate);
        txtHello = findViewById(R.id.txtHello);
        ivInput = findViewById(R.id.ivInput);

        btnCreate.setOnClickListener(view -> {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ivInput.getDrawable();
            bitmap = bitmapDrawable.getBitmap();

            bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
            final Vector<Box> results = mtcnn.detectFaces(bitmap, 40);
            Log.d("ABC", results.toString());

            txtHello.setText(results + "");
        });

    }
}
