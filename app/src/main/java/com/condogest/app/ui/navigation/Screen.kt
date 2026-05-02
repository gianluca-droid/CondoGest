package com.condogest.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val subtitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dashboard", "Panoramica generale", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    data object Units : Screen("units", "Unità", "Gestione condòmini", Icons.Filled.Apartment, Icons.Outlined.Apartment)
    data object Expenses : Screen("expenses", "Spese", "Registrazione spese", Icons.Filled.Receipt, Icons.Outlined.Receipt)
    data object Payments : Screen("payments", "Pagamenti", "Gestione pagamenti", Icons.Filled.CreditCard, Icons.Outlined.CreditCard)
    data object Cedolini : Screen("cedolini", "Cedolini", "Cedolini di pagamento", Icons.Filled.Description, Icons.Outlined.Description)
    data object Documenti : Screen("documenti", "Documenti", "Archivio documenti PDF", Icons.Filled.Folder, Icons.Outlined.FolderOpen)
    data object Reports : Screen("reports", "Report", "Statistiche e report", Icons.Filled.BarChart, Icons.Outlined.BarChart)

    companion object {
        val bottomNavItems = listOf(Dashboard, Units, Expenses, Payments, Documenti)
        val allScreens = listOf(Dashboard, Units, Expenses, Payments, Cedolini, Documenti, Reports)
    }
}
