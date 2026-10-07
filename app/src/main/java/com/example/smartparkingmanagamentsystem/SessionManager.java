package com.example.smartparkingmanagamentsystem;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ParkSmartUserPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_NAME = "userName";
    private static final String KEY_EMAIL = "userEmail";
    private static final String KEY_PHONE = "userPhone";
    private static final String KEY_VEHICLE_NO = "userVehicleNo";
    private static final String KEY_VEHICLE_TYPE = "userVehicleType";
    private static final String KEY_ROLE = "userRole";
    private static final String KEY_ADMIN_ENT_ID = "adminEnterpriseId";

    // Backup keys for switching roles
    private static final String KEY_SAVED_NAME = "savedName";
    private static final String KEY_SAVED_EMAIL = "savedEmail";
    private static final String KEY_SAVED_PHONE = "savedPhone";
    private static final String KEY_SAVED_VEHICLE_NO = "savedVehicleNo";
    private static final String KEY_SAVED_VEHICLE_TYPE = "savedVehicleType";

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String email, String phone, String vehicleNumber, String vehicleType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_VEHICLE_NO, vehicleNumber);
        editor.putString(KEY_VEHICLE_TYPE, vehicleType);
        editor.putString(KEY_ROLE, ROLE_USER);

        // Also save as backup
        editor.putString(KEY_SAVED_NAME, name);
        editor.putString(KEY_SAVED_EMAIL, email);
        editor.putString(KEY_SAVED_PHONE, phone);
        editor.putString(KEY_SAVED_VEHICLE_NO, vehicleNumber);
        editor.putString(KEY_SAVED_VEHICLE_TYPE, vehicleType);

        editor.apply();
    }

    public void createAdminSession(String email, String enterpriseId) {
        // Backup current user profile if present
        if (ROLE_USER.equalsIgnoreCase(pref.getString(KEY_ROLE, ROLE_USER))) {
            editor.putString(KEY_SAVED_NAME, pref.getString(KEY_NAME, "User"));
            editor.putString(KEY_SAVED_EMAIL, pref.getString(KEY_EMAIL, ""));
            editor.putString(KEY_SAVED_PHONE, pref.getString(KEY_PHONE, ""));
            editor.putString(KEY_SAVED_VEHICLE_NO, pref.getString(KEY_VEHICLE_NO, ""));
            editor.putString(KEY_SAVED_VEHICLE_TYPE, pref.getString(KEY_VEHICLE_TYPE, DatabaseHelper.TYPE_CAR));
        }

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_NAME, "Parking Manager");
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, "");
        editor.putString(KEY_VEHICLE_NO, "");
        editor.putString(KEY_VEHICLE_TYPE, DatabaseHelper.TYPE_CAR);
        editor.putString(KEY_ROLE, ROLE_ADMIN);
        editor.putString(KEY_ADMIN_ENT_ID, enterpriseId != null ? enterpriseId : "ent_nexus_mall");
        editor.apply();
    }

    public boolean switchToUserMode() {
        String savedName = pref.getString(KEY_SAVED_NAME, "");
        if (!savedName.isEmpty()) {
            editor.putString(KEY_NAME, savedName);
            editor.putString(KEY_EMAIL, pref.getString(KEY_SAVED_EMAIL, ""));
            editor.putString(KEY_PHONE, pref.getString(KEY_SAVED_PHONE, ""));
            editor.putString(KEY_VEHICLE_NO, pref.getString(KEY_SAVED_VEHICLE_NO, ""));
            editor.putString(KEY_VEHICLE_TYPE, pref.getString(KEY_SAVED_VEHICLE_TYPE, DatabaseHelper.TYPE_CAR));
            editor.putString(KEY_ROLE, ROLE_USER);
            editor.apply();
            return true;
        }
        return false;
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(pref.getString(KEY_ROLE, ROLE_USER));
    }

    public String getAdminEnterpriseId() {
        return pref.getString(KEY_ADMIN_ENT_ID, "ent_nexus_mall");
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, "Guest User");
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getUserPhone() {
        return pref.getString(KEY_PHONE, "");
    }

    public String getUserVehicleNumber() {
        return pref.getString(KEY_VEHICLE_NO, "");
    }

    public String getUserVehicleType() {
        return pref.getString(KEY_VEHICLE_TYPE, DatabaseHelper.TYPE_CAR);
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, ROLE_USER);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
