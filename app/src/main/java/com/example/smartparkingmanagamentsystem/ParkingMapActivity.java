package com.example.smartparkingmanagamentsystem;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParkingMapActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private RecyclerView rvMapSlots;
    private TextView tvMapHeaderTitle;
    private ChipGroup chipGroupFloors;

    private String currentVehicleType = DatabaseHelper.TYPE_CAR;
    private String currentEnterpriseId = "ent_nexus_mall";
    private Enterprise currentEnterprise;

    private String selectedFloor = "Basement B1";
    private int hourlyRate = 20;

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
        sessionManager = new SessionManager(this);

        if (getIntent() != null) {
            if (getIntent().hasExtra(VehicleSelectionActivity.EXTRA_VEHICLE_TYPE)) {
                currentVehicleType = getIntent().getStringExtra(VehicleSelectionActivity.EXTRA_VEHICLE_TYPE);
            }
            if (getIntent().hasExtra(EnterpriseSearchActivity.EXTRA_ENTERPRISE_ID)) {
                currentEnterpriseId = getIntent().getStringExtra(EnterpriseSearchActivity.EXTRA_ENTERPRISE_ID);
            }
        }

        currentEnterprise = dbHelper.getEnterpriseById(currentEnterpriseId);

        if (DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(currentVehicleType)) {
            hourlyRate = currentEnterprise != null ? currentEnterprise.getBikeRate() : 10;
        } else {
            hourlyRate = currentEnterprise != null ? currentEnterprise.getCarRate() : 20;
        }

        rvMapSlots = findViewById(R.id.rvMapSlots);
        tvMapHeaderTitle = findViewById(R.id.tvMapHeaderTitle);
        chipGroupFloors = findViewById(R.id.chipGroupFloors);
        ImageButton btnBackFromMap = findViewById(R.id.btnBackFromMap);

        btnBackFromMap.setOnClickListener(v -> finish());

        String venueName = currentEnterprise != null ? currentEnterprise.getName() : "Nexus Mall";
        if (DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(currentVehicleType)) {
            tvMapHeaderTitle.setText("🏍️ " + venueName + " Map");
        } else {
            tvMapHeaderTitle.setText("🚗 " + venueName + " Map");
        }

        rvMapSlots.setLayoutManager(new GridLayoutManager(this, 3));

        setupFloorChips();
        loadFloorMapSlots();
    }

    private void setupFloorChips() {
        chipGroupFloors.removeAllViews();
        List<String> floors = dbHelper.getAvailableFloors(currentVehicleType, currentEnterpriseId);

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

        List<ParkingSlot> floorSlots = dbHelper.getSlotsByFloor(currentVehicleType, selectedFloor, currentEnterpriseId);

        ParkingSlotAdapter adapter = new ParkingSlotAdapter(this, floorSlots, slot -> {
            if (slot.isOccupied()) {
                showTicketDetailsDialog(slot);
            } else {
                showSingleBookSlotDialog(slot);
            }
        }, null);

        rvMapSlots.setAdapter(adapter);
    }

    private void showSingleBookSlotDialog(ParkingSlot slot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_book_slot, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvBookSlotTitle = dialogView.findViewById(R.id.tvBookSlotTitle);
        TextView tvRateNotice = dialogView.findViewById(R.id.tvRateNotice);
        TextInputLayout tilVehicleNumber = dialogView.findViewById(R.id.tilVehicleNumber);
        TextInputLayout tilPhoneNumber = dialogView.findViewById(R.id.tilPhoneNumber);
        TextInputLayout tilBookingHours = dialogView.findViewById(R.id.tilBookingHours);

        EditText etVehicleNumber = dialogView.findViewById(R.id.etVehicleNumber);
        EditText etPhoneNumber = dialogView.findViewById(R.id.etPhoneNumber);
        EditText etBookingHours = dialogView.findViewById(R.id.etBookingHours);
        TextView tvEstimatedFee = dialogView.findViewById(R.id.tvEstimatedFee);
        Button btnCancelBook = dialogView.findViewById(R.id.btnCancelBook);
        Button btnConfirmBook = dialogView.findViewById(R.id.btnConfirmBook);

        tvBookSlotTitle.setText("Book " + slot.getSlotCode() + " (" + currentVehicleType + ")");
        tvEstimatedFee.setText("₹" + hourlyRate);

        if (sessionManager != null && sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                tvRateNotice.setText("Manager Mode: ₹" + hourlyRate + "/hr (" + slot.getFloorZone() + ")");
            } else {
                tvRateNotice.setText("Zone: " + slot.getFloorZone() + " | Rate: ₹" + hourlyRate + " / Hour");
                if (sessionManager.getUserVehicleNumber() != null && !sessionManager.getUserVehicleNumber().isEmpty()) {
                    etVehicleNumber.setText(sessionManager.getUserVehicleNumber());
                }
                if (sessionManager.getUserPhone() != null && !sessionManager.getUserPhone().isEmpty()) {
                    etPhoneNumber.setText(sessionManager.getUserPhone());
                }
            }
        }

        etBookingHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (!input.isEmpty()) {
                    try {
                        int hours = Integer.parseInt(input);
                        if (hours >= 1 && hours <= 24) {
                            int totalFee = hours * hourlyRate;
                            tvEstimatedFee.setText("₹" + totalFee);
                            tilBookingHours.setError(null);
                        } else {
                            tvEstimatedFee.setText("₹0");
                            tilBookingHours.setError("1 - 24 hours max");
                        }
                    } catch (NumberFormatException e) {
                        tvEstimatedFee.setText("₹0");
                    }
                } else {
                    tvEstimatedFee.setText("₹0");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancelBook.setOnClickListener(v -> dialog.dismiss());

        btnConfirmBook.setOnClickListener(v -> {
            String vehicleNum = etVehicleNumber.getText().toString().trim().toUpperCase(Locale.ROOT);
            String phoneNum = etPhoneNumber.getText().toString().trim().replaceAll("\\s+", "");
            String hoursStr = etBookingHours.getText().toString().trim();

            if (vehicleNum.isEmpty()) {
                tilVehicleNumber.setError("Vehicle number is required");
                return;
            }
            if (phoneNum.isEmpty()) {
                tilPhoneNumber.setError("Mobile number is required");
                return;
            }
            if (hoursStr.isEmpty()) {
                tilBookingHours.setError("Booking duration is required");
                return;
            }

            int hours = Integer.parseInt(hoursStr);
            String paymentId = "UPI_REF_" + System.currentTimeMillis();

            boolean success = dbHelper.bookSlot(slot.getSlotNumber(), currentVehicleType, vehicleNum, phoneNum, hours, System.currentTimeMillis(), paymentId, "SUCCESS", currentEnterpriseId);
            if (success) {
                Toast.makeText(this, "📱 Slot " + slot.getSlotCode() + " Booked Successfully!", Toast.LENGTH_LONG).show();
                loadFloorMapSlots();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to book slot.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showTicketDetailsDialog(ParkingSlot slot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ticket_details, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvTicketTitle = dialogView.findViewById(R.id.tvTicketTitle);
        TextView tvTicketVehicle = dialogView.findViewById(R.id.tvTicketVehicle);
        TextView tvTicketPhone = dialogView.findViewById(R.id.tvTicketPhone);
        TextView tvTicketEntryTime = dialogView.findViewById(R.id.tvTicketEntryTime);
        TextView tvTicketDuration = dialogView.findViewById(R.id.tvTicketDuration);
        TextView tvTicketTotalFee = dialogView.findViewById(R.id.tvTicketTotalFee);
        Button btnCloseTicket = dialogView.findViewById(R.id.btnCloseTicket);
        Button btnCheckout = dialogView.findViewById(R.id.btnCheckout);

        tvTicketTitle.setText("Active Ticket - " + slot.getSlotCode() + " (" + slot.getEnterpriseName() + ")");
        tvTicketVehicle.setText("Vehicle: " + slot.getVehicleNumber());
        tvTicketPhone.setText("Phone: +91 " + slot.getPhoneNumber());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        tvTicketEntryTime.setText("Entry Time: " + sdf.format(new Date(slot.getEntryTime())));
        tvTicketDuration.setText("Duration: " + slot.getBookingHours() + " Hour(s)");

        int totalFee = slot.getBookingHours() * hourlyRate;
        tvTicketTotalFee.setText("Total Amount: ₹" + totalFee + " (PAID)");

        btnCloseTicket.setOnClickListener(v -> dialog.dismiss());

        btnCheckout.setOnClickListener(v -> {
            boolean success = dbHelper.leaveSlotAndRecordHistory(slot, totalFee);
            if (success) {
                Toast.makeText(this, "Slot " + slot.getSlotCode() + " checked out!", Toast.LENGTH_SHORT).show();
                loadFloorMapSlots();
                dialog.dismiss();
            }
        });

        dialog.show();
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
