package com.nutrisafe.app.ui.screens

import com.nutrisafe.app.ui.components.TinyIcons

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutrisafe.app.ui.components.PrivacyGuaranteeCard
import com.nutrisafe.app.ui.components.SectionTitle
import com.nutrisafe.app.ui.components.SensitiveValue
import com.nutrisafe.app.ui.components.SoftCard
import com.nutrisafe.app.ui.theme.BrandMagenta
import com.nutrisafe.app.ui.theme.Ink
import com.nutrisafe.app.ui.theme.Line
import com.nutrisafe.app.ui.theme.Muted
import com.nutrisafe.app.viewmodel.NutriSafeViewModel

@Composable
fun PrivacyScreen(viewModel: NutriSafeViewModel, padding: PaddingValues, onDeleteAccount: () -> Unit) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val records by viewModel.healthRecords.collectAsStateWithLifecycle()
    var healthText by rememberSaveable { mutableStateOf("") }
    val prescriptionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importPrescription(uri)
        } else {
            viewModel.showNotice("Document import cancelled.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text("Security & Privacy", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text("Configure your local health vault", color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(TinyIcons.Lock, null, tint = BrandMagenta)
            Spacer(Modifier.width(8.dp))
            SectionTitle("Privacy Controls")
        }
        Spacer(Modifier.height(12.dp))
        ToggleCard(
            title = "Biometric Privacy Lock",
            subtitle = "Require fingerprint or PIN to open NutriSafe.",
            checked = preferences.biometricLock,
            onCheckedChange = viewModel::setBiometricLock
        )
        Spacer(Modifier.height(12.dp))
        ToggleCard(
            title = "Sensitive Info Blur",
            subtitle = "Blur calories and stats unless tapped.",
            checked = preferences.sensitiveBlur,
            onCheckedChange = viewModel::setSensitiveBlur
        )
        Spacer(Modifier.height(30.dp))
        SectionTitle("Dietary Vault")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            DietChoice("Veg", preferences.dietVault, Modifier.weight(1f), viewModel::setDietVault)
            DietChoice("Non-Veg", preferences.dietVault, Modifier.weight(1f), viewModel::setDietVault)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            DietChoice("Veg+Egg", preferences.dietVault, Modifier.weight(1f), viewModel::setDietVault)
            DietChoice("Jain", preferences.dietVault, Modifier.weight(1f), viewModel::setDietVault)
        }
        Spacer(Modifier.height(34.dp))
        SectionTitle("Health History") {
            Row(
                modifier = Modifier.clickable {
                    prescriptionLauncher.launch(arrayOf("application/pdf", "image/*", "text/plain"))
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(TinyIcons.UploadFile, null, tint = BrandMagenta)
                Spacer(Modifier.width(4.dp))
                Text("Import Prescription", color = BrandMagenta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = healthText,
                onValueChange = { healthText = it },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                placeholder = { Text("Add medical record...", color = Muted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Line,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    viewModel.addHealthRecord(healthText)
                    healthText = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandMagenta),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(56.dp)
            ) { Text("Add", color = Color.White, fontWeight = FontWeight.ExtraBold) }
        }
        Spacer(Modifier.height(12.dp))
        records.forEach { record ->
            SoftCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(record.label, color = Ink, fontWeight = FontWeight.Bold)
                        Text(record.type.uppercase(), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    IconButton(onClick = { viewModel.deleteHealthRecord(record.id) }) {
                        Icon(TinyIcons.Delete, null, tint = Color(0xFFFF3B30))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(24.dp))
        PrivacyGuaranteeCard(danger = true, onWipe = onDeleteAccount)
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun ToggleCard(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(subtitle, color = Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun DietChoice(label: String, selected: String, modifier: Modifier, onClick: (String) -> Unit) {
    val active = selected == label
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(2.dp, if (active) BrandMagenta else Line, RoundedCornerShape(14.dp))
            .clickable { onClick(label) },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (active) BrandMagenta else Color(0xFF596273), fontWeight = FontWeight.ExtraBold)
    }
}

