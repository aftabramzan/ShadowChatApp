package com.example.shadowchatapp2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.widget.ArrayAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> realImagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;


    private final String PROFILE_URL = "https://shadowtalk-omega.vercel.app/api/create-user";

    private OkHttpClient client = new OkHttpClient();

    private EditText etFirstName, etMiddleName, etLastName, etAge, etCity, etAnonymous, etPostal;
    private AutoCompleteTextView actvCountry;
    private Button btnSubmitProfile, btnSkip;

    private int UAID = 1; // Dynamic UAID after login
    private Bitmap realImageBitmap; // Get this from image picker or camera
    private Bitmap hideImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        etFirstName = findViewById(R.id.et_first_name);
        etMiddleName = findViewById(R.id.et_middle_name);
        etLastName = findViewById(R.id.et_last_name);
        etAge = findViewById(R.id.et_age);
        etCity = findViewById(R.id.et_city);
        etAnonymous = findViewById(R.id.et_anonymous_name);
        etPostal = findViewById(R.id.et_postal_code);
        actvCountry = findViewById(R.id.et_country);
        btnSubmitProfile = findViewById(R.id.btn_save_profile);
        btnSkip = findViewById(R.id.btn_skip);


        setupCountryDropdown();


        btnSubmitProfile.setOnClickListener(v -> validateAndSaveProfile());
        btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void setupCountryDropdown() {
        // Get all country names
        String[] countries = Locale.getISOCountries();
        String[] countryNames = new String[countries.length];

        for (int i = 0; i < countries.length; i++) {
            Locale locale = new Locale("", countries[i]);
            countryNames[i] = locale.getDisplayCountry();
        }

        // Create adapter for the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                countryNames
        );

        actvCountry.setAdapter(adapter);
        actvCountry.setThreshold(1); // Start showing suggestions after 1 character
    }

    private void validateAndSaveProfile() {
        // Validate all required fields
        if (isEmpty(etFirstName) ||
                isEmpty(etLastName) ||
                isEmpty(etAge) ||
                isEmpty(actvCountry) ||
                isEmpty(etCity) ||
                isEmpty(etAnonymous) ||
                isEmpty(etPostal)) {

            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate age is a number
        try {
            int age = Integer.parseInt(etAge.getText().toString());
            if (age <= 0 || age > 120) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Age must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        // If validation passes, save profile
        saveProfile();
    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private boolean isEmpty(AutoCompleteTextView autoCompleteTextView) {
        return autoCompleteTextView.getText().toString().trim().isEmpty();
    }

    private void saveProfile() {
        // Convert bitmaps to base64 strings, handle null case
        String realImage = realImageBitmap != null ? bitmapToBase64(realImageBitmap) : "";
        String hideImage = hideImageBitmap != null ? bitmapToBase64(hideImageBitmap) : "";

        // Create a JSON object to hold the data
        String jsonBody = String.format("{\n" +
                        "\"uaid\": %d,\n" +
                        "\"first_name\": \"%s\",\n" +
                        "\"middle_name\": \"%s\",\n" +
                        "\"last_name\": \"%s\",\n" +
                        "\"age\": \"%s\",\n" +
                        "\"country\": \"%s\",\n" +
                        "\"city\": \"%s\",\n" +
                        "\"anonymous_name\": \"%s\",\n" +
                        "\"postal_code\": \"%s\",\n" +
                        "\"real_image\": \"%s\",\n" +
                        "\"hide_image\": \"%s\"\n" +
                        "}",
                UAID,
                etFirstName.getText().toString().trim(),
                etMiddleName.getText().toString().trim(),
                etLastName.getText().toString().trim(),
                etAge.getText().toString().trim(),
                actvCountry.getText().toString().trim(),
                etCity.getText().toString().trim(),
                etAnonymous.getText().toString().trim(),
                etPostal.getText().toString().trim(),
                realImage,
                hideImage
        );

        RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(PROFILE_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UserProfileActivity.this,
                            "Server connection failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(UserProfileActivity.this, ProfileImageActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(UserProfileActivity.this,
                                "Error: " + res,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}