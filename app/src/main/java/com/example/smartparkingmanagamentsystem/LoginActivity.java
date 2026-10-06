package com.example.smartparkingmanagamentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private TextInputLayout tilLoginName;
    private TextInputLayout tilLoginEmail;
    private TextInputLayout tilLoginPhone;
    private TextInputLayout tilLoginVehicleNo;

    private EditText etLoginName;
    private EditText etLoginEmail;
    private EditText etLoginPhone;
    private EditText etLoginVehicleNo;
    private RadioButton rbTwoWheeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, VehicleSelectionActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        tilLoginName = findViewById(R.id.tilLoginName);
        tilLoginEmail = findViewById(R.id.tilLoginEmail);
        tilLoginPhone = findViewById(R.id.tilLoginPhone);
        tilLoginVehicleNo = findViewById(R.id.tilLoginVehicleNo);

        etLoginName = findViewById(R.id.etLoginName);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPhone = findViewById(R.id.etLoginPhone);
        etLoginVehicleNo = findViewById(R.id.etLoginVehicleNo);

        rbTwoWheeler = findViewById(R.id.rbTwoWheeler);

        Button btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
        Button btnOpenAdminLogin = findViewById(R.id.btnOpenAdminLogin);

        btnLoginSubmit.setOnClickListener(v -> handleLoginSubmit());

        btnOpenAdminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });
    }

    private void handleLoginSubmit() {
        tilLoginName.setError(null);
        tilLoginEmail.setError(null);
        tilLoginPhone.setError(null);
        tilLoginVehicleNo.setError(null);

        String name = etLoginName.getText().toString().trim();
        String email = etLoginEmail.getText().toString().trim();
        String phone = etLoginPhone.getText().toString().trim().replaceAll("\\s+", "");
        String vehicleNo = etLoginVehicleNo.getText().toString().trim().toUpperCase(Locale.ROOT);
        String vehicleType = rbTwoWheeler.isChecked() ? DatabaseHelper.TYPE_BIKE : DatabaseHelper.TYPE_CAR;

        if (name.isEmpty()) {
            tilLoginName.setError("Full Name is required");
            etLoginName.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilLoginEmail.setError("Enter a valid email address");
            etLoginEmail.requestFocus();
            return;
        }

        if (phone.isEmpty() || (!phone.matches("^[6-9]\\d{9}$") && !phone.matches("^\\d{10}$"))) {
            tilLoginPhone.setError("Enter a valid 10-digit mobile number");
            etLoginPhone.requestFocus();
            return;
        }

        String cleanVehicle = vehicleNo.replaceAll("[^A-Z0-9]", "");
        if (vehicleNo.isEmpty() || cleanVehicle.length() < 4) {
            tilLoginVehicleNo.setError("Enter valid vehicle registration (e.g. MH12AB1234)");
            etLoginVehicleNo.requestFocus();
            return;
        }

        // Save session locally
        sessionManager.createLoginSession(name, email, phone, vehicleNo, vehicleType);

        // Register User in SQLite & Firebase Cloud Database
        User user = new User(0, name, email, phone, vehicleNo, vehicleType, System.currentTimeMillis());
        dbHelper.registerUser(user);

        Toast.makeText(this, "Profile Registered! Welcome " + name, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, VehicleSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
