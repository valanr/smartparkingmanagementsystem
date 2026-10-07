package com.example.smartparkingmanagamentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private TextInputLayout tilLoginName;
    private TextInputLayout tilLoginEmail;
    private TextInputLayout tilLoginPhone;
    private TextInputLayout tilLoginVehicleNo;

    private TextInputEditText etLoginName;
    private TextInputEditText etLoginEmail;
    private TextInputEditText etLoginPhone;
    private TextInputEditText etLoginVehicleNo;

    private RadioButton rbTwoWheeler;
    private RadioButton rbFourWheeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, EnterpriseSearchActivity.class));
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
        rbFourWheeler = findViewById(R.id.rbFourWheeler);

        Button btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
        Button btnOpenAdminLogin = findViewById(R.id.btnOpenAdminLogin);

        if (btnLoginSubmit != null) {
            btnLoginSubmit.setOnClickListener(v -> attemptProfileSave());
        }

        if (btnOpenAdminLogin != null) {
            btnOpenAdminLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminLoginActivity.class));
            });
        }
    }

    private void attemptProfileSave() {
        if (tilLoginName != null) tilLoginName.setError(null);
        if (tilLoginEmail != null) tilLoginEmail.setError(null);
        if (tilLoginPhone != null) tilLoginPhone.setError(null);
        if (tilLoginVehicleNo != null) tilLoginVehicleNo.setError(null);

        String name = etLoginName != null && etLoginName.getText() != null ? etLoginName.getText().toString().trim() : "";
        String email = etLoginEmail != null && etLoginEmail.getText() != null ? etLoginEmail.getText().toString().trim() : "";
        String phone = etLoginPhone != null && etLoginPhone.getText() != null ? etLoginPhone.getText().toString().trim().replaceAll("\\s+", "") : "";
        String vehicleNumber = etLoginVehicleNo != null && etLoginVehicleNo.getText() != null ? etLoginVehicleNo.getText().toString().trim().toUpperCase(Locale.ROOT) : "";

        if (name.isEmpty()) {
            if (tilLoginName != null) tilLoginName.setError("Name is required");
            if (etLoginName != null) etLoginName.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilLoginEmail != null) tilLoginEmail.setError("Enter a valid email address");
            if (etLoginEmail != null) etLoginEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            if (tilLoginPhone != null) tilLoginPhone.setError("Mobile number is required");
            if (etLoginPhone != null) etLoginPhone.requestFocus();
            return;
        } else if (!phone.matches("^[6-9]\\d{9}$") && !phone.matches("^\\d{10}$")) {
            if (tilLoginPhone != null) tilLoginPhone.setError("Enter a valid 10-digit Indian mobile number");
            if (etLoginPhone != null) etLoginPhone.requestFocus();
            return;
        }

        String cleanVehicleStr = vehicleNumber.replaceAll("[^A-Z0-9]", "");
        if (vehicleNumber.isEmpty()) {
            if (tilLoginVehicleNo != null) tilLoginVehicleNo.setError("Vehicle number is required");
            if (etLoginVehicleNo != null) etLoginVehicleNo.requestFocus();
            return;
        } else if (cleanVehicleStr.length() < 4) {
            if (tilLoginVehicleNo != null) tilLoginVehicleNo.setError("Enter valid vehicle number (e.g. MH12AB1234)");
            if (etLoginVehicleNo != null) etLoginVehicleNo.requestFocus();
            return;
        }

        String vehicleType = rbTwoWheeler != null && rbTwoWheeler.isChecked() ? DatabaseHelper.TYPE_BIKE : DatabaseHelper.TYPE_CAR;

        // Save User record in SQLite DB and Cloud Firestore
        User user = new User(0, name, email, phone, vehicleNumber, vehicleType, System.currentTimeMillis());
        dbHelper.registerUser(user);

        sessionManager.createLoginSession(name, email, phone, vehicleNumber, vehicleType);

        Toast.makeText(this, "Profile Saved! Welcome " + name, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, EnterpriseSearchActivity.class);
        startActivity(intent);
        finish();
    }
}
