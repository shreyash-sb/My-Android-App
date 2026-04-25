# 📱 Smart Expense Tracker

A full-featured Android application built using **Java + XML + Firebase** to manage expenses, income, budgets, and savings goals with analytics and reporting.

---

## 🚀 Features

- 🔐 User Authentication (Email/Password)
- 💸 Expense Tracking (Add, Edit, Delete)
- 💰 Income Management
- 📊 Budget Planning (Monthly)
- 🎯 Savings Goals Tracking
- 📈 Analytics Dashboard
- 📄 PDF Report Generation
- 👤 Profile Management
- 🌙 Theme & Language Settings
- 🔔 Local Notifications (Budget Alerts)
- 📱 Drawer-based Navigation

---

## 📦 APK Download

👉 https://github.com/shreyash-sb/My-Android-App/releases/download/v1.0/app-debug.apk

---

## 🛠 Tech Stack

- **Frontend:** Java, XML
- **Backend:** Firebase
- **Database:** Cloud Firestore
- **Authentication:** Firebase Authentication
- **IDE:** Android Studio

---

## 📁 Project Structure


app/src/main/java/com/example/expensetracker/
├── activities/
├── adapters/
├── firebase/
├── models/
└── utils/


---

## 🔥 Firebase Setup

### 1. Create Project
- Go to Firebase Console
- Add Android App
- Package Name: `com.example.expensetracker`
- Download `google-services.json` and place in `/app`

### 2. Enable Authentication
- Go to Authentication → Sign-in Method
- Enable **Email/Password**

### 3. Firestore Database
- Create database in **Production Mode**

---

## 📊 Firestore Database Structure


users/{uid}
users/{uid}/expenses/{expenseId}
users/{uid}/incomes/{incomeId}
users/{uid}/budgets/{monthKey}
users/{uid}/goals/{monthKey}
phone_lookup/{phone}


---

## 🔐 Security

- Users can access only their own data
- Firebase Authentication enforced
- Firestore rules ensure data privacy

---

## 🔔 Notifications

- Local Android notifications only
- Budget alerts configurable from Settings

---

## 🌐 Offline Support

- Firestore caching enabled
- Works offline after first sync
- Internet required for login/register

---

## ⚠️ Important Notes

- APK does NOT include user data
- Data is fetched from Firebase
- Same Firebase project must be used across devices

---

## 📷 Screenshots

(Add your screenshots in the repo and link here)

---

## 👨‍💻 Developer

**Shreyash Bobalade**

---

## ⭐ Support

If you like this project, give it a ⭐ on GitHub!