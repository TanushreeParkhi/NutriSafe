package com.nutrisafe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisafe.app.ui.components.SoftCard
import com.nutrisafe.app.ui.components.TinyIcons
import com.nutrisafe.app.ui.theme.Ink
import com.nutrisafe.app.ui.theme.Muted
import com.nutrisafe.app.viewmodel.NutriSafeViewModel

@Composable
fun DeleteAccountScreen(
    viewModel: NutriSafeViewModel,
    padding: PaddingValues,
    onCancel: () -> Unit
) {
    var armed by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 24.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SoftCard(Modifier.fillMaxWidth(), color = Color(0xFFFFF1F0)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(TinyIcons.Shield, contentDescription = null, tint = Color(0xFFE5484D), modifier = Modifier.size(58.dp))
                Spacer(Modifier.height(18.dp))
                Text("Delete Account", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
                Text(
                    "This permanently erases all local NutriSafe caches: preferences, meal logs, health records, expert chat, timers, and authentication state.",
                    color = Color(0xFF8A2A20),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(24.dp))
                if (!armed) {
                    Button(
                        onClick = { armed = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5484D)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text("Yes, I understand the risks", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Button(
                        onClick = viewModel::wipeVaultAndLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC81E1E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text("Confirm Permanent Deletion", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Cancel", color = Muted, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
