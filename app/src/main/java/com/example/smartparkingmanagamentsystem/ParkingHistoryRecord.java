package com.example.smartparkingmanagamentsystem;

public class ParkingHistoryRecord {
    private int id;
    private int slotNumber;
    private String vehicleType;
    private String vehicleNumber;
    private String phoneNumber;
    private int bookedHours;
    private long entryTime;
    private long exitTime;
    private int feePaid;
    private String violationReason;
    private String proofImagePath;
    private String paymentId;
    private String paymentStatus;

    public ParkingHistoryRecord(int id, int slotNumber, String vehicleType, String vehicleNumber, String phoneNumber, int bookedHours, long entryTime, long exitTime, int feePaid, String violationReason, String proofImagePath) {
        this(id, slotNumber, vehicleType, vehicleNumber, phoneNumber, bookedHours, entryTime, exitTime, feePaid, violationReason, proofImagePath, "", "PAID");
    }

    public ParkingHistoryRecord(int id, int slotNumber, String vehicleType, String vehicleNumber, String phoneNumber, int bookedHours, long entryTime, long exitTime, int feePaid, String violationReason, String proofImagePath, String paymentId, String paymentStatus) {
        this.id = id;
        this.slotNumber = slotNumber;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.phoneNumber = phoneNumber;
        this.bookedHours = bookedHours;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.feePaid = feePaid;
        this.violationReason = violationReason;
        this.proofImagePath = proofImagePath;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
    }

    public int getId() {
        return id;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getBookedHours() {
        return bookedHours;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public int getFeePaid() {
        return feePaid;
    }

    public String getViolationReason() {
        return violationReason;
    }

    public String getProofImagePath() {
        return proofImagePath;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
