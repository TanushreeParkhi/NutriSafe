package com.nutrisafe.app.ui.screens

import com.nutrisafe.app.ui.components.TinyIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisafe.app.ui.components.GradientButton
import com.nutrisafe.app.ui.components.NutriSafeLogoTile
import com.nutrisafe.app.ui.components.PrivacyGuaranteeCard
import com.nutrisafe.app.ui.theme.Ink
import com.nutrisafe.app.ui.theme.Line
import com.nutrisafe.app.ui.theme.Muted
import com.nutrisafe.app.ui.theme.Surface
import com.nutrisafe.app.viewmodel.NutriSafeViewModel

@Composable
fun AuthScreen(viewModel: NutriSafeViewModel) {
    var email by rememberSaveable { mutableStateOf("admin@nutrisafe.com") }
    var password by rememberSaveable { mutableStateOf("password123") }
    var isSignUp by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color(0xFFF9ECEF))
        )
        Box(
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(Color(0xFFFFF5E8))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NutriSafeLogoTile(size = 80.dp)
            Spacer(Modifier.height(26.dp))
            Text("NutriSafe", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text("Personalized Nutrition. Zero Cloud Data.", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(48.dp))
            AuthFieldLabel("EMAIL")
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                leadingIcon = { Icon(TinyIcons.Mail, null, tint = Muted) },
                placeholder = { Text("e.g. admin@nutrisafe.com", color = Muted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Line,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(Modifier.height(24.dp))
            AuthFieldLabel("PASSWORD")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                leadingIcon = { Icon(TinyIcons.Lock, null, tint = Muted) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Line,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            viewModel.authError?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Color(0xFFE5484D), textAlign = TextAlign.Center, fontSize = 12.sp)
            }
            Spacer(Modifier.height(18.dp))
            GradientButton(
                if (isSignUp) "Create Account" else "Sign In",
                onClick = {
                    if (isSignUp) viewModel.signUp(email, password) else viewModel.signIn(email, password)
                }
            )
            Spacer(Modifier.height(34.dp))
            Text(
                if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                modifier = Modifier.clickable { isSignUp = !isSignUp },
                color = Muted,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(52.dp))
            PrivacyGuaranteeCard()
        }
    }
}

@Composable
private fun AuthFieldLabel(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 9.dp),
        color = Muted,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold
    )
}


