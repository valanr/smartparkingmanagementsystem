package com.example.smartparkingmanagamentsystem;

public class User {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String vehicleNumber;
    private String vehicleType;
    private long createdAt;

    public User(int id, String name, String email, String phone, String vehicleNumber, String vehicleType, long createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
