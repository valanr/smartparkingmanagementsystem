package com.example.smartparkingmanagamentsystem;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminSlotManagerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvManagedSlots;
    private TextView tvTotalConfiguredSlots;

    private String currentEnterpriseId = "ent_nexus_mall";
    private Enterprise currentEnterprise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_slot_manager);

        dbHelper = new DatabaseHelper(this);

        if (getIntent() != null && getIntent().hasExtra(EnterpriseSearchActivity.EXTRA_ENTERPRISE_ID)) {
            currentEnterpriseId = getIntent().getStringExtra(EnterpriseSearchActivity.EXTRA_ENTERPRISE_ID);
        }

        currentEnterprise = dbHelper.getEnterpriseById(currentEnterpriseId);

        rvManagedSlots = findViewById(R.id.rvManagedSlots);
        tvTotalConfiguredSlots = findViewById(R.id.tvTotalConfiguredSlots);
        ImageButton btnBackFromSlotManager = findViewById(R.id.btnBackFromSlotManager);
        Button btnAddNewSlot = findViewById(R.id.btnAddNewSlot);

        btnBackFromSlotManager.setOnClickListener(v -> finish());
        btnAddNewSlot.setOnClickListener(v -> showAddSlotDialog());

        rvManagedSlots.setLayoutManager(new LinearLayoutManager(this));

        loadSlotsData();
    }

    private void loadSlotsData() {
        List<ParkingSlot> bikeSlots = dbHelper.getSlotsByEnterprise(currentEnterpriseId, DatabaseHelper.TYPE_BIKE);
        List<ParkingSlot> carSlots = dbHelper.getSlotsByEnterprise(currentEnterpriseId, DatabaseHelper.TYPE_CAR);

        List<ParkingSlot> allSlots = new ArrayList<>();
        allSlots.addAll(bikeSlots);
        allSlots.addAll(carSlots);

        String venueName = currentEnterprise != null ? currentEnterprise.getName() : "Nexus Mall";
        tvTotalConfiguredSlots.setText(venueName + " Slots: " + allSlots.size());

        ParkingSlotAdapter adapter = new ParkingSlotAdapter(this, allSlots, slot -> {
            new AlertDialog.Builder(AdminSlotManagerActivity.this)
                    .setTitle("Manage " + slot.getSlotCode() + " (" + slot.getVehicleType() + ")")
                    .setMessage("Venue: " + slot.getEnterpriseName() + "\nFloor Zone: " + slot.getFloorZone() + "\nStatus: " + (slot.isOccupied() ? "Occupied" : "Available"))
                    .setNegativeButton("Delete Slot", (dialog, which) -> {
                        if (slot.isOccupied()) {
                            Toast.makeText(AdminSlotManagerActivity.this, "Cannot delete occupied slot! Force release it first.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean success = dbHelper.deleteCustomSlot(slot.getSlotNumber(), slot.getVehicleType(), currentEnterpriseId);
                        if (success) {
                            Toast.makeText(AdminSlotManagerActivity.this, "Slot deleted successfully!", Toast.LENGTH_SHORT).show();
                            loadSlotsData();
                        }
                    })
                    .setPositiveButton("Close", null)
                    .show();
        }, null);

        rvManagedSlots.setAdapter(adapter);
    }

    private void showAddSlotDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_slot, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etAddSlotNumber = dialogView.findViewById(R.id.etAddSlotNumber);
        EditText etAddSlotCode = dialogView.findViewById(R.id.etAddSlotCode);
        EditText etAddSlotFloor = dialogView.findViewById(R.id.etAddSlotFloor);
        RadioButton rbAddBike = dialogView.findViewById(R.id.rbAddBike);
        Button btnCancelAddSlot = dialogView.findViewById(R.id.btnCancelAddSlot);
        Button btnSaveNewSlot = dialogView.findViewById(R.id.btnSaveNewSlot);

        btnCancelAddSlot.setOnClickListener(v -> dialog.dismiss());

        btnSaveNewSlot.setOnClickListener(v -> {
            String numStr = etAddSlotNumber.getText().toString().trim();
            String codeStr = etAddSlotCode.getText().toString().trim();
            String floorStr = etAddSlotFloor.getText().toString().trim();
            String vehicleType = rbAddBike.isChecked() ? DatabaseHelper.TYPE_BIKE : DatabaseHelper.TYPE_CAR;

            if (numStr.isEmpty()) {
                etAddSlotNumber.setError("Slot number is required");
                return;
            }

            int slotNum = Integer.parseInt(numStr);
            if (codeStr.isEmpty()) {
                codeStr = (DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(vehicleType) ? "B-" : "C-") + (100 + slotNum);
            }
            if (floorStr.isEmpty()) {
                floorStr = "Basement B1";
            }

            String venueName = currentEnterprise != null ? currentEnterprise.getName() : "Nexus Shopping Mall";
            ParkingSlot newSlot = new ParkingSlot(slotNum, vehicleType, false, "", "", 0, 0, "", "UNPAID", floorStr, codeStr, currentEnterpriseId, venueName);
            boolean success = dbHelper.addCustomSlot(newSlot);
            if (success) {
                Toast.makeText(AdminSlotManagerActivity.this, "Slot " + codeStr + " created for " + venueName + "!", Toast.LENGTH_SHORT).show();
                loadSlotsData();
                dialog.dismiss();
            } else {
                Toast.makeText(AdminSlotManagerActivity.this, "Failed to create slot.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
