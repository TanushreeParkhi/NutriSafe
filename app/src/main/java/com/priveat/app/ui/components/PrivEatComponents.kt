package com.priveat.app.ui.components

import com.priveat.app.ui.components.TinyIcons

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.priveat.app.R
import com.priveat.app.ui.theme.Blue
import com.priveat.app.ui.theme.BrandMagenta
import com.priveat.app.ui.theme.BrandOrange
import com.priveat.app.ui.theme.Card
import com.priveat.app.ui.theme.Ink
import com.priveat.app.ui.theme.Line
import com.priveat.app.ui.theme.Muted
import com.priveat.app.ui.theme.Success
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

fun brandGradient() = Brush.linearGradient(listOf(BrandMagenta, BrandOrange))

@Composable
fun PrivEatLogoTile(modifier: Modifier = Modifier, size: Dp = 52.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(brandGradient()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "P",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSize = (size.value * 0.46f).sp
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    showArrow: Boolean = true
) {
    Button(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = ButtonDefaults.ContentPadding,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(brandGradient()),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                if (showArrow) {
                    Spacer(Modifier.width(10.dp))
                    Icon(TinyIcons.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = Card,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content = { Box(Modifier.padding(18.dp)) { content() } }
    )
}

@Composable
fun SensitiveValue(
    text: String,
    blur: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Ink,
    fontSize: Int = 20,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    var revealed by remember(text, blur) { mutableStateOf(false) }
    val displayText = if (blur && !revealed) "••••" else text
    Text(
        text = displayText,
        modifier = modifier
            .clickable(enabled = blur) { revealed = !revealed },
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    selected: Boolean = false,
    icon: @Composable () -> Unit
) {
    val borderColor = if (selected) BrandMagenta else Color.Transparent
    Column(
        modifier = Modifier
            .width(94.dp)
            .height(106.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
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
fun MealThumbnail(imageUri: String?, modifier: Modifier = Modifier) {
    if (imageUri.isNullOrBlank()) {
        Image(
            painter = painterResource(R.drawable.meal_rice_curry),
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            fallback = painterResource(R.drawable.meal_rice_curry),
            error = painterResource(R.drawable.meal_rice_curry)
        )
    }
}

@Composable
fun DashedSnapCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(208.dp)
            .clip(RoundedCornerShape(28.dp))
            .dashedBorder(Line, 28.dp)
            .clickable(onClick = onClick)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(TinyIcons.CameraAlt, contentDescription = null, tint = BrandMagenta, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(18.dp))
        Text("Snap your meal", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        Text(
            "AI will check freshness, risk,\nand nutrition automatically",
            color = Muted,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun GoalChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) brandGradient() else Brush.linearGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (selected) Color.Transparent else Line, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else Color(0xFF576173), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoPill(text: String, color: Color = BrandMagenta, background: Color = Color(0xFFFFF0DE)) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(TinyIcons.LocalFireDepartment, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
    }
}

@Composable
fun CalorieLineChart(
    values: List<Int>,
    blur: Boolean,
    modifier: Modifier = Modifier,
    title: String = "Calories Count (kcal)",
    valuePrefix: String = "calories : ",
    valueSuffix: String = "",
    lineColor: Color = BrandMagenta,
    fallback: List<Int> = listOf(1680, 1900, 1810, 2010, 1880, 2100, 1970)
) {
    val points = if (values.isEmpty()) fallback else {
        val recent = values.takeLast(7)
        fallback.take(7 - recent.size) + recent
    }
    val today = LocalDate.now()
    val labels = (6 downTo 0).map { daysAgo ->
        today.minusDays(daysAgo.toLong())
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
            .take(3)
    }
    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex) }
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text("PAST 7 DAYS", color = Muted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Spacer(Modifier.height(18.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().height(190.dp)) {
                val tooltipWidth = 132.dp
                val chartLeft = 10.dp
                val chartRight = maxWidth - 10.dp
                val pointX = chartLeft + (chartRight - chartLeft) * (selectedIndex / points.lastIndex.coerceAtLeast(1).toFloat())
                val tooltipX = (pointX - tooltipWidth / 2).coerceIn(0.dp, maxWidth - tooltipWidth)
                androidx.compose.foundation.Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val left = 10.dp.toPx()
                                val right = size.width.toFloat() - 10.dp.toPx()
                                val step = (right - left) / points.lastIndex.coerceAtLeast(1).toFloat()
                                selectedIndex = ((offset.x - left) / step).roundToInt()
                                    .coerceIn(0, points.lastIndex)
                            }
                        }
                ) {
                    val left = 10.dp.toPx()
                    val top = 12.dp.toPx()
                    val right = size.width - 10.dp.toPx()
                    val bottom = size.height - 28.dp.toPx()
                    val min = (points.minOrNull() ?: 0) - 120
                    val max = (points.maxOrNull() ?: 1) + 120
                    repeat(4) { index ->
                        val y = top + (bottom - top) * index / 3f
                        drawLine(Color(0xFFEFF1F6), Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                    }
                    var previous: Offset? = null
                    points.forEachIndexed { index, value ->
                        val x = left + (right - left) * index / (points.lastIndex.coerceAtLeast(1)).toFloat()
                        val y = bottom - ((value - min).toFloat() / (max - min).toFloat()) * (bottom - top)
                        val current = Offset(x, y)
                        previous?.let { drawLine(lineColor, it, current, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round) }
                        drawCircle(
                            color = lineColor,
                            radius = if (index == selectedIndex) 7.dp.toPx() else 5.5.dp.toPx(),
                            center = current
                        )
                        previous = current
                    }
                }
                SoftCard(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = tooltipX, y = 72.dp)
                        .width(tooltipWidth),
                    color = Color.White
                ) {
                    Column {
                        Text(labels[selectedIndex], color = Ink, fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        SensitiveValue("$valuePrefix${points[selectedIndex]}$valueSuffix", blur = blur, color = lineColor, fontSize = 16, fontWeight = FontWeight.Normal)
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    labels.forEach {
                        Text(it, color = Color(0xFF5E6675), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MacroDots(protein: Float, carbs: Float, fat: Float, blur: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
        MacroDot("P", protein, Color(0xFFFF7B39), blur)
        MacroDot("C", carbs, Blue, blur)
        MacroDot("F", fat, Color(0xFFF2B21E), blur)
    }
}

@Composable
private fun MacroDot(label: String, value: Float, color: Color, blur: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Column {
            SensitiveValue("${value.toInt()}g", blur = blur, fontSize = 10, fontWeight = FontWeight.Bold, color = Ink)
            Text(label, color = Color(0xFF5B6680), fontSize = 10.sp)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        trailing?.invoke()
    }
}

@Composable
fun PrivacyGuaranteeCard(modifier: Modifier = Modifier, danger: Boolean = false, onWipe: (() -> Unit)? = null) {
    SoftCard(
        modifier = modifier.fillMaxWidth(),
        color = if (danger) Color(0xFFFFF5E8) else Color.White
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(TinyIcons.Info, contentDescription = null, tint = if (danger) Color(0xFFFF5A1F) else BrandMagenta, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Zero-Cloud Guarantee", color = if (danger) Color(0xFFFF5A1F) else Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Your profile, logs, and AI conversations are stored solely in your device's secure sandbox. PrivEat does not have servers that store your biometric or nutritional data.",
                color = if (danger) Color(0xFFD63B00) else Color(0xFF344054),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            if (onWipe != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "WIPE VAULT PERMANENTLY  >",
                    modifier = Modifier.clickable(onClick = onWipe),
                    color = Color(0xFFFF5A1F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        topLeft = Offset.Zero,
        size = Size(size.width, size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx(), radius.toPx()),
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
    )
}

