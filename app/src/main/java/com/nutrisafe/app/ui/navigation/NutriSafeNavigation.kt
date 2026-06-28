package com.nutrisafe.app.ui.navigation

import com.nutrisafe.app.ui.components.TinyIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.nutrisafe.app.ui.components.NutriSafeLogoTile
import com.nutrisafe.app.ui.components.brandGradient
import com.nutrisafe.app.ui.screens.AiDieticianScreen
import com.nutrisafe.app.ui.screens.DashboardScreen
import com.nutrisafe.app.ui.screens.DeleteAccountScreen
import com.nutrisafe.app.ui.screens.ExpertChatScreen
import com.nutrisafe.app.ui.screens.InsightsScreen
import com.nutrisafe.app.ui.screens.MealTrackerScreen
import com.nutrisafe.app.ui.screens.PrivacyScreen
import com.nutrisafe.app.ui.theme.BrandMagenta
import com.nutrisafe.app.ui.theme.Ink
import com.nutrisafe.app.ui.theme.Line
import com.nutrisafe.app.ui.theme.Muted
import com.nutrisafe.app.ui.theme.Surface
import com.nutrisafe.app.viewmodel.NutriSafeViewModel
import kotlinx.coroutines.launch

object Routes {
    const val Dashboard = "dashboard"
    const val Privacy = "privacy"
    const val Insights = "insights"
    const val ExpertChat = "expert_chat"
    const val MealTracker = "meal_tracker"
    const val AiDietician = "ai_dietician"
    const val DeleteAccount = "delete_account"
}

private data class DrawerRoute(val route: String, val label: String, val icon: ImageVector)

@Composable
fun NutriSafeApp(viewModel: NutriSafeViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.Dashboard
    val notice = viewModel.notice

    LaunchedEffect(notice) {
        if (!notice.isNullOrBlank()) {
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NutriSafeDrawer(
                onClose = { scope.launch { drawerState.close() } },
                onRoute = { route ->
                    navigate(route)
                    scope.launch { drawerState.close() }
                },
                onSignOut = {
                    viewModel.signOut()
                    scope.launch { drawerState.close() }
                },
                onDelete = {
                    navigate(Routes.DeleteAccount)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Surface,
            topBar = {
                NutriSafeTopBar(
                    onMenu = { scope.launch { drawerState.open() } },
                    onProfile = { navigate(Routes.Privacy) }
                )
            },
            bottomBar = { NutriSafeBottomBar(currentRoute, ::navigate) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
            floatingActionButton = { CenterAiButton { navigate(Routes.AiDietician) } }
        ) { padding ->
            NavHost(navController = navController, startDestination = Routes.Dashboard) {
                composable(Routes.Dashboard) { DashboardScreen(viewModel, padding) }
                composable(Routes.Privacy) { PrivacyScreen(viewModel, padding, onDeleteAccount = { navigate(Routes.DeleteAccount) }) }
                composable(Routes.Insights) { InsightsScreen(viewModel, padding) }
                composable(Routes.ExpertChat) { ExpertChatScreen(viewModel, padding) }
                composable(Routes.MealTracker) { MealTrackerScreen(viewModel, padding) }
                composable(Routes.AiDietician) { AiDieticianScreen(viewModel, padding) }
                composable(Routes.DeleteAccount) { DeleteAccountScreen(viewModel, padding, onCancel = { navigate(Routes.Privacy) }) }
            }
        }
    }
}

@Composable
private fun NutriSafeTopBar(onMenu: () -> Unit, onProfile: () -> Unit) {
    Column(
        Modifier
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenu) { Icon(TinyIcons.Menu, null, tint = Ink) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                NutriSafeLogoTile(size = 32.dp)
                Spacer(Modifier.width(8.dp))
                Text("NutriSafe", color = BrandMagenta, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, BrandMagenta, CircleShape)
                    .clickable(onClick = onProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(TinyIcons.Person, null, tint = BrandMagenta, modifier = Modifier.size(22.dp))
            }
        }
        HorizontalDivider(color = Line, thickness = 1.dp)
    }
}

@Composable
private fun NutriSafeBottomBar(currentRoute: String, navigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomIcon(TinyIcons.Home, Routes.Dashboard, currentRoute, navigate)
        BottomIcon(TinyIcons.BarChart, Routes.Insights, currentRoute, navigate)
        Spacer(Modifier.width(58.dp))
        BottomIcon(TinyIcons.Restaurant, Routes.MealTracker, currentRoute, navigate)
        BottomIcon(TinyIcons.Settings, Routes.Privacy, currentRoute, navigate)
    }
}

@Composable
private fun BottomIcon(icon: ImageVector, route: String, currentRoute: String, navigate: (String) -> Unit) {
    val selected = currentRoute == route
    IconButton(onClick = { navigate(route) }) {
        Icon(icon, null, tint = if (selected) BrandMagenta else Muted, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun CenterAiButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(brandGradient())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(TinyIcons.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun NutriSafeDrawer(
    onClose: () -> Unit,
    onRoute: (String) -> Unit,
    onSignOut: () -> Unit,
    onDelete: () -> Unit
) {
    val items = listOf(
        DrawerRoute(Routes.Dashboard, "Dashboard", TinyIcons.Home),
        DrawerRoute(Routes.Privacy, "Security & Privacy", TinyIcons.Security),
        DrawerRoute(Routes.Insights, "Insights", TinyIcons.BarChart),
        DrawerRoute(Routes.ExpertChat, "Expert Chat", TinyIcons.PersonAdd),
        DrawerRoute(Routes.MealTracker, "Meal Tracker", TinyIcons.Restaurant),
        DrawerRoute(Routes.AiDietician, "AI Dietician", TinyIcons.AutoAwesome)
    )
    ModalDrawerSheet(
        modifier = Modifier.width(258.dp).fillMaxHeight().statusBarsPadding(),
        drawerContainerColor = Color.White
    ) {
        Row(
            Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Explore", color = BrandMagenta, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            IconButton(onClick = onClose) { Icon(TinyIcons.Close, null, tint = Color(0xFF667085)) }
        }
        HorizontalDivider(color = Line)
        Spacer(Modifier.height(18.dp))
        items.forEach { item ->
            DrawerRow(item.icon, item.label) { onRoute(item.route) }
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))
        DrawerRow(TinyIcons.Close, "Sign Out", color = Color(0xFF667085), onClick = onSignOut)
        DrawerRow(TinyIcons.Delete, "Delete Account", color = Color(0xFFFF3B30), onClick = onDelete)
    }
}

@Composable
private fun DrawerRow(icon: ImageVector, label: String, color: Color = Color(0xFF344054), onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}



