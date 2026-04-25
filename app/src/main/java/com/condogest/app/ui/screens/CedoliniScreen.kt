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
import com.condogest.app.data.model.*
import com.condogest.app.ui.components.*
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel
import java.util.*

@Composable
fun CedoliniScreen(viewModel: CondoViewModel) {
    val cedolini by viewModel.cedolini.collectAsState()
    val cedoliniWithItems by viewModel.cedoliniWithItems.collectAsState()
    val units by viewModel.units.collectAsState()
    val pendingCount by viewModel.pendingCedolini.collectAsState()
    var showGenerateDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf<CedolinoWithItems?>(null) }
    var deleteTarget by remember { mutableStateOf<Cedolino?>(null) }
    var filterStatus by remember { mutableStateOf<String?>(null) }

    val filtered = if (filterStatus != null) cedolini.filter { it.status == filterStatus } else cedolini

    Box(modifier = Modifier.fillMaxSize()) {
        if (cedolini.isEmpty()) {
            EmptyState("Nessun cedolino generato", Icons.Filled.Description)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            title = "Cedolini Totali", value = "${cedolini.size}",
                            icon = Icons.Filled.Description, accentColor = Cyan400,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "In Sospeso", value = "$pendingCount",
                            icon = Icons.Filled.PendingActions, accentColor = Amber400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Filter chips
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        FilterChip(
                            selected = filterStatus == null, onClick = { filterStatus = null },
                            label = { Text("Tutti") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan500.copy(alpha = 0.2f))
                        )
                        CedolinoStatuses.statuses.forEach { s ->
                            FilterChip(
                                selected = filterStatus == s, onClick = { filterStatus = if (filterStatus == s) null else s },
                                label = { Text(s) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan500.copy(alpha = 0.2f))
                            )
                        }
                    }
                }

                items(filtered, key = { it.id }) { cedolino ->
                    ItemCard(
                        onDelete = { deleteTarget = cedolino }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    viewModel.getUnitName(cedolino.unitId),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(cedolino.period, style = MaterialTheme.typography.bodySmall, color = Cyan400)
                            }
                            StatusBadge(cedolino.status)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                Formatters.currency(cedolino.total),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(Modifier.weight(1f))
                            Text("Scad: ${Formatters.date(cedolino.dueDate)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        if (cedolino.paidAmount > 0 && cedolino.status != "Pagato") {
                            Text("Versato: ${Formatters.currency(cedolino.paidAmount)}", style = MaterialTheme.typography.bodySmall, color = Green400)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Detail button
                            OutlinedButton(
                                onClick = {
                                    val cwi = cedoliniWithItems.find { it.cedolino.id == cedolino.id }
                                    showDetailDialog = cwi
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan400)
                            ) {
                                Icon(Icons.Filled.Visibility, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Dettaglio", style = MaterialTheme.typography.labelMedium)
                            }
                            // Mark paid
                            if (cedolino.status != "Pagato") {
                                Button(
                                    onClick = { viewModel.markCedolinoPaid(cedolino) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pagato", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        GradientFab(
            icon = Icons.Filled.NoteAdd, contentDescription = "Genera cedolini",
            onClick = { showGenerateDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (showGenerateDialog) {
        GenerateCedoliniDialog(
            onDismiss = { showGenerateDialog = false },
            onGenerate = { period, dueDate ->
                viewModel.generateCedoliniForAllUnits(period, dueDate)
                showGenerateDialog = false
            }
        )
    }

    showDetailDialog?.let { cwi ->
        CedolinoDetailDialog(cwi = cwi, unitName = viewModel.getUnitName(cwi.cedolino.unitId), onDismiss = { showDetailDialog = null })
    }

    deleteTarget?.let { cedolino ->
        ConfirmDeleteDialog(
            itemName = "Cedolino ${cedolino.period} - ${viewModel.getUnitName(cedolino.unitId)}",
            onConfirm = { viewModel.deleteCedolino(cedolino); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun GenerateCedoliniDialog(onDismiss: () -> Unit, onGenerate: (String, Long) -> Unit) {
    var period by remember { mutableStateOf("II Trimestre 2026") }
    val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
    val dueDate = cal.timeInMillis

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Genera Cedolini") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Verranno generati cedolini per tutte le unità con ripartizione millesimale automatica.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                OutlinedTextField(value = period, onValueChange = { period = it }, label = { Text("Periodo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Scadenza: ${Formatters.date(dueDate)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        },
        confirmButton = {
            TextButton(onClick = { onGenerate(period, dueDate) }, enabled = period.isNotBlank()) { Text("Genera") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
        containerColor = DarkSurface
    )
}

@Composable
private fun CedolinoDetailDialog(cwi: CedolinoWithItems, unitName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cedolino - ${cwi.cedolino.period}") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    Text(unitName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Cyan400)
                    Spacer(Modifier.height(4.dp))
                    Row { Text("Emesso: ", color = TextMuted, style = MaterialTheme.typography.bodySmall); Text(Formatters.date(cwi.cedolino.issueDate), style = MaterialTheme.typography.bodySmall, color = TextPrimary) }
                    Row { Text("Scadenza: ", color = TextMuted, style = MaterialTheme.typography.bodySmall); Text(Formatters.date(cwi.cedolino.dueDate), style = MaterialTheme.typography.bodySmall, color = TextPrimary) }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TextMuted.copy(alpha = 0.3f))
                    Text("Voci di spesa", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                }
                items(cwi.items) { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text(Formatters.currency(item.amount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    }
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Cyan400.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("TOTALE", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Spacer(Modifier.weight(1f))
                        Text(Formatters.currency(cwi.cedolino.total), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Cyan400)
                    }
                    Spacer(Modifier.height(8.dp))
                    StatusBadge(cwi.cedolino.status)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } },
        containerColor = DarkSurface
    )
}
