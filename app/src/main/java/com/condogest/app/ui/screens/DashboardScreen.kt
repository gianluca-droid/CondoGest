package com.condogest.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.condogest.app.data.model.ExpenseCategories
import com.condogest.app.ui.components.*
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel

@Composable
fun DashboardScreen(viewModel: CondoViewModel) {
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalPayments by viewModel.totalPayments.collectAsState()
    val units by viewModel.units.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val pendingCedolini by viewModel.pendingCedolini.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val balance = totalPayments - totalExpenses

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ─── Summary Cards ──────────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Totale Spese",
                    value = Formatters.currency(totalExpenses),
                    icon = Icons.Filled.TrendingDown,
                    accentColor = Red400,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Totale Incassi",
                    value = Formatters.currency(totalPayments),
                    icon = Icons.Filled.TrendingUp,
                    accentColor = Green400,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Saldo",
                    value = Formatters.currency(balance),
                    icon = Icons.Filled.AccountBalance,
                    accentColor = if (balance >= 0) Green400 else Amber400,
                    modifier = Modifier.weight(1f),
                    subtitle = if (balance >= 0) "In positivo" else "In negativo"
                )
                SummaryCard(
                    title = "Cedolini Aperti",
                    value = pendingCedolini.toString(),
                    icon = Icons.Filled.Description,
                    accentColor = Cyan400,
                    modifier = Modifier.weight(1f),
                    subtitle = "${units.size} unità totali"
                )
            }
        }

        // ─── Spese per Categoria ────────────────────────────────
        item { SectionHeader("Spese per Categoria") }

        if (expensesByCategory.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        expensesByCategory.forEach { ct ->
                            val color = CategoryColors[ct.category] ?: TextSecondary
                            val icon = ExpenseCategories.getIcon(ct.category)
                            val pct = if (totalExpenses > 0) (ct.total / totalExpenses * 100) else 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryChip(ct.category, icon)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    Formatters.currency(ct.total),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    "  ${String.format("%.0f", pct)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (pct / 100).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 2.dp),
                                color = color,
                                trackColor = color.copy(alpha = 0.1f)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // ─── Ultimi Movimenti ───────────────────────────────────
        item { SectionHeader("Ultime Spese") }

        val recentExpenses = expenses.take(5)
        items(recentExpenses) { expense ->
            val icon = ExpenseCategories.getIcon(expense.category)
            ItemCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryChip(expense.category, icon)
                    Spacer(Modifier.weight(1f))
                    Text(Formatters.date(expense.date), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Spacer(Modifier.height(8.dp))
                Text(expense.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text(
                    Formatters.currency(expense.amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Red400
                )
            }
        }

        // ─── Ultimi Pagamenti ───────────────────────────────────
        item { SectionHeader("Ultimi Pagamenti") }

        val recentPayments = payments.take(5)
        items(recentPayments) { payment ->
            ItemCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(viewModel.getUnitName(payment.unitId), style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                    StatusBadge(payment.method)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Formatters.currency(payment.amount),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Green400
                    )
                    Spacer(Modifier.weight(1f))
                    Text(Formatters.date(payment.date), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
