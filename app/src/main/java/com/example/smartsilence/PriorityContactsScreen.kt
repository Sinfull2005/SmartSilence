package com.example.smartsilence

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat

import com.example.smartsilence.data.LocationEntity
import com.example.smartsilence.data.PriorityContactEntity
import com.example.smartsilence.ui.theme.BackgroundNavy
import com.example.smartsilence.ui.theme.ElectricBlue
import com.example.smartsilence.ui.theme.SurfaceNavy
import com.example.smartsilence.ui.theme.TextSecondary
import com.example.smartsilence.viewmodel.SmartSilenceViewModel

private val TextWhite = Color(0xFFF5F7FF)

@Composable
fun PriorityContactsScreen(
    viewModel: SmartSilenceViewModel,
    onRequestCallScreeningRole: () -> Unit,
    onAddPriorityContact: (String, String) -> Unit,
    onDeletePriorityContact: (PriorityContactEntity) -> Unit
) {
    val context = LocalContext.current

    val priorityContacts by viewModel.priorityContacts.collectAsState()
    val locations by viewModel.locations.collectAsState()

    var showPermissionMessage by remember {
        mutableStateOf(false)
    }

    var selectedLocation by remember {
        mutableStateOf<LocationEntity?>(null)
    }

    var assignedContactIds by remember {
        mutableStateOf<Set<Int>>(emptySet())
    }

    var locationDropdownExpanded by remember {
        mutableStateOf(false)
    }

    // ============================================================
    // CONTACT PICKER
    // ============================================================

    val contactPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickContact()
        ) { contactUri: Uri? ->

            if (contactUri != null) {
                try {
                    val cursor = context.contentResolver.query(
                        contactUri,
                        arrayOf(
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME
                        ),
                        null,
                        null,
                        null
                    )

                    cursor?.use { contactCursor ->
                        if (contactCursor.moveToFirst()) {
                            val contactId = contactCursor.getString(
                                contactCursor.getColumnIndexOrThrow(
                                    ContactsContract.Contacts._ID
                                )
                            )

                            val name = contactCursor.getString(
                                contactCursor.getColumnIndexOrThrow(
                                    ContactsContract.Contacts.DISPLAY_NAME
                                )
                            )

                            val phoneCursor =
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    arrayOf(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                    ),
                                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                    arrayOf(contactId),
                                    null
                                )

                            phoneCursor?.use { phones ->
                                if (phones.moveToFirst()) {
                                    val phoneNumber = phones.getString(
                                        phones.getColumnIndexOrThrow(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER
                                        )
                                    )

                                    onAddPriorityContact(
                                        name,
                                        phoneNumber
                                    )
                                } else {
                                    Log.w(
                                        "PriorityContacts",
                                        "Selected contact has no phone number"
                                    )
                                }
                            }
                        }
                    }
                } catch (exception: Exception) {
                    Log.e(
                        "PriorityContacts",
                        "Failed to read selected contact",
                        exception
                    )
                }
            }
        }

    // ============================================================
    // LOAD LOCATION CONTACTS
    // ============================================================

    LaunchedEffect(selectedLocation?.id) {
        val location = selectedLocation

        if (location != null) {
            val relations =
                viewModel.getContactRelationsForLocation(location.id)

            assignedContactIds = relations
                .map { it.contactId }
                .toSet()
        } else {
            assignedContactIds = emptySet()
        }
    }

    // ============================================================
    // SCREEN
    // ============================================================

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = 25.dp,
            bottom = 24.dp
        )
    ) {

        // ========================================================
        // HEADER
        // ========================================================

        item {
            Text(
                text = "PRIORITY CONTACTS",
                color = ElectricBlue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Allow selected contacts during DND",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        // ========================================================
        // CALL SCREENING
        // ========================================================

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceNavy
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "CALL SCREENING",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Smart Silence needs Call Screening access to identify priority contacts during incoming calls.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onRequestCallScreeningRole()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = BackgroundNavy
                        )
                    ) {
                        Text(
                            text = "ENABLE CALL SCREENING",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ========================================================
        // GLOBAL PRIORITY CONTACTS HEADER
        // ========================================================

        item {
            Text(
                text = "GLOBAL PRIORITY CONTACTS",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "These contacts are used when no Smart Silence location is active.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // ========================================================
        // GLOBAL PRIORITY CONTACTS
        // ========================================================

        if (priorityContacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "No Priority Contacts",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Add contacts that should be allowed to reach you during Smart Silence.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = priorityContacts,
                key = { it.id }
            ) { contact ->
                PriorityContactCard(
                    contact = contact,
                    onDelete = {
                        onDeletePriorityContact(contact)
                    }
                )
            }
        }

        // ========================================================
        // LOCATION PRIORITY HEADER
        // ========================================================

        item {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "LOCATION PRIORITY",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Choose a saved location and select the contacts allowed during DND there.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // ========================================================
        // LOCATION PRIORITY CARD
        // ========================================================

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceNavy
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    // LOCATION DROPDOWN

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                locationDropdownExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BackgroundNavy,
                                contentColor = TextWhite
                            )
                        ) {
                            Text(
                                text = selectedLocation?.name
                                    ?: if (locations.isEmpty()) {
                                        "No saved locations"
                                    } else {
                                        "Select a location"
                                    }
                            )
                        }

                        DropdownMenu(
                            expanded = locationDropdownExpanded,
                            onDismissRequest = {
                                locationDropdownExpanded = false
                            }
                        ) {
                            locations.forEach { location ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = location.name)
                                    },
                                    onClick = {
                                        selectedLocation = location
                                        locationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // CONTACTS FOR SELECTED LOCATION

                    if (selectedLocation != null) {
                        Spacer(modifier = Modifier.height(14.dp))

                        priorityContacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = contact.name,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = contact.phoneNumber,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Checkbox(
                                    checked = assignedContactIds.contains(
                                        contact.id
                                    ),
                                    onCheckedChange = { checked ->
                                        val location = selectedLocation
                                            ?: return@Checkbox

                                        if (checked) {
                                            assignedContactIds =
                                                assignedContactIds + contact.id

                                            viewModel
                                                .assignPriorityContactToLocation(
                                                    locationId = location.id,
                                                    contactId = contact.id
                                                )
                                        } else {
                                            assignedContactIds =
                                                assignedContactIds - contact.id

                                            viewModel
                                                .removePriorityContactFromLocation(
                                                    locationId = location.id,
                                                    contactId = contact.id
                                                )
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ElectricBlue,
                                        checkmarkColor = BackgroundNavy
                                    )
                                )
                            }
                        }

                        if (priorityContacts.isEmpty()) {
                            Text(
                                text = "Add global priority contacts first.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select a saved location to configure its priority contacts.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // ========================================================
        // ADD PRIORITY CONTACT
        // ========================================================

        item {
            Button(
                onClick = {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        contactPickerLauncher.launch(null)
                    } else {
                        showPermissionMessage = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = BackgroundNavy
                )
            ) {
                Text(
                    text = "ADD PRIORITY CONTACT",
                    fontWeight = FontWeight.Bold
                )
            }

            if (showPermissionMessage) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Contacts permission is required. Please enable it in Android Settings.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ================================================================
// GLOBAL PRIORITY CONTACT CARD
// ================================================================

@Composable
private fun PriorityContactCard(
    contact: PriorityContactEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceNavy
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = contact.name,
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = contact.phoneNumber,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (contact.enabled) {
                            "PRIORITY ENABLED"
                        } else {
                            "PRIORITY DISABLED"
                        },
                        color = if (contact.enabled) {
                            ElectricBlue
                        } else {
                            TextSecondary
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BackgroundNavy,
                        contentColor = TextSecondary
                    )
                ) {
                    Text(
                        text = "DELETE",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
