package com.abdur.rahman.attendanceapp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdur.rahman.attendanceapp.data.model.AttendanceStatus
import com.abdur.rahman.attendanceapp.data.model.Student
import com.abdur.rahman.attendanceapp.ui.viewmodel.AttendanceViewModel
import com.abdur.rahman.attendanceapp.ui.viewmodel.SnackbarMessage
import com.abdur.rahman.attendanceapp.ui.viewmodel.StudentAttendanceItem
import com.abdur.rahman.attendanceapp.util.SenderNumberPreferences
import com.abdur.rahman.attendanceapp.util.SmsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Color definitions based on requirements
val PresentColor = Color(0xFF4CAF50)  // Green
val AbsentColor = Color(0xFFF44336)   // Red

/**
 * Theme Toggle Button Component
 */
@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    IconButton(onClick = onToggleTheme) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode"
        )
    }
}

/**
 * SMS Settings Button Component
 */
@Composable
fun SmsSettingsButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.SimCard,
            contentDescription = "SMS Settings"
        )
    }
}

/**
 * SMS Settings Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSettingsDialog(
    currentNumber: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDetectNumber: () -> String?
) {
    var phoneNumber by remember { mutableStateOf(currentNumber) }
    var isDetecting by remember { mutableStateOf(false) }
    var detectionMessage by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.SimCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SMS Sender Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Description
                Text(
                    text = "Enter the phone number of the SIM card that will be used to send absence notifications.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone number input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Sender Phone Number") },
                    placeholder = { Text("+880XXXXXXXXXX") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Auto-detect button
                OutlinedButton(
                    onClick = {
                        isDetecting = true
                        val detected = onDetectNumber()
                        if (detected != null && detected.isNotBlank()) {
                            phoneNumber = detected
                            detectionMessage = "Number detected successfully!"
                        } else {
                            detectionMessage = "Could not detect SIM number. Please enter manually."
                        }
                        isDetecting = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDetecting
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDetecting) "Detecting..." else "Auto-Detect SIM Number"
                    )
                }
                
                // Detection message
                detectionMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = if (message.contains("success")) 
                            PresentColor 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Info text
                Text(
                    text = "Note: Auto-detection may not work on all devices as some carriers don't store the number on the SIM card.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onSave(phoneNumber) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * SMS Error Dialog - shown when SMS sending fails
 */
@Composable
fun SmsErrorDialog(
    studentName: String,
    parentPhone: String,
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = AbsentColor,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "SMS Failed",
                fontWeight = FontWeight.Bold,
                color = AbsentColor
            )
        },
        text = {
            Column {
                Text(
                    text = "Failed to send absence notification for:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Student info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = studentName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = parentPhone,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Attendance was NOT marked. Please check SMS permission and try again.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("OK")
            }
        }
    )
}

