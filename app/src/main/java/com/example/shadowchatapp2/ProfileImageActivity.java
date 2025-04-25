package com.example.shadowchatapp2;

import android.os.Bundle;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.IOException;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileImageActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    private static final int TAKE_PICTURE = 101;

    private ShapeableImageView imgProfilePreview;
    private TextView txtSelectionStatus;
    private MaterialButton btnContinue;
    private ImageView imgSelectionIndicator;

    private Bitmap selectedImageBitmap;
    private OkHttpClient client = new OkHttpClient();

    private final String PROFILE_URL = "http://192.168.20.211/myapp/user_image.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_image);
        imgProfilePreview = findViewById(R.id.img_profile_preview);
        txtSelectionStatus = findViewById(R.id.txt_selection_status);
        btnContinue = findViewById(R.id.btn_continue);
        imgSelectionIndicator = findViewById(R.id.img_selection_indicator);

        // Initially disable the "Continue" button
        btnContinue.setEnabled(false);

        // Camera and Gallery options
        findViewById(R.id.card_camera).setOnClickListener(v -> openCamera());
        findViewById(R.id.card_gallery).setOnClickListener(v -> openGallery());

        // Continue button click to upload the image
        btnContinue.setOnClickListener(v -> uploadProfileImage());
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PICTURE);
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE) {
                // Gallery image selected
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    imgProfilePreview.setImageBitmap(selectedImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == TAKE_PICTURE) {
                // Camera image captured
                Bundle extras = data.getExtras();
                if (extras != null) {
                    selectedImageBitmap = (Bitmap) extras.get("data");
                    imgProfilePreview.setImageBitmap(selectedImageBitmap);
                }
            }

            // Update status text and enable "Continue" button
            txtSelectionStatus.setText("Photo selected");
            btnContinue.setEnabled(true);
            imgSelectionIndicator.setVisibility(View.GONE);
        }
    }

    private void uploadProfileImage() {
        if (selectedImageBitmap == null) {
            Toast.makeText(this, "Please select a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        String base64Image = bitmapToBase64(selectedImageBitmap);

        // Create request body
        RequestBody formData = new FormBody.Builder()
                .add("real_image", base64Image)
                .build();

        Request request = new Request.Builder()
                .url(PROFILE_URL)
                .post(formData)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileImageActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileImageActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ProfileImageActivity.this,MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ProfileImageActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}

