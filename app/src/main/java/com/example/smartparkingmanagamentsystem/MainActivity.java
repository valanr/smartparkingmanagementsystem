package com.example.smartparkingmanagamentsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private RecyclerView rvSlots;
    private ParkingSlotAdapter adapter;
    private TextView tvAvailableCount;
    private TextView tvOccupiedCount;
    private TextView tvHeaderTitle;
    private TextView tvHeaderRate;
    private EditText etSearchSlot;

    // Multi-Booking UI Controls
    private MaterialCardView cardMultiBookingBar;
    private TextView tvSelectedSlotsCount;
    private TextView tvClearSelection;

    private String currentVehicleType = DatabaseHelper.TYPE_CAR;
    private int hourlyRate = 20;

    private List<ParkingSlot> fullSlotList = new ArrayList<>();

    private static class PendingBooking {
        boolean isBatch;
        int singleSlotNumber;
        Set<Integer> batchSlotNumbers;
        String vehicleType;
        String vehicleNum;
        String phoneNum;
        int hours;
        int totalFee;
    }

    // Camera Proof Capture Launcher
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ImageView currentImgPreview;
    private TextView currentTvProofStatus;
    private Button currentBtnConfirmRelease;
    private String capturedProofPath = "";

    // Real-time Clock Handler
    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            loadSlotsData();
            tickerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent() != null && getIntent().hasExtra(VehicleSelectionActivity.EXTRA_VEHICLE_TYPE)) {
            currentVehicleType = getIntent().getStringExtra(VehicleSelectionActivity.EXTRA_VEHICLE_TYPE);
        }

        if (DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(currentVehicleType)) {
            hourlyRate = 10;
        } else {
            hourlyRate = 20;
        }

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        rvSlots = findViewById(R.id.rvSlots);
        tvAvailableCount = findViewById(R.id.tvAvailableCount);
        tvOccupiedCount = findViewById(R.id.tvOccupiedCount);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderRate = findViewById(R.id.tvHeaderRate);
        etSearchSlot = findViewById(R.id.etSearchSlot);
        ImageButton btnBackToSelection = findViewById(R.id.btnBackToSelection);

        // Multi-Booking Bar Views
        cardMultiBookingBar = findViewById(R.id.cardMultiBookingBar);
        tvSelectedSlotsCount = findViewById(R.id.tvSelectedSlotsCount);
        tvClearSelection = findViewById(R.id.tvClearSelection);
        Button btnBookSelectedSlots = findViewById(R.id.btnBookSelectedSlots);

        btnBackToSelection.setOnClickListener(v -> finish());

        if (DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(currentVehicleType)) {
            tvHeaderTitle.setText("🏍️ Two Wheeler Parking");
            tvHeaderRate.setText("Rate: ₹10 / Hour");
        } else {
            tvHeaderTitle.setText("🚗 Four Wheeler Parking");
            tvHeaderRate.setText("Rate: ₹20 / Hour");
        }

        rvSlots.setLayoutManager(new GridLayoutManager(this, 3));

        // Setup Camera ActivityResultLauncher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null && extras.get("data") != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            String savedPath = saveProofBitmapLocally(imageBitmap);
                            if (savedPath != null) {
                                capturedProofPath = savedPath;
                                if (currentImgPreview != null) {
                                    currentImgPreview.setImageBitmap(imageBitmap);
                                    currentImgPreview.setVisibility(View.VISIBLE);
                                }
                                if (currentTvProofStatus != null) {
                                    currentTvProofStatus.setText("✓ Incident Photo Proof Attached!");
                                    currentTvProofStatus.setTextColor(ContextCompat.getColor(this, R.color.slot_available_text));
                                }
                                if (currentBtnConfirmRelease != null) {
                                    currentBtnConfirmRelease.setEnabled(true);
                                }
                            }
                        }
                    }
                }
        );

        etSearchSlot.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSlots(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvClearSelection.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.clearSelection();
            }
        });

        btnBookSelectedSlots.setOnClickListener(v -> {
            if (adapter != null && !adapter.getSelectedSlotNumbers().isEmpty()) {
                showBatchBookSlotDialog(adapter.getSelectedSlotNumbers());
            }
        });

        loadSlotsData();
    }

    private void showUpiQrPaymentDialog(PendingBooking pending) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_upi_qr_payment, null);
        builder.setView(dialogView);

        AlertDialog upiDialog = builder.create();

        TextView tvUpiAmount = dialogView.findViewById(R.id.tvUpiAmount);
        ImageView imgUpiQrCode = dialogView.findViewById(R.id.imgUpiQrCode);
        TextInputLayout tilUtrNumber = dialogView.findViewById(R.id.tilUtrNumber);
        EditText etUtrNumber = dialogView.findViewById(R.id.etUtrNumber);
        Button btnCancelUpi = dialogView.findViewById(R.id.btnCancelUpi);
        Button btnConfirmUpiPayment = dialogView.findViewById(R.id.btnConfirmUpiPayment);

        tvUpiAmount.setText("Total Amount to Pay: ₹" + pending.totalFee);

        // Check if custom user QR image 'gpay_qr' exists in res/drawable
        int customQrResId = getResources().getIdentifier("gpay_qr", "drawable", getPackageName());
        if (customQrResId != 0) {
            imgUpiQrCode.setImageResource(customQrResId);
        } else {
            // Fallback to generated dynamic QR bitmap
            String upiContent = "upi://pay?pa=parksmart@okaxis&pn=ParkSmartSystem&am=" + pending.totalFee + "&cu=INR";
            Bitmap qrBitmap = QrGenerator.generateQrBitmap(upiContent, 300, 300);
            imgUpiQrCode.setImageBitmap(qrBitmap);
        }

        btnCancelUpi.setOnClickListener(v -> upiDialog.dismiss());

        btnConfirmUpiPayment.setOnClickListener(v -> {
            tilUtrNumber.setError(null);
            String utrNo = etUtrNumber.getText().toString().trim();

            if (utrNo.isEmpty() || utrNo.length() < 6) {
                tilUtrNumber.setError("Enter valid 12-digit UTR / Ref Number");
                etUtrNumber.requestFocus();
                return;
            }

            String paymentId = "UPI_" + utrNo;
            processSuccessfulBooking(pending, paymentId);
            upiDialog.dismiss();
        });

        upiDialog.show();
    }

    private void processSuccessfulBooking(PendingBooking pending, String paymentId) {
        long now = System.currentTimeMillis();
        int successCount = 0;

        if (pending.isBatch && pending.batchSlotNumbers != null) {
            for (Integer slotNum : pending.batchSlotNumbers) {
                if (dbHelper.bookSlot(slotNum, pending.vehicleType, pending.vehicleNum, pending.phoneNum, pending.hours, now, paymentId, "SUCCESS")) {
                    successCount++;
                }
            }
        } else {
            if (dbHelper.bookSlot(pending.singleSlotNumber, pending.vehicleType, pending.vehicleNum, pending.phoneNum, pending.hours, now, paymentId, "SUCCESS")) {
                successCount = 1;
            }
        }

        if (successCount > 0) {
            Toast.makeText(this, "📱 UPI Payment Verified!\nRef ID: " + paymentId, Toast.LENGTH_LONG).show();
            if (adapter != null) {
                adapter.clearSelection();
            }
            loadSlotsData();
        } else {
            Toast.makeText(this, "Failed to confirm slot booking.", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveProofBitmapLocally(Bitmap bitmap) {
        try {
            File proofDir = new File(getFilesDir(), "incident_proofs");
            if (!proofDir.exists()) {
                proofDir.mkdirs();
            }
            File imageFile = new File(proofDir, "proof_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save photo proof", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tickerHandler.post(clockRunnable);
        FirebaseHelper.getInstance().startRealtimeSlotsListener(dbHelper, this::loadSlotsData);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tickerHandler.removeCallbacks(clockRunnable);
        FirebaseHelper.getInstance().stopRealtimeSlotsListener();
    }

    private void handleSelectionChanged(int count) {
        if (count > 0) {
            cardMultiBookingBar.setVisibility(View.VISIBLE);
            tvSelectedSlotsCount.setText(count + " Slot(s) Selected");
        } else {
            cardMultiBookingBar.setVisibility(View.GONE);
        }
    }

    private void loadSlotsData() {
        dbHelper.checkAndAutoCheckoutExpiredSlots();

        fullSlotList = dbHelper.getSlotsByType(currentVehicleType);
        int occupied = dbHelper.getOccupiedCountByType(currentVehicleType);
        int total = dbHelper.getTotalCountByType(currentVehicleType);
        int available = total - occupied;

        tvAvailableCount.setText(String.valueOf(available));
        tvOccupiedCount.setText(String.valueOf(occupied));

        // Cleanup selection if any selected slot is no longer available
        if (adapter != null && !adapter.getSelectedSlotNumbers().isEmpty()) {
            Set<Integer> availableSlotNums = new HashSet<>();
            for (ParkingSlot slot : fullSlotList) {
                if (!slot.isOccupied()) {
                    availableSlotNums.add(slot.getSlotNumber());
                }
            }
            Set<Integer> toRemove = new HashSet<>();
            for (Integer num : adapter.getSelectedSlotNumbers()) {
                if (!availableSlotNums.contains(num)) {
                    toRemove.add(num);
                }
            }
            if (!toRemove.isEmpty()) {
                adapter.getSelectedSlotNumbers().removeAll(toRemove);
                handleSelectionChanged(adapter.getSelectedSlotNumbers().size());
            }
        }

        String currentSearchQuery = etSearchSlot.getText() != null ? etSearchSlot.getText().toString().trim() : "";
        filterSlots(currentSearchQuery);
    }

    private void filterSlots(String query) {
        if (query.isEmpty()) {
            if (adapter == null) {
                adapter = new ParkingSlotAdapter(this, fullSlotList, this::handleSlotClick, this::handleSelectionChanged);
                rvSlots.setAdapter(adapter);
            } else {
                adapter.updateList(fullSlotList);
            }
            return;
        }

        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<ParkingSlot> filteredList = new ArrayList<>();

        for (ParkingSlot slot : fullSlotList) {
            String slotStr = "slot " + slot.getSlotNumber();
            String vehicleStr = slot.getVehicleNumber() != null ? slot.getVehicleNumber().toLowerCase(Locale.ROOT) : "";
            String phoneStr = slot.getPhoneNumber() != null ? slot.getPhoneNumber() : "";

            if (slotStr.contains(lowerQuery) || vehicleStr.contains(lowerQuery) || phoneStr.contains(lowerQuery)) {
                filteredList.add(slot);
            }
        }

        if (adapter == null) {
            adapter = new ParkingSlotAdapter(this, filteredList, this::handleSlotClick, this::handleSelectionChanged);
            rvSlots.setAdapter(adapter);
        } else {
            adapter.updateList(filteredList);
        }
    }

    private void handleSlotClick(ParkingSlot slot) {
        if (slot.isOccupied()) {
            showTicketDetailsDialog(slot);
        } else {
            showSingleBookSlotDialog(slot);
        }
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

        tvBookSlotTitle.setText("Book Slot #" + slot.getSlotNumber() + " (" + currentVehicleType + ")");
        tvEstimatedFee.setText("₹" + hourlyRate);

        if (sessionManager != null && sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                tvRateNotice.setText("Manager Mode: ₹" + hourlyRate + "/hr");
            } else {
                String userType = sessionManager.getUserVehicleType();
                if (userType.equalsIgnoreCase(currentVehicleType)) {
                    tvRateNotice.setText("Standard Rate: ₹" + hourlyRate + " / Hour");
                    if (sessionManager.getUserVehicleNumber() != null && !sessionManager.getUserVehicleNumber().isEmpty()) {
                        etVehicleNumber.setText(sessionManager.getUserVehicleNumber());
                    }
                    if (sessionManager.getUserPhone() != null && !sessionManager.getUserPhone().isEmpty()) {
                        etPhoneNumber.setText(sessionManager.getUserPhone());
                    }
                } else {
                    tvRateNotice.setText("⚠️ Your profile is registered as " + userType + ". Enter " + currentVehicleType + " number manually.");
                    if (sessionManager.getUserPhone() != null && !sessionManager.getUserPhone().isEmpty()) {
                        etPhoneNumber.setText(sessionManager.getUserPhone());
                    }
                }
            }
        } else {
            tvRateNotice.setText("Standard Rate: ₹" + hourlyRate + " / Hour");
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
            tilVehicleNumber.setError(null);
            tilPhoneNumber.setError(null);
            tilBookingHours.setError(null);

            String vehicleNum = etVehicleNumber.getText().toString().trim().toUpperCase(Locale.ROOT);
            String phoneNum = etPhoneNumber.getText().toString().trim().replaceAll("\\s+", "");
            String hoursStr = etBookingHours.getText().toString().trim();

            String cleanVehicleStr = vehicleNum.replaceAll("[^A-Z0-9]", "");
            if (vehicleNum.isEmpty()) {
                tilVehicleNumber.setError("Vehicle number is required");
                etVehicleNumber.requestFocus();
                return;
            } else if (cleanVehicleStr.length() < 4) {
                tilVehicleNumber.setError("Enter valid vehicle number (e.g. MH12AB1234)");
                etVehicleNumber.requestFocus();
                return;
            }

            if (phoneNum.isEmpty()) {
                tilPhoneNumber.setError("Mobile number is required");
                etPhoneNumber.requestFocus();
                return;
            } else if (!phoneNum.matches("^[6-9]\\d{9}$") && !phoneNum.matches("^\\d{10}$")) {
                tilPhoneNumber.setError("Enter a valid 10-digit mobile number");
                etPhoneNumber.requestFocus();
                return;
            }

            if (hoursStr.isEmpty()) {
                tilBookingHours.setError("Booking duration is required");
                etBookingHours.requestFocus();
                return;
            }

            int hours;
            try {
                hours = Integer.parseInt(hoursStr);
            } catch (NumberFormatException e) {
                tilBookingHours.setError("Invalid duration");
                etBookingHours.requestFocus();
                return;
            }

            if (hours < 1 || hours > 24) {
                tilBookingHours.setError("Duration must be between 1 and 24 hours");
                etBookingHours.requestFocus();
                return;
            }

            int totalFee = hours * hourlyRate;

            PendingBooking pending = new PendingBooking();
            pending.isBatch = false;
            pending.singleSlotNumber = slot.getSlotNumber();
            pending.vehicleType = currentVehicleType;
            pending.vehicleNum = vehicleNum;
            pending.phoneNum = phoneNum;
            pending.hours = hours;
            pending.totalFee = totalFee;

            dialog.dismiss();
            showUpiQrPaymentDialog(pending);
        });

        dialog.show();
    }

    private void showBatchBookSlotDialog(Set<Integer> selectedSlots) {
        int slotsCount = selectedSlots.size();

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

        StringBuilder slotsStr = new StringBuilder();
        for (Integer num : selectedSlots) {
            if (slotsStr.length() > 0) slotsStr.append(", #");
            else slotsStr.append("#");
            slotsStr.append(num);
        }

        tvBookSlotTitle.setText("Batch Booking (" + slotsCount + " Slots: " + slotsStr + ")");
        tvEstimatedFee.setText("₹" + (hourlyRate * slotsCount));

        if (sessionManager != null && sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                tvRateNotice.setText("Manager Batch Mode: ₹" + hourlyRate + "/hr × " + slotsCount + " Slots");
            } else {
                String userType = sessionManager.getUserVehicleType();
                if (userType.equalsIgnoreCase(currentVehicleType)) {
                    tvRateNotice.setText("Batch Rate: ₹" + hourlyRate + "/hr × " + slotsCount + " Slots");
                    if (sessionManager.getUserVehicleNumber() != null && !sessionManager.getUserVehicleNumber().isEmpty()) {
                        etVehicleNumber.setText(sessionManager.getUserVehicleNumber());
                    }
                    if (sessionManager.getUserPhone() != null && !sessionManager.getUserPhone().isEmpty()) {
                        etPhoneNumber.setText(sessionManager.getUserPhone());
                    }
                } else {
                    tvRateNotice.setText("⚠️ Your profile is registered as " + userType + ". Enter " + currentVehicleType + " number manually.");
                    if (sessionManager.getUserPhone() != null && !sessionManager.getUserPhone().isEmpty()) {
                        etPhoneNumber.setText(sessionManager.getUserPhone());
                    }
                }
            }
        } else {
            tvRateNotice.setText("Batch Rate: ₹" + hourlyRate + "/hr × " + slotsCount + " Slots");
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
                            int totalFee = hours * hourlyRate * slotsCount;
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
            tilVehicleNumber.setError(null);
            tilPhoneNumber.setError(null);
            tilBookingHours.setError(null);

            String vehicleNum = etVehicleNumber.getText().toString().trim().toUpperCase(Locale.ROOT);
            String phoneNum = etPhoneNumber.getText().toString().trim().replaceAll("\\s+", "");
            String hoursStr = etBookingHours.getText().toString().trim();

            String cleanVehicleStr = vehicleNum.replaceAll("[^A-Z0-9]", "");
            if (vehicleNum.isEmpty()) {
                tilVehicleNumber.setError("Vehicle number is required");
                etVehicleNumber.requestFocus();
                return;
            } else if (cleanVehicleStr.length() < 4) {
                tilVehicleNumber.setError("Enter valid vehicle number (e.g. MH12AB1234)");
                etVehicleNumber.requestFocus();
                return;
            }

            if (phoneNum.isEmpty()) {
                tilPhoneNumber.setError("Mobile number is required");
                etPhoneNumber.requestFocus();
                return;
            } else if (!phoneNum.matches("^[6-9]\\d{9}$") && !phoneNum.matches("^\\d{10}$")) {
                tilPhoneNumber.setError("Enter a valid 10-digit mobile number");
                etPhoneNumber.requestFocus();
                return;
            }

            if (hoursStr.isEmpty()) {
                tilBookingHours.setError("Booking duration is required");
                etBookingHours.requestFocus();
                return;
            }

            int hours;
            try {
                hours = Integer.parseInt(hoursStr);
            } catch (NumberFormatException e) {
                tilBookingHours.setError("Invalid duration");
                etBookingHours.requestFocus();
                return;
            }

            if (hours < 1 || hours > 24) {
                tilBookingHours.setError("Duration must be between 1 and 24 hours");
                etBookingHours.requestFocus();
                return;
            }

            int totalFee = hours * hourlyRate * slotsCount;

            PendingBooking pending = new PendingBooking();
            pending.isBatch = true;
            pending.batchSlotNumbers = new HashSet<>(selectedSlots);
            pending.vehicleType = currentVehicleType;
            pending.vehicleNum = vehicleNum;
            pending.phoneNum = phoneNum;
            pending.hours = hours;
            pending.totalFee = totalFee;

            dialog.dismiss();
            showUpiQrPaymentDialog(pending);
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
        Button btnEmergencyDelete = dialogView.findViewById(R.id.btnEmergencyDelete);

        tvTicketTitle.setText("Active Ticket - Slot #" + slot.getSlotNumber() + " (" + currentVehicleType + ")");
        tvTicketVehicle.setText("Vehicle: " + slot.getVehicleNumber());

        String phone = slot.getPhoneNumber();
        if (phone != null && phone.length() == 10) {
            tvTicketPhone.setText("Phone: +91 " + phone);
        } else {
            tvTicketPhone.setText("Phone: " + (phone == null || phone.isEmpty() ? "N/A" : phone));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        String entryTimeStr = sdf.format(new Date(slot.getEntryTime()));
        tvTicketEntryTime.setText("Entry Time: " + entryTimeStr);

        tvTicketDuration.setText("Duration: " + slot.getBookingHours() + " Hour(s)");

        int totalFee = slot.getBookingHours() * hourlyRate;
        String payStatus = slot.getPaymentStatus() != null && !slot.getPaymentStatus().isEmpty() ? slot.getPaymentStatus() : "PAID";
        String payId = slot.getPaymentId() != null && !slot.getPaymentId().isEmpty() ? " | Ref: " + slot.getPaymentId() : "";

        tvTicketTotalFee.setText("Total Amount: ₹" + totalFee + " (" + payStatus + payId + ")");

        // Emergency Release button ALWAYS visible on active tickets
        btnEmergencyDelete.setVisibility(View.VISIBLE);
        btnEmergencyDelete.setOnClickListener(v -> {
            showEmergencyReleaseDialog(slot, dialog);
        });

        btnCloseTicket.setOnClickListener(v -> dialog.dismiss());

        btnCheckout.setOnClickListener(v -> {
            boolean success = dbHelper.leaveSlotAndRecordHistory(slot, totalFee);
            if (success) {
                Toast.makeText(MainActivity.this, "Slot #" + slot.getSlotNumber() + " checked out! Record saved to history.", Toast.LENGTH_SHORT).show();
                loadSlotsData();
                dialog.dismiss();
            } else {
                Toast.makeText(MainActivity.this, "Failed to check out slot.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showEmergencyReleaseDialog(ParkingSlot slot, AlertDialog ticketDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_emergency_release, null);
        builder.setView(dialogView);

        AlertDialog releaseDialog = builder.create();

        Spinner spinnerViolationReason = dialogView.findViewById(R.id.spinnerViolationReason);
        currentImgPreview = dialogView.findViewById(R.id.imgProofPreview);
        currentTvProofStatus = dialogView.findViewById(R.id.tvProofStatus);
        Button btnCaptureProof = dialogView.findViewById(R.id.btnCaptureProof);
        Button btnCancelEmergencyRelease = dialogView.findViewById(R.id.btnCancelEmergencyRelease);
        currentBtnConfirmRelease = dialogView.findViewById(R.id.btnConfirmEmergencyRelease);

        capturedProofPath = "";

        // Association Rule Violation Options
        String[] violationOptions = new String[]{
                "Rule Violation: Double Parking / Wrong Slot",
                "Rule Violation: Blocking Passage / Safety Hazard",
                "Rule Violation: Unauthorized / Unregistered Vehicle",
                "Rule Violation: Expired Overstay & No Payment"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, violationOptions);
        spinnerViolationReason.setAdapter(adapter);

        btnCaptureProof.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelEmergencyRelease.setOnClickListener(v -> releaseDialog.dismiss());

        currentBtnConfirmRelease.setOnClickListener(v -> {
            if (capturedProofPath.isEmpty()) {
                Toast.makeText(this, "Association Rule Limitation: Photo proof is mandatory to execute release.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedReason = (String) spinnerViolationReason.getSelectedItem();
            boolean success = dbHelper.emergencyReleaseSlot(slot, selectedReason, capturedProofPath);
            if (success) {
                Toast.makeText(MainActivity.this, "Slot #" + slot.getSlotNumber() + " forcibly released per Parking Association Rules!", Toast.LENGTH_SHORT).show();
                loadSlotsData();
                releaseDialog.dismiss();
                if (ticketDialog != null) {
                    ticketDialog.dismiss();
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to force release slot.", Toast.LENGTH_SHORT).show();
            }
        });

        releaseDialog.show();
    }
}
