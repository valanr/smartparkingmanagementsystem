package com.example.smartparkingmanagamentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class EnterpriseRegisterActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private TextInputLayout tilEntName;
    private TextInputLayout tilEntCategory;
    private TextInputLayout tilEntAddress;
    private TextInputLayout tilEntEmail;
    private TextInputLayout tilEntPassword;

    private EditText etEntName;
    private AutoCompleteTextView spinnerEntCategory;
    private EditText etEntAddress;
    private EditText etEntEmail;
    private EditText etEntPassword;
    private EditText etEntBikeRate;
    private EditText etEntCarRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterprise_register);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        tilEntName = findViewById(R.id.tilEntName);
        tilEntCategory = findViewById(R.id.tilEntCategory);
        tilEntAddress = findViewById(R.id.tilEntAddress);
        tilEntEmail = findViewById(R.id.tilEntEmail);
        tilEntPassword = findViewById(R.id.tilEntPassword);

        etEntName = findViewById(R.id.etEntName);
        spinnerEntCategory = findViewById(R.id.spinnerEntCategory);
        etEntAddress = findViewById(R.id.etEntAddress);
        etEntEmail = findViewById(R.id.etEntEmail);
        etEntPassword = findViewById(R.id.etEntPassword);
        etEntBikeRate = findViewById(R.id.etEntBikeRate);
        etEntCarRate = findViewById(R.id.etEntCarRate);

        ImageButton btnBackFromEntRegister = findViewById(R.id.btnBackFromEntRegister);
        Button btnRegisterEnterpriseSubmit = findViewById(R.id.btnRegisterEnterpriseSubmit);

        btnBackFromEntRegister.setOnClickListener(v -> finish());

        String[] categories = new String[]{"Shopping Mall", "Restaurant & Hotel", "Cinema & Entertainment", "Commercial Office"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerEntCategory.setAdapter(adapter);

        btnRegisterEnterpriseSubmit.setOnClickListener(v -> handleEnterpriseRegister());
    }

    private void handleEnterpriseRegister() {
        tilEntName.setError(null);
        tilEntCategory.setError(null);
        tilEntAddress.setError(null);
        tilEntEmail.setError(null);
        tilEntPassword.setError(null);

        String name = etEntName.getText() != null ? etEntName.getText().toString().trim() : "";
        String category = spinnerEntCategory.getText() != null ? spinnerEntCategory.getText().toString().trim() : "";
        String address = etEntAddress.getText() != null ? etEntAddress.getText().toString().trim() : "";
        String email = etEntEmail.getText() != null ? etEntEmail.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        String password = etEntPassword.getText() != null ? etEntPassword.getText().toString().trim() : "";
        String bikeRateStr = etEntBikeRate.getText() != null ? etEntBikeRate.getText().toString().trim() : "10";
        String carRateStr = etEntCarRate.getText() != null ? etEntCarRate.getText().toString().trim() : "20";

        if (name.isEmpty()) {
            tilEntName.setError("Business name is required");
            etEntName.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            tilEntCategory.setError("Select venue category");
            spinnerEntCategory.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            tilEntAddress.setError("Address location is required");
            etEntAddress.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEntEmail.setError("Enter valid business authority email");
            etEntEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 4) {
            tilEntPassword.setError("Password must be at least 4 characters");
            etEntPassword.requestFocus();
            return;
        }

        int bikeRate = 10;
        int carRate = 20;
        try {
            if (!bikeRateStr.isEmpty()) bikeRate = Integer.parseInt(bikeRateStr);
            if (!carRateStr.isEmpty()) carRate = Integer.parseInt(carRateStr);
        } catch (NumberFormatException ignored) {}

        String cleanEntId = "ent_" + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "_");
        Enterprise newEnterprise = new Enterprise(cleanEntId, name, category, address, bikeRate, carRate, email, password);

        boolean success = dbHelper.registerNewEnterprise(newEnterprise, 6, 6);
        if (success) {
            sessionManager.createAdminSession(email, cleanEntId);
            Toast.makeText(this, "🏢 Business " + name + " Registered Successfully!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(EnterpriseRegisterActivity.this, EnterpriseSearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to register business. Email might already exist.", Toast.LENGTH_SHORT).show();
        }
    }
}
