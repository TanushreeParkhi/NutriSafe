package com.priveat.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.priveat.app.data.model.MealEntity
import com.priveat.app.ui.components.CalorieLineChart
import com.priveat.app.ui.components.MetricCard
import com.priveat.app.ui.components.SectionTitle
import com.priveat.app.ui.components.SensitiveValue
import com.priveat.app.ui.components.SoftCard
import com.priveat.app.ui.components.TinyIcons
import com.priveat.app.ui.theme.Blue
import com.priveat.app.ui.theme.BrandMagenta
import com.priveat.app.ui.theme.BrandOrange
import com.priveat.app.ui.theme.Ink
import com.priveat.app.ui.theme.Muted
import com.priveat.app.ui.theme.Success
import com.priveat.app.viewmodel.PrivEatViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun InsightsScreen(viewModel: PrivEatViewModel, padding: PaddingValues) {
    val report = viewModel.weeklyReport
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val calorieValues = dailyMealSums(meals) { it.calories }
    val proteinValues = dailyMealSums(meals) { it.proteinGrams.toInt() }
    val waterValues = List(6) { (preferences.waterGlasses - (5 - it)).coerceAtLeast(0) } + preferences.waterGlasses
    val waterLiters = preferences.waterGlasses * 0.25f
    val proteinToday = proteinValues.lastOrNull() ?: 0
    val energyToday = calorieValues.lastOrNull() ?: 0
    var selectedMetric by remember { mutableStateOf("Water") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Text("Statistics", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text("Your meal insights", color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(26.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricSelector("Water", String.format("%.1fL", waterLiters), selectedMetric == "Water", Blue, Modifier.weight(1f), onClick = {
                selectedMetric = "Water"
            }) {
                Icon(TinyIcons.LocalDrink, null, tint = Blue, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(12.dp))
            MetricSelector("Protein", "${proteinToday}g", selectedMetric == "Protein", BrandOrange, Modifier.weight(1f), onClick = {
                selectedMetric = "Protein"
            }) {
                Icon(TinyIcons.FitnessCenter, null, tint = BrandOrange, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(12.dp))
            MetricSelector("Energy", if (energyToday >= 1000) String.format("%.1fk", energyToday / 1000f) else energyToday.toString(), selectedMetric == "Energy", BrandMagenta, Modifier.weight(1f), onClick = {
                selectedMetric = "Energy"
            }) {
                Icon(TinyIcons.Bolt, null, tint = BrandMagenta, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        WaterInputCard(
            glasses = preferences.waterGlasses,
            onMinus = { viewModel.setWaterGlasses(preferences.waterGlasses - 1) },
            onPlus = { viewModel.setWaterGlasses(preferences.waterGlasses + 1) }
        )
        Spacer(Modifier.height(22.dp))
        when (selectedMetric) {
            "Water" -> WaterBarChart(waterValues)
            "Protein" -> CalorieLineChart(
                values = proteinValues,
                blur = preferences.sensitiveBlur,
                title = "Protein Count (g)",
                valuePrefix = "protein : ",
                valueSuffix = "g",
                lineColor = BrandOrange,
                fallback = List(7) { 0 }
            )
            else -> CalorieLineChart(calorieValues, blur = preferences.sensitiveBlur)
        }
        Spacer(Modifier.height(18.dp))
        InterpretationCard(selectedMetric, waterValues.lastOrNull() ?: 0, proteinToday, energyToday)
        Spacer(Modifier.height(22.dp))
        SectionTitle("Weekly Food Risk Report")
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InsightMetric("Street Food", report.streetFoodCount.toString(), BrandOrange, Modifier.weight(1f))
            InsightMetric("Ultra Processed", report.ultraProcessedCount.toString(), BrandMagenta, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InsightMetric("Microbial Risk", report.highMicrobialRiskMeals.toString(), Color(0xFFE5484D), Modifier.weight(1f))
            InsightMetric("Safety Avg", "${report.averageSafetyScore}/100", Success, Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Suggestions")
        Spacer(Modifier.height(12.dp))
        report.suggestions.forEach { suggestion ->
            SoftCard(Modifier.fillMaxWidth()) {
                Text(suggestion, color = Color(0xFF344054), fontSize = 14.sp, lineHeight = 20.sp)
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun MetricSelector(
    label: String,
    value: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(2.dp, if (selected) color else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        icon()
        Text(label.uppercase(), color = if (selected) Ink else Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = if (selected) Ink else Muted, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun WaterInputCard(glasses: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Water Today", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("1 glass = 250ml", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMinus) { Text("-", color = BrandMagenta, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) }
                SensitiveValue("$glasses", blur = false, color = Blue, fontSize = 26)
                IconButton(onClick = onPlus) { Text("+", color = BrandMagenta, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}

@Composable
private fun WaterBarChart(values: List<Int>) {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val chartValues = if (values.any { it > 0 }) values else listOf(8, 6, 10, 7, 5, 4, 9)
    SoftCard(Modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Water Intake (glasses)", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text("PAST 7 DAYS", color = Muted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Spacer(Modifier.height(18.dp))
            Canvas(Modifier.fillMaxWidth().height(190.dp)) {
                val max = (chartValues.maxOrNull() ?: 1).coerceAtLeast(10)
                val gap = size.width / (chartValues.size * 1.6f)
                val barWidth = gap * 0.72f
                val baseline = size.height - 32.dp.toPx()
                chartValues.forEachIndexed { index, value ->
                    val x = gap * 0.55f + index * gap * 1.6f
                    val height = (baseline - 10.dp.toPx()) * value / max
                    drawLine(
                        color = Blue,
                        start = Offset(x, baseline),
                        end = Offset(x, baseline - height),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { Text(it, color = Color(0xFF5E6675), fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun InterpretationCard(metric: String, water: Int, protein: Int, calories: Int) {
    val text = when (metric) {
        "Water" -> if (water >= 8) "Hydration looks solid today. Keep spacing glasses through the day instead of drinking all at once." else "Water intake is below target. Add ${8 - water} more glass${if (8 - water == 1) "" else "es"} today."
        "Protein" -> if (protein >= 50) "Protein intake is on track. Keep pairing protein with fiber for steadier energy." else "Protein is low today. Add dal, curd, egg, tofu, paneer, fish, or lean meat based on your vault."
        else -> if (calories > 2200) "Your calorie intake is above the daily target. Balance the next meal with protein, vegetables, and water." else "Energy intake is within target. Keep protein and hydration consistent."
    }
    SoftCard(Modifier.fillMaxWidth(), color = Color(0xFFFFF7E8)) {
        Text(text, color = Color(0xFF8A4B00), fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InsightMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    SoftCard(modifier) {
        Column {
            Text(label, color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        }
    }
}

private fun dailyMealSums(meals: List<MealEntity>, selector: (MealEntity) -> Int): List<Int> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    return (6 downTo 0).map { daysAgo ->
        val date = today.minusDays(daysAgo.toLong())
        meals.filter {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() == date
        }.sumOf(selector)
    }
}
