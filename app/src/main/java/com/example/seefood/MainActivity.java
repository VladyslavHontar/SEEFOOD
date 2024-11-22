package com.example.seefood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView midPlainText, touchToSeefood;
    private ImageView imageView, imageView2, imageView3, snap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        midPlainText = findViewById(R.id.mid_plain_text);
        touchToSeefood = findViewById(R.id.touch_to_seefood);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        snap = findViewById(R.id.snap);

        snap.setVisibility(View.INVISIBLE);
        touchToSeefood.setVisibility(View.INVISIBLE);

//        snap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
//                } else {
//                    takePhoto();
//                }
//            }
//        });
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, camera.class);
                startActivity(intent);
            }
        });

        ConstraintLayout mainLayout = findViewById(R.id.main);
        mainLayout.setOnClickListener(v -> {
            snap.setVisibility(View.VISIBLE);
            snap.setAlpha(0f);
            snap.animate()
                    .alpha(1f)
                    .setDuration(1000);

            touchToSeefood.setVisibility(View.VISIBLE);
            touchToSeefood.setAlpha(0f);
            touchToSeefood.animate()
                    .alpha(1f)
                    .setDuration(1000);
            fadeOutViews();
        });

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                Bundle extras = result.getData().getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//                imageView.setImageBitmap(imageBitmap);
//
//            }
//        });

    }

    private void fadeOutViews() {
        midPlainText.animate()
                .alpha(0f)
                .setDuration(1000)
                .withEndAction(() -> midPlainText.setVisibility(View.GONE));

        imageView.animate()
                .alpha(1f)
                .withEndAction(() -> {
                    imageView.setImageTintMode(null);
                    imageView.animate().alpha(0.9f).setDuration(1000).withEndAction(() -> imageView.animate().alpha(1f));
                });

        imageView2.animate()
                .alpha(1f)
                .withEndAction(() -> {
                    imageView2.setImageTintMode(null);
                    imageView2.animate().alpha(0.9f).setDuration(1000).withEndAction(() -> imageView2.animate().alpha(1f));
                });

        imageView3.animate()
                .alpha(1f)
                .withEndAction(() -> {
                    imageView3.setImageTintMode(null);
                    imageView3.animate().alpha(0.9f).setDuration(1000).withEndAction(() -> imageView3.animate().alpha(1f));
                });
    }

//    private void takePhoto() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            cameraLauncher.launch(takePictureIntent);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == CAMERA_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                takePhoto();
//            }
//        }
//    }
}