package com.example.smartparkingmanagamentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class AdminLoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private TextInputLayout tilAdminEmail;
    private TextInputLayout tilAdminPassword;

    private EditText etAdminEmail;
    private EditText etAdminPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        sessionManager = new SessionManager(this);

        tilAdminEmail = findViewById(R.id.tilAdminEmail);
        tilAdminPassword = findViewById(R.id.tilAdminPassword);

        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);

        Button btnAdminLoginSubmit = findViewById(R.id.btnAdminLoginSubmit);
        Button btnBackToUserPortal = findViewById(R.id.btnBackToUserPortal);

        btnBackToUserPortal.setOnClickListener(v -> finish());

        btnAdminLoginSubmit.setOnClickListener(v -> handleAdminLogin());
    }

    private void handleAdminLogin() {
        tilAdminEmail.setError(null);
        tilAdminPassword.setError(null);

        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilAdminEmail.setError("Enter a valid admin email");
            etAdminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilAdminPassword.setError("Password is required");
            etAdminPassword.requestFocus();
            return;
        }

        // Demo Admin credentials check
        if ("admin@parksmart.com".equalsIgnoreCase(email) && "admin123".equals(password)) {
            sessionManager.createAdminSession(email);
            Toast.makeText(this, "Manager Login Successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AdminLoginActivity.this, VehicleSelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            tilAdminPassword.setError("Invalid admin credentials (Use: admin@parksmart.com / admin123)");
            etAdminPassword.requestFocus();
        }
    }
}
