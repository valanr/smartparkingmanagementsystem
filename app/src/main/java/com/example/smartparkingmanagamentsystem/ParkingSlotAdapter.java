package com.example.smartparkingmanagamentsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.SlotViewHolder> {

    public interface OnSlotClickListener {
        void onSlotClick(ParkingSlot slot);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
    }

    private final Context context;
    private List<ParkingSlot> slotList;
    private final OnSlotClickListener clickListener;
    private final OnSelectionChangeListener selectionChangeListener;

    private final Set<Integer> selectedSlotNumbers = new HashSet<>();

    public ParkingSlotAdapter(Context context, List<ParkingSlot> slotList, OnSlotClickListener clickListener, OnSelectionChangeListener selectionChangeListener) {
        this.context = context;
        this.slotList = slotList;
        this.clickListener = clickListener;
        this.selectionChangeListener = selectionChangeListener;
    }

    public void updateList(List<ParkingSlot> newList) {
        this.slotList = newList;
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedSlotNumbers() {
        return selectedSlotNumbers;
    }

    public void clearSelection() {
        selectedSlotNumbers.clear();
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(0);
        }
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        ParkingSlot slot = slotList.get(position);
        boolean isSelected = selectedSlotNumbers.contains(slot.getSlotNumber());

        String displayName = slot.getSlotCode() != null && !slot.getSlotCode().isEmpty() ? slot.getSlotCode() : "Slot " + slot.getSlotNumber();
        holder.tvSlotNumber.setText(displayName);

        if (slot.isOccupied()) {
            holder.tvSlotStatus.setText("OCCUPIED");
            holder.tvSlotStatus.setTextColor(ContextCompat.getColor(context, R.color.slot_occupied_text));
            holder.containerSlot.setBackgroundColor(ContextCompat.getColor(context, R.color.slot_occupied_bg));
            holder.cardSlot.setStrokeColor(ContextCompat.getColor(context, R.color.slot_occupied_stroke));

            String vehicleStr = slot.getVehicleNumber();
            if (vehicleStr == null || vehicleStr.trim().isEmpty()) {
                vehicleStr = "Booked";
            }
            holder.tvVehicleInfo.setText(vehicleStr);
            holder.tvVehicleInfo.setTextColor(ContextCompat.getColor(context, R.color.slot_occupied_text));

            long remainingMillis = slot.getRemainingMillis();
            if (remainingMillis > 0) {
                long totalSeconds = remainingMillis / 1000;
                long hrs = totalSeconds / 3600;
                long mins = (totalSeconds % 3600) / 60;
                long secs = totalSeconds % 60;

                String timeStr;
                if (hrs > 0) {
                    timeStr = String.format(Locale.ROOT, "⏱️ %02dh %02dm left", hrs, mins);
                } else {
                    timeStr = String.format(Locale.ROOT, "⏱️ %02dm %02ds left", mins, secs);
                }
                holder.tvCountdown.setText(timeStr);
                holder.tvCountdown.setTextColor(ContextCompat.getColor(context, R.color.slot_occupied_text));
                holder.tvCountdown.setVisibility(View.VISIBLE);
            } else {
                holder.tvCountdown.setText("⚠️ Auto Releasing...");
                holder.tvCountdown.setTextColor(ContextCompat.getColor(context, R.color.slot_occupied_text));
                holder.tvCountdown.setVisibility(View.VISIBLE);
            }

        } else if (isSelected) {
            // Highlight Selected Available Slot
            holder.tvSlotStatus.setText("✓ SELECTED");
            holder.tvSlotStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_indigo_dark));
            holder.containerSlot.setBackgroundColor(ContextCompat.getColor(context, R.color.bike_bg));
            holder.cardSlot.setStrokeColor(ContextCompat.getColor(context, R.color.accent_indigo));

            holder.tvVehicleInfo.setText("Tap to Book");
            holder.tvVehicleInfo.setTextColor(ContextCompat.getColor(context, R.color.accent_indigo_dark));

            holder.tvCountdown.setVisibility(View.GONE);
        } else {
            // Available Slot
            holder.tvSlotStatus.setText("AVAILABLE");
            holder.tvSlotStatus.setTextColor(ContextCompat.getColor(context, R.color.slot_available_text));
            holder.containerSlot.setBackgroundColor(ContextCompat.getColor(context, R.color.slot_available_bg));
            holder.cardSlot.setStrokeColor(ContextCompat.getColor(context, R.color.slot_available_stroke));

            holder.tvVehicleInfo.setText("Tap to Book / Select");
            holder.tvVehicleInfo.setTextColor(ContextCompat.getColor(context, R.color.slate_900));

            holder.tvCountdown.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSlotClick(slot);
            }

            if (!slot.isOccupied()) {
                if (selectedSlotNumbers.contains(slot.getSlotNumber())) {
                    selectedSlotNumbers.remove(slot.getSlotNumber());
                } else {
                    selectedSlotNumbers.add(slot.getSlotNumber());
                }
                notifyItemChanged(position);

                if (selectionChangeListener != null) {
                    selectionChangeListener.onSelectionChanged(selectedSlotNumbers.size());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return slotList != null ? slotList.size() : 0;
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardSlot;
        LinearLayout containerSlot;
        TextView tvSlotNumber;
        TextView tvSlotStatus;
        TextView tvVehicleInfo;
        TextView tvCountdown;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSlot = itemView.findViewById(R.id.cardSlot);
            containerSlot = itemView.findViewById(R.id.containerSlot);
            tvSlotNumber = itemView.findViewById(R.id.tvSlotNumber);
            tvSlotStatus = itemView.findViewById(R.id.tvSlotStatus);
            tvVehicleInfo = itemView.findViewById(R.id.tvVehicleInfo);
            tvCountdown = itemView.findViewById(R.id.tvCountdown);
        }
    }
}
