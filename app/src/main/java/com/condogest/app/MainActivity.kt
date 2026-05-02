package com.condogest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.condogest.app.ui.navigation.Screen
import com.condogest.app.ui.screens.*
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CondoGestTheme { MainApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: CondoViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isLoading by viewModel.isLoading.collectAsState()
    val activeCondominioId by viewModel.activeCondominioId.collectAsState()
    val activeCondominio by viewModel.activeCondominio.collectAsState()

    // ── Loading screen ──────────────────────────────────────────────
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Cyan400)
                Spacer(Modifier.height(16.dp))
                Text("Caricamento...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    // ── Dopo il loading: scegli startDestination in base al condominio attivo ──
    val startDestination = if (activeCondominioId > 0L) Screen.Dashboard.route
                           else Screen.CondominioSelector.route

    val isInSelector = currentRoute == Screen.CondominioSelector.route
    val currentScreen = Screen.allScreens.find { it.route == currentRoute } ?: Screen.Dashboard

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            if (!isInSelector) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                currentScreen.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            // Nome condominio attivo
                            activeCondominio?.let {
                                Text(
                                    "🏢 ${it.nome}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cyan400
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBg, titleContentColor = TextPrimary
                    ),
                    actions = {
                        // Cambia condominio
                        IconButton(onClick = {
                            navController.navigate(Screen.CondominioSelector.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                            }
                        }) {
                            Icon(Icons.Filled.Business, "Cambia condominio", tint = TextSecondary)
                        }
                        // Reports
                        if (currentRoute != Screen.Reports.route) {
                            IconButton(onClick = { navController.navigate(Screen.Reports.route) }) {
                                Icon(Icons.Filled.BarChart, "Report", tint = TextSecondary)
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isInSelector) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Cyan400,
                                selectedTextColor = Cyan400,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Cyan400.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable(Screen.CondominioSelector.route) {
                CondominioSelectorScreen(
                    viewModel = viewModel,
                    onCondominioSelected = { condoId ->
                        viewModel.setActiveCondominio(condoId)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.CondominioSelector.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route)  { DashboardScreen(viewModel) }
            composable(Screen.Units.route)      { UnitsScreen(viewModel) }
            composable(Screen.Expenses.route)   { ExpensesScreen(viewModel) }
            composable(Screen.Payments.route)   { PaymentsScreen(viewModel) }
            composable(Screen.Cedolini.route)   { CedoliniScreen(viewModel) }
            composable(Screen.Documenti.route)  { DocumentiScreen(viewModel) }
            composable(Screen.Reports.route)    { ReportsScreen(viewModel) }
        }
    }
}
