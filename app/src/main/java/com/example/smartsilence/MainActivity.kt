package com.example.smartsilence

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.smartsilence.data.LocationEntity
import com.example.smartsilence.data.PriorityContactEntity
import com.example.smartsilence.data.SettingsEntity
import com.example.smartsilence.data.TaskEntity
import com.example.smartsilence.data.SmartSilenceDatabase
import com.example.smartsilence.location.GeofenceManager
import com.example.smartsilence.location.LocationManager
import com.example.smartsilence.repository.SmartSilenceRepository
import com.example.smartsilence.security.AdaptiveBiometricAuthenticator
import com.example.smartsilence.security.AuthenticationResult
import com.example.smartsilence.security.ContextTrustLevel
import com.example.smartsilence.security.DndBehaviorHistory
import com.example.smartsilence.security.FailedAuthenticationTracker
import com.example.smartsilence.security.RiskContext
import com.example.smartsilence.security.RiskContextBuilder
import com.example.smartsilence.security.SensitiveAction
import com.example.smartsilence.security.SecurityAlertNotifier
import com.example.smartsilence.security.SecurityAuthenticationManager
import com.example.smartsilence.security.SecurityEventLogger
import com.example.smartsilence.security.SecurityRecoveryManager
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.BackgroundNavy2
import com.example.smartsilence.ui.theme.SmartSilenceTheme
import com.example.smartsilence.viewmodel.SmartSilenceViewModel


class MainActivity : FragmentActivity() {

    companion object {

        private const val EXTRA_REQUEST_AUTHENTICATED_DND =
            "request_authenticated_dnd"

        private const val EXTRA_TRUST_SCORE =
            "trust_score"
    }


    // =========================================================
    // DATABASE
    // =========================================================

    private val database by lazy {
        SmartSilenceDatabase.getDatabase(
            applicationContext
        )
    }

    private val repository by lazy {
        SmartSilenceRepository(
            database.smartSilenceDao()
        )
    }


    // =========================================================
    // VIEWMODEL
    // =========================================================

