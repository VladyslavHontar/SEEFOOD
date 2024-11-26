package com.example.seefood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class accountSettings extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profilePicture;
    private TextView hotDogCountTextView;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        profilePicture = findViewById(R.id.profile_picture);
        hotDogCountTextView = findViewById(R.id.hot_dog_count);
        Button changePictureButton = findViewById(R.id.change_picture_button);
        Button logoutButton = findViewById(R.id.logout_button);

        changePictureButton.setOnClickListener(v -> openGallery());
        logoutButton.setOnClickListener(v -> logout());

        loadUserData();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profilePicture.setImageBitmap(bitmap);
                uploadImageToFirebase(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            StorageReference profilePicRef = mStorageRef.child("users/" + userId + "/profile_picture.jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            Log.d("FirebaseUpload", "Starting upload to: users/" + userId + "/profile_picture.jpg");

            UploadTask uploadTask = profilePicRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d("FirebaseUpload", "Upload successful");
                profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveUserSettings(userId, downloadUrl);
                    Log.d("FirebaseUpload", "Download URL: " + downloadUrl);
                }).addOnFailureListener(e -> {
                    Log.e("FirebaseUpload", "Failed to fetch download URL: " + e.getMessage());
                    Toast.makeText(accountSettings.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e("FirebaseUpload", "Upload failed: " + e.getMessage());
                Toast.makeText(accountSettings.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserSettings(String userId, String profilePictureUrl) {
        Map<String, Object> userSettings = new HashMap<>();
        userSettings.put("profilePictureUrl", profilePictureUrl);
        userSettings.put("name", "User's Name");
        userSettings.put("email", mAuth.getCurrentUser().getEmail());
        userSettings.put("updatedAt", System.currentTimeMillis());

        db.collection("users").document(userId).set(userSettings)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(accountSettings.this, "Profile picture and settings updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(accountSettings.this, "Failed to update settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long hotDogCount = documentSnapshot.getLong("hotDogCount");
                            if (hotDogCount != null) {
                                hotDogCountTextView.setText("Hot-dog pictures taken: " + hotDogCount);
                            } else {
                                hotDogCountTextView.setText("Hot-dog pictures taken: 0");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FirestoreLoad", "Failed to load user data: " + e.getMessage()));
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(accountSettings.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(accountSettings.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}