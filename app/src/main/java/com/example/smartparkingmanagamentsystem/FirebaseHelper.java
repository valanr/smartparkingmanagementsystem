package com.example.smartparkingmanagamentsystem;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static final String COLLECTION_SLOTS = "parking_slots";
    private static final String COLLECTION_HISTORY = "parking_history";
    private static final String COLLECTION_USERS = "users";

    private static FirebaseHelper instance;
    private FirebaseFirestore db;
    private ListenerRegistration slotsListener;

    private FirebaseHelper() {
        try {
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FirebaseFirestore: " + e.getMessage());
        }
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public void syncUserToCloud(User user) {
        if (db == null || user == null) return;

        String docId = user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "user_" + System.currentTimeMillis();

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("vehicleNumber", user.getVehicleNumber());
        userData.put("vehicleType", user.getVehicleType());
        userData.put("createdAt", user.getCreatedAt());

        db.collection(COLLECTION_USERS)
                .document(docId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User synced to Cloud: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync user " + docId + " to Cloud: " + e.getMessage()));
    }

    public void syncSlotToCloud(ParkingSlot slot) {
        if (db == null || slot == null) return;

        String docId = slot.getVehicleType() + "_slot_" + slot.getSlotNumber();

        Map<String, Object> slotData = new HashMap<>();
        slotData.put("slotNumber", slot.getSlotNumber());
        slotData.put("vehicleType", slot.getVehicleType());
        slotData.put("isOccupied", slot.isOccupied());
        slotData.put("vehicleNumber", slot.getVehicleNumber());
        slotData.put("phoneNumber", slot.getPhoneNumber());
        slotData.put("bookingHours", slot.getBookingHours());
        slotData.put("entryTime", slot.getEntryTime());
        slotData.put("paymentId", slot.getPaymentId() != null ? slot.getPaymentId() : "");
        slotData.put("paymentStatus", slot.getPaymentStatus() != null ? slot.getPaymentStatus() : "UNPAID");
        slotData.put("lastUpdated", System.currentTimeMillis());

        db.collection(COLLECTION_SLOTS)
                .document(docId)
                .set(slotData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Slot synced to Cloud: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync slot " + docId + " to Cloud: " + e.getMessage()));
    }

    public void syncHistoryRecordToCloud(ParkingHistoryRecord record) {
        if (db == null || record == null) return;

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("id", record.getId());
        historyData.put("slotNumber", record.getSlotNumber());
        historyData.put("vehicleType", record.getVehicleType());
        historyData.put("vehicleNumber", record.getVehicleNumber());
        historyData.put("phoneNumber", record.getPhoneNumber());
        historyData.put("bookedHours", record.getBookedHours());
        historyData.put("entryTime", record.getEntryTime());
        historyData.put("exitTime", record.getExitTime());
        historyData.put("feePaid", record.getFeePaid());
        historyData.put("violationReason", record.getViolationReason() != null ? record.getViolationReason() : "");
        historyData.put("proofImagePath", record.getProofImagePath() != null ? record.getProofImagePath() : "");
        historyData.put("paymentId", record.getPaymentId() != null ? record.getPaymentId() : "");
        historyData.put("paymentStatus", record.getPaymentStatus() != null ? record.getPaymentStatus() : "PAID");

        db.collection(COLLECTION_HISTORY)
                .add(historyData)
                .addOnSuccessListener(docRef -> Log.d(TAG, "History record synced to Cloud ID: " + docRef.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync history to Cloud: " + e.getMessage()));
    }

    public void startRealtimeSlotsListener(DatabaseHelper localDbHelper, Runnable onCloudUpdate) {
        if (db == null || slotsListener != null) return;

        slotsListener = db.collection(COLLECTION_SLOTS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Cloud slot listener failed: ", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        boolean updated = false;
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            if (doc.exists()) {
                                Long slotNumLong = doc.getLong("slotNumber");
                                String vType = doc.getString("vehicleType");
                                Boolean isOccupied = doc.getBoolean("isOccupied");
                                String vehicleNum = doc.getString("vehicleNumber");
                                String phoneNum = doc.getString("phoneNumber");
                                Long hoursLong = doc.getLong("bookingHours");
                                Long entryTimeLong = doc.getLong("entryTime");

                                if (slotNumLong != null && vType != null && isOccupied != null) {
                                    int slotNum = slotNumLong.intValue();
                                    int hours = hoursLong != null ? hoursLong.intValue() : 0;
                                    long entryTime = entryTimeLong != null ? entryTimeLong : 0;
                                    String vNum = vehicleNum != null ? vehicleNum : "";
                                    String pNum = phoneNum != null ? phoneNum : "";

                                    localDbHelper.updateLocalSlotOnly(slotNum, vType, isOccupied, vNum, pNum, hours, entryTime);
                                    updated = true;
                                }
                            }
                        }

                        if (updated && onCloudUpdate != null) {
                            onCloudUpdate.run();
                        }
                    }
                });
    }

    public void stopRealtimeSlotsListener() {
        if (slotsListener != null) {
            slotsListener.remove();
            slotsListener = null;
        }
    }
}
