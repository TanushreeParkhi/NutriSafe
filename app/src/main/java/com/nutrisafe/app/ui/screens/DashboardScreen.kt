package com.nutrisafe.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nutrisafe.app.ui.components.SoftCard
import com.nutrisafe.app.ui.components.TinyIcons
import com.nutrisafe.app.ui.components.brandGradient
import com.nutrisafe.app.ui.theme.Blue
import com.nutrisafe.app.ui.theme.BrandMagenta
import com.nutrisafe.app.ui.theme.BrandOrange
import com.nutrisafe.app.ui.theme.Ink
import com.nutrisafe.app.ui.theme.Muted
import com.nutrisafe.app.viewmodel.NutriSafeViewModel

@Composable
fun DashboardScreen(viewModel: NutriSafeViewModel, padding: PaddingValues) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val waterAlert = preferences.waterGlasses < 8
    val proteinToday = meals.take(3).sumOf { it.proteinGrams.toDouble() }.toInt()
    val proteinAlert = meals.isNotEmpty() && proteinToday < 50
    val alerts = buildList {
        if (waterAlert) add("Hydration Reminder" to "Time to drink some water! 250ml suggested.")
        if (proteinAlert) add("Protein Check" to "Today's protein is low. Add dal, egg, tofu, paneer, fish, or curd.")
        if (meals.isEmpty()) add("Meal Log Empty" to "Snap or upload your first meal to unlock nutrition insights.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 18.dp, vertical = 24.dp)
    ) {
        PrivacyScoreCard()
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Health Alerts", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("${alerts.size} New", color = BrandMagenta, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        alerts.take(3).forEachIndexed { index, alert ->
            AlertCard(alert.first, alert.second, "${10 + index * 8}m ago")
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text("Featured Workouts", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        WorkoutCard(
            tag = "MORNING YOGA",
            title = "Surya Namaskar Flow",
            meta = "15 mins • 120 kcal",
            color = BrandOrange,
            imageUrl = "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=320&q=80"
        )
        Spacer(Modifier.height(12.dp))
        WorkoutCard(
            tag = "STRENGTH TRAINING",
            title = "Protein Support Circuit",
            meta = "22 mins • 180 kcal",
            color = Blue,
            imageUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=320&q=80"
        )
        Spacer(Modifier.height(22.dp))
        Text("Traditional Diet Suggestions", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(recipesFor(preferences.dietVault)) { recipe ->
                RecipeCard(recipe)
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun PrivacyScoreCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(216.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(brandGradient())
            .padding(28.dp)
    ) {
        Icon(
            TinyIcons.LocalFireDepartment,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(128.dp)
        )
        Column(Modifier.align(Alignment.CenterStart)) {
            Text("Hi there!", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Text("Your privacy score is Excellent today.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrivacyPill("LOCAL STORAGE", TinyIcons.Shield)
                PrivacyPill("SANDBOXED", TinyIcons.Lock)
            }
        }
    }
}

@Composable
private fun PrivacyPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun AlertCard(title: String, body: String, time: String) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(TinyIcons.LocalDrink, contentDescription = null, tint = Blue, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(5.dp))
                Text(body, color = Color(0xFF5F6B7A), fontSize = 14.sp, lineHeight = 19.sp)
            }
            Text(time, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WorkoutCard(
    tag: String,
    title: String,
    meta: String,
    color: Color,
    imageUrl: String
) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Spacer(Modifier.width(18.dp))
            Column {
                Text(tag, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(6.dp))
                Text(title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(meta, color = Color(0xFF5F6B7A), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe) {
    SoftCard(Modifier.width(196.dp)) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandMagenta.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(recipe.name, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text("${recipe.kcal} kcal | ${recipe.minutes}m", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private data class Recipe(val name: String, val kcal: Int, val minutes: Int, val imageUrl: String)

private fun recipesFor(vault: String): List<Recipe> {
    return when (vault) {
        "Non-Veg" -> listOf(
            Recipe("Butter Chicken", 450, 35, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=320&q=80"),
            Recipe("Fish Amritsari", 310, 20, "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?auto=format&fit=crop&w=320&q=80"),
            Recipe("Masala Chai", 40, 5, "https://images.unsplash.com/photo-1571934811356-5cc061b6821f?auto=format&fit=crop&w=320&q=80")
        )
        "Veg+Egg" -> listOf(
            Recipe("Egg Curry", 280, 15, "https://images.unsplash.com/photo-1608039829572-78524f79c4c7?auto=format&fit=crop&w=320&q=80"),
            Recipe("Masala Omelette", 150, 10, "https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=320&q=80"),
            Recipe("Paneer Masala", 350, 25, "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=320&q=80"),
            Recipe("Dal Tadka", 320, 20, "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=320&q=80"),
            Recipe("Masala Chai", 40, 5, "https://images.unsplash.com/photo-1571934811356-5cc061b6821f?auto=format&fit=crop&w=320&q=80")
        )
        "Jain" -> listOf(
            Recipe("Jain Dal Fry", 180, 15, "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=320&q=80"),
            Recipe("Raw Banana Bhaji", 160, 15, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=320&q=80"),
            Recipe("Jeera Aloo (Jain)", 140, 12, "https://images.unsplash.com/photo-1518013431117-eb1465fa5752?auto=format&fit=crop&w=320&q=80"),
            Recipe("Masala Chai", 40, 5, "https://images.unsplash.com/photo-1571934811356-5cc061b6821f?auto=format&fit=crop&w=320&q=80")
        )
        else -> listOf(
            Recipe("Paneer Butter Masala", 350, 25, "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=320&q=80"),
            Recipe("Dal Tadka & Rice", 320, 20, "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=320&q=80"),
            Recipe("Aloo Gobi", 180, 15, "https://images.unsplash.com/photo-1518013431117-eb1465fa5752?auto=format&fit=crop&w=320&q=80"),
            Recipe("Masala Chai", 40, 5, "https://images.unsplash.com/photo-1571934811356-5cc061b6821f?auto=format&fit=crop&w=320&q=80")
        )
    }
}
