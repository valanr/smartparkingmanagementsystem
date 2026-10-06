package com.example.smartparkingmanagamentsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_record, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());

        String phone = user.getPhone();
        if (phone != null && phone.length() == 10) {
            holder.tvUserPhone.setText("📞 +91 " + phone);
        } else {
            holder.tvUserPhone.setText("📞 " + (phone == null || phone.isEmpty() ? "N/A" : phone));
        }

        holder.tvUserVehicleNo.setText("🚘 " + user.getVehicleNumber());

        boolean isBike = DatabaseHelper.TYPE_BIKE.equalsIgnoreCase(user.getVehicleType());
        if (isBike) {
            holder.tvUserVehicleBadge.setText("🏍️ BIKE");
            holder.tvUserVehicleBadge.setTextColor(ContextCompat.getColor(context, R.color.bike_accent));
        } else {
            holder.tvUserVehicleBadge.setText("🚗 CAR");
            holder.tvUserVehicleBadge.setTextColor(ContextCompat.getColor(context, R.color.car_accent));
        }
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvUserEmail;
        TextView tvUserPhone;
        TextView tvUserVehicleNo;
        TextView tvUserVehicleBadge;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserVehicleNo = itemView.findViewById(R.id.tvUserVehicleNo);
            tvUserVehicleBadge = itemView.findViewById(R.id.tvUserVehicleBadge);
        }
    }
}