    private val viewModel: SmartSilenceViewModel by viewModels {

        object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {

                if (
                    modelClass.isAssignableFrom(
                        SmartSilenceViewModel::class.java
                    )
                ) {

                    return SmartSilenceViewModel(
                        repository
                    ) as T
                }

                throw IllegalArgumentException(
                    "Unknown ViewModel class"
                )
            }
        }
    }


    // =========================================================
    // SECURITY / ADAPTIVE AUTHENTICATION
    // =========================================================

    private val failedAuthenticationTracker by lazy {
        FailedAuthenticationTracker(
            applicationContext
        )
    }

    private val securityEventLogger by lazy {
        SecurityEventLogger(
            applicationContext
        )
    }

    private val securityAlertNotifier by lazy {
        SecurityAlertNotifier(
            applicationContext
        )
    }

    private val riskContextBuilder by lazy {
        RiskContextBuilder(
            applicationContext
        )
    }

    private val securityAuthenticationManager by lazy {
        SecurityAuthenticationManager(
            applicationContext
        )
    }
    private var pendingAuthenticatedAction: (() -> Unit)? = null

    private val biometricAuthenticator by lazy {

        AdaptiveBiometricAuthenticator(

            activity = this,

            onAuthenticationResult = { result ->

                handleAuthenticationResult(
                    result
                )
            },

            onBiometricFailure = {

                failedAuthenticationTracker
                    .recordFailure()

                securityEventLogger.logEvent(
                    eventType =
                        "BIOMETRIC_FAILURE"
                )

                if (
                    failedAuthenticationTracker.isLockedOut()
                ) {

                    securityEventLogger.logEvent(
                        eventType =
                            "AUTHENTICATION_LOCKOUT"
                    )

                    securityAlertNotifier
                        .showAuthenticationLockoutAlert()
                }
            }
        )
    }


    // =========================================================
    // LOCATION RESULT
    // =========================================================

    private var locationResult:
            ((Double, Double) -> Unit)? = null


    // =========================================================
    // FIRST LAUNCH SETUP
    // =========================================================

    private var setupCompleted by mutableStateOf(
        false
    )


    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseGranted =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (
                fineGranted ||
                coarseGranted
            ) {

                getCurrentLocation()

            } else {

                locationResult = null
            }
        }


    // =========================================================
    // CONTACTS PERMISSION
    // =========================================================

    private var contactsPermissionResult:
            ((Boolean) -> Unit)? = null

    private val contactsPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            contactsPermissionResult?.invoke(
                granted
            )

            contactsPermissionResult = null
        }


    // =========================================================
    // SMS PERMISSION
    // =========================================================

    private var smsPermissionResult:
            ((Boolean) -> Unit)? = null

    private val smsPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            smsPermissionResult?.invoke(
                granted
            )

            smsPermissionResult = null
        }


    // =========================================================
    // SETUP LOCATION PERMISSION
    // =========================================================

    private var setupLocationResult:
            ((Boolean) -> Unit)? = null

    private val setupLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseGranted =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            setupLocationResult?.invoke(
                fineGranted || coarseGranted
            )

            setupLocationResult = null
        }


    // =========================================================
    // CALL SCREENING ROLE
    // =========================================================

    private val callScreeningRoleLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // The result is handled by Android.
        }


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setupCompleted =
            getSharedPreferences(
                "smart_silence_setup",
                MODE_PRIVATE
            ).getBoolean(
                "setup_completed",
                false
            )

        setContent {

            SmartSilenceTheme {

                SmartSilenceApp(

                    viewModel = viewModel,

                    setupCompleted =
                        setupCompleted,

                    onSetupComplete = {

                        getSharedPreferences(
                            "smart_silence_setup",
                            MODE_PRIVATE
                        ).edit {

                            putBoolean(
                                "setup_completed",
                                true
                            )
                        }

                        setupCompleted = true
                    },

                    onRequestLocation = { callback ->

                        requestLocation(
                            callback
                        )
                    },

                    onRequestSetupLocation = { callback ->

                        requestSetupLocation(
                            callback
                        )
                    },

                    onRequestContactsPermission = { callback ->

                        requestContactsPermission(
                            callback
                        )
                    },

                    onRequestSmsPermission = { callback ->

                        requestSmsPermission(
                            callback
                        )
                    },

                    onRequestCallScreeningRole = {

                        requestCallScreeningRole()
                    },


                    // =================================================
                    // STEP 14B - PROTECT SECURITY SETTINGS ACCESS
                    // =================================================

                    onSecuritySettingsClick = { onAuthenticated ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.CHANGE_AUTHENTICATION_METHOD,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated =
                                        onAuthenticated
                                )
                            }
                        )
                    },


                    // =================================================
                    // STEP 14D PART 7
                    // CLEAR SECURITY EVENT LOG
                    // =================================================

                    onClearSecurityLog = {

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.RESET_SECURITY_SETTINGS,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        securityEventLogger
                                            .clearEvents()

                                        securityEventLogger.logEvent(

                                            eventType =
                                                "SECURITY_EVENT_LOG_CLEARED",

                                            action =
                                                SensitiveAction
                                                    .RESET_SECURITY_SETTINGS
                                                    .name,

                                            details =
                                                "Security event log cleared after successful authentication."
                                        )
                                    }
                                )
                            }
                        )
                    },


                    // =================================================
                    // STEP 18C - SAVE SECURITY RECOVERY QUESTIONS
                    // =================================================

                    onSaveRecoveryQuestions = {
                            firstQuestion,
                            firstAnswer,
                            secondQuestion,
                            secondAnswer ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.CHANGE_AUTHENTICATION_METHOD,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        val recoveryManager =
                                            SecurityRecoveryManager(
                                                applicationContext
                                            )

                                        val saved =
                                            recoveryManager
                                                .saveRecoveryQuestions(

                                                    firstQuestion =
                                                        firstQuestion,

                                                    firstAnswer =
                                                        firstAnswer,

                                                    secondQuestion =
                                                        secondQuestion,

                                                    secondAnswer =
                                                        secondAnswer
                                                )

                                        if (saved) {

                                            securityEventLogger
                                                .logEvent(

                                                    eventType =
                                                        "RECOVERY_QUESTIONS_UPDATED",

                                                    action =
                                                        SensitiveAction
                                                            .CHANGE_AUTHENTICATION_METHOD
                                                            .name
                                                )

                                            Toast.makeText(

                                                this@MainActivity,

                                                "Security recovery questions saved.",

                                                Toast.LENGTH_SHORT

                                            ).show()

                                        } else {

                                            Toast.makeText(

                                                this@MainActivity,

                                                "Unable to save security recovery questions.",

                                                Toast.LENGTH_LONG

                                            ).show()
                                        }
                                    }
                                )
                            }
                        )
                    },


                    // =================================================
                    // STEP 18D - VERIFY SECURITY RECOVERY ANSWERS
                    // =================================================

                    onVerifyRecoveryAnswers = {
                            firstAnswer,
                            secondAnswer ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.RESET_AUTHENTICATION_CREDENTIALS,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        val recoveryManager =
                                            SecurityRecoveryManager(
                                                applicationContext
                                            )

                                        val verified =
                                            recoveryManager
                                                .verifyRecoveryAnswers(

                                                    firstAnswer =
                                                        firstAnswer,

                                                    secondAnswer =
                                                        secondAnswer
                                                )

                                        if (verified) {

                                            failedAuthenticationTracker
                                                .resetAfterSuccess()

                                            securityEventLogger
                                                .logEvent(

                                                    eventType =
                                                        "SECURITY_RECOVERY_SUCCESS",

                                                    action =
                                                        SensitiveAction
                                                            .RESET_AUTHENTICATION_CREDENTIALS
                                                            .name
                                                )

                                            Toast.makeText(

                                                this@MainActivity,

                                                "Recovery verification successful.",

                                                Toast.LENGTH_SHORT

                                            ).show()

                                        } else {

                                            securityEventLogger
                                                .logEvent(

                                                    eventType =
                                                        "SECURITY_RECOVERY_FAILURE",

                                                    action =
                                                        SensitiveAction
                                                            .RESET_AUTHENTICATION_CREDENTIALS
                                                            .name
                                                )

                                            Toast.makeText(

                                                this@MainActivity,

                                                "Incorrect recovery answers.",

                                                Toast.LENGTH_LONG

                                            ).show()
                                        }
                                    }
                                )
                            }
                        )
                    },


                    // =================================================
                    // ADD PRIORITY CONTACT
                    // =================================================

                    onAddPriorityContact = {
                            savedLocations,
                            name,
                            phoneNumber ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.ADD_PRIORITY_CONTACT,

                            savedLocations =
                                savedLocations,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        viewModel.addPriorityContact(

                                            name =
                                                name,

                                            phoneNumber =
                                                phoneNumber
                                        )
                                    }
                                )
                            }
                        )
                    },


                    // =================================================
                    // SAVE SETTINGS / AUTO REPLY
                    // =================================================

                    onSaveSettings = {
                            newSettings ->

                        val currentSettings =
                            viewModel.settings.value

                        val action =
                            if (
                                currentSettings?.autoReplyEnabled !=
                                newSettings.autoReplyEnabled
                            ) {

                                SensitiveAction
                                    .ENABLE_DISABLE_AUTO_REPLY

                            } else {

                                SensitiveAction
                                    .EDIT_AUTO_REPLY
                            }

                        buildCurrentRiskContext(

                            action =
                                action,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {
                                        Log.d(
                                            "SmartSilenceAutoReply",
                                            "Authenticated; saving autoReplyEnabled=${newSettings.autoReplyEnabled}"
                                        )
                                        viewModel.saveSettings(newSettings)
                                        Toast.makeText(
                                            this@MainActivity,
                                            if (newSettings.autoReplyEnabled) {
                                                "Automatic SMS reply enabled"
                                            } else {
                                                "Automatic SMS reply disabled"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        )
                    },


                    // =================================================
                    // DELETE PRIORITY CONTACT
                    // =================================================

                    onDeletePriorityContact = {
                            savedLocations,
                            contact ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.DELETE_PRIORITY_CONTACT,

                            savedLocations =
                                savedLocations,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        viewModel.deletePriorityContact(
                                            contact
                                        )
                                    }
                                )
                            }
                        )
                    },


                    // =================================================
                    // PROTECT MANUAL DND RULE CHANGES
                    // =================================================

                    onDndRuleChange = { action ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.CHANGE_DND_RULES,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated =
                                        action
                                )
                            }
                        )
                    },


                    // =================================================
                    // STEP 13 - PROTECT TASK OPERATIONS
                    // =================================================

                    onAddTask = {
                            title,
                            description,
                            locationId ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.ADD_TASK,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        viewModel.addTask(

                                            title =
                                                title,

                                            description =
                                                description,

                                            locationId =
                                                locationId
                                        )
                                    }
                                )
                            }
                        )
                    },

                    onUpdateTask = { task ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.EDIT_TASK,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        viewModel.updateTask(
                                            task
                                        )
                                    }
                                )
                            }
                        )
                    },

                    onDeleteTask = { task ->

                        buildCurrentRiskContext(

                            action =
                                SensitiveAction.DELETE_TASK,

                            savedLocations =
                                viewModel.locations.value,

                            onContextReady = { riskContext ->

                                requestAdaptiveAuthentication(

                                    context =
                                        riskContext,

                                    onAuthenticated = {

                                        viewModel.deleteTask(
                                            task
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }
        }

        // =========================================================
        // MEDIUM-TRUST AUTHENTICATED DND REQUEST
        // =========================================================

        if (
            intent.getBooleanExtra(
                EXTRA_REQUEST_AUTHENTICATED_DND,
                false
            )
        ) {

            val trustScore =
                intent.getIntExtra(
                    EXTRA_TRUST_SCORE,
                    50
                )

            handleAuthenticatedDndRequest(
                trustScore =
                    trustScore
            )

            // Prevent the same intent from triggering the
            // authentication request again on recreation.
            intent.removeExtra(
                EXTRA_REQUEST_AUTHENTICATED_DND
            )
        }
    }


    // =========================================================
    // HANDLE MEDIUM-TRUST DND REQUEST
    // =========================================================

    private fun handleAuthenticatedDndRequest(
        trustScore: Int
    ) {

        Log.d(
            "SmartSilenceSecurity",
            "Processing authenticated DND request. " +
                    "Trust score: $trustScore"
        )

        buildCurrentRiskContext(

            action =
                SensitiveAction.CHANGE_DND_RULES,

            savedLocations =
                viewModel.locations.value,

            onContextReady = { riskContext ->

                requestAdaptiveAuthentication(

                    context =
                        riskContext,

                    onAuthenticated = {

                        val dndManager =
                            DndManager(
                                applicationContext
                            )

                        if (
                            !dndManager.hasDndAccess()
                        ) {

                            Toast.makeText(

                                this@MainActivity,

                                "DND access is unavailable.",

                                Toast.LENGTH_LONG

                            ).show()

                            securityEventLogger.logEvent(

                                eventType =
                                    "TRUST_DND_ACCESS_UNAVAILABLE",

                                action =
                                    SensitiveAction
                                        .CHANGE_DND_RULES
                                        .name
                            )

                            return@requestAdaptiveAuthentication
                        }

                        dndManager.enableDnd()

                        DndBehaviorHistory(
                            applicationContext
                        ).record(

                            trustScore =
                                trustScore.coerceIn(
                                    50,
                                    79
                                ),

                            trustLevel =
                                ContextTrustLevel.MEDIUM,

                            decision =
                                DndBehaviorHistory
                                    .DECISION_USER_ENABLED
                        )

                        securityEventLogger.logEvent(

                            eventType =
                                "TRUST_DND_AUTHENTICATED",

                            action =
                                SensitiveAction
                                    .CHANGE_DND_RULES
                                    .name,

                            details =
                                "DND enabled after successful adaptive authentication from a medium-confidence trust prompt."
                        )

                        Toast.makeText(

                            this@MainActivity,

                            "Authentication successful. DND enabled.",

                            Toast.LENGTH_SHORT

                        ).show()

                        Log.d(
                            "SmartSilenceSecurity",
                            "DND enabled after successful adaptive authentication."
                        )
                    }
                )
            }
        )
    }


    // =========================================================
    // HANDLE NEW INTENT
    // =========================================================

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(
            intent
        )

        setIntent(
            intent
        )

        if (
            intent.getBooleanExtra(
                EXTRA_REQUEST_AUTHENTICATED_DND,
                false
            )
        ) {

            val trustScore =
                intent.getIntExtra(
                    EXTRA_TRUST_SCORE,
                    50
                )

            handleAuthenticatedDndRequest(
                trustScore =
                    trustScore
            )

            intent.removeExtra(
                EXTRA_REQUEST_AUTHENTICATED_DND
            )
        }
    }


    // =========================================================
    // ADAPTIVE AUTHENTICATION REQUEST
    // =========================================================

    private fun requestAdaptiveAuthentication(
        context: RiskContext,
        onAuthenticated: () -> Unit
    ) {

        if (
            failedAuthenticationTracker.isLockedOut()
        ) {

            val remainingSeconds =
                (
                        failedAuthenticationTracker
                            .getRemainingLockoutMillis() / 1000L
                        ).coerceAtLeast(1L)

            Toast.makeText(
                this,
                "Too many failed authentication attempts. Try again in $remainingSeconds seconds.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val policyResult =
            securityAuthenticationManager
                .createAuthenticationRequirement(
                    context
                )

        if (
            !policyResult.authenticationRequired
        ) {

            onAuthenticated()
            return
        }

        pendingAuthenticatedAction =
            onAuthenticated

        biometricAuthenticator.authenticate(

            strength =
                policyResult.authenticationStrength
        )
    }


    // =========================================================
    // AUTHENTICATION RESULT
    // =========================================================

    private fun handleAuthenticationResult(
        result: AuthenticationResult
    ) {

        when (result) {

            is AuthenticationResult.Success -> {

                failedAuthenticationTracker
                    .recordResult(result)

                securityEventLogger.logEvent(
                    eventType =
                        "AUTHENTICATION_SUCCESS"
                )

                Log.d(
                    "SmartSilenceSecurityLog",
                    "Stored security events: ${
                        securityEventLogger
                            .getEvents()
                            .size
                    }"
                )

                val action =
                    pendingAuthenticatedAction

                pendingAuthenticatedAction =
                    null

                action?.invoke()
            }

            is AuthenticationResult.Failure -> {

                failedAuthenticationTracker
                    .recordResult(result)

                securityEventLogger.logEvent(
                    eventType =
                        "AUTHENTICATION_FAILURE",

                    details =
                        result.reason
                )

                pendingAuthenticatedAction =
                    null
            }

            AuthenticationResult.Cancelled -> {

                securityEventLogger.logEvent(
                    eventType =
                        "AUTHENTICATION_CANCELLED"
                )

                pendingAuthenticatedAction =
                    null
            }

            is AuthenticationResult.Unavailable -> {

                securityEventLogger.logEvent(
                    eventType =
                        "AUTHENTICATION_UNAVAILABLE",

                    details =
                        result.reason
                )

                pendingAuthenticatedAction =
                    null
            }
        }
    }


    // =========================================================
    // LOCATION REQUEST
    // =========================================================

    private fun requestLocation(
        callback: (Double, Double) -> Unit
    ) {

        locationResult =
            callback

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (
            fineGranted ||
            coarseGranted
        ) {

            getCurrentLocation()

        } else {

            locationPermissionLauncher.launch(

                arrayOf(

                    Manifest.permission.ACCESS_FINE_LOCATION,

                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    // =========================================================
    // SETUP LOCATION REQUEST
    // =========================================================

    private fun requestSetupLocation(
        callback: (Boolean) -> Unit
    ) {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (
            fineGranted ||
            coarseGranted
        ) {

            callback(true)

        } else {

            setupLocationResult =
                callback

            setupLocationPermissionLauncher.launch(

                arrayOf(

                    Manifest.permission.ACCESS_FINE_LOCATION,

                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    // =========================================================
    // CONTACTS PERMISSION
    // =========================================================

    private fun requestContactsPermission(
        callback: (Boolean) -> Unit
    ) {

        val granted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED

        if (granted) {

            callback(true)

        } else {

            contactsPermissionResult =
                callback

            contactsPermissionLauncher.launch(
                Manifest.permission.READ_CONTACTS
            )
        }
    }


    // =========================================================
    // SMS PERMISSION
    // =========================================================

    private fun requestSmsPermission(
        callback: (Boolean) -> Unit
    ) {

        val granted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

        if (granted) {

            callback(true)

        } else {

            smsPermissionResult =
                callback

            smsPermissionLauncher.launch(
                Manifest.permission.SEND_SMS
            )
        }
    }


    // =========================================================
    // GET CURRENT LOCATION
    // =========================================================

    private fun getCurrentLocation() {

        val locationManager =
            LocationManager(this)

        locationManager.getCurrentLocation(

            onLocationReceived = {
                    latitude,
                    longitude ->

                locationResult?.invoke(
                    latitude,
                    longitude
                )

                locationResult = null
            },

            onError = {

                locationResult = null
            }
        )
    }


    // =========================================================
    // BUILD CURRENT SECURITY RISK CONTEXT
    // =========================================================

    private fun buildCurrentRiskContext(
        action: SensitiveAction,
        savedLocations: List<LocationEntity>,
        onContextReady: (RiskContext) -> Unit
    ) {

        val failedAttempts =
            failedAuthenticationTracker
                .getFailedAttempts()

        requestLocation { latitude, longitude ->

            val riskContext =
                riskContextBuilder.build(

                    action =
                        action,

                    currentLatitude =
                        latitude,

                    currentLongitude =
                        longitude,

                    savedLocations =
                        savedLocations,

                    failedAttempts =
                        failedAttempts
                )

            onContextReady(
                riskContext
            )
        }
    }


    // =========================================================
    // BUILD SECURITY RISK CONTEXT WITHOUT LOCATION
    // =========================================================

    private fun buildRiskContextWithoutLocation(
        action: SensitiveAction,
        onContextReady: (RiskContext) -> Unit
    ) {

        val failedAttempts =
            failedAuthenticationTracker
                .getFailedAttempts()

        val riskContext =
            riskContextBuilder.buildWithoutLocation(

                action =
                    action,

                failedAttempts =
                    failedAttempts
            )

        onContextReady(
            riskContext
        )
    }


    // =========================================================
    // CALL SCREENING ROLE
    // =========================================================

    private fun requestCallScreeningRole() {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.Q
        ) {
            return
        }

        val roleManager =
            getSystemService(
                RoleManager::class.java
            )

        if (
            !roleManager.isRoleAvailable(
                RoleManager.ROLE_CALL_SCREENING
            ) ||
            roleManager.isRoleHeld(
                RoleManager.ROLE_CALL_SCREENING
            )
        ) {
            return
        }

        // ---------------------------------------------------------
        // STEP 12 - PROTECT CALL SCREENING BEHAVIOR
        // ---------------------------------------------------------

        buildCurrentRiskContext(

            action =
                SensitiveAction.CHANGE_CALL_SCREENING_BEHAVIOR,

            savedLocations =
                viewModel.locations.value,

            onContextReady = { riskContext ->

                requestAdaptiveAuthentication(

                    context =
                        riskContext,

                    onAuthenticated = {

                        val intent =
                            roleManager.createRequestRoleIntent(
                                RoleManager.ROLE_CALL_SCREENING
                            )

                        callScreeningRoleLauncher.launch(
                            intent
                        )
                    }
                )
            }
        )
    }
}


// =============================================================
// SMART SILENCE APP
// =============================================================

@Composable
fun SmartSilenceApp(

    viewModel: SmartSilenceViewModel,

    setupCompleted: Boolean,

    onSetupComplete: () -> Unit,

    onRequestLocation:
        ((Double, Double) -> Unit) -> Unit,

    onRequestSetupLocation:
        ((Boolean) -> Unit) -> Unit,

    onRequestContactsPermission:
        ((Boolean) -> Unit) -> Unit,

    onRequestSmsPermission:
        ((Boolean) -> Unit) -> Unit,

    onRequestCallScreeningRole:
        () -> Unit,

    onSecuritySettingsClick:
        (() -> Unit) -> Unit,

    onClearSecurityLog:
        () -> Unit,

    onSaveRecoveryQuestions:
        (String, String, String, String) -> Unit,

    onVerifyRecoveryAnswers:
        (String, String) -> Unit,

    onAddPriorityContact:
        (List<LocationEntity>, String, String) -> Unit,

    onSaveSettings:
        (SettingsEntity) -> Unit,

    onDeletePriorityContact:
        (List<LocationEntity>, PriorityContactEntity) -> Unit,

    onDndRuleChange:
        (() -> Unit) -> Unit,

    onAddTask:
        (String, String, Int?) -> Unit,

    onUpdateTask:
        (TaskEntity) -> Unit,

    onDeleteTask:
        (TaskEntity) -> Unit

) {

    // Incrementing this key forces the local authentication gate
    // to return to its locked state after the user taps Lock App.
    var authSessionKey by remember {
        mutableStateOf(0)
    }

    // =========================================================
    // FIRST LAUNCH SETUP
    // =========================================================

    if (!setupCompleted) {

        SetupScreen(

            onSetupStart = {

                onRequestSetupLocation { locationGranted ->

                    if (locationGranted) {

                        onRequestContactsPermission {
                                contactsGranted ->

                            if (contactsGranted) {

                                onSetupComplete()
                            }
                        }
                    }
                }
            }
        )

        return
    }
    // =========================================================
    // REGISTER SAVED GEOFENCES BEFORE THE APP-LOCK GATE
    // =========================================================
    // Keep this registration outside the local UI authentication gate.
    // The UI can remain locked while Android processes geofence events.

    val locations by
    viewModel.locations.collectAsState()

    val locationsLoaded by
    viewModel.locationsLoaded.collectAsState()

    val context =
        LocalContext.current

    val geofenceManager =
        remember {
            GeofenceManager(context.applicationContext)
        }

    RegisterSavedGeofences(
        context = context.applicationContext,
        locations = locations,
        locationsLoaded = locationsLoaded,
        geofenceManager = geofenceManager
    )

    // =========================================================
    // LOCAL APP AUTHENTICATION GATE
    // This gate protects the UI, not background geofence processing.
    // =========================================================

    val isLocallyAuthenticated =
        LocalAuthenticationGate(sessionKey = authSessionKey)

    if (!isLocallyAuthenticated) {
        return
    }

    // =========================================================
    // NAVIGATION
    // =========================================================

    var selectedScreen by remember {
        mutableStateOf("Home")
    }

    val tasks by
    viewModel.tasks.collectAsState()

    val settings by
    viewModel.settings.collectAsState()

    // =========================================================
    // MAIN UI
    // =========================================================

    Box(

        modifier =
            androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(

                    Brush.verticalGradient(

                        colors =
                            listOf(
                                BackgroundNavy,
                                BackgroundNavy2,
                                BackgroundNavy
                            )
                    )
                )
    ) {

        Column(

            modifier =
                androidx.compose.ui.Modifier
                    .fillMaxSize()
        ) {

            Box(

                modifier =
                    androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .weight(1f)
            ) {

                when (selectedScreen) {

                    // =================================================
                    // HOME
                    // =================================================

                    "Home" -> {

                        HomeScreen(

                            onDndRuleChange =
                                onDndRuleChange
                        )
                    }


                    // =================================================
                    // LOCATIONS
                    // =================================================

                    "Locations" -> {

                        LocationsScreen(

                            locations =
                                locations,

                            onAddLocation = {
                                    name,
                                    latitude,
                                    longitude,
                                    radius ->

                                viewModel.addLocation(

                                    name =
                                        name,

                                    latitude =
                                        latitude,

                                    longitude =
                                        longitude,

                                    radius =
                                        radius
                                )
                            },

                            onEditLocation = {
                                    oldLocation,
                                    newName,
                                    newLatitude,
                                    newLongitude,
                                    newRadius ->

                                editLocation(

                                    oldLocation =
                                        oldLocation,

                                    newName =
                                        newName,

                                    newLatitude =
                                        newLatitude,

                                    newLongitude =
                                        newLongitude,

                                    newRadius =
                                        newRadius,

                                    geofenceManager =
                                        geofenceManager,

                                    viewModel =
                                        viewModel
                                )
                            },

                            onDeleteLocation = {
                                    location ->

                                deleteLocation(

                                    location =
                                        location,

                                    geofenceManager =
                                        geofenceManager,

                                    viewModel =
                                        viewModel
                                )
                            },

                            onRequestLocation =
                                onRequestLocation
                        )
                    }


                    // =================================================
                    // TASKS - STEP 13
                    // =================================================

                    "Tasks" -> {

                        TasksScreen(

                            tasks =
                                tasks,

                            locations =
                                locations,

                            onAddTask =
                                onAddTask,

                            onUpdateTask =
                                onUpdateTask,

                            onDeleteTask =
                                onDeleteTask
                        )
                    }


                    // =================================================
                    // MORE
                    // =================================================

                    "More" -> {

                        MoreScreen(

                            settings =
                                settings,

                            onSaveSettings = {
                                    newSettings ->

                                onSaveSettings(
                                    newSettings
                                )
                            },

                            onRequestSmsPermission = {
                                    callback ->

                                onRequestSmsPermission(
                                    callback
                                )
                            },

                            onPriorityContactsClick = {

                                selectedScreen =
                                    "PriorityContacts"
                            },

                            onCalendarSyncClick = {

                                selectedScreen =
                                    "Calendar"
                            },

                            onSecuritySettingsClick = {

                                onSecuritySettingsClick {

                                    selectedScreen =
                                        "SecuritySettings"
                                }
                            },

                            onLockApp = {
                                authSessionKey += 1
                                selectedScreen = "Home"
                            }
                        )
                    }


                    // =================================================
                    // PRIORITY CONTACTS
                    // =================================================

                    "PriorityContacts" -> {

                        PriorityContactsScreen(

                            viewModel =
                                viewModel,

                            onRequestCallScreeningRole =
                                onRequestCallScreeningRole,

                            onAddPriorityContact = {
                                    name,
                                    phoneNumber ->

                                onAddPriorityContact(

                                    locations,

                                    name,

                                    phoneNumber
                                )
                            },

                            onDeletePriorityContact = {
                                    contact ->

                                onDeletePriorityContact(

                                    locations,

                                    contact
                                )
                            }
                        )
                    }


                    // =================================================
                    // CALENDAR
                    // =================================================

                    "Calendar" -> {

                        CalendarScreen(

                            onBack = {

                                selectedScreen =
                                    "More"
                            }
                        )
                    }


                    // =================================================
                    // SECURITY SETTINGS
                    // =================================================

                    "SecuritySettings" -> {

                        SecuritySettingsScreen(

                            onBack = {

                                selectedScreen =
                                    "More"
                            },

                            onClearSecurityLog = {

                                onClearSecurityLog()
                            },

                            onSaveRecoveryQuestions = {
                                    firstQuestion,
                                    firstAnswer,
                                    secondQuestion,
                                    secondAnswer ->

                                onSaveRecoveryQuestions(

                                    firstQuestion,
                                    firstAnswer,
                                    secondQuestion,
                                    secondAnswer
                                )
                            },

                            onVerifyRecoveryAnswers = {
                                    firstAnswer,
                                    secondAnswer ->

                                onVerifyRecoveryAnswers(

                                    firstAnswer,
                                    secondAnswer
                                )
                            }
                        )
                    }
                }
            }


            // =========================================================
            // BOTTOM NAVIGATION
            // =========================================================

            BottomNavigationBar(

                selectedScreen =
                    selectedScreen,

                onScreenSelected = {

                    selectedScreen =
                        it
                }
            )
        }
    }
}