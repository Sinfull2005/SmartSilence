# Smart Silence – A Context-Aware Intelligent Do Not Disturb System

**Smart Silence** is an Android application designed to make smartphone notification management more intelligent and context-aware. Instead of requiring users to manually enable and disable Do Not Disturb (DND) mode, the application uses contextual information such as saved locations, calendar schedules, and user-defined priorities to automate notification management.

The project also incorporates a context-based risk assessment approach and a TensorFlow Lite machine-learning component to support intelligent security decisions for sensitive actions.

The primary objective is to help users maintain focus, reduce unnecessary interruptions, and manage important communications without missing notifications that matter.

---

## Table of Contents

- [1. Problem Statement](#1-problem-statement)
- [2. Project Objectives](#2-project-objectives)
- [3. Key Features](#3-key-features)
- [4. System Workflow](#4-system-workflow)
- [5. Technology Stack](#5-technology-stack)
- [6. System Architecture](#6-system-architecture)
- [7. Machine Learning Component](#7-machine-learning-component)
- [8. Adaptive Authentication and Risk Assessment](#8-adaptive-authentication-and-risk-assessment)
- [9. Database Management](#9-database-management)
- [10. Permissions Required](#10-permissions-required)
- [11. Installation and Setup](#11-installation-and-setup)
- [12. How to Use](#12-how-to-use)
- [13. Privacy and Security](#13-privacy-and-security)
- [14. Limitations](#14-limitations)
- [15. Future Enhancements](#15-future-enhancements)
- [16. Project Structure](#16-project-structure)
- [17. Contributing](#17-contributing)
- [18. License](#18-license)

---

## 1. Problem Statement

Smartphone notifications are an important part of everyday communication, but frequent calls, messages, and application notifications can interrupt studying, working, travelling, and other activities that require attention.

Traditional Do Not Disturb functionality generally requires users to configure schedules or manually change notification settings. Static schedules may not reflect a user's actual context, while strict silent modes can cause important calls or messages to be missed.

Smart Silence addresses these challenges by providing a context-aware approach to notification management. It automates DND behaviour using configured locations and calendar schedules, supports priority-contact handling, and provides additional security mechanisms for sensitive application actions.

## 2. Project Objectives

The main objectives of Smart Silence are:

1. **Automate DND management:** Enable and disable DND according to supported contextual triggers.
2. **Provide location awareness:** Use saved locations and geofencing to identify entry into or exit from configured areas.
3. **Support calendar-based automation:** Use selected calendars and scheduled events to manage DND around planned activities.
4. **Prioritise important communications:** Allow users to configure priority contacts globally and for specific locations.
5. **Provide task reminders:** Associate tasks with saved locations and display relevant reminders when location-based triggers occur.
6. **Enable customised SMS auto-replies:** Send configured automatic replies for supported situations, subject to the application's settings and permissions.
7. **Introduce adaptive authentication:** Evaluate contextual risk before allowing access to protected actions and select an appropriate authentication requirement.
8. **Explore machine-learning-assisted decisions:** Use a TensorFlow Lite model as part of the context-trust assessment pipeline.
9. **Maintain local application data:** Store configuration information such as saved locations, tasks, contacts, and settings using a local database.

---

## 3. Key Features

### 3.1 Location-Based DND

Smart Silence allows users to save locations and configure DND behaviour for those areas.

- Save locations with a name, geographic coordinates, and geofence radius.
- Register supported geofences using location services.
- Detect supported geofence entry and exit events.
- Enable or disable DND according to the configured location rules.
- Track active locations so that overlapping geofences can be handled more carefully.
- Restore geofence monitoring after device restarts where supported by the system and application configuration.

This feature is useful for environments such as classrooms, libraries, workplaces, and other places where interruptions should be minimised.

### 3.2 Calendar-Based DND Automation

The application supports calendar-based notification management.

- Read calendar events after the required permission is granted.
- Display available calendar information for user selection.
- Allow users to choose which calendars should be considered for automation.
- Schedule supported DND start and end actions around eligible events.
- Use Android alarm mechanisms to trigger scheduled actions.
- Exclude all-day events from the applicable timed-event automation logic.

Calendar automation depends on the selected calendars, event information, Android permissions, and device restrictions.

### 3.3 Priority Contact Management

Smart Silence provides a priority-contact configuration system to help users manage important communications.

- Maintain globally configured priority contacts.
- Support location-specific priority-contact configuration.
- Distinguish between global and location-specific contact preferences.
- Use the configured priority rules when determining how supported communications should be handled.

Priority contacts are intended to reduce the chance of overlooking important communications while DND is active. Actual call and notification behaviour remains subject to Android's system-level policies and device configuration.

### 3.4 Location-Associated Tasks and Reminders

Users can associate tasks with saved locations to support context-aware reminders.

- Create tasks and associate them with locations.
- View tasks within the application.
- Update task information.
- Delete tasks when they are no longer needed.
- Display relevant task reminders when a supported location-entry event occurs.

For example, a user can associate a task with a college location and receive a reminder when entering the configured area.

### 3.5 Customised SMS Auto-Reply

Smart Silence includes an SMS auto-reply feature for supported communication scenarios.

- Configure automatic reply messages.
- Apply the configured reply logic to eligible contacts and situations.
- Use priority-contact settings when deciding whether an automatic response should be sent.
- Support SMS sending only when the required permission and device capabilities are available.

The feature is intended to help users communicate their temporary unavailability without having to respond manually to every message.

**Note:** SMS auto-replies are subject to Android permissions, telephony availability, and the application's implemented contact and location rules. The feature does not imply automatic replies through internet-based messaging applications.

### 3.6 Adaptive Authentication

Smart Silence incorporates a contextual risk-assessment design for protecting sensitive actions.

Rather than requiring the same authentication process for every situation, the application evaluates contextual signals and determines the level of risk associated with a protected operation.

The intended approach considers factors such as:

- The user's current location relative to saved or familiar locations.
- Failed authentication attempts and repeated attempts to access protected functionality.
- The sensitivity of the action being performed.
- Available device-security and behavioural context.

Depending on the resulting risk level and configured policy, the application can require an appropriate authentication step before allowing a protected action.

Authentication options depend on the methods implemented and supported by the device. Android's authentication APIs can support biometric authentication and device-credential-based authentication when configured appropriately.

### 3.7 Machine-Learning-Assisted Context Assessment

The project includes a TensorFlow Lite model intended to contribute to context-trust or behavioural risk assessment.

The model works as part of a larger decision pipeline rather than replacing all application security rules. Rule-based checks, contextual features, and fallback handling remain important components of the overall design.

The objective is to explore how lightweight on-device inference can complement rule-based decision-making without requiring every assessment to depend on a remote server.

### 3.8 Modern Android Interface

Smart Silence uses Jetpack Compose to build its Android user interface.

The interface organises functionality into screens for the application's major features, including:

- Home and contextual status.
- Saved locations.
- Tasks and reminders.
- Calendar configuration.
- Priority contacts.
- Additional settings and security options.

The application uses a consistent visual theme and separates major features into reusable UI components.

---

## 4. System Workflow

The application follows a context-driven workflow:

1. **Initial configuration:** The user grants the required permissions and configures locations, calendars, contacts, tasks, and preferences.
2. **Context monitoring:** The application uses supported Android services to receive relevant geofence and scheduled-event triggers.
3. **Context evaluation:** The application evaluates the trigger against its configured rules and current state.
4. **DND management:** When the applicable conditions are satisfied, the application requests the appropriate DND state change.
5. **Communication handling:** Configured priority-contact and auto-reply rules determine how supported communication scenarios are handled.
6. **Task reminders:** Relevant location-associated tasks can be surfaced when their configured location trigger occurs.
7. **Security evaluation:** When a protected action is requested, the authentication policy evaluates contextual risk and determines the required authentication behaviour.
8. **Local persistence:** Application configuration and supported records are stored locally for subsequent use.

The exact behaviour depends on the feature configuration, available permissions, Android version, and device-specific restrictions.

---

## 5. Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language for Android development |
| Android SDK | Access to Android platform services and device capabilities |
| Jetpack Compose | Declarative user-interface development |
| Material Design components | UI components and visual consistency |
| Room Persistence Library | Structured local database storage |
| Kotlin Coroutines and Flow | Asynchronous work and observable application state where used |
| Google Play services Location | Location-related services and geofencing |
| Android Notification Policy APIs | Requesting and managing permitted DND behaviour |
| Android Calendar Provider | Accessing calendar information with permission |
| Android AlarmManager | Scheduling supported calendar-related actions |
| Android telephony and SMS APIs | Supporting SMS functionality subject to permissions and device capability |
| Android authentication APIs | Supporting authentication for protected actions |
| TensorFlow Lite | Lightweight on-device machine-learning inference |
| Python and TensorFlow | Supporting machine-learning model training and preparation |
| Gradle with Kotlin DSL | Build configuration and dependency management |
| Android Studio | Development, testing, debugging, and building |

---

## 6. System Architecture

Smart Silence follows a modular Android application structure that separates the user interface, application logic, data storage, contextual triggers, and security-related functionality.

### Presentation Layer

The presentation layer is implemented using Jetpack Compose. It contains the application's screens, reusable components, and user-interaction elements.

### ViewModel and Repository Layer

The ViewModel manages screen-related state and coordinates user actions with application logic. The repository provides an abstraction between application features and the underlying data-access layer.

### Local Data Layer

Room provides structured persistence for application records, including saved locations, tasks, priority contacts, and settings represented by the application's entities and data-access objects.

### Context and Automation Layer

This layer handles location-related events, geofence registration, calendar-event processing, scheduled actions, and DND state changes.

Relevant components include:

- `GeofenceManager`
- `GeofenceBroadcastReceiver`
- `GeofenceBootReceiver`
- `CalendarManager`
- `CalendarDndScheduler`
- `CalendarDndReceiver`
- `DndManager`

### Security Layer

The security package separates contextual risk evaluation, authentication policy, authentication tracking, and machine-learning-assisted assessment into dedicated components.

Examples of components in this layer include:

- `RiskEngine`
- `ContextTrustCalculator`
- `AdaptiveAuthenticationPolicy`
- `SecurityAuthenticationManager`
- `TensorFlowLiteInferenceEngine`
- `ContextTrustMlCombiner`

These components help keep security-related logic separate from the main presentation layer.

---

## 7. Machine Learning Component

Smart Silence includes a lightweight machine-learning component intended to support contextual trust assessment.

### Model Development

The project contains a Python training script, `train_smart_silence_model.py`, for the model-training workflow. A trained TensorFlow Lite model is included in the Android project for compatible on-device inference.

The model is designed to process a fixed set of numerical input features and produce a score that can contribute to the application's contextual assessment.

### On-Device Inference

TensorFlow Lite allows compatible models to execute locally on Android devices.

The intended inference workflow is:

1. Collect the supported contextual features.
2. Convert the features into the model's expected input representation.
3. Run inference using the TensorFlow Lite runtime.
4. Obtain the model's output score.
5. Combine the output with rule-based security decisions and applicable fallback logic.
6. Use the resulting assessment as an input to the authentication policy.

### Model and Evaluation Considerations

The included model and its training workflow are experimental project components. Training or validation using synthetic examples does not establish that the model can reliably detect real-world suspicious behaviour.

Model quality depends on representative training data, appropriate evaluation, feature correctness, and runtime integration. Machine-learning output should not be treated as a guarantee of security.

---

## 8. Adaptive Authentication and Risk Assessment

Adaptive authentication aims to balance convenience and security by adjusting authentication requirements to the context of a sensitive operation.

### Risk Inputs

The risk-assessment design considers three major categories:

**1. Location context**

The system evaluates whether the current location is consistent with configured or familiar locations, subject to the available location information.

**2. Authentication behaviour**

Repeated failed authentication attempts or suspicious access patterns can increase the assessed risk.

**3. Action sensitivity**

Actions are evaluated according to their sensitivity. An operation that changes security settings or accesses protected functionality may require stronger verification than a routine navigation action.

### Risk-Based Decision Process

The general decision flow is:

`Context collection → Risk evaluation → Authentication policy → Authentication challenge or permitted action`

The policy can distinguish lower-risk situations from situations requiring stronger verification. The precise thresholds and authentication requirements depend on the implemented policy.

The machine-learning component supplements this process where available; it should not override mandatory security checks or independently grant access to sensitive functionality.

---

## 9. Database Management

Smart Silence uses Room to manage structured local application data.

The data layer includes entities and database-access components for supported features such as:

- Saved locations.
- Location-associated tasks.
- Global priority contacts.
- Location-specific priority contacts.
- Application settings.
- Calendar-event data used by the application.

Room provides an abstraction over SQLite and supports structured queries, data-access objects, and entity-based persistence.

Local storage reduces the need to send application configuration to a remote service. Data retention, backup behaviour, and device-level protection still depend on the application's configuration and Android's storage policies.

---

## 10. Permissions Required

Smart Silence relies on Android permissions and user-granted system access for certain features.

| Permission or access | Purpose |
|---|---|
| Location permission | Supports location-related functionality and geofencing |
| Background location access, where required | Supports location monitoring when the application is not in the foreground |
| Notification permission | Allows the application to display supported notifications on Android versions that require it |
| Notification Policy Access | Allows the application to request DND changes |
| Calendar read permission | Allows access to calendar information |
| Exact-alarm access, where required | Supports appropriately scheduled calendar-related actions |
| SMS permission | Supports sending configured automatic SMS replies |
| Device telephony capability | Required for SMS functionality on compatible devices |

Not every feature requires every permission. Users should grant only the permissions needed for the features they choose to use.

Android may restrict background execution, exact alarms, location updates, and notification behaviour. Permission requirements can also vary with the Android version and target SDK.

---

## 11. Installation and Setup

### Prerequisites

- Android Studio with a compatible Android SDK.
- A compatible JDK and Gradle environment.
- An Android device or emulator for testing.
- A device that supports the platform features required by the functionality being tested.

### Step 1: Clone the Repository

```bash
git clone https://github.com/Sinfull2005/SmartSilence.git
```

### Step 2: Open the Project

1. Launch Android Studio.
2. Select **Open**.
3. Choose the cloned `SmartSilence` project directory.
4. Allow Gradle to sync the project.
5. Resolve any SDK or dependency prompts displayed by Android Studio.

### Step 3: Configure the Android SDK

Ensure that the SDK and JDK versions specified by the project's Gradle configuration are installed.

Do not copy another developer's `local.properties` file. Android Studio can generate the appropriate local SDK configuration for your computer.

### Step 4: Build the Application

Use Android Studio's **Build > Make Project** option, or run the Gradle wrapper from the project root:

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

### Step 5: Run the Application

1. Connect an Android device or start a compatible emulator.
2. Enable developer options and USB debugging if using a physical device.
3. Select the device in Android Studio.
4. Run the application using the Run button.

### Step 6: Grant Required Permissions

Grant the relevant permissions and system access when prompted. Configure the locations, calendars, contacts, and other options needed for the features you want to test.

**Important:** A successful build does not guarantee that every background feature will behave identically on every device. Validate location transitions, calendar scheduling, DND access, SMS behaviour, and authentication flows on a compatible test device.

---

## 12. How to Use

### Configure Location Automation

1. Open the Locations section.
2. Add a saved location.
3. Configure its details and desired DND behaviour.
4. Grant the necessary location permissions.
5. Test entry into and exit from the configured geofence.

### Configure Calendar Automation

1. Open the calendar section.
2. Grant calendar read access.
3. Select the calendars that should be considered.
4. Configure and verify a suitable test event.
5. Confirm the scheduled behaviour on the target device.

### Configure Priority Contacts

1. Open the Priority Contacts section.
2. Add the contacts that should receive priority treatment.
3. Configure global or location-specific preferences as appropriate.
4. Test the resulting behaviour with DND enabled.

### Configure Tasks and SMS Auto-Replies

1. Create tasks and associate them with relevant locations.
2. Configure the desired automatic reply message where supported.
3. Grant the required SMS permission if using SMS functionality.
4. Test the relevant trigger and confirm that the expected notification or reply occurs.

### Test Adaptive Authentication

1. Open the relevant security settings.
2. Configure the supported authentication options.
3. Trigger a protected action.
4. Observe the risk evaluation and authentication requirement.
5. Repeat with different supported context conditions to evaluate the policy.

---

## 13. Privacy and Security

Smart Silence is designed around local Android functionality and user-configured context.

Important privacy and security considerations include:

- Location and calendar access require the applicable permissions.
- Users should review the permissions granted to the application.
- Local database storage does not automatically guarantee encryption.
- Sensitive credentials and signing keys must not be committed to a public repository.
- Authentication decisions should rely on robust checks rather than machine-learning predictions alone.
- SMS functionality should be tested carefully because messages may incur carrier charges and can expose information to recipients.
- Device-specific restrictions and security settings can affect application behaviour.

The project should be evaluated on its actual implementation and configuration rather than assumed to provide guarantees that have not been independently verified.

---

## 14. Limitations

- Location-based behaviour depends on location accuracy, geofence delivery, permissions, and Android background-execution policies.
- DND changes require the appropriate Notification Policy Access and remain subject to platform behaviour.
- Calendar automation depends on calendar access, selected calendars, event data, and scheduling restrictions.
- SMS auto-replies require the relevant permission and compatible telephony functionality.
- Priority-contact handling depends on the implemented rules and Android's call and notification mechanisms.
- Machine-learning predictions depend on training data quality and appropriate evaluation; synthetic-data results do not establish real-world reliability.
- Adaptive authentication depends on the correctness of contextual signals and the security of the authentication mechanisms used.
- Battery consumption and trigger timing may vary across device manufacturers and Android versions.

---

## 15. Future Enhancements

Potential improvements for future versions include:

- More comprehensive activity recognition for driving, walking, and other supported contexts.
- Improved fusion of location, calendar, activity, and user preferences.
- Expanded battery-consumption testing and background-execution optimisation.
- More extensive testing across Android versions and device manufacturers.
- Evaluation of the machine-learning model using representative, ethically collected data.
- More detailed analytics for understanding automation decisions.
- Enhanced user controls for automation rules, exception handling, and notification preferences.
- Additional accessibility and usability improvements.
- More comprehensive automated tests for geofencing, calendar triggers, authentication policies, and state transitions.

These are potential extensions and should not be interpreted as features already available in the current version.

---

## 16. Project Structure

The project is organised into feature-oriented components to make the code easier to understand and maintain.

```text
SmartSilence/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/smartsilence/
│           │   ├── calendar/
│           │   ├── data/
│           │   ├── location/
│           │   ├── notification/
│           │   ├── repository/
│           │   ├── security/
│           │   ├── ui/
│           │   │   └── theme/
│           │   ├── viewmodel/
│           │   ├── MainActivity.kt
│           │   ├── HomeScreen.kt
│           │   ├── LocationsScreen.kt
│           │   ├── TasksScreen.kt
│           │   ├── CalendarScreen.kt
│           │   ├── PriorityContactsScreen.kt
│           │   └── DndManager.kt
│           ├── assets/
│           │   └── [ML model assets]
│           └── AndroidManifest.xml
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── train_smart_silence_model.py
└── README.md
```

*Note: This is a representative overview of the repository. Exact paths and files may vary with the checked-in project version.*

---

## 17. Contributing

Contributions and suggestions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Make focused changes and test the affected functionality.
4. Ensure that existing features continue to work.
5. Submit a pull request describing the changes and any relevant testing.

For bug reports, include the Android version, device or emulator details, reproduction steps, and relevant logs. Remove personal information, phone numbers, location details, and credentials from logs before sharing them.

---

## 18. License

No license is specified here. Unless a license file is added to the repository, the project's reuse and redistribution permissions should not be assumed.

---

**Project:** Smart Silence – A Context-Aware Intelligent Do Not Disturb System  
**Platform:** Android  
**Primary Language:** Kotlin  
**UI Framework:** Jetpack Compose  
**Machine Learning:** TensorFlow Lite

Repository: https://github.com/Sinfull2005/SmartSilence
