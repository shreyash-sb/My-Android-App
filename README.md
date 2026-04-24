# 📱 Smart Expense Tracker

A full-featured Android application built using **Java + XML + Firebase** to manage expenses, track income, plan budgets, and achieve savings goals with analytics and reporting.

---

## 🚀 Features

* 🔐 User Authentication (Email/Password via Firebase)
* 💸 Expense Tracking (Add, Edit, Delete, Categorize)
* 💰 Income Management
* 📊 Budget Planning (Monthly)
* 🎯 Savings Goals Tracking
* 📈 Analytics Dashboard (Insights & summaries)
* 📄 PDF Report Generation
* 👤 Profile Management
* 🌙 Theme & Language Settings
* 🔔 Local Notifications (Budget alerts)
* 📱 Drawer-based Navigation UI

---

## 🛠️ Tech Stack

* **Frontend:** Java, XML
* **Backend:** Firebase
* **Database:** Cloud Firestore
* **Authentication:** Firebase Authentication
* **IDE:** Android Studio

---

## 📦 APK Download

👉 [Download APK](https://github.com/shreyash-sb/My-Android-App/releases/download/v1.0/app-debug.apk)

---

## ⚙️ Installation Guide

1. Download the APK file
2. Enable **Install from Unknown Sources**
3. Install and open the app

---

## 🔥 Firebase Services Used

* Firebase Authentication
* Cloud Firestore

### ❌ Not Used

* Firebase Storage
* Google Sign-In
* Phone OTP Authentication
* Realtime Database
* Cloud Functions

---

## 🔐 Authentication Flow

* Email/Password login via Firebase
* Phone login implemented using **Firestore lookup (no OTP)**
* Phone → Email mapping stored in:
  phone_lookup/{phone}

---

## 🗂️ Firestore Database Structure

users/{uid}
users/{uid}/expenses/{expenseId}
users/{uid}/incomes/{incomeId}
users/{uid}/budgets/{monthKey}
users/{uid}/goals/{monthKey}
phone_lookup/{phone}

---

## 📊 Data Models

### User

* userId, name, email, phone

### Expense

* amount, category, note, date, paymentSource

### Income

* amount, reason, date

### Budget

* month, amount

### Savings Goal

* targetAmount, note

---

## 🔒 Security

* Users can only access their own data
* Firebase Authentication enforced
* Firestore rules ensure user-level isolation

---

## 📁 Project Structure

com.example.expensetracker
├── activities/
├── adapters/
├── firebase/
├── models/
└── utils/

---

## 📷 Screenshots

(Add your screenshots in repo and link here)

![Home](home.png)
![Dashboard](dashboard.png)
![Analytics](analytics.png)

---

## 🔔 Notifications

* Local Android notifications only
* Budget alerts configurable from Settings
* Android 13+ permission handled

---

## 🌐 Offline Support

* Firestore caching enabled
* Works offline after first sync
* Internet required for login/register

---

## ⚠️ Important Notes

* APK does NOT include user data
* Data is fetched from Firebase
* Same Firebase project must be used across devices

---

## 👨‍💻 Developer

Shreyash Bobalade

---

## 📌 Future Improvements

* Google Sign-In integration
* OTP-based phone authentication
* Advanced analytics
* Cloud backup improvements

---

## ⭐ Support

If you like this project, give it a ⭐ on GitHub!
