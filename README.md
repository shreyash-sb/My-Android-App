# Smart Expense Tracker

Smart Expense Tracker is an Android app built with Java, XML, Material Design 3, Firebase Authentication, and Cloud Firestore. It helps users track expenses and income, manage monthly budgets, monitor savings goals, view analytics, and export PDF reports.

## Highlights

- Java + XML Android app with a clean multi-screen flow
- Firebase Auth + Firestore only, with no paid Firebase services required
- Separate expense and income management with budget-aware tracking
- Monthly budget planner, savings goals, analytics, and PDF report export
- Light and dark theme, local budget alerts, and offline Firestore cache after first sync

## Features

### Authentication
- Register with full name, Gmail address, 10-digit mobile number, and password
- Login using Gmail or 10-digit mobile number plus password
- Mobile login works through Firestore phone lookup and Firebase email/password auth
- Password reset support
- Note: mobile login is not OTP-based phone authentication

### Dashboard
- Total expenses
- Current month spending
- Total income
- Current month income
- Remaining monthly budget
- Income balance
- Highest expense for the current month
- Highest income for the current month
- Recent credit and debit activity
- Budget alert card when spending nears or exceeds the monthly budget

### Expense Management
- Add expense
- Edit expense
- Delete expense
- Category, amount, date, note, and payment source support
- Payment source options:
  - Monthly budget
  - Income balance
- Search and filter expense history

### Income Management
- Add income
- Edit income
- Delete income
- Amount, reason, and date support

### Planning and Insights
- Monthly budget planner
- Savings goals tracking
- Analytics with category-wise and monthly charts
- PDF report generation for selected date ranges

### Profile and Settings
- Update name and mobile number from profile
- Gmail remains the locked identity field
- Theme mode:
  - System
  - Light
  - Dark
- Budget alert controls from settings
- Android 13+ notification permission handling

### Navigation
- Drawer-based navigation across all major modules

## APK Download

[Download app-debug.apk](https://github.com/shreyash-sb/My-Android-App/releases/download/v1.0/app-debug.apk)

## Tech Stack

- Frontend: Java, XML, Material Design 3
- Architecture: Activity + Repository pattern
- Backend: Firebase
- Authentication: Firebase Authentication (Email/Password)
- Database: Cloud Firestore
- Charts: MPAndroidChart
- IDE: Android Studio
- Java Version: 17
- Min SDK: 24
- Target SDK: 34

## Project Structure

```text
app/src/main/java/com/example/expensetracker/
|- activities/
|- adapters/
|- firebase/
|- models/
`- utils/
```

## Main Screens

- Splash
- Login
- Register
- Dashboard
- Profile
- Add Expense
- Edit Expense
- Expense List
- Add Income
- Edit Income
- Income List
- Budget Planner
- Savings Goals
- Analytics
- Report
- Settings
- About

## Firebase Setup

### 1. Create a Firebase Project
- Open Firebase Console
- Create a new project or use an existing one

### 2. Add the Android App
- Add an Android app to Firebase
- Package name: `com.example.expensetracker`
- Download `google-services.json`
- Place it inside:

```text
app/google-services.json
```

### 3. Enable Authentication
- Go to `Authentication -> Sign-in method`
- Enable `Email/Password`
- Keep Google Sign-In and Phone Auth disabled for this build unless you plan to add code for them later

### 4. Create Firestore Database
- Go to `Firestore Database`
- Click `Create database`
- Create it manually once
- Choose `Production mode`
- Select your preferred region

### 5. Paste Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function signedIn() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return signedIn() && request.auth.uid == userId;
    }

    match /users/{userId} {
      allow create, read, update, delete: if isOwner(userId);

      match /expenses/{expenseId} {
        allow create, read, update, delete: if isOwner(userId);
      }

      match /incomes/{incomeId} {
        allow create, read, update, delete: if isOwner(userId);
      }

      match /budgets/{budgetId} {
        allow create, read, update, delete: if isOwner(userId);
      }

      match /goals/{goalId} {
        allow create, read, update, delete: if isOwner(userId);
      }
    }

    match /phone_lookup/{phone} {
      allow get: if true;
      allow list: if false;

      allow create, update: if signedIn()
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.phone == phone
        && request.resource.data.email is string;

      allow delete: if signedIn()
        && resource.data.userId == request.auth.uid;
    }
  }
}
```

### 6. Firebase Notes
- Cloud Firestore must be created manually once in Firebase Console
- Collections and documents are created automatically by the app when data is saved
- Firebase Storage is not required for the current build
- Blaze plan or billing account is not required for the current build

## Firestore Database Structure

```text
users/{uid}
users/{uid}/expenses/{expenseId}
users/{uid}/incomes/{incomeId}
users/{uid}/budgets/{monthKey}
users/{uid}/goals/{monthKey}
phone_lookup/{phone}
```

`phone_lookup/{phone}` is used only to support login by 10-digit mobile number. It maps the stored phone number to the user's Gmail address, then signs in through Firebase email/password auth. It is not an OTP login flow.

## Run Locally

1. Clone this repository
2. Open the project in Android Studio
3. Add your `google-services.json` file to `app/`
4. Sync Gradle
5. Connect a device or start an emulator
6. Run the app

## Offline Support

- Firestore caching is available on Android
- Previously synced data can be shown offline
- Internet is required for first login or register
- On a new device, old cloud data appears only after the app connects to the same Firebase project online at least once

## Important Notes

- The app accepts only `@gmail.com` addresses for registration and email login
- Mobile numbers must be exactly 10 digits
- The APK does not include any user data
- Data is loaded from Firebase, not bundled inside the app
- The same Firebase project must be used across devices to access the same account data

## Screenshots

Core screenshots are available in `docs/screenshots/`.

- [Splash](docs/screenshots/splash.png)
- [Login](docs/screenshots/login.png)
- [Register](docs/screenshots/register.png)
- [Dashboard](docs/screenshots/dashboard.png)
- [Drawer](docs/screenshots/drawer.png)
- [Add Expense](docs/screenshots/add-expense.png)
- [Expense List](docs/screenshots/expense-list.png)
- [Add Income](docs/screenshots/add-income.png)
- [Income List](docs/screenshots/income-list.png)
- [Budget](docs/screenshots/budget.png)
- [Goals](docs/screenshots/goals.png)
- [Analytics](docs/screenshots/analytics.png)
- [Report](docs/screenshots/report.png)
- [Profile](docs/screenshots/profile.png)
- [Settings](docs/screenshots/settings.png)
- [About](docs/screenshots/about.png)

## Developer

Shreyash Bobalade

## Support

If you like this project, give it a star on GitHub.
