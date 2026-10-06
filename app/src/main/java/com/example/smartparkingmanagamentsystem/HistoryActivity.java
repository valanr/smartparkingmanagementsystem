package com.example.smartparkingmanagamentsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private RecyclerView rvHistory;
    private LinearLayout containerRevenueSummary;
    private LinearLayout containerAdminTabs;
    private TextView tvHistoryHeaderTitle;
    private TextView tvHistorySectionTitle;
    private TextView tvTotalRevenue;
    private TextView tvTotalCompleted;
    private TextView tvBikeRevenue;
    private TextView tvCarRevenue;
    private TextView tvTotalUsers;
    private TextView tvEmptyHistory;

    private Button btnTabTransactions;
    private Button btnTabUsers;

    private boolean showingUsersTab = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        rvHistory = findViewById(R.id.rvHistory);
        containerRevenueSummary = findViewById(R.id.containerRevenueSummary);
        containerAdminTabs = findViewById(R.id.containerAdminTabs);

        tvHistoryHeaderTitle = findViewById(R.id.tvHistoryHeaderTitle);
        tvHistorySectionTitle = findViewById(R.id.tvHistorySectionTitle);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalCompleted = findViewById(R.id.tvTotalCompleted);
        tvBikeRevenue = findViewById(R.id.tvBikeRevenue);
        tvCarRevenue = findViewById(R.id.tvCarRevenue);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        btnTabTransactions = findViewById(R.id.btnTabTransactions);
        btnTabUsers = findViewById(R.id.btnTabUsers);

        ImageButton btnBackFromHistory = findViewById(R.id.btnBackFromHistory);

        btnBackFromHistory.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        btnTabTransactions.setOnClickListener(v -> {
            showingUsersTab = false;
            updateTabStyles();
            loadHistoryData();
        });

        btnTabUsers.setOnClickListener(v -> {
            showingUsersTab = true;
            updateTabStyles();
            loadUsersData();
        });

        loadHistoryData();
    }

    private void updateTabStyles() {
        if (showingUsersTab) {
            btnTabUsers.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.slate_900));
            btnTabUsers.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabTransactions.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.slate_50));
            btnTabTransactions.setTextColor(ContextCompat.getColor(this, R.color.slate_900));
        } else {
            btnTabTransactions.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.slate_900));
            btnTabTransactions.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabUsers.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.slate_50));
            btnTabUsers.setTextColor(ContextCompat.getColor(this, R.color.slate_900));
        }
    }

    private void loadHistoryData() {
        boolean isAdmin = sessionManager.isAdmin();

        List<ParkingHistoryRecord> historyRecords;

        if (isAdmin) {
            containerRevenueSummary.setVisibility(View.VISIBLE);
            containerAdminTabs.setVisibility(View.VISIBLE);
            tvHistoryHeaderTitle.setText("Revenue & Analytics (Admin)");
            tvHistorySectionTitle.setText("All System Transactions");

            int totalRev = dbHelper.getTotalRevenue();
            int bikeRev = dbHelper.getRevenueByVehicleType(DatabaseHelper.TYPE_BIKE);
            int carRev = dbHelper.getRevenueByVehicleType(DatabaseHelper.TYPE_CAR);
            int completedCount = dbHelper.getTotalCompletedCount();
            int userCount = dbHelper.getTotalUserCount();

            tvTotalRevenue.setText("₹" + totalRev);
            tvBikeRevenue.setText("🏍️ Bike: ₹" + bikeRev);
            tvCarRevenue.setText("🚗 Car: ₹" + carRev);
            tvTotalCompleted.setText(String.valueOf(completedCount));
            tvTotalUsers.setText(String.valueOf(userCount));

            historyRecords = dbHelper.getAllHistory();
        } else {
            containerRevenueSummary.setVisibility(View.GONE);
            containerAdminTabs.setVisibility(View.GONE);
            tvHistoryHeaderTitle.setText("My Parking Receipts");
            tvHistorySectionTitle.setText("My Completed Bookings");

            String userPhone = sessionManager.getUserPhone();
            historyRecords = dbHelper.getHistoryByPhone(userPhone);
        }

        if (historyRecords.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            if (!isAdmin) {
                tvEmptyHistory.setText("No completed parking receipts found for your account.");
            } else {
                tvEmptyHistory.setText("No completed parking records yet.");
            }
            rvHistory.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            ParkingHistoryAdapter adapter = new ParkingHistoryAdapter(this, historyRecords);
            rvHistory.setAdapter(adapter);
        }
    }

    private void loadUsersData() {
        tvHistorySectionTitle.setText("Registered Users List");

        List<User> userList = dbHelper.getAllRegisteredUsers();

        if (userList.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            tvEmptyHistory.setText("No registered users found.");
            rvHistory.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            UserAdapter adapter = new UserAdapter(this, userList);
            rvHistory.setAdapter(adapter);
        }
    }
}
