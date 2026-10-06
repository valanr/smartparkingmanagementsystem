package com.example.smartparkingmanagamentsystem;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<ParkingHistoryRecord> historyList;

    public ParkingHistoryAdapter(Context context, List<ParkingHistoryRecord> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_record, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ParkingHistoryRecord record = historyList.get(position);

        boolean isBike = DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(record.getVehicleType());
        if (isBike) {
            holder.tvHistoryBadge.setText("🏍️ BIKE - Slot #" + record.getSlotNumber());
            holder.tvHistoryBadge.setTextColor(ContextCompat.getColor(context, R.color.bike_accent));
        } else {
            holder.tvHistoryBadge.setText("🚗 CAR - Slot #" + record.getSlotNumber());
            holder.tvHistoryBadge.setTextColor(ContextCompat.getColor(context, R.color.car_accent));
        }

        boolean isEmergency = record.getFeePaid() == 0 && record.getVehicleNumber() != null && record.getVehicleNumber().contains("EMERGENCY");
        if (isEmergency) {
            holder.tvHistoryFee.setText("🚨 EMERGENCY RELEASE");
            holder.tvHistoryFee.setTextColor(ContextCompat.getColor(context, R.color.slot_occupied_text));
        } else {
            holder.tvHistoryFee.setText("₹" + record.getFeePaid() + " Paid");
            holder.tvHistoryFee.setTextColor(ContextCompat.getColor(context, R.color.slot_available_text));
        }

        holder.tvHistoryVehicle.setText("Vehicle: " + record.getVehicleNumber());

        String phone = record.getPhoneNumber();
        if (phone != null && phone.length() == 10) {
            holder.tvHistoryPhone.setText("Phone: +91 " + phone);
        } else {
            holder.tvHistoryPhone.setText("Phone: " + (phone == null || phone.isEmpty() ? "N/A" : phone));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        String exitTimeStr = sdf.format(new Date(record.getExitTime()));
        holder.tvHistoryExitTime.setText("Check-out: " + exitTimeStr);

        holder.tvHistoryDuration.setText(record.getBookedHours() + " Hour(s)");

        String proofPath = record.getProofImagePath();
        if (proofPath != null && !proofPath.isEmpty()) {
            holder.btnViewProof.setVisibility(View.VISIBLE);
            holder.btnViewProof.setOnClickListener(v -> showProofDialog(record));
        } else {
            holder.btnViewProof.setVisibility(View.GONE);
        }
    }

    private void showProofDialog(ParkingHistoryRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_view_proof, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvProofViolationReason = dialogView.findViewById(R.id.tvProofViolationReason);
        TextView tvProofDetails = dialogView.findViewById(R.id.tvProofDetails);
        ImageView imgFullProof = dialogView.findViewById(R.id.imgFullProof);
        Button btnCloseProofDialog = dialogView.findViewById(R.id.btnCloseProofDialog);

        String reason = record.getViolationReason();
        if (reason == null || reason.isEmpty()) {
            reason = "Association Rule Violation";
        }
        tvProofViolationReason.setText(reason);

        tvProofDetails.setText("Slot #" + record.getSlotNumber() + " (" + record.getVehicleType() + ") | Vehicle: " + record.getVehicleNumber());

        if (record.getProofImagePath() != null && !record.getProofImagePath().isEmpty()) {
            File imgFile = new File(record.getProofImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imgFullProof.setImageBitmap(bitmap);
            }
        }

        btnCloseProofDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryBadge;
        TextView tvHistoryFee;
        TextView tvHistoryVehicle;
        TextView tvHistoryPhone;
        TextView tvHistoryExitTime;
        TextView tvHistoryDuration;
        Button btnViewProof;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryBadge = itemView.findViewById(R.id.tvHistoryBadge);
            tvHistoryFee = itemView.findViewById(R.id.tvHistoryFee);
            tvHistoryVehicle = itemView.findViewById(R.id.tvHistoryVehicle);
            tvHistoryPhone = itemView.findViewById(R.id.tvHistoryPhone);
            tvHistoryExitTime = itemView.findViewById(R.id.tvHistoryExitTime);
            tvHistoryDuration = itemView.findViewById(R.id.tvHistoryDuration);
            btnViewProof = itemView.findViewById(R.id.btnViewProof);
        }
    }
}
