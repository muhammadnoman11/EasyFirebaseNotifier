# 🚀 EasyFirebaseNotifier

A lightweight **Firebase Cloud Messaging (FCM)** helper library for Android.
Send and receive normal or dialog-based notifications easily

---

## 📦 Overview

**EasyFirebaseNotifier** helps you:

* 🔔 Send notifications to all users, specific topics, or specific devices
* 💬 Handle dialog-style notifications
* 🔐 Use Firebase Service Account securely
* ⚙️ Auto-subscribe to topics
* 🌙 One-time setup in your `Application` class

---

## ⚙️ Library Setup

### Step 1️⃣ — Add Dependency

#### 🧩 Add JitPack Repository (Project `settings.gradle`)

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### 📦 Add Library Dependency (Module `build.gradle`)

```gradle
android {
    // Add if duplicate META-INF issue
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation("com.github.muhammadnoman11:EasyFirebaseNotifier:1.0")
    
    // Coil for image loading (optional)
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
}
```

---

## 🔥 Firebase Setup

Set up Firebase in your project as usual just add Firebase and Firebase Messaging dependencies and your google-services.json file like in any standard FCM setup.

---


### Step 1️⃣: Get Project ID & Generate Private Key

| Step | Description                                                                                 | Screenshot                                     |
| ---- | ------------------------------------------------------------------------------------------- |------------------------------------------------|
| 1    | **Get Project ID** → Go to Firebase Console → Project Settings.                             | <img src="screenshots/step1.png" width="400"/> |
| 2    | **Generate Private Key** → Go to *Service Accounts* and click **Generate New Private Key**. | <img src="screenshots/step2.png" width="400"/> |

Download the generated `.json` file — it contains your Service Account credentials.

---

### Step 2️⃣: Initialize FCM in Application Class

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        FCMConfig.initialize(
            projectId = "your-firebase-project-id",
            serviceAccountJson = "
            // Paste your downloaded private key JSON here
            {
                "type": "service_account",
                "project_id": "...........",
                "private_key_id": "...",
                "private_key": "-----BEGIN PRIVATE KEY-----\nABC...\n-----END PRIVATE KEY-----\n",
                "client_email": "...@......iam.gserviceaccount.com"
            }
            "
        )
    }
}
```

---

## 🧩 Receiving Notifications

Create a service extending `EasyNotificationReceiver` to handle received FCM messages.

```kotlin
class FCMService : EasyNotificationReceiver() {

    override fun getActivityClass() = MainActivity::class.java // The activity to open when the notification is tapped
    override fun getChannelId() = "user_notifications" // Unique ID for your notification channel
    override fun getChannelName() = "User Notifications" // Name displayed for your notification channel
    override fun getNotificationIcon() = R.drawable.ic_launcher_foreground // Icon shown in the notification


    // Optional: Use a custom topic if needed - Default is ALL_USERS
    override fun getSubscriptionTopics(): List<String> {
        return listOf(
            FCMTopics.ALL_USERS, // Default
            FCMTopics.PREMIUM_USERS, // Example: premium users
            "custom_offers"          // Example: custom topic
        )
    }

    // Optional: Filter notifications by your own logic
    override fun shouldProcessNotification(data: Map<String, String>): Boolean {
        val userId = data["user_id"]
        val currentUserId = getCurrentUserId() // Example helper function
        return userId == currentUserId
    }

    override fun onTokenUpdated(token: String) {
        // Save or upload the updated token if needed
    }
}
```
📄 Add Service in AndroidManifest.xml

```
<service
    android:name=".service.FCMService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```
---

## ⚙️ Initialize Notification Sender

```kotlin
private val notificationSender = EasyNotificationSender()
```

---

## 🚀 Sending Notifications

### Send to All Users

```kotlin
notificationSender.sendToAllUsers(
    notificationData = NotificationData(
        title = title,
        body = body,
        imageUrl = imageUrl,
        type = NotificationType.NORMAL
    ),
    onSuccess = { Log.d("TAG", "Sent to all users") },
    onFailure = { e -> Log.e("TAG", "Failed", e) }
)
```

---

### Send to a Custom Topic

```kotlin
notificationSender.sendToTopic(
    topic = topic,
    notificationData = NotificationData(
        title = title,
        body = body,
        imageUrl = imageUrl,
        type = NotificationType.NORMAL
    )
)
```

---

### Send Dialog-Type Notification to Topic

```kotlin
notificationSender.sendToTopic(
    topic = topic,
    notificationData = NotificationData(
        title = title,
        body = body,
        imageUrl = imageUrl,
        type = NotificationType.DIALOG
    )
)
```

---

### Send to a Specific Device

```kotlin
notificationSender.sendToDevice(
    deviceToken = deviceToken,
    notificationData = NotificationData(
        title = title,
        body = body,
        imageUrl = imageUrl,
        type = NotificationType.NORMAL
    )
)
```

---

## 💬 Handle Dialog Notifications

### Initialize DialogPreferenceManager

```kotlin
val dialogPrefsManager = DialogPreferenceManager.getInstance(this)
```

---

### Access Dialog Data

```kotlin
val dialogData by dialogPrefsManager.dialogDataFlow.collectAsState()

LaunchedEffect(dialogData) {
    if (dialogData != null) {
        showReceivedDialog = true
    }
}

if (showReceivedDialog) {
    ReceivedMessageDialog(
        title = dialogData?.title,
        message = dialogData?.body,
        imageUrl = dialogData?.imageUrl,
        onClose = {
            showReceivedDialog = false
            // Clear dialog data when the dialog is closed
            dialogPrefsManager.clearDialogData()
        }

    )
}
```

---

### Clear Dialog Data

```kotlin
dialogPrefsManager.clearDialogData()
```

---

## 🧠 Optional Logic (Advanced Customization)

### 🔹 Process Notifications Based on Admin/User ID

```kotlin
override fun shouldProcessNotification(data: Map<String, String>): Boolean {
    val adminId = data["admin_id"]
    val currentAdminId = getMyAdminId()
    return adminId == currentAdminId
}
```

---

### 🔹 Custom Subscription Topics

```kotlin
override fun getSubscriptionTopics(): List<String> {
    return listOf(
        FCMTopics.ALL_USERS, // Default topic
        FCMTopics.PREMIUM_USERS, // Example for premium users
        "custom_offers" // Any custom topic
    )
}
```

---

## ✅ Summary

| Feature                 | Description                                    |
| ----------------------- | ---------------------------------------------- |
| 🔔 Send Notifications   | To all users, topics, or specific devices      |
| 💬 Dialog Notifications | Store and display dialog-based messages        |
| ⚙️ Configurable         | Custom topics, filters, and notification types |
| 🔐 Secure               | Uses Firebase Service Account                  |
| 🌙 Easy Setup           | Initialize once in your `Application` class    |

---


## 🤝 Contributing

Contributions are welcome! 🎉
Whether it's fixing bugs, improving documentation, or adding new features — everyone can help make **EasyFirebaseNotifier** even better and easier to use for all developers.

Your feedback and improvements are always appreciated! 💙

---

