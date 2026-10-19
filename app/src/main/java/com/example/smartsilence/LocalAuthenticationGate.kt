
package com.example.smartsilence

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.biometric.BiometricManager.Authenticators
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.smartsilence.security.FailedAuthenticationTracker
import com.example.smartsilence.security.LocalCredentialManager
import com.example.smartsilence.security.LocalSessionManager
import com.example.smartsilence.security.SecurityEventLogger
import com.example.smartsilence.security.SecurityRecoveryManager
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.BackgroundNavy2
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.TextSecondary
import com.example.smartsilence.ui.theme.TextWhite
import kotlinx.coroutines.delay

private enum class AuthenticationScreen {
    LOGIN,
    SETUP_RECOVERY,
    VERIFY_RECOVERY,
    RESET_CREDENTIAL
}

/**
 * Protects interactive Compose screens only.
 *
 * Background receivers and services must not use this gate,
 * so geofence and calendar automation can continue while
 * the app's interactive UI is locked.
 */
@Composable
fun LocalAuthenticationGate(sessionKey: Int = 0): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    val credentialManager = remember(context) {
        LocalCredentialManager(context.applicationContext)
    }
    val failedTracker = remember(context) {
        FailedAuthenticationTracker(context.applicationContext)
    }
    val eventLogger = remember(context) {
        SecurityEventLogger(context.applicationContext)
    }
    val sessionManager = remember(context) {
        LocalSessionManager(context.applicationContext)
    }
    val recoveryManager = remember(context) {
        SecurityRecoveryManager(context.applicationContext)
    }

    var authenticated by remember {
        mutableStateOf(sessionManager.isSessionValid())
    }
    var previousSessionKey by remember {
        mutableIntStateOf(sessionKey)
    }

    val lockRequested = sessionKey != previousSessionKey

    LaunchedEffect(sessionKey) {
        if (sessionKey != previousSessionKey) {
            sessionManager.clearSession()
            authenticated = false
            previousSessionKey = sessionKey
        }
    }

    var screen by remember {
        mutableStateOf(AuthenticationScreen.LOGIN)
    }
    var selectedType by remember {
        mutableStateOf(LocalCredentialManager.CredentialType.PIN)
    }
    var credential by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var recoveryQuestion1 by remember { mutableStateOf("") }
    var recoveryQuestion2 by remember { mutableStateOf("") }
    var recoveryAnswer1 by remember { mutableStateOf("") }
    var recoveryAnswer2 by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Do not clear the local session on ON_STOP. Android can stop this
    // Activity temporarily when opening the contact picker, biometric prompt,
    // or system settings. Session expiry is still enforced below, and an
    // explicit sessionKey change still locks the app.

    LaunchedEffect(authenticated) {
        if (authenticated) {
            val remaining = sessionManager.getRemainingMillis()
            if (remaining <= 0L) {
                authenticated = false
                sessionManager.clearSession()
                errorMessage = "Your session expired. Please sign in again."
                eventLogger.logEvent(eventType = "LOCAL_SESSION_EXPIRED")
            } else {
                delay(remaining)
                authenticated = false
                sessionManager.clearSession()
                credential = ""
                confirmation = ""
                errorMessage = "Your session expired. Please sign in again."
                eventLogger.logEvent(eventType = "LOCAL_SESSION_EXPIRED")
            }
        }
    }

    val registered = credentialManager.isRegistered()
    val registeredType = credentialManager.getCredentialType()

    // First-time registration begins with the credential screen.
    val isRegistration = !registered &&
            screen != AuthenticationScreen.SETUP_RECOVERY &&
            screen != AuthenticationScreen.VERIFY_RECOVERY &&
            screen != AuthenticationScreen.RESET_CREDENTIAL

    if (authenticated && !lockRequested && sessionManager.isSessionValid()) {
        return true
    }

    val activeType = when (screen) {
        AuthenticationScreen.RESET_CREDENTIAL -> selectedType
        else -> if (isRegistration) selectedType else registeredType
    }

    val recoveryQuestions = remember(screen, recoveryManager) {
        recoveryManager.getRecoveryQuestions()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundNavy, BackgroundNavy2, BackgroundNavy)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SMART SILENCE",
                color = ElectricBlue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            val heading = when (screen) {
                AuthenticationScreen.LOGIN ->
                    if (isRegistration) "Create your local credential"
                    else "Unlock Smart Silence"

                AuthenticationScreen.SETUP_RECOVERY ->
                    "Set up account recovery"

                AuthenticationScreen.VERIFY_RECOVERY ->
                    "Recover your credential"

                AuthenticationScreen.RESET_CREDENTIAL ->
                    "Create a new credential"
            }

            Text(
                text = heading,
                color = TextWhite,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            val description = when (screen) {
                AuthenticationScreen.LOGIN ->
                    if (isRegistration) {
                        "Choose a PIN, password, or pattern to protect access to this app."
                    } else {
                        "Enter your registered credential to continue."
                    }

                AuthenticationScreen.SETUP_RECOVERY ->
                    "Choose two questions and save answers you can remember. These are used if you forget your credential."

                AuthenticationScreen.VERIFY_RECOVERY ->
                    "Answer both recovery questions to reset your forgotten credential."

                AuthenticationScreen.RESET_CREDENTIAL ->
                    "Choose a replacement credential. Your old credential is not required."
            }

            Text(
                text = description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(20.dp))

            when (screen) {
                AuthenticationScreen.LOGIN -> {
                    if (isRegistration || activeType == null) {
                        Text(
                            text = "Choose credential type",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        LocalCredentialManager.CredentialType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    credential = ""
                                    confirmation = ""
                                    errorMessage = null
                                },
                                label = {
                                    Text(
                                        when (type) {
                                            LocalCredentialManager.CredentialType.PIN ->
                                                "PIN (6–12 digits)"

                                            LocalCredentialManager.CredentialType.PASSWORD ->
                                                "Password (8–128 characters)"

                                            LocalCredentialManager.CredentialType.PATTERN ->
                                                "Pattern (4–9 distinct dots)"
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Text(
                            text = "Credential type: ${activeType.name}",
                            color = TextSecondary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    CredentialInput(
                        value = credential,
                        onValueChange = {
                            credential = it
                            errorMessage = null
                        },
                        label = if (isRegistration) "Create credential"
                        else "Enter credential",
                        type = activeType
                    )

                    if (isRegistration) {
                        Spacer(Modifier.height(12.dp))
                        CredentialInput(
                            value = confirmation,
                            onValueChange = {
                                confirmation = it
                                errorMessage = null
                            },
                            label = "Confirm credential",
                            type = activeType
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val type = activeType
                            if (type == null) {
                                errorMessage = "Credential type is unavailable."
                                return@Button
                            }

                            if (failedTracker.isLockedOut()) {
                                val seconds =
                                    (failedTracker.getRemainingLockoutMillis() + 999L) / 1000L
                                errorMessage =
                                    "Too many failed attempts. Try again in $seconds seconds."
                                return@Button
                            }

                            if (isRegistration) {
                                if (credential != confirmation) {
                                    errorMessage = "The credentials do not match."
                                    return@Button
                                }

                                when (
                                    val result = credentialManager.registerCredential(
                                        type = type,
                                        credential = credential
                                    )
                                ) {
                                    LocalCredentialManager.RegistrationResult.Success -> {
                                        failedTracker.resetAfterSuccess()
                                        eventLogger.logEvent(
                                            eventType = "LOCAL_CREDENTIAL_REGISTERED",
                                            action = type.name
                                        )
                                        credential = ""
                                        confirmation = ""
                                        recoveryQuestion1 = ""
                                        recoveryQuestion2 = ""
                                        recoveryAnswer1 = ""
                                        recoveryAnswer2 = ""
                                        errorMessage = null
                                        screen = AuthenticationScreen.SETUP_RECOVERY
                                    }

                                    LocalCredentialManager.RegistrationResult.AlreadyRegistered -> {
                                        errorMessage =
                                            "A credential is already registered. Please sign in."
                                    }

                                    LocalCredentialManager.RegistrationResult.StorageFailure -> {
                                        errorMessage =
                                            "The credential could not be saved. Please try again."
                                    }

                                    is LocalCredentialManager.RegistrationResult.InvalidInput -> {
                                        errorMessage = result.message
                                    }
                                }
                            } else {
                                val matches = credentialManager.verifyCredential(
                                    type = type,
                                    credential = credential
                                )

                                if (matches) {
                                    failedTracker.resetAfterSuccess()
                                    eventLogger.logEvent(
                                        eventType = "LOCAL_LOGIN_SUCCESS",
                                        action = type.name
                                    )
                                    credential = ""
                                    errorMessage = null
                                    sessionManager.startSession()
                                    authenticated = true
                                } else {
                                    failedTracker.recordFailure()
                                    eventLogger.logEvent(
                                        eventType = "LOCAL_LOGIN_FAILURE",
                                        action = type.name
                                    )
                                    errorMessage =
                                        if (failedTracker.isLockedOut()) {
                                            "Too many failed attempts. Login is temporarily locked."
                                        } else {
                                            "Incorrect credential. Please try again."
                                        }
                                    credential = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = BackgroundNavy
                        )
                    ) {
                        Text(
                            text = if (isRegistration) "CREATE CREDENTIAL" else "UNLOCK",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isRegistration) {
                        val activity = context as? FragmentActivity
                        val biometricAvailable = activity != null &&
                                BiometricManager.from(activity).canAuthenticate(
                                    Authenticators.BIOMETRIC_WEAK
                                ) == BiometricManager.BIOMETRIC_SUCCESS

                        if (biometricAvailable && activity != null) {
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val prompt = BiometricPrompt(
                                        activity,
                                        ContextCompat.getMainExecutor(activity),
                                        object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(
                                                result: BiometricPrompt.AuthenticationResult
                                            ) {
                                                failedTracker.resetAfterSuccess()
                                                sessionManager.startSession()
                                                eventLogger.logEvent(
                                                    eventType = "LOCAL_BIOMETRIC_LOGIN_SUCCESS"
                                                )
                                                credential = ""
                                                errorMessage = null
                                                authenticated = true
                                            }

                                            override fun onAuthenticationFailed() {
                                                failedTracker.recordFailure()
                                                eventLogger.logEvent(
                                                    eventType = "LOCAL_BIOMETRIC_LOGIN_FAILURE"
                                                )
                                            }

                                            override fun onAuthenticationError(
                                                errorCode: Int,
                                                errString: CharSequence
                                            ) {
                                                if (
                                                    errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                                    errorCode != BiometricPrompt.ERROR_CANCELED
                                                ) {
                                                    errorMessage = errString.toString()
                                                }
                                            }
                                        }
                                    )

                                    val promptInfo =
                                        BiometricPrompt.PromptInfo.Builder()
                                            .setTitle("Unlock Smart Silence")
                                            .setSubtitle(
                                                "Authenticate using your device biometric"
                                            )
                                            .setNegativeButtonText(
                                                "Use PIN / password / pattern"
                                            )
                                            .setAllowedAuthenticators(
                                                Authenticators.BIOMETRIC_WEAK
                                            )
                                            .build()

                                    prompt.authenticate(promptInfo)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceNavy,
                                    contentColor = TextWhite
                                )
                            ) {
                                Text("UNLOCK WITH BIOMETRICS")
                            }
                        }

                        if (recoveryManager.isRecoveryConfigured()) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    recoveryAnswer1 = ""
                                    recoveryAnswer2 = ""
                                    errorMessage = null
                                    screen = AuthenticationScreen.VERIFY_RECOVERY
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceNavy,
                                    contentColor = TextWhite
                                )
                            ) {
                                Text("FORGOT CREDENTIAL?")
                            }
                        }
                    }
                }

                AuthenticationScreen.SETUP_RECOVERY -> {
                    OutlinedTextField(
                        value = recoveryQuestion1,
                        onValueChange = { recoveryQuestion1 = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Recovery question 1") },
                        placeholder = { Text("Example: What was my first school's name?") },
                        singleLine = false
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = recoveryAnswer1,
                        onValueChange = { recoveryAnswer1 = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Answer 1") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = recoveryQuestion2,
                        onValueChange = { recoveryQuestion2 = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Recovery question 2") },
                        placeholder = { Text("Choose a different question") },
                        singleLine = false
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = recoveryAnswer2,
                        onValueChange = { recoveryAnswer2 = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Answer 2") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (
                                recoveryQuestion1.isBlank() ||
                                recoveryQuestion2.isBlank() ||
                                recoveryAnswer1.isBlank() ||
                                recoveryAnswer2.isBlank()
                            ) {
                                errorMessage = "Complete both recovery questions and answers."
                                return@Button
                            }

                            if (recoveryQuestion1.trim().equals(
                                    recoveryQuestion2.trim(),
                                    ignoreCase = true
                                )
                            ) {
                                errorMessage = "Choose two different recovery questions."
                                return@Button
                            }

                            val saved = recoveryManager.saveRecoveryQuestions(
                                firstQuestion = recoveryQuestion1,
                                firstAnswer = recoveryAnswer1,
                                secondQuestion = recoveryQuestion2,
                                secondAnswer = recoveryAnswer2
                            )

                            if (saved) {
                                eventLogger.logEvent(
                                    eventType = "LOCAL_RECOVERY_CONFIGURED"
                                )
                                recoveryAnswer1 = ""
                                recoveryAnswer2 = ""
                                sessionManager.startSession()
                                authenticated = true
                                errorMessage = null
                            } else {
                                errorMessage =
                                    "Recovery information could not be saved. Please try again."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = BackgroundNavy
                        )
                    ) {
                        Text("SAVE RECOVERY AND CONTINUE")
                    }
                }

                AuthenticationScreen.VERIFY_RECOVERY -> {
                    if (recoveryQuestions.size < 2) {
                        Text(
                            text = "Recovery questions are unavailable. You cannot reset the credential through this screen.",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = recoveryQuestions[0],
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = recoveryAnswer1,
                            onValueChange = { recoveryAnswer1 = it; errorMessage = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Answer 1") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = recoveryQuestions[1],
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = recoveryAnswer2,
                            onValueChange = { recoveryAnswer2 = it; errorMessage = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Answer 2") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (recoveryManager.isRecoveryLockedOut()) {
                                    val seconds =
                                        (recoveryManager.getRecoveryLockoutRemainingMillis() + 999L) / 1000L
                                    errorMessage =
                                        "Recovery is temporarily locked. Try again in $seconds seconds."
                                    return@Button
                                }

                                val verified = recoveryManager.verifyRecoveryAnswers(
                                    firstAnswer = recoveryAnswer1,
                                    secondAnswer = recoveryAnswer2
                                )

                                if (verified) {
                                    eventLogger.logEvent(
                                        eventType = "LOCAL_RECOVERY_VERIFIED"
                                    )
                                    recoveryAnswer1 = ""
                                    recoveryAnswer2 = ""
                                    credential = ""
                                    confirmation = ""
                                    selectedType =
                                        LocalCredentialManager.CredentialType.PIN
                                    errorMessage = null
                                    screen = AuthenticationScreen.RESET_CREDENTIAL
                                } else {
                                    eventLogger.logEvent(
                                        eventType = "LOCAL_RECOVERY_FAILURE"
                                    )
                                    errorMessage =
                                        if (recoveryManager.isRecoveryLockedOut()) {
                                            "Too many failed recovery attempts. Try again in 30 seconds."
                                        } else {
                                            "The recovery answers did not match."
                                        }
                                    recoveryAnswer1 = ""
                                    recoveryAnswer2 = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = BackgroundNavy
                            )
                        ) {
                            Text("VERIFY ANSWERS")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            screen = AuthenticationScreen.LOGIN
                            errorMessage = null
                            recoveryAnswer1 = ""
                            recoveryAnswer2 = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BACK TO LOGIN")
                    }
                }

                AuthenticationScreen.RESET_CREDENTIAL -> {
                    Text(
                        text = "Choose the new credential type",
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    LocalCredentialManager.CredentialType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                credential = ""
                                confirmation = ""
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    when (type) {
                                        LocalCredentialManager.CredentialType.PIN ->
                                            "PIN (6–12 digits)"

                                        LocalCredentialManager.CredentialType.PASSWORD ->
                                            "Password (8–128 characters)"

                                        LocalCredentialManager.CredentialType.PATTERN ->
                                            "Pattern (4–9 distinct dots)"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    CredentialInput(
                        value = credential,
                        onValueChange = { credential = it; errorMessage = null },
                        label = "New credential",
                        type = selectedType
                    )
                    Spacer(Modifier.height(12.dp))
                    CredentialInput(
                        value = confirmation,
                        onValueChange = { confirmation = it; errorMessage = null },
                        label = "Confirm new credential",
                        type = selectedType
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (credential != confirmation) {
                                errorMessage = "The new credentials do not match."
                                return@Button
                            }

                            when (
                                val result = credentialManager.replaceCredentialAfterRecovery(
                                    type = selectedType,
                                    credential = credential
                                )
                            ) {
                                LocalCredentialManager.RegistrationResult.Success -> {
                                    sessionManager.clearSession()
                                    authenticated = false
                                    failedTracker.resetAfterSuccess()
                                    eventLogger.logEvent(
                                        eventType = "LOCAL_CREDENTIAL_RESET_SUCCESS",
                                        action = selectedType.name
                                    )
                                    credential = ""
                                    confirmation = ""
                                    recoveryAnswer1 = ""
                                    recoveryAnswer2 = ""
                                    errorMessage = "Credential reset successfully. Sign in with your new credential."
                                    screen = AuthenticationScreen.LOGIN
                                }

                                LocalCredentialManager.RegistrationResult.AlreadyRegistered -> {
                                    errorMessage =
                                        "A credential already exists. Please retry recovery."
                                }

                                LocalCredentialManager.RegistrationResult.StorageFailure -> {
                                    errorMessage =
                                        "The new credential could not be saved. Please try again."
                                }

                                is LocalCredentialManager.RegistrationResult.InvalidInput -> {
                                    errorMessage = result.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = BackgroundNavy
                        )
                    ) {
                        Text("RESET CREDENTIAL")
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Your credential is verified locally on this device.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    return false
}

@Composable
private fun CredentialInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    type: LocalCredentialManager.CredentialType?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = when (type) {
                LocalCredentialManager.CredentialType.PIN ->
                    KeyboardType.NumberPassword

                LocalCredentialManager.CredentialType.PASSWORD ->
                    KeyboardType.Password

                LocalCredentialManager.CredentialType.PATTERN ->
                    KeyboardType.Ascii

                null -> KeyboardType.Password
            }
        )
    )
}
