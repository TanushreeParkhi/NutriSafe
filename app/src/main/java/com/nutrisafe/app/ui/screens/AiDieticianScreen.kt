package com.nutrisafe.app.ui.screens

import com.nutrisafe.app.ui.components.TinyIcons

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutrisafe.app.ui.components.GoalChip
import com.nutrisafe.app.ui.components.GradientButton
import com.nutrisafe.app.ui.components.SoftCard
import com.nutrisafe.app.ui.components.dashedBorder
import com.nutrisafe.app.ui.theme.BrandMagenta
import com.nutrisafe.app.ui.theme.Ink
import com.nutrisafe.app.ui.theme.Line
import com.nutrisafe.app.ui.theme.Muted
import com.nutrisafe.app.viewmodel.NutriSafeViewModel

@Composable
fun AiDieticianScreen(viewModel: NutriSafeViewModel, padding: PaddingValues) {
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 26.dp, vertical = 22.dp)
    ) {
        Text("Ask NutriSafe AI", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text("Personalized diet plans using your history", color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(30.dp))
        Text("Set Your Goal", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        val goals = listOf("Weight Loss", "Muscle Gain", "Maintenance", "Endurance", "General Wellness")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                goals.take(2).forEach { GoalChip(it, viewModel.selectedGoal == it) { viewModel.setGoal(it) } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                goals.drop(2).take(2).forEach { GoalChip(it, viewModel.selectedGoal == it) { viewModel.setGoal(it) } }
            }
            Row { GoalChip(goals.last(), viewModel.selectedGoal == goals.last()) { viewModel.setGoal(goals.last()) } }
        }
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .dashedBorder(Line, 28.dp)
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(TinyIcons.Psychology, null, tint = Color.Black, modifier = Modifier.size(54.dp))
            Spacer(Modifier.height(18.dp))
            Text("Ready to plan?", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
            Text(
                "We'll look at your logged meals\nand dietary preferences to create a\nguide.",
                color = Muted,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(24.dp))
            if (viewModel.isGeneratingPlan) {
                CircularProgressIndicator(color = BrandMagenta)
            } else {
                GradientButton("Generate Plan", modifier = Modifier.fillMaxWidth(0.82f), onClick = viewModel::generatePlan)
            }
        }
        if (viewModel.generatedPlan.isNotBlank()) {
            Spacer(Modifier.height(22.dp))
            SoftCard(Modifier.fillMaxWidth()) {
                Column {
                    Text("Local Plan", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(viewModel.generatedPlan, color = Color(0xFF344054), fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            SoftCard(Modifier.fillMaxWidth(), color = Color(0xFFFFF7D6)) {
                Text(
                    "Medical reminder: review major diet changes with a qualified clinician, especially for diabetes, kidney issues, pregnancy, medication, or diagnosed conditions.",
                    color = Color(0xFF7A4B00),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            GradientButton("Regenerate Plan", onClick = viewModel::generatePlan)
        }
        if (meals.isEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("Meal history is empty.", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(92.dp))
    }
}

