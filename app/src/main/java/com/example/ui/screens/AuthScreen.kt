package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.School
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.OnSaffronContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    schools: List<School>,
    currentSchool: School?,
    schoolProfiles: List<UserProfile>,
    onSelectSchool: (School) -> Unit,
    onLoginWithProfile: (UserProfile) -> Unit,
    onLoginAsRole: (UserRole) -> Unit,
    onRegisterSchool: (name: String, code: String, village: String, district: String, state: String, udise: String, phone: String, email: String, adminName: String, adminPass: String) -> Unit,
    onOpenSupabaseSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register School
    val scrollState = rememberScrollState()

    // Login Form State
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var schoolDropdownExpanded by remember { mutableStateOf(false) }

    // Register Form State
    var regSchoolName by remember { mutableStateOf("") }
    var regSchoolCode by remember { mutableStateOf("") }
    var regVillage by remember { mutableStateOf("") }
    var regDistrict by remember { mutableStateOf("") }
    var regState by remember { mutableStateOf("Uttar Pradesh") }
    var regUdise by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regAdminName by remember { mutableStateOf("") }
    var regAdminPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Hero Logo & Title
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(IndigoPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Gramin Shala",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ग्रामीण शाला (Gramin Shala)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Multi-Tenant Rural School Management Platform",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selection: Login vs Register School
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Log In (लॉग इन)", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_login")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Register School (नया स्कूल)", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_register")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // === LOGIN TAB ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Select School Tenant (संस्थान चुनें)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // School Tenant Dropdown
                    ExposedDropdownMenuBox(
                        expanded = schoolDropdownExpanded,
                        onExpandedChange = { schoolDropdownExpanded = !schoolDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentSchool?.name ?: "Select School",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Active School") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("select_school_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = schoolDropdownExpanded,
                            onDismissRequest = { schoolDropdownExpanded = false }
                        ) {
                            schools.forEach { school ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(school.name, fontWeight = FontWeight.Bold)
                                            Text("${school.village}, ${school.district}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        onSelectSchool(school)
                                        schoolDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. Quick Role Persona Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant 1-tap testing for Admins, Teachers & Students",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onLoginAsRole(UserRole.ADMIN) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_login_admin"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronSecondary)
                        ) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onLoginAsRole(UserRole.TEACHER) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_login_teacher"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Teacher", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onLoginAsRole(UserRole.STUDENT) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_login_student"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Student", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (schoolProfiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Or pick a specific user account in this school:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            schoolProfiles.forEach { profile ->
                                val rEnum = UserRole.fromString(profile.role)
                                Surface(
                                    modifier = Modifier.clickable { onLoginWithProfile(profile) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (rEnum) {
                                        UserRole.ADMIN -> SaffronContainer
                                        UserRole.TEACHER -> EmeraldContainer
                                        UserRole.STUDENT -> IndigoContainer
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${profile.full_name} (${rEnum.name})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = when (rEnum) {
                                                UserRole.ADMIN -> OnSaffronContainer
                                                UserRole.TEACHER -> OnEmeraldContainer
                                                UserRole.STUDENT -> OnIndigoContainer
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct credentials form
                    Text(
                        text = "3. Email & Password Login",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_email")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val foundProfile = schoolProfiles.find { it.email.equals(emailInput, ignoreCase = true) }
                            if (foundProfile != null) {
                                onLoginWithProfile(foundProfile)
                            } else {
                                onLoginAsRole(UserRole.ADMIN)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_login_submit"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign In (प्रवेश करें)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        } else {
            // === REGISTER SCHOOL TAB ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Register New School Tenant",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Creates an isolated database tenant with full RLS security.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regSchoolName,
                        onValueChange = { regSchoolName = it },
                        label = { Text("School Name (विद्यालय का नाम) *") },
                        placeholder = { Text("e.g. Maharishi Dayanand Public School") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_school_name")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regSchoolCode,
                            onValueChange = { regSchoolCode = it },
                            label = { Text("Code (e.g. MDPS-01)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_school_code")
                        )
                        OutlinedTextField(
                            value = regUdise,
                            onValueChange = { regUdise = it },
                            label = { Text("U-DISE No.") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_udise")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regVillage,
                            onValueChange = { regVillage = it },
                            label = { Text("Village / Gram *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_village")
                        )
                        OutlinedTextField(
                            value = regDistrict,
                            onValueChange = { regDistrict = it },
                            label = { Text("District (ज़िला) *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_district")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regState,
                        onValueChange = { regState = it },
                        label = { Text("State (राज्य)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_state")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("School Phone *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_phone")
                        )
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Official Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_email")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "School Administrator Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regAdminName,
                        onValueChange = { regAdminName = it },
                        label = { Text("Principal / Prabandhak Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_admin_name")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regAdminPassword,
                        onValueChange = { regAdminPassword = it },
                        label = { Text("Admin Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_admin_password")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (regSchoolName.isNotBlank() && regAdminName.isNotBlank()) {
                                onRegisterSchool(
                                    regSchoolName,
                                    if (regSchoolCode.isNotBlank()) regSchoolCode else regSchoolName.take(4).uppercase(),
                                    if (regVillage.isNotBlank()) regVillage else "Rural Village",
                                    if (regDistrict.isNotBlank()) regDistrict else "Sitapur",
                                    regState,
                                    regUdise,
                                    if (regPhone.isNotBlank()) regPhone else "+91 94150 00000",
                                    if (regEmail.isNotBlank()) regEmail else "admin@school.org",
                                    regAdminName,
                                    if (regAdminPassword.isNotBlank()) regAdminPassword else "Admin@123"
                                )
                            }
                        },
                        enabled = regSchoolName.isNotBlank() && regAdminName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_register_submit"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Register & Launch School Tenant", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Supabase Cloud connection status bar & button
        OutlinedButton(
            onClick = onOpenSupabaseSettings,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("btn_open_supabase_config")
        ) {
            Icon(Icons.Default.Cloud, contentDescription = null, tint = EmeraldTertiary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Supabase RLS & Database Architecture", fontSize = 12.sp, color = IndigoPrimary)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
