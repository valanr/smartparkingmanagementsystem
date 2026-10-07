package com.example.smartparkingmanagamentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartParking.db";
    private static final int DATABASE_VERSION = 6;

    // Slots Table
    private static final String TABLE_SLOTS = "parking_slots";
    private static final String COLUMN_SLOT_NUMBER = "slot_number";
    private static final String COLUMN_VEHICLE_TYPE = "vehicle_type";
    private static final String COLUMN_IS_OCCUPIED = "is_occupied";
    private static final String COLUMN_VEHICLE_NUMBER = "vehicle_number";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_BOOKING_HOURS = "booking_hours";
    private static final String COLUMN_ENTRY_TIME = "entry_time";

    // History Table
    private static final String TABLE_HISTORY = "parking_history";
    private static final String COLUMN_HISTORY_ID = "id";
    private static final String COLUMN_EXIT_TIME = "exit_time";
    private static final String COLUMN_FEE_PAID = "fee_paid";
    private static final String COLUMN_VIOLATION_REASON = "violation_reason";
    private static final String COLUMN_PROOF_IMAGE_PATH = "proof_image_path";

    // Payment Columns
    private static final String COLUMN_PAYMENT_ID = "payment_id";
    private static final String COLUMN_PAYMENT_STATUS = "payment_status";

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PHONE = "phone";
    private static final String COLUMN_USER_VEHICLE_NO = "vehicle_number";
    private static final String COLUMN_USER_VEHICLE_TYPE = "vehicle_type";
    private static final String COLUMN_USER_CREATED_AT = "created_at";

    public static final String TYPE_BIKE = "BIKE";
    public static final String TYPE_CAR = "CAR";

    private static final int DEFAULT_SLOTS_PER_TYPE = 10;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSlotsTableQuery = "CREATE TABLE " + TABLE_SLOTS + " ("
                + COLUMN_SLOT_NUMBER + " INTEGER, "
                + COLUMN_VEHICLE_TYPE + " TEXT, "
                + COLUMN_IS_OCCUPIED + " INTEGER DEFAULT 0, "
                + COLUMN_VEHICLE_NUMBER + " TEXT, "
                + COLUMN_PHONE_NUMBER + " TEXT, "
                + COLUMN_BOOKING_HOURS + " INTEGER DEFAULT 0, "
                + COLUMN_ENTRY_TIME + " INTEGER DEFAULT 0, "
                + COLUMN_PAYMENT_ID + " TEXT, "
                + COLUMN_PAYMENT_STATUS + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_SLOT_NUMBER + ", " + COLUMN_VEHICLE_TYPE + "))";
        db.execSQL(createSlotsTableQuery);

        String createHistoryTableQuery = "CREATE TABLE " + TABLE_HISTORY + " ("
                + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SLOT_NUMBER + " INTEGER, "
                + COLUMN_VEHICLE_TYPE + " TEXT, "
                + COLUMN_VEHICLE_NUMBER + " TEXT, "
                + COLUMN_PHONE_NUMBER + " TEXT, "
                + COLUMN_BOOKING_HOURS + " INTEGER, "
                + COLUMN_ENTRY_TIME + " INTEGER, "
                + COLUMN_EXIT_TIME + " INTEGER, "
                + COLUMN_FEE_PAID + " INTEGER, "
                + COLUMN_VIOLATION_REASON + " TEXT, "
                + COLUMN_PROOF_IMAGE_PATH + " TEXT, "
                + COLUMN_PAYMENT_ID + " TEXT, "
                + COLUMN_PAYMENT_STATUS + " TEXT)";
        db.execSQL(createHistoryTableQuery);

        String createUsersTableQuery = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_USER_EMAIL + " TEXT, "
                + COLUMN_USER_PHONE + " TEXT UNIQUE, "
                + COLUMN_USER_VEHICLE_NO + " TEXT, "
                + COLUMN_USER_VEHICLE_TYPE + " TEXT, "
                + COLUMN_USER_CREATED_AT + " INTEGER)";
        db.execSQL(createUsersTableQuery);

        populateDefaultSlotsForType(db, TYPE_BIKE);
        populateDefaultSlotsForType(db, TYPE_CAR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void populateDefaultSlotsForType(SQLiteDatabase db, String type) {
        for (int i = 1; i <= DEFAULT_SLOTS_PER_TYPE; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_SLOT_NUMBER, i);
            cv.put(COLUMN_VEHICLE_TYPE, type);
            cv.put(COLUMN_IS_OCCUPIED, 0);
            cv.put(COLUMN_VEHICLE_NUMBER, "");
            cv.put(COLUMN_PHONE_NUMBER, "");
            cv.put(COLUMN_BOOKING_HOURS, 0);
            cv.put(COLUMN_ENTRY_TIME, 0);
            cv.put(COLUMN_PAYMENT_ID, "");
            cv.put(COLUMN_PAYMENT_STATUS, "UNPAID");
            db.insert(TABLE_SLOTS, null, cv);
        }
    }

    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_NAME, user.getName());
        cv.put(COLUMN_USER_EMAIL, user.getEmail());
        cv.put(COLUMN_USER_PHONE, user.getPhone());
        cv.put(COLUMN_USER_VEHICLE_NO, user.getVehicleNumber());
        cv.put(COLUMN_USER_VEHICLE_TYPE, user.getVehicleType());
        cv.put(COLUMN_USER_CREATED_AT, user.getCreatedAt());

        long rowId = db.insertWithOnConflict(TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId != -1) {
            FirebaseHelper.getInstance().syncUserToCloud(user);
            return true;
        }
        return false;
    }

    public List<User> getAllRegisteredUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " ORDER BY " + COLUMN_USER_CREATED_AT + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE));
                String vehicleNo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_VEHICLE_NO));
                String vehicleType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_VEHICLE_TYPE));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_CREATED_AT));

                list.add(new User(id, name, email, phone, vehicleNo, vehicleType, createdAt));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getTotalUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<ParkingSlot> getSlotsByType(String vehicleType) {
        checkAndAutoCheckoutExpiredSlots();

        List<ParkingSlot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        ensureDefaultSlots(db);

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SLOTS + " WHERE " + COLUMN_VEHICLE_TYPE + " = ? ORDER BY " + COLUMN_SLOT_NUMBER + " ASC", new String[]{vehicleType});
        if (cursor.moveToFirst()) {
            do {
                int slotNum = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLOT_NUMBER));
                String vType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE));
                boolean isOccupied = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_OCCUPIED)) == 1;
                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NUMBER));
                String phoneNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
                int hours = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_HOURS));
                long entryTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TIME));
                String payId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_ID));
                String payStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_STATUS));

                list.add(new ParkingSlot(slotNum, vType, isOccupied, vehicleNum, phoneNum, hours, entryTime, payId, payStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<ParkingSlot> getUsersActiveSlots(String phone) {
        checkAndAutoCheckoutExpiredSlots();

        List<ParkingSlot> list = new ArrayList<>();
        if (phone == null || phone.isEmpty()) return list;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SLOTS + " WHERE " + COLUMN_PHONE_NUMBER + " = ? AND " + COLUMN_IS_OCCUPIED + " = 1", new String[]{phone});
        if (cursor.moveToFirst()) {
            do {
                int slotNum = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLOT_NUMBER));
                String vType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE));
                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NUMBER));
                String phoneNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
                int hours = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_HOURS));
                long entryTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TIME));
                String payId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_ID));
                String payStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_STATUS));

                list.add(new ParkingSlot(slotNum, vType, true, vehicleNum, phoneNum, hours, entryTime, payId, payStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private void ensureDefaultSlots(SQLiteDatabase db) {
        Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SLOTS, null);
        int count = 0;
        if (countCursor.moveToFirst()) {
            count = countCursor.getInt(0);
        }
        countCursor.close();

        if (count == 0) {
            populateDefaultSlotsForType(db, TYPE_BIKE);
            populateDefaultSlotsForType(db, TYPE_CAR);
        }
    }

    public boolean bookSlot(int slotNumber, String vehicleType, String vehicleNumber, String phoneNumber, int hours, long entryTime) {
        return bookSlot(slotNumber, vehicleType, vehicleNumber, phoneNumber, hours, entryTime, "pay_mock_" + System.currentTimeMillis(), "SUCCESS");
    }

    public boolean bookSlot(int slotNumber, String vehicleType, String vehicleNumber, String phoneNumber, int hours, long entryTime, String paymentId, String paymentStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_OCCUPIED, 1);
        cv.put(COLUMN_VEHICLE_NUMBER, vehicleNumber);
        cv.put(COLUMN_PHONE_NUMBER, phoneNumber);
        cv.put(COLUMN_BOOKING_HOURS, hours);
        cv.put(COLUMN_ENTRY_TIME, entryTime);
        cv.put(COLUMN_PAYMENT_ID, paymentId);
        cv.put(COLUMN_PAYMENT_STATUS, paymentStatus);

        int rows = db.update(TABLE_SLOTS, cv, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ?", new String[]{String.valueOf(slotNumber), vehicleType});
        if (rows > 0) {
            ParkingSlot bookedSlot = new ParkingSlot(slotNumber, vehicleType, true, vehicleNumber, phoneNumber, hours, entryTime, paymentId, paymentStatus);
            FirebaseHelper.getInstance().syncSlotToCloud(bookedSlot);
            return true;
        }
        return false;
    }

    public boolean updateLocalSlotOnly(int slotNumber, String vehicleType, boolean isOccupied, String vehicleNumber, String phoneNumber, int hours, long entryTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_OCCUPIED, isOccupied ? 1 : 0);
        cv.put(COLUMN_VEHICLE_NUMBER, isOccupied ? vehicleNumber : "");
        cv.put(COLUMN_PHONE_NUMBER, isOccupied ? phoneNumber : "");
        cv.put(COLUMN_BOOKING_HOURS, isOccupied ? hours : 0);
        cv.put(COLUMN_ENTRY_TIME, isOccupied ? entryTime : 0);

        int rows = db.update(TABLE_SLOTS, cv, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ?", new String[]{String.valueOf(slotNumber), vehicleType});
        return rows > 0;
    }

    public boolean leaveSlotAndRecordHistory(ParkingSlot slot, int feePaid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            long now = System.currentTimeMillis();

            ContentValues historyCv = new ContentValues();
            historyCv.put(COLUMN_SLOT_NUMBER, slot.getSlotNumber());
            historyCv.put(COLUMN_VEHICLE_TYPE, slot.getVehicleType());
            historyCv.put(COLUMN_VEHICLE_NUMBER, slot.getVehicleNumber());
            historyCv.put(COLUMN_PHONE_NUMBER, slot.getPhoneNumber());
            historyCv.put(COLUMN_BOOKING_HOURS, slot.getBookingHours());
            historyCv.put(COLUMN_ENTRY_TIME, slot.getEntryTime());
            historyCv.put(COLUMN_EXIT_TIME, now);
            historyCv.put(COLUMN_FEE_PAID, feePaid);
            historyCv.put(COLUMN_VIOLATION_REASON, "Normal Check-out");
            historyCv.put(COLUMN_PROOF_IMAGE_PATH, "");
            historyCv.put(COLUMN_PAYMENT_ID, slot.getPaymentId() != null ? slot.getPaymentId() : "");
            historyCv.put(COLUMN_PAYMENT_STATUS, "PAID");

            long historyId = db.insert(TABLE_HISTORY, null, historyCv);

            ContentValues slotCv = new ContentValues();
            slotCv.put(COLUMN_IS_OCCUPIED, 0);
            slotCv.put(COLUMN_VEHICLE_NUMBER, "");
            slotCv.put(COLUMN_PHONE_NUMBER, "");
            slotCv.put(COLUMN_BOOKING_HOURS, 0);
            slotCv.put(COLUMN_ENTRY_TIME, 0);
            slotCv.put(COLUMN_PAYMENT_ID, "");
            slotCv.put(COLUMN_PAYMENT_STATUS, "UNPAID");

            int rows = db.update(TABLE_SLOTS, slotCv, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ?", new String[]{String.valueOf(slot.getSlotNumber()), slot.getVehicleType()});

            if (historyId != -1 && rows > 0) {
                db.setTransactionSuccessful();

                // Sync slot release & history to Firebase Cloud
                ParkingSlot freedSlot = new ParkingSlot(slot.getSlotNumber(), slot.getVehicleType(), false, "", "", 0, 0);
                FirebaseHelper.getInstance().syncSlotToCloud(freedSlot);

                ParkingHistoryRecord historyRecord = new ParkingHistoryRecord(
                        (int) historyId, slot.getSlotNumber(), slot.getVehicleType(),
                        slot.getVehicleNumber(), slot.getPhoneNumber(), slot.getBookingHours(),
                        slot.getEntryTime(), now, feePaid, "Normal Check-out", "",
                        slot.getPaymentId(), "PAID"
                );
                FirebaseHelper.getInstance().syncHistoryRecordToCloud(historyRecord);

                return true;
            }
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public boolean emergencyReleaseSlot(ParkingSlot slot, String violationReason, String proofImagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            long now = System.currentTimeMillis();

            ContentValues historyCv = new ContentValues();
            historyCv.put(COLUMN_SLOT_NUMBER, slot.getSlotNumber());
            historyCv.put(COLUMN_VEHICLE_TYPE, slot.getVehicleType());
            historyCv.put(COLUMN_VEHICLE_NUMBER, slot.getVehicleNumber() + " (EMERGENCY RELEASE)");
            historyCv.put(COLUMN_PHONE_NUMBER, slot.getPhoneNumber());
            historyCv.put(COLUMN_BOOKING_HOURS, slot.getBookingHours());
            historyCv.put(COLUMN_ENTRY_TIME, slot.getEntryTime());
            historyCv.put(COLUMN_EXIT_TIME, now);
            historyCv.put(COLUMN_FEE_PAID, 0);
            historyCv.put(COLUMN_VIOLATION_REASON, violationReason);
            historyCv.put(COLUMN_PROOF_IMAGE_PATH, proofImagePath);
            historyCv.put(COLUMN_PAYMENT_ID, "EMERGENCY_WAIVED");
            historyCv.put(COLUMN_PAYMENT_STATUS, "WAIVED");

            long historyId = db.insert(TABLE_HISTORY, null, historyCv);

            ContentValues slotCv = new ContentValues();
            slotCv.put(COLUMN_IS_OCCUPIED, 0);
            slotCv.put(COLUMN_VEHICLE_NUMBER, "");
            slotCv.put(COLUMN_PHONE_NUMBER, "");
            slotCv.put(COLUMN_BOOKING_HOURS, 0);
            slotCv.put(COLUMN_ENTRY_TIME, 0);
            slotCv.put(COLUMN_PAYMENT_ID, "");
            slotCv.put(COLUMN_PAYMENT_STATUS, "UNPAID");

            int rows = db.update(TABLE_SLOTS, slotCv, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ?", new String[]{String.valueOf(slot.getSlotNumber()), slot.getVehicleType()});

            if (historyId != -1 && rows > 0) {
                db.setTransactionSuccessful();

                // Sync emergency release & history to Firebase Cloud
                ParkingSlot freedSlot = new ParkingSlot(slot.getSlotNumber(), slot.getVehicleType(), false, "", "", 0, 0);
                FirebaseHelper.getInstance().syncSlotToCloud(freedSlot);

                ParkingHistoryRecord historyRecord = new ParkingHistoryRecord(
                        (int) historyId, slot.getSlotNumber(), slot.getVehicleType(),
                        slot.getVehicleNumber() + " (EMERGENCY RELEASE)", slot.getPhoneNumber(),
                        slot.getBookingHours(), slot.getEntryTime(), now, 0, violationReason, proofImagePath,
                        "EMERGENCY_WAIVED", "WAIVED"
                );
                FirebaseHelper.getInstance().syncHistoryRecordToCloud(historyRecord);

                return true;
            }
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public int checkAndAutoCheckoutExpiredSlots() {
        int expiredCount = 0;
        List<ParkingSlot> allOccupied = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SLOTS + " WHERE " + COLUMN_IS_OCCUPIED + " = 1", null);
        if (cursor.moveToFirst()) {
            do {
                int slotNum = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLOT_NUMBER));
                String vType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE));
                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NUMBER));
                String phoneNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
                int hours = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_HOURS));
                long entryTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TIME));
                String payId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_ID));
                String payStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_STATUS));

                allOccupied.add(new ParkingSlot(slotNum, vType, true, vehicleNum, phoneNum, hours, entryTime, payId, payStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (ParkingSlot slot : allOccupied) {
            if (slot.isExpired()) {
                int rate = TYPE_BIKE.equalsIgnoreCase(slot.getVehicleType()) ? 10 : 20;
                int feePaid = slot.getBookingHours() * rate;
                if (leaveSlotAndRecordHistory(slot, feePaid)) {
                    expiredCount++;
                }
            }
        }

        return expiredCount;
    }

    public List<ParkingHistoryRecord> getAllHistory() {
        List<ParkingHistoryRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COLUMN_EXIT_TIME + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_ID));
                int slotNum = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLOT_NUMBER));
                String vType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE));
                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NUMBER));
                String phoneNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
                int hours = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_HOURS));
                long entryTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TIME));
                long exitTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXIT_TIME));
                int feePaid = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEE_PAID));
                String violationReason = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIOLATION_REASON));
                String proofPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROOF_IMAGE_PATH));
                String payId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_ID));
                String payStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_STATUS));

                list.add(new ParkingHistoryRecord(id, slotNum, vType, vehicleNum, phoneNum, hours, entryTime, exitTime, feePaid, violationReason, proofPath, payId, payStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<ParkingHistoryRecord> getHistoryByPhone(String phone) {
        List<ParkingHistoryRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " WHERE " + COLUMN_PHONE_NUMBER + " = ? ORDER BY " + COLUMN_EXIT_TIME + " DESC", new String[]{phone});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_ID));
                int slotNum = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLOT_NUMBER));
                String vType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE));
                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NUMBER));
                String phoneNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
                int hours = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_HOURS));
                long entryTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TIME));
                long exitTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXIT_TIME));
                int feePaid = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEE_PAID));
                String violationReason = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIOLATION_REASON));
                String proofPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROOF_IMAGE_PATH));
                String payId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_ID));
                String payStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_STATUS));

                list.add(new ParkingHistoryRecord(id, slotNum, vType, vehicleNum, phoneNum, hours, entryTime, exitTime, feePaid, violationReason, proofPath, payId, payStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_FEE_PAID + ") FROM " + TABLE_HISTORY, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    public int getRevenueByVehicleType(String vehicleType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_FEE_PAID + ") FROM " + TABLE_HISTORY + " WHERE " + COLUMN_VEHICLE_TYPE + " = ?", new String[]{vehicleType});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    public int getTotalCompletedCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HISTORY, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getOccupiedCountByType(String vehicleType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SLOTS + " WHERE " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_IS_OCCUPIED + " = 1", new String[]{vehicleType});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalCountByType(String vehicleType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SLOTS + " WHERE " + COLUMN_VEHICLE_TYPE + " = ?", new String[]{vehicleType});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
