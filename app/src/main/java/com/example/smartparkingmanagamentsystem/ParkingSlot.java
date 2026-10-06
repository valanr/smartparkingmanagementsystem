package com.example.smartparkingmanagamentsystem;

public class ParkingSlot {
    private int slotNumber;
    private String vehicleType; // "BIKE" or "CAR"
    private boolean isOccupied;
    private String vehicleNumber;
    private String phoneNumber;
    private int bookingHours;
    private long entryTime;
    private String paymentId;
    private String paymentStatus;

    public ParkingSlot(int slotNumber, String vehicleType, boolean isOccupied, String vehicleNumber, String phoneNumber, int bookingHours, long entryTime) {
        this(slotNumber, vehicleType, isOccupied, vehicleNumber, phoneNumber, bookingHours, entryTime, "", "UNPAID");
    }

    public ParkingSlot(int slotNumber, String vehicleType, boolean isOccupied, String vehicleNumber, String phoneNumber, int bookingHours, long entryTime, String paymentId, String paymentStatus) {
        this.slotNumber = slotNumber;
        this.vehicleType = vehicleType;
        this.isOccupied = isOccupied;
        this.vehicleNumber = vehicleNumber;
        this.phoneNumber = phoneNumber;
        this.bookingHours = bookingHours;
        this.entryTime = entryTime;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getBookingHours() {
        return bookingHours;
    }

    public void setBookingHours(int bookingHours) {
        this.bookingHours = bookingHours;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public long getExpiryTime() {
        if (!isOccupied || entryTime <= 0 || bookingHours <= 0) {
            return 0;
        }
        return entryTime + (bookingHours * 3600000L);
    }

    public boolean isExpired() {
        if (!isOccupied) return false;
        long expiry = getExpiryTime();
        return expiry > 0 && System.currentTimeMillis() >= expiry;
    }

    public long getRemainingMillis() {
        if (!isOccupied) return 0;
        long remaining = getExpiryTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
