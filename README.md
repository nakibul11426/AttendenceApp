# 📚 School Attendance App

A modern Android application for managing student attendance with SMS notifications, built with Jetpack Compose and Firebase Firestore.

---

## ✨ Features

### 📋 Student Management

- Add new students with name and parent phone number
- **Edit student information** (name and parent phone)
- View all students in a clean list
- Remove students (soft delete - preserves attendance history)
- View individual student attendance summary and history

### ✅ Attendance Marking

- Daily attendance tracking with four states:
  - **Not Marked** (Black/Gray) - Default state
  - **Present** (Green) - Student attended
  - **Absent** (Red) - Student was absent
  - **Holiday** (Blue) - Holiday/leave
- **Two-tap confirmation** system to prevent accidental marking
- Color-coded status indicators
- Real-time sync with Firebase

### 📱 SMS Notifications

- **Automatic SMS** sent to parents when student is marked absent
- SMS must succeed before attendance is recorded
- **Beautiful Snackbar** notifications for SMS success/error
- Error dialog shown if SMS completely fails
- Configure sender number through settings
- Auto-detect SIM number (when available)

### 📊 Attendance History

- View attendance records by date
- **Navigate to dedicated detail screen** for each date (optimized performance)
- Summary statistics (Present/Absent/Holiday counts)
- Individual student history with attendance percentage
- Circular progress indicator for attendance rate

### 🎨 Theme Support

- Light and Dark theme toggle
- Theme preference persisted locally
- Dynamic bottom navigation colors

---

## 🛠 Tech Stack

| Component         | Technology                 |
| ----------------- | -------------------------- |
| **Language**      | Kotlin                     |
| **UI Framework**  | Jetpack Compose            |
| **Design System** | Material 3                 |
| **Architecture**  | MVVM                       |
| **Database**      | Firebase Firestore         |
| **Local Storage** | DataStore Preferences      |
| **SMS**           | Android SmsManager         |
| **Navigation**    | Jetpack Navigation Compose |
| **Minimum SDK**   | Android 10 (API 29)        |
| **Target SDK**    | Android 15 (API 36)        |

---

## 📦 Project Structure

```
app/src/main/java/com/abdur/rahman/attendanceapp/
├── MainActivity.kt                 # App entry point
├── data/
│   ├── model/
│   │   ├── Student.kt             # Student data model
│   │   ├── AttendanceRecord.kt    # Attendance record model
│   │   ├── AttendanceStatus.kt    # Enum for attendance states
│   │   └── DailyAttendance.kt     # Daily attendance model
│   └── repository/
│       └── AttendanceRepository.kt # Firebase data operations
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt          # Bottom navigation host
│   │   ├── AttendanceScreen.kt    # Attendance marking screen
│   │   ├── StudentsScreen.kt      # Student list & management
│   │   ├── StudentDetailScreen.kt # Individual student summary
│   │   ├── HistoryScreen.kt       # Attendance history list
│   │   ├── HistoryDetailScreen.kt # Date-wise attendance details
│   │   ├── AddStudentScreen.kt    # Add student form
│   │   └── ManageStudentsScreen.kt
│   ├── viewmodel/
│   │   ├── AttendanceViewModel.kt
│   │   ├── AddStudentViewModel.kt
│   │   ├── StudentDetailViewModel.kt
│   │   ├── StudentManagementViewModel.kt
│   │   ├── HistoryViewModel.kt
│   │   └── HistoryDetailViewModel.kt
│   └── theme/
│       ├── Color.kt               # Color definitions
│       ├── Theme.kt               # Theme configuration
│       └── Type.kt                # Typography
├── util/
│   ├── SmsHelper.kt               # SMS sending utility
│   ├── SenderNumberPreferences.kt # Sender number storage
│   └── ThemePreferences.kt        # Theme preference storage
└── navigation/
    ├── Screen.kt                  # Navigation routes
    └── AppNavigation.kt           # Navigation graph
```

---

## 🚀 Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android device/emulator with API 29+
- Firebase account

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd AttendanceApp
```

### Step 2: Firebase Setup

1. **Create Firebase Project**

   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add Project" and follow the setup wizard

2. **Add Android App to Firebase**

   - In Firebase Console, click "Add app" → Android
   - Package name: `com.abdur.rahman.attendanceapp`
   - Download `google-services.json`

3. **Place Configuration File**

   ```
   Copy google-services.json to: app/google-services.json
   ```

4. **Enable Firestore Database**

   - In Firebase Console → Build → Firestore Database
   - Click "Create database"
   - Start in **test mode** (for development)
   - Select your preferred region

5. **Firestore Security Rules** (for production)
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /students/{studentId} {
         allow read, write: if true; // Modify for production
       }
       match /attendance/{recordId} {
         allow read, write: if true; // Modify for production
       }
     }
   }
   ```

### Step 3: Build and Run

```bash
# Using Gradle
./gradlew assembleDebug

# Or open in Android Studio and click Run
```

---

## 📱 App Flow

### 1. Home Screen (Attendance Tab)

```
┌─────────────────────────────────┐
│  📱 Attendance                  │
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │ 📅 Monday, December 23    │  │
│  │    2025                   │  │
│  └───────────────────────────┘  │
│                                 │
│  5 Students    ✓3  ✗1          │
│                                 │
│  ┌───────────────────────────┐  │
│  │ John Doe                  │  │
│  │ 📞 +880123456789          │  │
│  │              Not Marked → │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ Jane Smith                │  │
│  │ 📞 +880987654321          │  │
│  │              ✓ Present    │  │
│  └───────────────────────────┘  │
│                                 │
├─────────────────────────────────┤
│ [Attendance] [Students] [History]│
└─────────────────────────────────┘
```