/**
 * Standalone Attendance Screen Content (for bottom navigation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreenContent(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    viewModel: AttendanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // SMS Settings
    val senderNumberPreferences = remember { SenderNumberPreferences(context) }
    val senderNumber by senderNumberPreferences.senderNumber.collectAsState(initial = "")
    var showSmsSettingsDialog by remember { mutableStateOf(false) }
    
    // Dialog state
    var selectedStudentItem by remember { mutableStateOf<StudentAttendanceItem?>(null) }
    
    // SMS Permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denied
        }
    }
    
    // Phone state permission launcher (for SIM detection)
    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions granted, try to detect number
        val detected = senderNumberPreferences.detectSimNumber()
        if (detected != null && detected.isNotBlank()) {
            scope.launch {
                senderNumberPreferences.saveSenderNumber(detected)
            }
        }
    }
    
    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            val text = when (message) {
                is SnackbarMessage.Success -> message.message
                is SnackbarMessage.Error -> message.message
            }
            snackbarHostState.showSnackbar(
                message = text,
                duration = SnackbarDuration.Short
            )
            // Auto-dismiss after 3 seconds (Short duration is ~4 seconds)
            delay(3000)
            viewModel.clearSnackbarMessage()
        }
    }
    
    // Request SMS permission on first load
    LaunchedEffect(Unit) {
        if (!SmsHelper.hasSmSPermission(context)) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }
    
    // Show attendance dialog when a student is selected
    selectedStudentItem?.let { item ->
        AttendanceDialog(
            item = item,
            onDismiss = { selectedStudentItem = null },
            onPresentTap = {
                viewModel.onAttendanceTap(context, item.student, AttendanceStatus.PRESENT)
                // Update selected item to reflect changes
                val updatedItem = uiState.students.find { it.student.id == item.student.id }
                if (updatedItem != null) {
                    selectedStudentItem = updatedItem
                }
            },
            onAbsentTap = {
                viewModel.onAttendanceTap(context, item.student, AttendanceStatus.ABSENT)
                val updatedItem = uiState.students.find { it.student.id == item.student.id }
                if (updatedItem != null) {
                    selectedStudentItem = updatedItem
                }
            },
            onConfirmed = {
                selectedStudentItem = null
            }
        )
    }
    
    // SMS Error Dialog
    uiState.smsError?.let { smsError ->
        SmsErrorDialog(
            studentName = smsError.studentName,
            parentPhone = smsError.parentPhone,
            errorMessage = smsError.errorMessage,
            onDismiss = {
                viewModel.clearSmsError()
                selectedStudentItem = null // Close the attendance dialog too
            }
        )
    }
    
    // SMS Settings Dialog
    if (showSmsSettingsDialog) {
        SmsSettingsDialog(
            currentNumber = senderNumber,
            onDismiss = { showSmsSettingsDialog = false },
            onSave = { number ->
                scope.launch {
                    senderNumberPreferences.saveSenderNumber(number)
                }
                showSmsSettingsDialog = false
            },
            onDetectNumber = {
                senderNumberPreferences.detectSimNumber()
            }
        )
    }
    
    // Update selected item when UI state changes
    LaunchedEffect(uiState.students) {
        selectedStudentItem?.let { currentSelected ->
            val updatedItem = uiState.students.find { it.student.id == currentSelected.student.id }
            if (updatedItem != null && updatedItem != currentSelected) {
                selectedStudentItem = updatedItem
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    SmsSettingsButton(
                        onClick = { showSmsSettingsDialog = true }
                    )
                    ThemeToggleButton(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = uiState.snackbarMessage is SnackbarMessage.Error
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        Color(0xFF4CAF50) // Success green
                    },
                    contentColor = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        Color.White
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { viewModel.clearSelection() }
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.students.isEmpty()) {
                EmptyAttendanceView(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stylish Date Header
                    item {
                        DateHeaderCard(date = uiState.todayDate)
                    }
                    
                    // Student count
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.students.size} Students",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Summary chips
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val presentCount = uiState.students.count { it.status == AttendanceStatus.PRESENT }
                                val absentCount = uiState.students.count { it.status == AttendanceStatus.ABSENT }
                                
                                if (presentCount > 0) {
                                    MiniChip(text = "$presentCount ✓", color = PresentColor)
                                }
                                if (absentCount > 0) {
                                    MiniChip(text = "$absentCount ✗", color = AbsentColor)
                                }
                            }
                        }
                    }
                    
                    items(
                        items = uiState.students,
                        key = { it.student.id }
                    ) { item ->
                        StudentAttendanceCard(
                            item = item,
                            onClick = { selectedStudentItem = item }
                        )
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun DateHeaderCard(date: String) {
    val calendar = Calendar.getInstance()
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
    val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(calendar.time)
    val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
    val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dayOfWeek,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$month $dayOfMonth, $year",
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📅 Today's Attendance",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Day circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayOfMonth,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MiniChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun EmptyAttendanceView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Students Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add students in the Students tab to start taking attendance",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onNavigateToAddStudent: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToManageStudents: () -> Unit,
    viewModel: AttendanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // SMS Permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denied
        }
    }
    
    // Request SMS permission on first load
    LaunchedEffect(Unit) {
        if (!SmsHelper.hasSmSPermission(context)) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Attendance",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.todayDate,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToManageStudents) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "Manage Students"
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddStudent,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { viewModel.clearSelection() }
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.students.isEmpty()) {
                EmptyStudentsList(
                    modifier = Modifier.align(Alignment.Center),
                    onAddStudent = onNavigateToAddStudent
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.students,
                        key = { it.student.id }
                    ) { item ->
                        StudentAttendanceCard(
                            item = item,
                            onClick = { /* Legacy screen - no action */ }
                        )
                    }
                    
                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
            
            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceCard(
    item: StudentAttendanceItem,
    onClick: () -> Unit
) {
    // Determine status color based on attendance status
    val statusColor = when (item.status) {
        AttendanceStatus.NOT_MARKED -> MaterialTheme.colorScheme.onSurfaceVariant
        AttendanceStatus.PRESENT -> PresentColor
        AttendanceStatus.ABSENT -> AbsentColor
    }
    
    val statusText = when (item.status) {
        AttendanceStatus.NOT_MARKED -> "Not Marked"
        AttendanceStatus.PRESENT -> "Present"
        AttendanceStatus.ABSENT -> "Absent"
    }
    
    val statusIcon = when (item.status) {
        AttendanceStatus.NOT_MARKED -> null
        AttendanceStatus.PRESENT -> Icons.Default.Check
        AttendanceStatus.ABSENT -> Icons.Default.Close
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.student.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.student.parentPhone,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (statusIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = statusColor
                        )
                    }
                }
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }
    }
}

