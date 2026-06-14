package com.priveat.app.ui.screens

import com.priveat.app.ui.components.TinyIcons

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.priveat.app.data.model.MealEntity
import com.priveat.app.ui.components.DashedSnapCard
import com.priveat.app.ui.components.GoalChip
import com.priveat.app.ui.components.InfoPill
import com.priveat.app.ui.components.MacroDots
import com.priveat.app.ui.components.MealThumbnail
import com.priveat.app.ui.components.SectionTitle
import com.priveat.app.ui.components.SensitiveValue
import com.priveat.app.ui.components.SoftCard
import com.priveat.app.ui.theme.BrandMagenta
import com.priveat.app.ui.theme.Ink
import com.priveat.app.ui.theme.Line
import com.priveat.app.ui.theme.Muted
import com.priveat.app.ui.theme.Success
import com.priveat.app.viewmodel.PrivEatViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MealTrackerScreen(viewModel: PrivEatViewModel, padding: PaddingValues) {
    val context = LocalContext.current
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showMealPicker by remember { mutableStateOf(false) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.analyzeMealFromGallery(uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        if (captured) {
            viewModel.analyzeMealFromCamera(pendingCameraUri)
        } else {
            viewModel.showNotice("Camera capture cancelled.")
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = viewModel.createMealCaptureUri()
            pendingCameraUri = uri
            if (uri != null) cameraLauncher.launch(uri) else viewModel.showNotice("Camera could not prepare a local image file.")
        } else {
            viewModel.showNotice("Camera permission is needed to snap a meal.")
        }
    }
    val launchCamera = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = viewModel.createMealCaptureUri()
            pendingCameraUri = uri
            if (uri != null) cameraLauncher.launch(uri) else viewModel.showNotice("Camera could not prepare a local image file.")
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    val consumed = meals.sumOf { it.calories }
    val dailyGoal = 2200
    val remaining = (dailyGoal - consumed).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Meal Log", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Text("Tracking your nutrition", color = Muted, fontSize = 15.sp)
            }
            InfoPill("AI Enabled", color = Color(0xFFFF5A1F))
        }
        Spacer(Modifier.height(24.dp))
        SoftCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GoalNumber("$consumed", "CONSUMED", preferences.sensitiveBlur)
                GoalNumber("$dailyGoal", "DAILY GOAL", preferences.sensitiveBlur, faded = true)
                GoalNumber("$remaining", "REMAINING", preferences.sensitiveBlur, color = BrandMagenta)
            }
        }
        Spacer(Modifier.height(22.dp))
        DashedSnapCard { showMealPicker = true }
        if (viewModel.isAnalyzing) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandMagenta, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Analyzing locally with AI fallback...", color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        StorageControls(viewModel, preferences.keepRawImages)
        Spacer(Modifier.height(28.dp))
        SectionTitle("Daily History") {
            Text("(${meals.size})", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        if (meals.isEmpty()) {
            SoftCard(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(TinyIcons.Restaurant, null, tint = Muted, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("No meals yet", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text("Snap a meal or upload from gallery to start your log.", color = Muted, fontSize = 12.sp)
                }
            }
        } else {
            meals.forEach { meal ->
            MealDetailCard(meal, blur = preferences.sensitiveBlur, onDelete = { viewModel.deleteMeal(meal.id) })
            Spacer(Modifier.height(14.dp))
        }
        }
        Spacer(Modifier.height(84.dp))
    }

    if (showMealPicker) {
        AlertDialog(
            onDismissRequest = { showMealPicker = false },
            title = { Text("Add meal", color = Ink, fontWeight = FontWeight.ExtraBold) },
            text = { Text("Choose a source for PrivEat's local AI analysis.") },
            confirmButton = {
                TextButton(onClick = {
                    showMealPicker = false
                    launchCamera()
                }) { Text("Camera", color = BrandMagenta, fontWeight = FontWeight.ExtraBold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMealPicker = false
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Gallery", color = Muted, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (viewModel.isAnalyzing) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("AI is analyzing freshness & risk...", color = Ink, fontWeight = FontWeight.ExtraBold) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = BrandMagenta, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    Spacer(Modifier.width(14.dp))
                    Text("Checking nutrition, freshness, storage risk, and safety score.")
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun GoalNumber(value: String, label: String, blur: Boolean, faded: Boolean = false, color: Color = Ink) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SensitiveValue(value, blur = blur, color = if (faded) Color(0xFFC8CEDA) else color, fontSize = 24)
        Spacer(Modifier.height(8.dp))
        Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun StorageControls(viewModel: PrivEatViewModel, keepRawImages: Boolean) {
    SoftCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Storage Context", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("home", "street", "packaged").forEach { value ->
                    GoalChip(value.replaceFirstChar { it.uppercase() }, viewModel.sourceType == value) { viewModel.updateSourceType(value) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Outside ${viewModel.timeOutsideHours.roundToInt()}h", color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(if (viewModel.refrigerated) "Refrigerated" else "Room temp", color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Slider(value = viewModel.timeOutsideHours, onValueChange = viewModel::setTimeOutside, valueRange = 0f..12f, steps = 11)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Keep raw images local-only", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Switch(checked = keepRawImages, onCheckedChange = viewModel::setKeepRawImages)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Refrigerated before scan", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Switch(checked = viewModel.refrigerated, onCheckedChange = viewModel::updateRefrigerated)
            }
        }
    }
}

@Composable
fun MealDetailCard(meal: MealEntity, blur: Boolean, onDelete: () -> Unit = {}) {
    val time = DateTimeFormatter.ofPattern("hh:mm a").format(
        Instant.ofEpochMilli(meal.createdAt).atZone(ZoneId.systemDefault()).toLocalTime()
    )
    SoftCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                MealThumbnail(meal.imageUri, Modifier.size(80.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(meal.name, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, lineHeight = 22.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SensitiveValue("${meal.calories} kcal", blur = blur, color = BrandMagenta, fontSize = 14)
                        Text("  $time", color = Muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE9FFF2))
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text("Risk Score: ${meal.riskScore}/100", color = Success, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
                Icon(TinyIcons.KeyboardArrowUp, null, tint = Muted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Icon(
                    TinyIcons.DeleteOutline,
                    null,
                    tint = Color(0xFFD4DAE4),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onDelete)
                )
            }
            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F2F6)))
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailColumn(
                    title = "FRESHNESS",
                    text = meal.freshnessNotes,
                    modifier = Modifier.weight(1f),
                    icon = { Icon(TinyIcons.AccessTime, null, tint = Muted, modifier = Modifier.size(13.dp)) }
                )
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(TinyIcons.Science, null, tint = Muted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("COLORINGS", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(meal.additives, color = Success, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    MacroDots(meal.proteinGrams, meal.carbsGrams, meal.fatGrams, blur)
                }
            }
            Spacer(Modifier.height(14.dp))
            DetailColumn(
                title = "SHELF LIFE",
                text = meal.shelfLife,
                icon = { Icon(TinyIcons.CheckCircle, null, tint = Muted, modifier = Modifier.size(13.dp)) }
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFAFBFD))
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(TinyIcons.Shield, null, tint = Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(meal.safetyExplanation, color = Color(0xFF667085), fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun DetailColumn(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.width(4.dp))
            Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text(text, color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
    }
}


