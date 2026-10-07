package com.example.smartparkingmanagamentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class AdminLoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private TextInputLayout tilAdminEmail;
    private TextInputLayout tilAdminPassword;

    private EditText etAdminEmail;
    private EditText etAdminPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        tilAdminEmail = findViewById(R.id.tilAdminEmail);
        tilAdminPassword = findViewById(R.id.tilAdminPassword);

        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);

        Button btnAdminLoginSubmit = findViewById(R.id.btnAdminLoginSubmit);
        Button btnOpenEntRegister = findViewById(R.id.btnOpenEntRegister);
        Button btnBackToUserPortal = findViewById(R.id.btnBackToUserPortal);

        btnBackToUserPortal.setOnClickListener(v -> finish());

        if (btnOpenEntRegister != null) {
            btnOpenEntRegister.setOnClickListener(v -> {
                startActivity(new Intent(this, EnterpriseRegisterActivity.class));
            });
        }

        btnAdminLoginSubmit.setOnClickListener(v -> handleAdminLogin());
    }

    private void handleAdminLogin() {
        tilAdminEmail.setError(null);
        tilAdminPassword.setError(null);

        String email = etAdminEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = etAdminPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilAdminEmail.setError("Enter a valid authority email");
            etAdminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilAdminPassword.setError("Password is required");
            etAdminPassword.requestFocus();
            return;
        }

        String matchedEntId = null;
        if ("nexus@parksmart.com".equals(email) && ("nexus123".equals(password) || "admin123".equals(password))) {
            matchedEntId = "ent_nexus_mall";
        } else if ("hyatt@parksmart.com".equals(email) && ("hyatt123".equals(password) || "admin123".equals(password))) {
            matchedEntId = "ent_grand_hyatt";
        } else if ("pvr@parksmart.com".equals(email) && ("pvr123".equals(password) || "admin123".equals(password))) {
            matchedEntId = "ent_pvr_imax";
        } else if ("techpark@parksmart.com".equals(email) && ("techpark123".equals(password) || "admin123".equals(password))) {
            matchedEntId = "ent_techpark";
        } else if ("admin@parksmart.com".equals(email) && "admin123".equals(password)) {
            matchedEntId = "ent_nexus_mall";
        } else {
            // Dynamic check against database registered enterprise accounts
            Enterprise registeredEnt = dbHelper.authenticateEnterpriseAdmin(email, password);
            if (registeredEnt != null) {
                matchedEntId = registeredEnt.getId();
            }
        }

        if (matchedEntId != null) {
            sessionManager.createAdminSession(email, matchedEntId);
            Toast.makeText(this, "Enterprise Authority Login Successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AdminLoginActivity.this, VehicleSelectionActivity.class);
            intent.putExtra(EnterpriseSearchActivity.EXTRA_ENTERPRISE_ID, matchedEntId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            tilAdminPassword.setError("Invalid credentials! Check authority email & password.");
            etAdminPassword.requestFocus();
        }
    }
}
