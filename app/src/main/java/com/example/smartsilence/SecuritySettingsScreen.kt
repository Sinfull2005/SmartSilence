
package com.example.smartsilence

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsilence.security.SecurityEvent
import com.example.smartsilence.security.SecurityEventLogger
import com.example.smartsilence.security.RiskEventLogger
import com.example.smartsilence.security.SecurityRecoveryManager
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.TextSecondary
import com.example.smartsilence.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecuritySettingsScreen(
    onBack: () -> Unit,
    onClearSecurityLog: () -> Unit,
    onSaveRecoveryQuestions: (
        firstQuestion: String,
        firstAnswer: String,
        secondQuestion: String,
        secondAnswer: String
    ) -> Unit,
    onVerifyRecoveryAnswers: (
        firstAnswer: String,
        secondAnswer: String
    ) -> Unit
) {
    val context = LocalContext.current

    val securityEventLogger = remember {
        SecurityEventLogger(context)
    }

    val securityRecoveryManager = remember {
        SecurityRecoveryManager(context)
    }

    var events by remember {
        mutableStateOf(securityEventLogger.getEvents().reversed())
    }

    val riskEventLogger = remember {
        RiskEventLogger(context)
    }

    var riskEvents by remember {
        mutableStateOf(riskEventLogger.getEvents().reversed())
    }

    var selectedLog by remember {
        mutableStateOf("SECURITY")
    }

    val authenticationCapability = remember(context) {
        getAuthenticationCapability(context)
    }

    val authenticationMethod = remember(context) {
        getAvailableAuthenticationMethod(context)
    }

    val savedRecoveryQuestions = remember {
        securityRecoveryManager.getRecoveryQuestions()
    }

    var firstQuestion by remember {
        mutableStateOf(savedRecoveryQuestions.getOrNull(0) ?: "")
    }

    var firstAnswer by remember { mutableStateOf("") }

    var secondQuestion by remember {
        mutableStateOf(savedRecoveryQuestions.getOrNull(1) ?: "")
    }

    var secondAnswer by remember { mutableStateOf("") }

    var recoveryMessage by remember {
        mutableStateOf<String?>(null)
    }

    var recoveryConfigured by remember {
        mutableStateOf(securityRecoveryManager.isRecoveryConfigured())
    }

    LaunchedEffect(Unit) {
        val configured = securityRecoveryManager.isRecoveryConfigured()
        recoveryConfigured = configured

        if (configured) {
            val questions = securityRecoveryManager.getRecoveryQuestions()
            firstQuestion = questions.getOrNull(0) ?: ""
            secondQuestion = questions.getOrNull(1) ?: ""

            // Never populate saved answers into the UI.
            firstAnswer = ""
            secondAnswer = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundNavy)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Security Settings",
            color = TextWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Smart Silence security and authentication",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(20.dp))

        SecuritySectionCard {
            Text(
                text = "Adaptive Authentication",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            SecurityInfoRow(
                label = "Status",
                value = "ACTIVE"
            )

            SecurityInfoRow(
                label = "Authentication Method",
                value = authenticationMethod
            )

            SecurityInfoRow(
                label = "Authentication Capability",
                value = authenticationCapability
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Authentication strength is selected automatically according to the security risk of the action.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        SecuritySectionCard {
            Text(
                text = "Protected Actions",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            ProtectedActionText("Task operations")
            ProtectedActionText("Priority contact changes")
            ProtectedActionText("DND rule changes")
            ProtectedActionText("Call screening changes")
            ProtectedActionText("Automatic reply changes")
            ProtectedActionText("Security settings access")
        }

        Spacer(Modifier.height(16.dp))

        // =========================================================
        // SECURITY RECOVERY
        // =========================================================

        SecuritySectionCard {
            Text(
                text = "Security Recovery",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            SecurityInfoRow(
                label = "Status",
                value = if (recoveryConfigured) "CONFIGURED" else "NOT CONFIGURED"
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Configure two security questions for Smart Silence account recovery. Recovery answers are stored using encrypted Android Keystore storage.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(14.dp))

            Text("Question 1", color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = firstQuestion,
                onValueChange = {
                    firstQuestion = it
                    recoveryMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Enter your first security question", color = TextSecondary)
                },
                colors = recoveryTextFieldColors()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = firstAnswer,
                onValueChange = {
                    firstAnswer = it
                    recoveryMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Enter your answer", color = TextSecondary)
                },
                colors = recoveryTextFieldColors()
            )

            Spacer(Modifier.height(16.dp))

            Text("Question 2", color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = secondQuestion,
                onValueChange = {
                    secondQuestion = it
                    recoveryMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Enter your second security question", color = TextSecondary)
                },
                colors = recoveryTextFieldColors()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = secondAnswer,
                onValueChange = {
                    secondAnswer = it
                    recoveryMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Enter your answer", color = TextSecondary)
                },
                colors = recoveryTextFieldColors()
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    if (
                        firstQuestion.isBlank() ||
                        firstAnswer.isBlank() ||
                        secondQuestion.isBlank() ||
                        secondAnswer.isBlank()
                    ) {
                        recoveryMessage =
                            "Please complete both questions and answers."
                    } else {
                        onSaveRecoveryQuestions(
                            firstQuestion,
                            firstAnswer,
                            secondQuestion,
                            secondAnswer
                        )
                        recoveryMessage = "Recovery details submitted."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE RECOVERY QUESTIONS", fontWeight = FontWeight.Bold)
            }

            if (recoveryConfigured) {
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (firstAnswer.isBlank() || secondAnswer.isBlank()) {
                            recoveryMessage =
                                "Enter both recovery answers to verify access."
                        } else {
                            onVerifyRecoveryAnswers(firstAnswer, secondAnswer)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceNavy,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VERIFY RECOVERY ANSWERS", fontWeight = FontWeight.Bold)
                }
            }

            recoveryMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = TextSecondary, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // =========================================================
        // SECURITY EVENT LOG / RISK & ML LOG
        // =========================================================

        SecuritySectionCard {
            Text(
                text = "Security Logs",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Choose which log to view or clear. Each log is stored separately.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        selectedLog = "SECURITY"
                        events = securityEventLogger.getEvents().reversed()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (selectedLog == "SECURITY") ElectricBlue else SurfaceNavy,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Security Log")
                }

                Button(
                    onClick = {
                        selectedLog = "RISK"
                        riskEvents = riskEventLogger.getEvents().reversed()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (selectedLog == "RISK") ElectricBlue else SurfaceNavy,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Risk & ML Log")
                }
            }

            Spacer(Modifier.height(14.dp))

            if (selectedLog == "SECURITY") {
                Text(
                    text = "Secure Event Log",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                if (events.isEmpty()) {
                    Text(
                        "No security events recorded.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                } else {
                    events.forEach { event ->
                        SecurityEventItem(event = event)
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Button(
                    onClick = {
                        securityEventLogger.clearEvents()
                        events = emptyList()
                        onClearSecurityLog()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceNavy,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CLEAR SECURITY EVENT LOG", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Risk & ML Assessment Log",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Shows rule-based risk, ML risk when available, combined score, risk level, context signals, and authentication decision.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(8.dp))

                if (riskEvents.isEmpty()) {
                    Text(
                        "No risk assessments recorded yet.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                } else {
                    riskEvents.forEach { event ->
                        SecurityEventItem(event = event)
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Button(
                    onClick = {
                        riskEventLogger.clearEvents()
                        riskEvents = riskEventLogger.getEvents().reversed()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceNavy,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CLEAR RISK & ML LOG", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlue,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("BACK", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(20.dp))
    }
}

private fun getAvailableAuthenticationMethod(context: Context): String {
    val biometricManager = BiometricManager.from(context)

    val normalAuthenticators =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

    val strongAuthenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

    val strongAvailable =
        biometricManager.canAuthenticate(strongAuthenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS

    val normalAvailable =
        biometricManager.canAuthenticate(normalAuthenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS

    return when {
        strongAvailable -> "Strong biometric / device credential"
        normalAvailable -> "Biometric / device credential"
        else -> "Authentication unavailable"
    }
}

private fun getAuthenticationCapability(context: Context): String {
    val biometricManager = BiometricManager.from(context)

    val strongAuthenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

    val normalAuthenticators =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

    val strongAvailable =
        biometricManager.canAuthenticate(strongAuthenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS

    val normalAvailable =
        biometricManager.canAuthenticate(normalAuthenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS

    return when {
        strongAvailable -> "NORMAL + STRONG"
        normalAvailable -> "NORMAL"
        else -> "UNAVAILABLE"
    }
}

@Composable
private fun recoveryTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextWhite,
        unfocusedTextColor = TextWhite,
        focusedBorderColor = ElectricBlue,
        unfocusedBorderColor = TextSecondary,
        cursorColor = ElectricBlue,
        focusedLabelColor = ElectricBlue,
        unfocusedLabelColor = TextSecondary
    )

@Composable
private fun SecuritySectionCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                SurfaceNavy,
                RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun SecurityInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = ElectricBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProtectedActionText(action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "•",
            color = ElectricBlue,
            fontSize = 18.sp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = action,
            color = TextWhite,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SecurityEventItem(event: SecurityEvent) {
    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                BackgroundNavy,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = event.eventType,
            color = ElectricBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = dateFormat.format(Date(event.timestamp)),
            color = TextSecondary,
            fontSize = 12.sp
        )

        event.action?.let { action ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Action: $action",
                color = TextWhite,
                fontSize = 13.sp
            )
        }

        event.details?.let { details ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = details,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
