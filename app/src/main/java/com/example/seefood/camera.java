package com.example.seefood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.ContentValues;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

public class camera extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private boolean responseReceived = false;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageCapture imageCapture;
    private ImageView capturedImageView;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        View customView = getLayoutInflater().inflate(R.layout.custom_toolbar_item, null);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT,
                Gravity.START
        );
        toolbar.addView(customView, layoutParams);

        ImageButton historyButton = customView.findViewById(R.id.history);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(camera.this, history.class);
            startActivity(intent);
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startCamera();
        }

        ImageView captureButton = findViewById(R.id.image_capture);
        capturedImageView = findViewById(R.id.capturedImageView);
        captureButton.setOnClickListener(v -> capturePhoto());

        capturedImageView.setOnClickListener(v -> resetCameraView());

        ConstraintLayout mainLayout = findViewById(R.id.main);
        mainLayout.setOnClickListener(v -> {
            if (responseReceived) {
                resetCameraView();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setActionView(R.layout.menu_item_layout);
            View view = item.getActionView();
            TextView textView = view.findViewById(R.id.menu_item_text);
            textView.setText(item.getTitle());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sms) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:1234567890"));
            intent.putExtra("sms_body", "Hello, I would like to contact you.");
            startActivity(Intent.createChooser(intent, "Choose an SMS client :"));
            return true;
        }

        if (id == R.id.phone) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:1234567890"));
            startActivity(intent);
            return true;
        }

        if (id == R.id.email) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"hontarvladie@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Contact form");
            email.putExtra(Intent.EXTRA_TEXT, "Watch \"SILICON VALLEY\" series!");
            email.setType("message/rfc822");
            startActivity(Intent.createChooser(email, "Choose an Email client :"));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void resetCameraView() {
        capturedImageView.setVisibility(View.GONE);
        ImageView itsNotHotDog = findViewById(R.id.its_not_hot_dog);
        itsNotHotDog.setVisibility(View.INVISIBLE);
        ImageView itsHotDog = findViewById(R.id.its_hot_dog);
        itsHotDog.setVisibility(View.INVISIBLE);
        ImageView imageCapture = findViewById(R.id.image_capture);
        imageCapture.setVisibility(View.VISIBLE);
        responseReceived = false;
        startCamera();
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Unbind all use cases before rebinding
        cameraProvider.unbindAll();

        // Create a Preview use case
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Create an ImageCapture use case with a lower resolution
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(720, 1280)) // Adjust resolution as needed
                .build();

        // Bind the use cases to the camera with the lifecycle
        try {
            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
            preview.setSurfaceProvider(((PreviewView) findViewById(R.id.previewView)).getSurfaceProvider());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to bind use cases: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            return;
        }

        File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(camera.this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
                displayCapturedImage(photoFile);
                try {
                    Bitmap compressedBitmap = compressImage(photoFile);
                    String base64Image = encodeImageToBase64(compressedBitmap);
                    sendImageToOpenAI(base64Image);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(camera.this, "Failed to capture photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap compressImage(File originalFile) throws IOException {
        Bitmap originalBitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());

        // Resize the bitmap to the target dimensions
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 720, 1280, true);

        // Rotate the image if needed (e.g., 90 degrees)
        Bitmap rotatedBitmap = rotateBitmap(resizedBitmap, 90); // adjust angle if rotation isn't needed

        // Clean up memory
        originalBitmap.recycle();
        resizedBitmap.recycle();

        return rotatedBitmap;
    }

    private void displayCapturedImage(File photoFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        Bitmap rotatedBitmap = rotateBitmap(bitmap, 90);
        capturedImageView.setImageBitmap(rotatedBitmap);
        capturedImageView.setVisibility(ImageView.VISIBLE);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String encodeImageToBase64(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 2, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String base64String = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
        Log.d(TAG, "Base64 image: " + base64String);
        return base64String;
    }


    private void sendImageToOpenAI(String base64Image) {
        new Thread(() -> {
            try {
                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");
                JSONArray contentArray = new JSONArray();
                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", "Is this a hot-dog? (answer with hot-dog or not hot-dog)");
                contentArray.put(textContent);
                JSONObject imageContent = new JSONObject();
                imageContent.put("type", "image_url");
                JSONObject imageUrlObject = new JSONObject();
                imageUrlObject.put("url", "data:image/jpeg;base64," + base64Image);

                imageContent.put("image_url", imageUrlObject);
                contentArray.put(imageContent);
                message.put("content", contentArray);
                messages.put(message);
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "gpt-4o-mini");
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", 300);

                String response = sendPostRequest(API_URL, requestBody.toString());
                handleResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleResponse(String response) {
        runOnUiThread(() -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("error")) {
                    JSONObject error = jsonResponse.getJSONObject("error");
                    String errorMessage = error.getString("message");
                    Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    responseReceived = true;
                } else {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    String content = message.getString("content");

                    Log.d(TAG, "Response content: " + content);

                    if ("not hot-dog".equals(content)) {
                        ImageView itsNotHotDog = findViewById(R.id.its_not_hot_dog);
                        itsNotHotDog.setVisibility(View.VISIBLE);
                        responseReceived = true;
                    } else if ("hot-dog".equals(content)) {
                        ImageView itsHotDog = findViewById(R.id.its_hot_dog);
                        itsHotDog.setVisibility(View.VISIBLE);
                        responseReceived = true;
                    } else {
                        Toast.makeText(this, "Invalid response from OpenAI", Toast.LENGTH_SHORT).show();
                        responseReceived = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                responseReceived = false;
            }
        });
    }

    private String sendPostRequest(String apiUrl, String jsonInputString) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
                throw new IOException("Error response from server: " + errorResponse.toString());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
}