/**
 * Attendance Dialog - shown when tapping a student
 */
@Composable
fun AttendanceDialog(
    item: StudentAttendanceItem,
    onDismiss: () -> Unit,
    onPresentTap: () -> Unit,
    onAbsentTap: () -> Unit,
    onConfirmed: () -> Unit
) {
    val pendingStatus = item.pendingStatus
    
    // Auto-close dialog when attendance is confirmed (not pending anymore)
    LaunchedEffect(item.status, pendingStatus) {
        if (item.status != AttendanceStatus.NOT_MARKED && pendingStatus == null) {
            // Status was just confirmed
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Student name
                Text(
                    text = item.student.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Phone number
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.student.parentPhone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Current status display
                val currentStatusColor = when (item.status) {
                    AttendanceStatus.NOT_MARKED -> MaterialTheme.colorScheme.onSurfaceVariant
                    AttendanceStatus.PRESENT -> PresentColor
                    AttendanceStatus.ABSENT -> AbsentColor
                }
                
                val currentStatusText = when (item.status) {
                    AttendanceStatus.NOT_MARKED -> "Not Marked"
                    AttendanceStatus.PRESENT -> "Present"
                    AttendanceStatus.ABSENT -> "Absent"
                }
                
                Text(
                    text = "Current: $currentStatusText",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = currentStatusColor
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Attendance buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Present button
                    AttendanceDialogButton(
                        text = if (pendingStatus == AttendanceStatus.PRESENT) "Confirm" else "Present",
                        color = PresentColor,
                        enabled = true,
                        isHighlighted = item.status == AttendanceStatus.PRESENT,
                        isPending = pendingStatus == AttendanceStatus.PRESENT,
                        onClick = {
                            onPresentTap()
                            if (pendingStatus == AttendanceStatus.PRESENT) {
                                onConfirmed()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Absent button
                    AttendanceDialogButton(
                        text = if (pendingStatus == AttendanceStatus.ABSENT) "Confirm" else "Absent",
                        color = AbsentColor,
                        enabled = true,
                        isHighlighted = item.status == AttendanceStatus.ABSENT,
                        isPending = pendingStatus == AttendanceStatus.ABSENT,
                        onClick = {
                            onAbsentTap()
                            if (pendingStatus == AttendanceStatus.ABSENT) {
                                onConfirmed()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Pending status hint
                if (pendingStatus != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap again to confirm ${pendingStatus.name.lowercase()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cancel button
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Close",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceDialogButton(
    text: String,
    color: Color,
    enabled: Boolean,
    isHighlighted: Boolean,
    isPending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isHighlighted -> color
        isPending -> color.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isHighlighted -> Color.White
        isPending -> color
        enabled -> color
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val disabledColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = disabledColor.copy(alpha = 0.1f),
            disabledContentColor = disabledColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPending) 2.dp else 1.dp,
            color = if (enabled) color else disabledColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isPending || isHighlighted) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

@Composable
fun EmptyStudentsList(
    modifier: Modifier = Modifier,
    onAddStudent: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Students Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first student to start taking attendance",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddStudent) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Student")
        }
    }
}
