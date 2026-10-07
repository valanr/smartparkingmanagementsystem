package com.example.smartparkingmanagamentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class EnterpriseSearchActivity extends AppCompatActivity {

    public static final String EXTRA_ENTERPRISE_ID = "extra_enterprise_id";

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private RecyclerView rvEnterprises;
    private EnterpriseAdapter adapter;
    private EditText etSearchEnterprise;
    private ChipGroup chipGroupCategories;
    private TextView tvUserProfileInfoHub;

    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterprise_search);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        rvEnterprises = findViewById(R.id.rvEnterprises);
        etSearchEnterprise = findViewById(R.id.etSearchEnterprise);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        tvUserProfileInfoHub = findViewById(R.id.tvUserProfileInfoHub);
        ImageButton btnLogoutHub = findViewById(R.id.btnLogoutHub);

        rvEnterprises.setLayoutManager(new LinearLayoutManager(this));

        if (sessionManager.isLoggedIn()) {
            tvUserProfileInfoHub.setText("👤 Driver: " + sessionManager.getUserName() + " (" + sessionManager.getUserPhone() + ")");
        } else {
            tvUserProfileInfoHub.setText("👤 Guest Driver Mode");
        }

        btnLogoutHub.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        etSearchEnterprise.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEnterprises();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupCategoryChips();
        filterEnterprises();
    }

    private void setupCategoryChips() {
        Chip chipAll = findViewById(R.id.chipCatAll);
        Chip chipMalls = findViewById(R.id.chipCatMalls);
        Chip chipRestaurants = findViewById(R.id.chipCatRestaurants);
        Chip chipCinemas = findViewById(R.id.chipCatCinemas);
        Chip chipOffices = findViewById(R.id.chipCatOffices);

        if (chipAll != null) chipAll.setOnClickListener(v -> { selectedCategory = "All"; filterEnterprises(); });
        if (chipMalls != null) chipMalls.setOnClickListener(v -> { selectedCategory = "Shopping Mall"; filterEnterprises(); });
        if (chipRestaurants != null) chipRestaurants.setOnClickListener(v -> { selectedCategory = "Restaurant"; filterEnterprises(); });
        if (chipCinemas != null) chipCinemas.setOnClickListener(v -> { selectedCategory = "Cinema"; filterEnterprises(); });
        if (chipOffices != null) chipOffices.setOnClickListener(v -> { selectedCategory = "Commercial"; filterEnterprises(); });
    }

    private void filterEnterprises() {
        String query = etSearchEnterprise.getText() != null ? etSearchEnterprise.getText().toString().trim() : "";
        List<Enterprise> filteredList = dbHelper.searchEnterprises(query, selectedCategory);

        if (adapter == null) {
            adapter = new EnterpriseAdapter(this, filteredList, dbHelper, ent -> {
                Intent intent = new Intent(EnterpriseSearchActivity.this, VehicleSelectionActivity.class);
                intent.putExtra(EXTRA_ENTERPRISE_ID, ent.getId());
                startActivity(intent);
            });
            rvEnterprises.setAdapter(adapter);
        } else {
            adapter.updateList(filteredList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        filterEnterprises();
    }
}