### 2. Marking Attendance Flow

```
Tap Student Card
       ↓
┌─────────────────────┐
│   Attendance Dialog │
│                     │
│   John Doe          │
│   📞 +880123456789  │
│                     │
│   Current: Not Marked│
│                     │
│ [Present] [Absent]  │
│                     │
│ [Mark as Holiday]   │
│                     │
│     [Close]         │
└─────────────────────┘
       ↓
First Tap → Button shows "Confirm"
       ↓
Second Tap → Attendance saved
       ↓
If Absent → SMS sent to parent
       ↓
If SMS fails → Error dialog, attendance NOT saved
```

### 3. Students Tab

```
┌─────────────────────────────────┐
│  👥 Students                    │
├─────────────────────────────────┤
│  5 Students                     │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 👤 John Doe      > ✏️ 🗑 │  │
│  │    📞 +880123456789       │  │
│  └───────────────────────────┘  │
│         ↓ Tap card              │
│  Opens Student Detail Screen    │
│         ↓ Tap ✏️                │
│  Opens Edit Student Dialog      │
│                                 │
├─────────────────────────────────┤
│                    [+ Add Student]
└─────────────────────────────────┘
```

### 4. Student Detail Screen

```
┌─────────────────────────────────┐
│  ← John Doe                     │
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │ 👤 John Doe               │  │
│  │    📞 +880123456789       │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │     Attendance Rate       │  │
│  │         ╭───╮             │  │
│  │        │ 85%│             │  │
│  │         ╰───╯             │  │
│  │    17 present of 20 days  │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌─────┐ ┌─────┐ ┌─────┐       │
│  │ 17  │ │  2  │ │  1  │       │
│  │Present│Absent│Holiday│       │
│  └─────┘ └─────┘ └─────┘       │
│                                 │
│  Attendance History             │
│  ┌───────────────────────────┐  │
│  │ Mon, Dec 23  │  ✓ Present │  │
│  │ Fri, Dec 20  │  ✗ Absent  │  │
│  │ Thu, Dec 19  │  ✓ Present │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

### 5. History Tab

```
┌─────────────────────────────────┐
│  📅 History                     │
├─────────────────────────────────┤
│  Select a date to view details  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 📅 Monday, December 23    > │
│  │    2025-12-23              │  │
│  └───────────────────────────┘  │
│         ↓ Tap                   │
│  ┌───────────────────────────┐  │
│  │ 📅 Sunday, December 22    > │
│  │    2025-12-22              │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

### 6. History Detail Screen

```
┌─────────────────────────────────┐
│  ← Attendance Details           │
│    Monday, December 23, 2025    │
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │        Summary            │  │
│  │  ┌───┐  ┌───┐  ┌───┐     │  │
│  │  │ 3 │  │ 1 │  │ 1 │     │  │
│  │  │ ✓ │  │ ✗ │  │ 🏖│     │  │
│  │  └───┘  └───┘  └───┘     │  │
│  │  Total Students: 5        │  │
│  └───────────────────────────┘  │
│                                 │
│  Student Records                │
│  ┌───────────────────────────┐  │
│  │ John Doe      │ ✓ Present │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ Jane Smith    │ ✗ Absent  │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

---

## 🔐 Permissions Required

```xml
<!-- Send SMS to parents -->
<uses-permission android:name="android.permission.SEND_SMS" />

<!-- Detect SIM number (optional) -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />

<!-- Firebase connectivity -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 📊 Firebase Data Structure

### Students Collection

```
/students/{studentId}
{
  "id": "uuid-string",
  "name": "John Doe",
  "parentPhone": "+880123456789",
  "isActive": true,
  "createdAt": 1703318400000
}
```

### Attendance Collection

```
/attendance/{studentId_date}
{
  "id": "studentId_2025-12-23",
  "studentId": "uuid-string",
  "studentName": "John Doe",
  "date": "2025-12-23",
  "status": "PRESENT",  // PRESENT, ABSENT, HOLIDAY, NOT_MARKED
  "smsSent": false,
  "timestamp": 1703318400000
}
```

---

## 🎯 Key Features Explained

### Two-Tap Confirmation

Prevents accidental attendance marking:

1. **First tap**: Button changes to "Confirm"
2. **Second tap**: Attendance is saved

### SMS Before Database

For absent marking:

1. SMS is sent FIRST
2. If SMS succeeds → Green Snackbar notification, Database is updated
3. If SMS fails → Red Error dialog shown, database NOT updated

### Edit Student

- Tap the pencil (✏️) icon on any student card
- Edit dialog opens with pre-filled name and phone
- Validation ensures valid data before saving
- Changes sync to Firebase immediately

### Theme Persistence

- Theme preference stored using DataStore
- Survives app restarts
- Toggle button in toolbar

---

## 🐛 Troubleshooting

### Firebase Connection Issues

- Verify `google-services.json` is in `app/` folder
- Check Firebase project settings match package name
- Ensure Firestore is enabled in Firebase Console

### SMS Not Sending

- Check SMS permission is granted
- Verify phone number format
- Some carriers block automated SMS

### Build Errors

```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

---

## 📄 License

This project is for educational purposes.

---

## 👨‍💻 Developer

**Abdur Rahman**

---

## 🔮 Future Improvements

- [ ] Export attendance reports (PDF/Excel)
- [ ] Multiple class/section support
- [ ] Teacher authentication
- [ ] Push notifications
- [ ] Offline mode with sync
- [ ] Attendance analytics dashboard
- [ ] Parent app for viewing attendance
