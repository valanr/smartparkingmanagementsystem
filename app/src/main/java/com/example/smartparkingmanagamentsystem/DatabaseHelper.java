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
    private static final int DATABASE_VERSION = 8;

    // Enterprises Table
    private static final String TABLE_ENTERPRISES = "enterprises";
    private static final String COLUMN_ENT_ID = "id";
    private static final String COLUMN_ENT_NAME = "name";
    private static final String COLUMN_ENT_CATEGORY = "category";
    private static final String COLUMN_ENT_ADDRESS = "address";
    private static final String COLUMN_ENT_BIKE_RATE = "bike_rate";
    private static final String COLUMN_ENT_CAR_RATE = "car_rate";
    private static final String COLUMN_ENT_ADMIN_EMAIL = "admin_email";

    // Slots Table
    private static final String TABLE_SLOTS = "parking_slots";
    private static final String COLUMN_SLOT_NUMBER = "slot_number";
    private static final String COLUMN_VEHICLE_TYPE = "vehicle_type";
    private static final String COLUMN_IS_OCCUPIED = "is_occupied";
    private static final String COLUMN_VEHICLE_NUMBER = "vehicle_number";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_BOOKING_HOURS = "booking_hours";
    private static final String COLUMN_ENTRY_TIME = "entry_time";
    private static final String COLUMN_FLOOR_ZONE = "floor_zone";
    private static final String COLUMN_SLOT_CODE = "slot_code";
    private static final String COLUMN_ENTERPRISE_ID = "enterprise_id";
    private static final String COLUMN_ENTERPRISE_NAME = "enterprise_name";

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
        String createEnterprisesTableQuery = "CREATE TABLE " + TABLE_ENTERPRISES + " ("
                + COLUMN_ENT_ID + " TEXT PRIMARY KEY, "
                + COLUMN_ENT_NAME + " TEXT, "
                + COLUMN_ENT_CATEGORY + " TEXT, "
                + COLUMN_ENT_ADDRESS + " TEXT, "
                + COLUMN_ENT_BIKE_RATE + " INTEGER, "
                + COLUMN_ENT_CAR_RATE + " INTEGER, "
                + COLUMN_ENT_ADMIN_EMAIL + " TEXT)";
        db.execSQL(createEnterprisesTableQuery);

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
                + COLUMN_FLOOR_ZONE + " TEXT DEFAULT 'Floor 1', "
                + COLUMN_SLOT_CODE + " TEXT, "
                + COLUMN_ENTERPRISE_ID + " TEXT DEFAULT 'ent_nexus_mall', "
                + COLUMN_ENTERPRISE_NAME + " TEXT DEFAULT 'Nexus Shopping Mall', "
                + "PRIMARY KEY (" + COLUMN_SLOT_NUMBER + ", " + COLUMN_VEHICLE_TYPE + ", " + COLUMN_ENTERPRISE_ID + "))";
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
                + COLUMN_PAYMENT_STATUS + " TEXT, "
                + COLUMN_ENTERPRISE_ID + " TEXT, "
                + COLUMN_ENTERPRISE_NAME + " TEXT)";
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

        populateDefaultEnterprisesAndSlots(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTERPRISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void populateDefaultEnterprisesAndSlots(SQLiteDatabase db) {
        // 1. Nexus Shopping Mall
        insertEnterprise(db, "ent_nexus_mall", "Nexus Shopping Mall", "Shopping Mall", "MG Road, Central Zone", 10, 20, "nexus@parksmart.com");
        populateSlotsForEnterprise(db, "ent_nexus_mall", "Nexus Shopping Mall");

        // 2. Grand Hyatt Restaurant
        insertEnterprise(db, "ent_grand_hyatt", "Grand Hyatt Fine Dining", "Restaurant & Hotel", "Park Avenue, South Bay", 15, 30, "hyatt@parksmart.com");
        populateSlotsForEnterprise(db, "ent_grand_hyatt", "Grand Hyatt Fine Dining");

        // 3. PVR IMAX Multiplex
        insertEnterprise(db, "ent_pvr_imax", "PVR IMAX Multiplex", "Cinema & Entertainment", "City Center Plaza", 10, 25, "pvr@parksmart.com");
        populateSlotsForEnterprise(db, "ent_pvr_imax", "PVR IMAX Multiplex");

        // 4. TechPark Towers
        insertEnterprise(db, "ent_techpark", "TechPark Commercial Towers", "Commercial Office", "IT Highway Corridor", 10, 20, "admin@parksmart.com");
        populateSlotsForEnterprise(db, "ent_techpark", "TechPark Commercial Towers");
    }

    private void insertEnterprise(SQLiteDatabase db, String id, String name, String category, String address, int bikeRate, int carRate, String adminEmail) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ENT_ID, id);
        cv.put(COLUMN_ENT_NAME, name);
        cv.put(COLUMN_ENT_CATEGORY, category);
        cv.put(COLUMN_ENT_ADDRESS, address);
        cv.put(COLUMN_ENT_BIKE_RATE, bikeRate);
        cv.put(COLUMN_ENT_CAR_RATE, carRate);
        cv.put(COLUMN_ENT_ADMIN_EMAIL, adminEmail);
        db.insertWithOnConflict(TABLE_ENTERPRISES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void populateSlotsForEnterprise(SQLiteDatabase db, String entId, String entName) {
        for (int i = 1; i <= 6; i++) {
            // Bikes
            ContentValues cvB = new ContentValues();
            cvB.put(COLUMN_SLOT_NUMBER, i);
            cvB.put(COLUMN_VEHICLE_TYPE, TYPE_BIKE);
            cvB.put(COLUMN_IS_OCCUPIED, 0);
            cvB.put(COLUMN_VEHICLE_NUMBER, "");
            cvB.put(COLUMN_PHONE_NUMBER, "");
            cvB.put(COLUMN_BOOKING_HOURS, 0);
            cvB.put(COLUMN_ENTRY_TIME, 0);
            cvB.put(COLUMN_PAYMENT_ID, "");
            cvB.put(COLUMN_PAYMENT_STATUS, "UNPAID");
            cvB.put(COLUMN_FLOOR_ZONE, i <= 3 ? "Basement B1" : "Floor 1");
            cvB.put(COLUMN_SLOT_CODE, "B-" + (100 + i));
            cvB.put(COLUMN_ENTERPRISE_ID, entId);
            cvB.put(COLUMN_ENTERPRISE_NAME, entName);
            db.insert(TABLE_SLOTS, null, cvB);

            // Cars
            ContentValues cvC = new ContentValues();
            cvC.put(COLUMN_SLOT_NUMBER, i);
            cvC.put(COLUMN_VEHICLE_TYPE, TYPE_CAR);
            cvC.put(COLUMN_IS_OCCUPIED, 0);
            cvC.put(COLUMN_VEHICLE_NUMBER, "");
            cvC.put(COLUMN_PHONE_NUMBER, "");
            cvC.put(COLUMN_BOOKING_HOURS, 0);
            cvC.put(COLUMN_ENTRY_TIME, 0);
            cvC.put(COLUMN_PAYMENT_ID, "");
            cvC.put(COLUMN_PAYMENT_STATUS, "UNPAID");
            cvC.put(COLUMN_FLOOR_ZONE, i <= 3 ? "Basement B1" : "Floor 1");
            cvC.put(COLUMN_SLOT_CODE, "C-" + (100 + i));
            cvC.put(COLUMN_ENTERPRISE_ID, entId);
            cvC.put(COLUMN_ENTERPRISE_NAME, entName);
            db.insert(TABLE_SLOTS, null, cvC);
        }
    }

    public List<Enterprise> getAllEnterprises() {
        List<Enterprise> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ENTERPRISES + " ORDER BY " + COLUMN_ENT_NAME + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_NAME));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_CATEGORY));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ADDRESS));
                int bikeRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENT_BIKE_RATE));
                int carRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENT_CAR_RATE));
                String adminEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ADMIN_EMAIL));

                list.add(new Enterprise(id, name, category, address, bikeRate, carRate, adminEmail));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Enterprise> searchEnterprises(String query, String categoryFilter) {
        List<Enterprise> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_ENTERPRISES + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (categoryFilter != null && !categoryFilter.isEmpty() && !"All".equalsIgnoreCase(categoryFilter)) {
            sql.append(" AND ").append(COLUMN_ENT_CATEGORY).append(" LIKE ?");
            args.add("%" + categoryFilter + "%");
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (").append(COLUMN_ENT_NAME).append(" LIKE ? OR ").append(COLUMN_ENT_ADDRESS).append(" LIKE ?)");
            args.add("%" + query.trim() + "%");
            args.add("%" + query.trim() + "%");
        }

        sql.append(" ORDER BY ").append(COLUMN_ENT_NAME).append(" ASC");

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_NAME));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_CATEGORY));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ADDRESS));
                int bikeRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENT_BIKE_RATE));
                int carRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENT_CAR_RATE));
                String adminEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ADMIN_EMAIL));

                list.add(new Enterprise(id, name, category, address, bikeRate, carRate, adminEmail));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Enterprise getEnterpriseById(String enterpriseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ENTERPRISES + " WHERE " + COLUMN_ENT_ID + " = ?", new String[]{enterpriseId});
        Enterprise ent = null;
        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_NAME));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_CATEGORY));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ADDRESS));
            int bikeRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENT_BIKE_RATE));
            int carRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENT_CAR_RATE));
            String adminEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENT_ADMIN_EMAIL));

            ent = new Enterprise(id, name, category, address, bikeRate, carRate, adminEmail);
        }
        cursor.close();
        return ent != null ? ent : new Enterprise("ent_nexus_mall", "Nexus Shopping Mall", "Shopping Mall", "MG Road, Central Zone", 10, 20, "nexus@parksmart.com");
    }

    public int getAvailableSlotCountForEnterprise(String enterpriseId, String vehicleType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SLOTS + " WHERE " + COLUMN_ENTERPRISE_ID + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_IS_OCCUPIED + " = 0", new String[]{enterpriseId, vehicleType});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<ParkingSlot> getSlotsByEnterprise(String enterpriseId, String vehicleType) {
        checkAndAutoCheckoutExpiredSlots();

        List<ParkingSlot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SLOTS + " WHERE " + COLUMN_ENTERPRISE_ID + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ? ORDER BY " + COLUMN_SLOT_NUMBER + " ASC", new String[]{enterpriseId, vehicleType});
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
                String floor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLOOR_ZONE));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SLOT_CODE));
                String entId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_ID));
                String entName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_NAME));

                list.add(new ParkingSlot(slotNum, vType, isOccupied, vehicleNum, phoneNum, hours, entryTime, payId, payStatus, floor, code, entId, entName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean addCustomSlot(ParkingSlot slot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SLOT_NUMBER, slot.getSlotNumber());
        cv.put(COLUMN_VEHICLE_TYPE, slot.getVehicleType());
        cv.put(COLUMN_IS_OCCUPIED, slot.isOccupied() ? 1 : 0);
        cv.put(COLUMN_VEHICLE_NUMBER, slot.getVehicleNumber());
        cv.put(COLUMN_PHONE_NUMBER, slot.getPhoneNumber());
        cv.put(COLUMN_BOOKING_HOURS, slot.getBookingHours());
        cv.put(COLUMN_ENTRY_TIME, slot.getEntryTime());
        cv.put(COLUMN_PAYMENT_ID, slot.getPaymentId());
        cv.put(COLUMN_PAYMENT_STATUS, slot.getPaymentStatus());
        cv.put(COLUMN_FLOOR_ZONE, slot.getFloorZone());
        cv.put(COLUMN_SLOT_CODE, slot.getSlotCode());
        cv.put(COLUMN_ENTERPRISE_ID, slot.getEnterpriseId());
        cv.put(COLUMN_ENTERPRISE_NAME, slot.getEnterpriseName());

        long rowId = db.insertWithOnConflict(TABLE_SLOTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId != -1) {
            FirebaseHelper.getInstance().syncSlotToCloud(slot);
            return true;
        }
        return false;
    }

    public boolean deleteCustomSlot(int slotNumber, String vehicleType, String enterpriseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_SLOTS, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_ENTERPRISE_ID + " = ?", new String[]{String.valueOf(slotNumber), vehicleType, enterpriseId});
        return rows > 0;
    }

    public List<String> getAvailableFloors(String vehicleType, String enterpriseId) {
        List<String> floors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_FLOOR_ZONE + " FROM " + TABLE_SLOTS + " WHERE " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_ENTERPRISE_ID + " = ? ORDER BY " + COLUMN_FLOOR_ZONE + " ASC", new String[]{vehicleType, enterpriseId});
        if (cursor.moveToFirst()) {
            do {
                String floor = cursor.getString(0);
                if (floor != null && !floor.isEmpty()) {
                    floors.add(floor);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (floors.isEmpty()) {
            floors.add("Basement B1");
            floors.add("Floor 1");
        }
        return floors;
    }

    public List<ParkingSlot> getSlotsByFloor(String vehicleType, String floorZone, String enterpriseId) {
        checkAndAutoCheckoutExpiredSlots();

        List<ParkingSlot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SLOTS + " WHERE " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_FLOOR_ZONE + " = ? AND " + COLUMN_ENTERPRISE_ID + " = ? ORDER BY " + COLUMN_SLOT_NUMBER + " ASC", new String[]{vehicleType, floorZone, enterpriseId});
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
                String floor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLOOR_ZONE));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SLOT_CODE));
                String entId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_ID));
                String entName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_NAME));

                list.add(new ParkingSlot(slotNum, vType, isOccupied, vehicleNum, phoneNum, hours, entryTime, payId, payStatus, floor, code, entId, entName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
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
        return getSlotsByEnterprise("ent_nexus_mall", vehicleType);
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
                String floor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLOOR_ZONE));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SLOT_CODE));
                String entId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_ID));
                String entName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_NAME));

                list.add(new ParkingSlot(slotNum, vType, true, vehicleNum, phoneNum, hours, entryTime, payId, payStatus, floor, code, entId, entName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean bookSlot(int slotNumber, String vehicleType, String vehicleNumber, String phoneNumber, int hours, long entryTime) {
        return bookSlot(slotNumber, vehicleType, vehicleNumber, phoneNumber, hours, entryTime, "pay_mock_" + System.currentTimeMillis(), "SUCCESS", "ent_nexus_mall");
    }

    public boolean bookSlot(int slotNumber, String vehicleType, String vehicleNumber, String phoneNumber, int hours, long entryTime, String paymentId, String paymentStatus) {
        return bookSlot(slotNumber, vehicleType, vehicleNumber, phoneNumber, hours, entryTime, paymentId, paymentStatus, "ent_nexus_mall");
    }

    public boolean bookSlot(int slotNumber, String vehicleType, String vehicleNumber, String phoneNumber, int hours, long entryTime, String paymentId, String paymentStatus, String enterpriseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_OCCUPIED, 1);
        cv.put(COLUMN_VEHICLE_NUMBER, vehicleNumber);
        cv.put(COLUMN_PHONE_NUMBER, phoneNumber);
        cv.put(COLUMN_BOOKING_HOURS, hours);
        cv.put(COLUMN_ENTRY_TIME, entryTime);
        cv.put(COLUMN_PAYMENT_ID, paymentId);
        cv.put(COLUMN_PAYMENT_STATUS, paymentStatus);

        int rows = db.update(TABLE_SLOTS, cv, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_ENTERPRISE_ID + " = ?", new String[]{String.valueOf(slotNumber), vehicleType, enterpriseId});
        if (rows > 0) {
            Enterprise ent = getEnterpriseById(enterpriseId);
            ParkingSlot bookedSlot = new ParkingSlot(slotNumber, vehicleType, true, vehicleNumber, phoneNumber, hours, entryTime, paymentId, paymentStatus, "Floor 1", "Slot " + slotNumber, enterpriseId, ent.getName());
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
            historyCv.put(COLUMN_ENTERPRISE_ID, slot.getEnterpriseId());
            historyCv.put(COLUMN_ENTERPRISE_NAME, slot.getEnterpriseName());

            long historyId = db.insert(TABLE_HISTORY, null, historyCv);

            ContentValues slotCv = new ContentValues();
            slotCv.put(COLUMN_IS_OCCUPIED, 0);
            slotCv.put(COLUMN_VEHICLE_NUMBER, "");
            slotCv.put(COLUMN_PHONE_NUMBER, "");
            slotCv.put(COLUMN_BOOKING_HOURS, 0);
            slotCv.put(COLUMN_ENTRY_TIME, 0);
            slotCv.put(COLUMN_PAYMENT_ID, "");
            slotCv.put(COLUMN_PAYMENT_STATUS, "UNPAID");

            int rows = db.update(TABLE_SLOTS, slotCv, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_ENTERPRISE_ID + " = ?", new String[]{String.valueOf(slot.getSlotNumber()), slot.getVehicleType(), slot.getEnterpriseId()});

            if (historyId != -1 && rows > 0) {
                db.setTransactionSuccessful();

                // Sync slot release & history to Firebase Cloud
                ParkingSlot freedSlot = new ParkingSlot(slot.getSlotNumber(), slot.getVehicleType(), false, "", "", 0, 0, "", "UNPAID", slot.getFloorZone(), slot.getSlotCode(), slot.getEnterpriseId(), slot.getEnterpriseName());
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
            historyCv.put(COLUMN_ENTERPRISE_ID, slot.getEnterpriseId());
            historyCv.put(COLUMN_ENTERPRISE_NAME, slot.getEnterpriseName());

            long historyId = db.insert(TABLE_HISTORY, null, historyCv);

            ContentValues slotCv = new ContentValues();
            slotCv.put(COLUMN_IS_OCCUPIED, 0);
            slotCv.put(COLUMN_VEHICLE_NUMBER, "");
            slotCv.put(COLUMN_PHONE_NUMBER, "");
            slotCv.put(COLUMN_BOOKING_HOURS, 0);
            slotCv.put(COLUMN_ENTRY_TIME, 0);
            slotCv.put(COLUMN_PAYMENT_ID, "");
            slotCv.put(COLUMN_PAYMENT_STATUS, "UNPAID");

            int rows = db.update(TABLE_SLOTS, slotCv, COLUMN_SLOT_NUMBER + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_ENTERPRISE_ID + " = ?", new String[]{String.valueOf(slot.getSlotNumber()), slot.getVehicleType(), slot.getEnterpriseId()});

            if (historyId != -1 && rows > 0) {
                db.setTransactionSuccessful();

                // Sync emergency release & history to Firebase Cloud
                ParkingSlot freedSlot = new ParkingSlot(slot.getSlotNumber(), slot.getVehicleType(), false, "", "", 0, 0, "", "UNPAID", slot.getFloorZone(), slot.getSlotCode(), slot.getEnterpriseId(), slot.getEnterpriseName());
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
                String floor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLOOR_ZONE));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SLOT_CODE));
                String entId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_ID));
                String entName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTERPRISE_NAME));

                allOccupied.add(new ParkingSlot(slotNum, vType, true, vehicleNum, phoneNum, hours, entryTime, payId, payStatus, floor, code, entId, entName));
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

    public int getOccupiedCountByEnterpriseAndType(String enterpriseId, String vehicleType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SLOTS + " WHERE " + COLUMN_ENTERPRISE_ID + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ? AND " + COLUMN_IS_OCCUPIED + " = 1", new String[]{enterpriseId, vehicleType});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalCountByEnterpriseAndType(String enterpriseId, String vehicleType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SLOTS + " WHERE " + COLUMN_ENTERPRISE_ID + " = ? AND " + COLUMN_VEHICLE_TYPE + " = ?", new String[]{enterpriseId, vehicleType});
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
