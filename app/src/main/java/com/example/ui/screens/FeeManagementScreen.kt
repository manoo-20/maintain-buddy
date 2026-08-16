package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.components.formatIndianRupees
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.OnRoseContainer
import com.example.ui.theme.OnSaffronContainer
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeManagementScreen(
    currentSchool: School?,
    classes: List<SchoolClass>,
    students: List<Student>,
    fees: List<FeeStructure>,
    payments: List<FeePayment>,
    onRecordPayment: (feeId: String, studentId: String, amount: Double, paymentMode: String, date: String, notes: String, (FeePayment) -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf<String?>(null) }
    var classFilterExpanded by remember { mutableStateOf(false) }

    // Dialog state for collecting fees
    var activeStudentForPayment by remember { mutableStateOf<Pair<Student, FeeStructure>?>(null) }
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var paymentModeExpanded by remember { mutableStateOf(false) }
    var paymentNotes by remember { mutableStateOf("") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var paymentDate by remember { mutableStateOf(todayStr) }

    // Generated receipt preview dialog
    var activeReceiptPreview by remember { mutableStateOf<FeePayment?>(null) }

    // Totals
    val totalExpected = remember(fees) { fees.sumOf { it.netPayable } }
    val totalCollected = remember(payments) { payments.sumOf { it.amount_paid } }
    val totalOutstanding = (totalExpected - totalCollected).coerceAtLeast(0.0)

    val filteredList = remember(students, fees, payments, searchQuery, selectedClassFilter) {
        students.filter { std ->
            val matchQuery = searchQuery.isBlank() ||
                    std.full_name.contains(searchQuery, ignoreCase = true) ||
                    std.roll_no.contains(searchQuery, ignoreCase = true) ||
                    std.admission_no.contains(searchQuery, ignoreCase = true)
            val matchClass = selectedClassFilter == null || std.class_id == selectedClassFilter
            matchQuery && matchClass
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Fee Management & Receipts (शुल्क प्रबंधन)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldTertiary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Metrics Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(IndigoContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("Total Fees (कुल)", style = MaterialTheme.typography.labelSmall)
                            Text(formatIndianRupees(totalExpected), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(EmeraldContainer.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("Collected (प्राप्त)", style = MaterialTheme.typography.labelSmall)
                            Text(formatIndianRupees(totalCollected), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(RoseContainer.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("Outstanding (बकाया)", style = MaterialTheme.typography.labelSmall)
                            Text(formatIndianRupees(totalOutstanding), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoseAlert)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search & Class filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by name or roll no...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("input_fee_search")
                        )

                        ExposedDropdownMenuBox(
                            expanded = classFilterExpanded,
                            onExpandedChange = { classFilterExpanded = !classFilterExpanded },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            val selectedClassName = classes.find { it.id == selectedClassFilter }?.displayName ?: "All Classes"
                            OutlinedTextField(
                                value = selectedClassName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Class") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classFilterExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = classFilterExpanded,
                                onDismissRequest = { classFilterExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Classes") },
                                    onClick = {
                                        selectedClassFilter = null
                                        classFilterExpanded = false
                                    }
                                )
                                classes.forEach { sc ->
                                    DropdownMenuItem(
                                        text = { Text(sc.displayName) },
                                        onClick = {
                                            selectedClassFilter = sc.id
                                            classFilterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Student Fee List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { std ->
                    val stdClass = classes.find { it.id == std.class_id }
                    val stdFee = fees.find { it.student_id == std.id } ?: FeeStructure(
                        school_id = std.school_id,
                        student_id = std.id,
                        academic_year_id = "default",
                        total_amount = 6500.0,
                        discount_amount = 0.0
                    )
                    val stdPayments = payments.filter { it.student_id == std.id }
                    val paidAmount = stdPayments.sumOf { it.amount_paid }
                    val remainingDues = (stdFee.netPayable - paidAmount).coerceAtLeast(0.0)

                    val isPaidFull = remainingDues <= 0
                    val isPartial = paidAmount > 0 && remainingDues > 0

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isPaidFull) EmeraldContainer else if (isPartial) SaffronContainer else RoseContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = std.roll_no,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isPaidFull) OnEmeraldContainer else if (isPartial) OnSaffronContainer else OnRoseContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(std.full_name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            text = "${stdClass?.displayName ?: "Class"} • S/O ${std.guardian_name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isPaidFull) EmeraldContainer else if (isPartial) SaffronContainer else RoseContainer
                                ) {
                                    Text(
                                        text = if (isPaidFull) "PAID (पूर्ण)" else if (isPartial) "PARTIAL (आंशिक)" else "DUE (बकाया)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isPaidFull) OnEmeraldContainer else if (isPartial) OnSaffronContainer else OnRoseContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Fee: ${formatIndianRupees(stdFee.netPayable)} | Paid: ${formatIndianRupees(paidAmount)}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "Balance Due: ${formatIndianRupees(remainingDues)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingDues > 0) RoseAlert else EmeraldTertiary
                                    )
                                }

                                Button(
                                    onClick = {
                                        activeStudentForPayment = Pair(std, stdFee)
                                        paymentAmountInput = if (remainingDues > 0) remainingDues.toInt().toString() else "1000"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (remainingDues > 0) EmeraldTertiary else IndigoPrimary
                                    ),
                                    modifier = Modifier.testTag("btn_collect_fee_${std.roll_no}")
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Collect Fee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Record Payment Dialog
        if (activeStudentForPayment != null) {
            val (std, stdFee) = activeStudentForPayment!!
            val stdPayments = payments.filter { it.student_id == std.id }
            val paidAmount = stdPayments.sumOf { it.amount_paid }
            val remainingDues = (stdFee.netPayable - paidAmount).coerceAtLeast(0.0)

            AlertDialog(
                onDismissRequest = { activeStudentForPayment = null },
                title = {
                    Text(
                        text = "Fee Payment for ${std.full_name}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Roll No: ${std.roll_no} • Current Balance: ${formatIndianRupees(remainingDues)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RoseAlert
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = paymentAmountInput,
                            onValueChange = { paymentAmountInput = it },
                            label = { Text("Amount Paid (₹) *") },
                            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_payment_amount")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Payment Mode Dropdown
                        ExposedDropdownMenuBox(
                            expanded = paymentModeExpanded,
                            onExpandedChange = { paymentModeExpanded = !paymentModeExpanded }
                        ) {
                            OutlinedTextField(
                                value = paymentMode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Payment Mode (माध्यम)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentModeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = paymentModeExpanded,
                                onDismissRequest = { paymentModeExpanded = false }
                            ) {
                                listOf("Cash (नकद)", "UPI / QR Code", "Bank Transfer", "Cheque").forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode) },
                                        onClick = {
                                            paymentMode = mode.split(" ").first()
                                            paymentModeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = paymentDate,
                            onValueChange = { paymentDate = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = paymentNotes,
                            onValueChange = { paymentNotes = it },
                            label = { Text("Notes / Remarks (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = paymentAmountInput.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onRecordPayment(
                                    stdFee.id,
                                    std.id,
                                    amount,
                                    paymentMode.lowercase(),
                                    paymentDate,
                                    paymentNotes
                                ) { generatedReceipt ->
                                    activeStudentForPayment = null
                                    activeReceiptPreview = generatedReceipt
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                        modifier = Modifier.testTag("btn_confirm_fee_payment")
                    ) {
                        Text("Record & Generate Receipt", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeStudentForPayment = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Receipt Print/Share Preview Dialog
        if (activeReceiptPreview != null) {
            val receipt = activeReceiptPreview!!
            val rStudent = students.find { it.id == receipt.student_id }
            val rClass = classes.find { it.id == rStudent?.class_id }

            AlertDialog(
                onDismissRequest = { activeReceiptPreview = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Official Fee Receipt", fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldTertiary)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IndigoContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = currentSchool?.name ?: "School Name",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                        Text(
                            text = "${currentSchool?.village}, ${currentSchool?.district} (${currentSchool?.state})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "UDISE: ${currentSchool?.udise_number ?: "N/A"} • Phone: ${currentSchool?.phone ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Receipt No: ${receipt.receipt_no}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text("Date: ${receipt.payment_date}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Student: ${rStudent?.full_name ?: "N/A"}", fontWeight = FontWeight.Bold)
                        Text("Class: ${rClass?.displayName ?: "Class"} • Roll No: ${rStudent?.roll_no ?: "01"}", style = MaterialTheme.typography.bodySmall)
                        Text("Guardian: ${rStudent?.guardian_name ?: "N/A"}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount Paid:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(formatIndianRupees(receipt.amount_paid), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                        }

                        Text("Payment Mode: ${receipt.payment_mode.uppercase()}", style = MaterialTheme.typography.bodySmall)
                        if (receipt.notes.isNotBlank()) {
                            Text("Remarks: ${receipt.notes}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Authorized Signatory • Gramin Shala Certified",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { activeReceiptPreview = null },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
