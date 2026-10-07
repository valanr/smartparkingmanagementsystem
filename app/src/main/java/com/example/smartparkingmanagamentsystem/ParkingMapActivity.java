package com.example.smartparkingmanagamentsystem;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ParkingMapActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvMapSlots;
    private TextView tvMapHeaderTitle;
    private ChipGroup chipGroupFloors;

    private String currentVehicleType = DatabaseHelper.TYPE_CAR;
    private String selectedFloor = "Basement B1";

    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            loadFloorMapSlots();
            tickerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_map);

        dbHelper = new DatabaseHelper(this);

        if (getIntent() != null && getIntent().hasExtra(VehicleSelectionActivity.EXTRA_VEHICLE_TYPE)) {
            currentVehicleType = getIntent().getStringExtra(VehicleSelectionActivity.EXTRA_VEHICLE_TYPE);
        }

        rvMapSlots = findViewById(R.id.rvMapSlots);
        tvMapHeaderTitle = findViewById(R.id.tvMapHeaderTitle);
        chipGroupFloors = findViewById(R.id.chipGroupFloors);
        ImageButton btnBackFromMap = findViewById(R.id.btnBackFromMap);

        btnBackFromMap.setOnClickListener(v -> finish());

        if (DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(currentVehicleType)) {
            tvMapHeaderTitle.setText("🏍️ Bike Floor Map");
        } else {
            tvMapHeaderTitle.setText("🚗 Car Floor Map");
        }

        rvMapSlots.setLayoutManager(new GridLayoutManager(this, 3));

        setupFloorChips();
        loadFloorMapSlots();
    }

    private void setupFloorChips() {
        chipGroupFloors.removeAllViews();
        List<String> floors = dbHelper.getAvailableFloors(currentVehicleType);

        for (int i = 0; i < floors.size(); i++) {
            String floor = floors.get(i);
            Chip chip = new Chip(this);
            chip.setText("📍 " + floor);
            chip.setCheckable(true);
            chip.setClickable(true);
            if (i == 0) {
                chip.setChecked(true);
                selectedFloor = floor;
            }
            chip.setOnClickListener(v -> {
                selectedFloor = floor;
                loadFloorMapSlots();
            });
            chipGroupFloors.addView(chip);
        }
    }

    private void loadFloorMapSlots() {
        dbHelper.checkAndAutoCheckoutExpiredSlots();

        List<ParkingSlot> floorSlots = dbHelper.getSlotsByFloor(currentVehicleType, selectedFloor);

        ParkingSlotAdapter adapter = new ParkingSlotAdapter(this, floorSlots, slot -> {
            if (slot.isOccupied()) {
                Toast.makeText(this, "Slot " + slot.getSlotCode() + " is currently occupied!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selected Slot " + slot.getSlotCode() + " on " + selectedFloor, Toast.LENGTH_SHORT).show();
            }
        }, null);

        rvMapSlots.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tickerHandler.post(clockRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tickerHandler.removeCallbacks(clockRunnable);
    }
}
