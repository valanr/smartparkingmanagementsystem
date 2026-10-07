# 🅿️ ParkSmart - Smart Parking Management System

**ParkSmart** is an advanced, real-time Android Smart Parking Management application built with Java, Material 3 Design, SQLite, and Firebase Cloud Firestore.

It provides a seamless, multi-role solution for drivers and parking facility managers, featuring real-time multi-device cloud synchronization, Google Pay / UPI QR payment verification, active ticket countdown timers, and rule-violation emergency slot releases with mandatory photo proof.

---

## ✨ Key Features

### 🚗 Driver / User Features
- **1-Tap Profile Auto-Fill**: Register once with Name, Email, Mobile (+91 format), and Vehicle Number. Slot booking pre-fills details automatically.
- **Vehicle Category Separation**: Independent parking slots for **Two-Wheelers (Bikes @ ₹10/hr)** and **Four-Wheelers (Cars @ ₹20/hr)** with category mismatch warnings.
- **Interactive Multi-Slot Selection**: Select multiple available green slots (e.g., Slot #1, Slot #2) and batch-book them in one transaction.
- **Google Pay & UPI Payment Gateway**: Generates a dynamic Google Pay QR Code with UPI ID and verifies 12-digit UTR payment reference numbers.
- **Live Countdown Timers**: Active slots display real-time remaining duration badges (`⏱️ 01h 45m left`).
- **Home Screen Active Ticket Shortcut**: Quick shortcut banner on the home screen to jump straight to your active parked vehicle ticket.
- **Digital Receipts**: Personal transaction history logs with fee receipts and payment IDs.

### 🔐 Manager / Admin Features
- **Manager Portal & Login**: Dedicated Admin entrance (`admin@parksmart.com` / `admin123`).
- **Revenue & System Analytics**: Real-time revenue dashboard displaying Total Revenue (`₹`), Bike vs. Car earnings breakdown, total completed bookings, and registered driver count.
- **Registered Users Directory**: View all registered drivers, email addresses, phone numbers, and default vehicles.
- **Emergency Slot Release (Parking Association Rules)**:
  - Restricted strictly to Parking Association Rule Violations (Double Parking, Blocking Passage, Unauthorized Vehicle, Expired Overstay).
  - Requires **Mandatory Incident Camera Photo Proof** via camera capture before releasing a slot.
  - Incident photo proofs can be inspected directly in the transaction history logs.

---

## ☁️ Tech Stack & Architecture

- **Language**: Java 11 / Android SDK (Min SDK 24, Target SDK 37)
- **UI Framework**: Android Material 3 Design, RecyclerView, ConstraintLayout, CardView
- **Database**:
  - **Local**: SQLite (`SQLiteOpenHelper`) for offline-first responsiveness.
  - **Cloud**: Firebase Cloud Firestore (`com.google.firebase:firebase-firestore`) for real-time multi-device cloud sync.
- **Payment Verification**: Google Pay / UPI QR Code Generator + UTR Ref Verification (plus Razorpay Checkout SDK integration).
- **Camera & Proof Capture**: `MediaStore.ACTION_IMAGE_CAPTURE` camera intent for incident photo proof logs.

---

## 🔑 Demo Credentials

| Role | Email | Password / Access |
| :--- | :--- | :--- |
| **Driver / User** | Any valid email & 10-digit mobile | Register directly on launch screen |
| **Parking Manager / Admin** | `admin@parksmart.com` | `admin123` |

---

## 🚀 Getting Started

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/valanr/smartparkingmanagementsystem.git
   ```
2. **Open in Android Studio**: Open the cloned directory in Android Studio (2026.1+).
3. **Run App**: Build and run on an Emulator or connected Android phone.
