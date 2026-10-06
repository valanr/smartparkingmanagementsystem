package com.example.smartparkingmanagamentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class VehicleSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_TYPE = "extra_vehicle_type";

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private TextView tvBikeAvailableCount;
    private TextView tvCarAvailableCount;
    private TextView tvWelcomeUser;
    private TextView tvUserProfileVehicle;
    private TextView tvUserProfilePhone;

    // Real-time Ticker Handler
    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateSlotCounters();
            tickerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_selection);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        tvBikeAvailableCount = findViewById(R.id.tvBikeAvailableCount);
        tvCarAvailableCount = findViewById(R.id.tvCarAvailableCount);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserProfileVehicle = findViewById(R.id.tvUserProfileVehicle);
        tvUserProfilePhone = findViewById(R.id.tvUserProfilePhone);

        MaterialCardView cardTwoWheeler = findViewById(R.id.cardTwoWheeler);
        MaterialCardView cardFourWheeler = findViewById(R.id.cardFourWheeler);
        Button btnSelectBike = findViewById(R.id.btnSelectBike);
        Button btnSelectCar = findViewById(R.id.btnSelectCar);
        Button btnUserHistory = findViewById(R.id.btnUserHistory);
        Button btnAdminPortal = findViewById(R.id.btnAdminPortal);
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        cardTwoWheeler.setOnClickListener(v -> openParkingSlots(DatabaseHelper.TYPE_BIKE));
        btnSelectBike.setOnClickListener(v -> openParkingSlots(DatabaseHelper.TYPE_BIKE));

        cardFourWheeler.setOnClickListener(v -> openParkingSlots(DatabaseHelper.TYPE_CAR));
        btnSelectCar.setOnClickListener(v -> openParkingSlots(DatabaseHelper.TYPE_CAR));

        btnUserHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        btnAdminPortal.setOnClickListener(v -> {
            if (sessionManager.isAdmin()) {
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(v -> {
            if (sessionManager.isAdmin()) {
                if (sessionManager.switchToUserMode()) {
                    Toast.makeText(this, "Switched back to User Profile!", Toast.LENGTH_SHORT).show();
                    loadUserProfile();
                    return;
                }
            }
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        loadUserProfile();
    }

    private void loadUserProfile() {
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                tvWelcomeUser.setText("Welcome, Manager!");
                tvUserProfileVehicle.setText("🔐 Role: Admin / Manager");
                tvUserProfilePhone.setText("admin@parksmart.com");
            } else {
                tvWelcomeUser.setText("Welcome, " + sessionManager.getUserName() + "!");
                String typeIcon = DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(sessionManager.getUserVehicleType()) ? "🏍️ Bike: " : "🚗 Car: ";
                tvUserProfileVehicle.setText(typeIcon + sessionManager.getUserVehicleNumber());
                tvUserProfilePhone.setText("📞 +91 " + sessionManager.getUserPhone());

                // Auto-sync current active user session to users table and Firestore
                User currentUser = new User(0, sessionManager.getUserName(), sessionManager.getUserEmail(), sessionManager.getUserPhone(), sessionManager.getUserVehicleNumber(), sessionManager.getUserVehicleType(), System.currentTimeMillis());
                dbHelper.registerUser(currentUser);
            }
        } else {
            tvWelcomeUser.setText("Welcome, Guest!");
            tvUserProfileVehicle.setText("🚘 No Vehicle Profile Set");
            tvUserProfilePhone.setText("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        tickerHandler.post(clockRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tickerHandler.removeCallbacks(clockRunnable);
    }

    private void updateSlotCounters() {
        dbHelper.checkAndAutoCheckoutExpiredSlots();

        int bikeOccupied = dbHelper.getOccupiedCountByType(DatabaseHelper.TYPE_BIKE);
        int bikeTotal = dbHelper.getTotalCountByType(DatabaseHelper.TYPE_BIKE);
        int bikeAvailable = bikeTotal - bikeOccupied;

        int carOccupied = dbHelper.getOccupiedCountByType(DatabaseHelper.TYPE_CAR);
        int carTotal = dbHelper.getTotalCountByType(DatabaseHelper.TYPE_CAR);
        int carAvailable = carTotal - carOccupied;

        tvBikeAvailableCount.setText(bikeAvailable + " / " + bikeTotal + " Slots Available");
        tvCarAvailableCount.setText(carAvailable + " / " + carTotal + " Slots Available");
    }

    private void openParkingSlots(String vehicleType) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_VEHICLE_TYPE, vehicleType);
        startActivity(intent);
    }
}
