package com.example.smartparkingmanagamentsystem;

public class Enterprise {
    private String id;
    private String name;
    private String category; // "Shopping Mall", "Restaurant & Hotel", "Cinema & Entertainment", "Commercial Office"
    private String address;
    private int bikeRate;
    private int carRate;
    private String adminEmail;
    private String password;

    public Enterprise(String id, String name, String category, String address, int bikeRate, int carRate, String adminEmail) {
        this(id, name, category, address, bikeRate, carRate, adminEmail, "admin123");
    }

    public Enterprise(String id, String name, String category, String address, int bikeRate, int carRate, String adminEmail, String password) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.address = address;
        this.bikeRate = bikeRate;
        this.carRate = carRate;
        this.adminEmail = adminEmail;
        this.password = password != null && !password.isEmpty() ? password : "admin123";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getBikeRate() {
        return bikeRate;
    }

    public void setBikeRate(int bikeRate) {
        this.bikeRate = bikeRate;
    }

    public int getCarRate() {
        return carRate;
    }

    public void setCarRate(int carRate) {
        this.carRate = carRate;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
