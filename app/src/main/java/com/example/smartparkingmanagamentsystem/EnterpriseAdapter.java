package com.example.smartparkingmanagamentsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EnterpriseAdapter extends RecyclerView.Adapter<EnterpriseAdapter.EnterpriseViewHolder> {

    public interface OnEnterpriseClickListener {
        void onEnterpriseClick(Enterprise enterprise);
    }

    private final Context context;
    private List<Enterprise> enterpriseList;
    private final DatabaseHelper dbHelper;
    private final OnEnterpriseClickListener listener;

    public EnterpriseAdapter(Context context, List<Enterprise> enterpriseList, DatabaseHelper dbHelper, OnEnterpriseClickListener listener) {
        this.context = context;
        this.enterpriseList = enterpriseList;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    public void updateList(List<Enterprise> newList) {
        this.enterpriseList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EnterpriseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_enterprise, parent, false);
        return new EnterpriseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnterpriseViewHolder holder, int position) {
        Enterprise ent = enterpriseList.get(position);

        holder.tvEntName.setText(ent.getName());
        holder.tvEntAddress.setText("📍 " + ent.getAddress());
        holder.tvEntCategoryBadge.setText(ent.getCategory());

        String cat = ent.getCategory() != null ? ent.getCategory().toLowerCase() : "";
        if (cat.contains("mall")) {
            holder.tvEntIcon.setText("🛒");
        } else if (cat.contains("restaurant") || cat.contains("hotel")) {
            holder.tvEntIcon.setText("🍽️");
        } else if (cat.contains("cinema") || cat.contains("multiplex")) {
            holder.tvEntIcon.setText("🎬");
        } else {
            holder.tvEntIcon.setText("💼");
        }

        int availBike = dbHelper.getAvailableSlotCountForEnterprise(ent.getId(), DatabaseHelper.TYPE_BIKE);
        int availCar = dbHelper.getAvailableSlotCountForEnterprise(ent.getId(), DatabaseHelper.TYPE_CAR);

        holder.tvEntSlotAvailability.setText("🏍️ " + availBike + " Bike | 🚗 " + availCar + " Car Slots Open");
        holder.tvEntRates.setText("Rates: Bike ₹" + ent.getBikeRate() + "/hr • Car ₹" + ent.getCarRate() + "/hr");

        View.OnClickListener clickAction = v -> {
            if (listener != null) {
                listener.onEnterpriseClick(ent);
            }
        };

        holder.itemView.setOnClickListener(clickAction);
        holder.btnSelectEnterprise.setOnClickListener(clickAction);
    }

    @Override
    public int getItemCount() {
        return enterpriseList != null ? enterpriseList.size() : 0;
    }

    static class EnterpriseViewHolder extends RecyclerView.ViewHolder {
        TextView tvEntIcon;
        TextView tvEntName;
        TextView tvEntAddress;
        TextView tvEntCategoryBadge;
        TextView tvEntSlotAvailability;
        TextView tvEntRates;
        Button btnSelectEnterprise;

        public EnterpriseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntIcon = itemView.findViewById(R.id.tvEntIcon);
            tvEntName = itemView.findViewById(R.id.tvEntName);
            tvEntAddress = itemView.findViewById(R.id.tvEntAddress);
            tvEntCategoryBadge = itemView.findViewById(R.id.tvEntCategoryBadge);
            tvEntSlotAvailability = itemView.findViewById(R.id.tvEntSlotAvailability);
            tvEntRates = itemView.findViewById(R.id.tvEntRates);
            btnSelectEnterprise = itemView.findViewById(R.id.btnSelectEnterprise);
        }
    }
}
