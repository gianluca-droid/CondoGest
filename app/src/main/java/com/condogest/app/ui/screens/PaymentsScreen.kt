package com.condogest.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.condogest.app.data.model.*
import com.condogest.app.ui.components.*
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel

@Composable
fun PaymentsScreen(viewModel: CondoViewModel) {
    val payments by viewModel.payments.collectAsState()
    val totalPayments by viewModel.totalPayments.collectAsState()
    val units by viewModel.units.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<Payment?>(null) }
    var deleteTarget by remember { mutableStateOf<Payment?>(null) }
    var filterMethod by remember { mutableStateOf<String?>(null) }

    val filteredPayments = if (filterMethod != null) payments.filter { it.method == filterMethod } else payments

    Box(modifier = Modifier.fillMaxSize()) {
        if (payments.isEmpty()) {
            EmptyState("Nessun pagamento registrato", Icons.Filled.CreditCard)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SummaryCard(
                        title = "Totale Incassi", value = Formatters.currency(totalPayments),
                        icon = Icons.Filled.AccountBalanceWallet, accentColor = Green400,
                        subtitle = "${payments.size} pagamenti registrati"
                    )
                }

                // Filter chips
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        FilterChip(
                            selected = filterMethod == null, onClick = { filterMethod = null },
                            label = { Text("Tutti") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan500.copy(alpha = 0.2f))
                        )
                        PaymentMethods.methods.forEach { method ->
                            FilterChip(
                                selected = filterMethod == method, onClick = { filterMethod = if (filterMethod == method) null else method },
                                label = { Text(method) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan500.copy(alpha = 0.2f))
                            )
                        }
                    }
                }

                items(filteredPayments, key = { it.id }) { payment ->
                    ItemCard(
                        onEdit = { editingPayment = payment; showDialog = true },
                        onDelete = { deleteTarget = payment }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                viewModel.getUnitName(payment.unitId),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary, modifier = Modifier.weight(1f)
                            )
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
                        if (payment.reference.isNotBlank()) {
                            Text("Rif: ${payment.reference}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        GradientFab(
            icon = Icons.Filled.Add, contentDescription = "Registra pagamento",
            onClick = { editingPayment = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (showDialog && units.isNotEmpty()) {
        PaymentFormDialog(
            payment = editingPayment, units = units,
            onDismiss = { showDialog = false; editingPayment = null },
            onSave = { p ->
                if (editingPayment != null) viewModel.updatePayment(p) else viewModel.addPayment(p)
                showDialog = false; editingPayment = null
            }
        )
    }

    deleteTarget?.let { payment ->
        ConfirmDeleteDialog(
            itemName = "${Formatters.currency(payment.amount)} - ${viewModel.getUnitName(payment.unitId)}",
            onConfirm = { viewModel.deletePayment(payment); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentFormDialog(payment: Payment?, units: List<CondoUnit>, onDismiss: () -> Unit, onSave: (Payment) -> Unit) {
    var selectedUnit by remember { mutableStateOf(units.find { it.id == payment?.unitId } ?: units.first()) }
    var amount by remember { mutableStateOf(payment?.amount?.toString() ?: "") }
    var method by remember { mutableStateOf(payment?.method ?: "Contanti") }
    var reference by remember { mutableStateOf(payment?.reference ?: "") }
    var notes by remember { mutableStateOf(payment?.notes ?: "") }
    var unitExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (payment != null) "Modifica Pagamento" else "Nuovo Pagamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                    OutlinedTextField(
                        value = "Int. ${selectedUnit.number} - ${selectedUnit.ownerName}", onValueChange = {},
                        readOnly = true, label = { Text("Unità") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) }
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text("Int. ${u.number} - ${u.ownerName}") },
                                onClick = { selectedUnit = u; unitExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Importo (€)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                ExposedDropdownMenuBox(expanded = methodExpanded, onExpandedChange = { methodExpanded = it }) {
                    OutlinedTextField(
                        value = method, onValueChange = {}, readOnly = true,
                        label = { Text("Metodo") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(methodExpanded) }
                    )
                    ExposedDropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                        PaymentMethods.methods.forEach { m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = { method = m; methodExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Riferimento") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = Payment(
                        id = payment?.id ?: 0, unitId = selectedUnit.id,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = payment?.date ?: System.currentTimeMillis(),
                        method = method, reference = reference, notes = notes
                    )
                    onSave(p)
                },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Salva") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
        containerColor = DarkSurface
    )
}
