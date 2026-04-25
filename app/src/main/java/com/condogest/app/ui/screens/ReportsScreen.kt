package com.condogest.app.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.condogest.app.data.model.ExpenseCategories
import com.condogest.app.ui.components.*
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: CondoViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val units by viewModel.units.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalPayments by viewModel.totalPayments.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val cedolini by viewModel.cedolini.collectAsState()
    val context = LocalContext.current

    val balance = totalPayments - totalExpenses
    val paidCedolini = cedolini.count { it.status == "Pagato" }
    val pendingCedolini = cedolini.count { it.status != "Pagato" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ─── Riepilogo Generale ─────────────────────────────────
        item {
            Text("Riepilogo Generale", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Spacer(Modifier.height(8.dp))
        }

        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ReportRow("Totale Spese Registrate", Formatters.currency(totalExpenses), Red400)
                    ReportRow("Totale Pagamenti Ricevuti", Formatters.currency(totalPayments), Green400)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TextMuted.copy(alpha = 0.2f))
                    ReportRow("Saldo", Formatters.currency(balance), if (balance >= 0) Green400 else Amber400)
                }
            }
        }

        // ─── Statistiche ────────────────────────────────────────
        item {
            Text("Statistiche", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(title = "Unità", value = "${units.size}", icon = Icons.Filled.Apartment, accentColor = Cyan400, modifier = Modifier.weight(1f))
                SummaryCard(title = "Spese", value = "${expenses.size}", icon = Icons.Filled.Receipt, accentColor = Red400, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(title = "Pagamenti", value = "${payments.size}", icon = Icons.Filled.CreditCard, accentColor = Green400, modifier = Modifier.weight(1f))
                SummaryCard(title = "Cedolini", value = "${cedolini.size}", icon = Icons.Filled.Description, accentColor = Purple400, modifier = Modifier.weight(1f), subtitle = "$paidCedolini pagati / $pendingCedolini aperti")
            }
        }

        // ─── Dettaglio per Categoria ────────────────────────────
        item {
            Text("Dettaglio per Categoria", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        }

        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (expensesByCategory.isEmpty()) {
                        Text("Nessuna spesa registrata", color = TextMuted)
                    } else {
                        expensesByCategory.forEach { ct ->
                            val pct = if (totalExpenses > 0) ct.total / totalExpenses * 100 else 0.0
                            val icon = ExpenseCategories.getIcon(ct.category)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("$icon  ", style = MaterialTheme.typography.bodyMedium)
                                Text(ct.category, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                                Text(Formatters.currency(ct.total), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                                Text("  (${String.format("%.1f", pct)}%)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // ─── Situazione Condòmini ───────────────────────────────
        item {
            Text("Situazione Condòmini", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        }

        items(units) { unit ->
            val unitPayments = payments.filter { it.unitId == unit.id }
            val unitTotal = unitPayments.sumOf { it.amount }
            val unitCedolini = cedolini.filter { it.unitId == unit.id }
            val unitDue = unitCedolini.sumOf { it.total }
            val unitBalance = unitTotal - unitDue

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Int. ${unit.number}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Cyan400)
                        Spacer(Modifier.width(8.dp))
                        Text(unit.ownerName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text("${unit.millesimi.toInt()} ‰", style = MaterialTheme.typography.bodySmall, color = Purple400)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text("Versato: ${Formatters.currency(unitTotal)}", style = MaterialTheme.typography.bodySmall, color = Green400)
                        Spacer(Modifier.weight(1f))
                        Text("Dovuto: ${Formatters.currency(unitDue)}", style = MaterialTheme.typography.bodySmall, color = Amber400)
                    }
                    if (unitBalance != 0.0) {
                        Text(
                            if (unitBalance > 0) "Credito: ${Formatters.currency(unitBalance)}" else "Debito: ${Formatters.currency(-unitBalance)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (unitBalance > 0) Green400 else Red400
                        )
                    }
                }
            }
        }

        // ─── Esporta CSV ────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { exportCsv(context, expenses, payments, units, viewModel) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = DarkBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.FileDownload, null)
                Spacer(Modifier.width(8.dp))
                Text("Esporta Report CSV", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = valueColor)
    }
}

private fun exportCsv(
    context: Context,
    expenses: List<com.condogest.app.data.model.Expense>,
    payments: List<com.condogest.app.data.model.Payment>,
    units: List<com.condogest.app.data.model.CondoUnit>,
    viewModel: CondoViewModel
) {
    try {
        val sb = StringBuilder()
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

        sb.appendLine("=== REPORT SPESE CONDOMINIALI ===")
        sb.appendLine("Data esportazione: ${df.format(Date())}")
        sb.appendLine()
        sb.appendLine("--- SPESE ---")
        sb.appendLine("Data;Categoria;Descrizione;Importo;Note")
        expenses.forEach { e ->
            sb.appendLine("${df.format(Date(e.date))};${e.category};${e.description};${e.amount};${e.notes}")
        }
        sb.appendLine()
        sb.appendLine("--- PAGAMENTI ---")
        sb.appendLine("Data;Unità;Importo;Metodo;Riferimento")
        payments.forEach { p ->
            sb.appendLine("${df.format(Date(p.date))};${viewModel.getUnitName(p.unitId)};${p.amount};${p.method};${p.reference}")
        }

        val file = File(context.cacheDir, "report_condominiale_${System.currentTimeMillis()}.csv")
        file.writeText(sb.toString())

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Condividi Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Errore nell'esportazione: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
