package com.condogest.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.condogest.app.data.model.*
import com.condogest.app.ui.components.*
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel
import java.util.Calendar

// Vista attiva
private enum class PaymentView { PER_UNITA, PER_MESE }

@Composable
fun PaymentsScreen(viewModel: CondoViewModel) {
    val payments by viewModel.payments.collectAsState()
    val totalPayments by viewModel.totalPayments.collectAsState()
    val units by viewModel.units.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<Payment?>(null) }
    var deleteTarget by remember { mutableStateOf<Payment?>(null) }
    var filterMethod by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeView by remember { mutableStateOf(PaymentView.PER_UNITA) }

    // Filtraggio base (metodo + ricerca)
    val filteredPayments = remember(payments, filterMethod, searchQuery, units) {
        payments
            .filter { p -> filterMethod == null || p.method == filterMethod }
            .filter { p ->
                if (searchQuery.isBlank()) true
                else {
                    val unitName = units.find { it.id == p.unitId }?.let { "Int. ${it.number} ${it.ownerName}" } ?: ""
                    unitName.contains(searchQuery, ignoreCase = true) ||
                    p.reference.contains(searchQuery, ignoreCase = true) ||
                    p.method.contains(searchQuery, ignoreCase = true)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (payments.isEmpty()) {
            EmptyState("Nessun pagamento registrato", Icons.Filled.CreditCard)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary
                item {
                    SummaryCard(
                        title = "Totale Incassi",
                        value = Formatters.currency(totalPayments),
                        icon = Icons.Filled.AccountBalanceWallet,
                        accentColor = Green400,
                        subtitle = "${payments.size} pagamenti registrati"
                    )
                }

                // Barra ricerca + toggle vista
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cerca proprietario, riferimento…", style = MaterialTheme.typography.bodySmall, color = TextMuted) },
                            leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Filled.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan400,
                                unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                cursorColor = Cyan400
                            )
                        )
                        // Toggle vista
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurface
                        ) {
                            Row {
                                IconButton(
                                    onClick = { activeView = PaymentView.PER_UNITA },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Apartment, "Per unità",
                                        tint = if (activeView == PaymentView.PER_UNITA) Cyan400 else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { activeView = PaymentView.PER_MESE },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.CalendarMonth, "Per mese",
                                        tint = if (activeView == PaymentView.PER_MESE) Cyan400 else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Filtri metodo
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        FilterChip(
                            selected = filterMethod == null,
                            onClick = { filterMethod = null },
                            label = { Text("Tutti") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan500.copy(alpha = 0.2f))
                        )
                        PaymentMethods.methods.forEach { method ->
                            FilterChip(
                                selected = filterMethod == method,
                                onClick = { filterMethod = if (filterMethod == method) null else method },
                                label = { Text(method) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan500.copy(alpha = 0.2f))
                            )
                        }
                    }
                }

                // Etichetta vista attiva
                item {
                    Text(
                        if (activeView == PaymentView.PER_UNITA) "🏠 Vista per Unità" else "📅 Vista per Mese",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Cyan400
                    )
                }

                // Nessun risultato ricerca
                if (filteredPayments.isEmpty() && (searchQuery.isNotBlank() || filterMethod != null)) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.SearchOff, null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Nessun pagamento trovato", color = TextMuted)
                            }
                        }
                    }
                } else if (activeView == PaymentView.PER_UNITA) {
                    // ── Vista per Unità ──────────────────────────────────────
                    val grouped = filteredPayments
                        .groupBy { it.unitId }
                        .toList()
                        .sortedBy { (unitId, _) ->
                            units.find { it.id == unitId }?.let { "${it.scala}_${it.number}" } ?: ""
                        }

                    grouped.forEach { (unitId, unitPayments) ->
                        val unit = units.find { it.id == unitId }
                        val unitLabel = unit?.let {
                            buildString {
                                if (it.scala.isNotBlank()) append("Sc.${it.scala} · ")
                                append("Int. ${it.number} — ${it.ownerName}")
                            }
                        } ?: "Unità sconosciuta"
                        val unitTotal = unitPayments.sumOf { it.amount }
                        val sortedPays = unitPayments.sortedByDescending { it.date }

                        item(key = "header_unit_$unitId") {
                            PaymentGroupHeader(
                                label = unitLabel,
                                count = unitPayments.size,
                                total = unitTotal,
                                icon = Icons.Filled.Home
                            )
                        }
                        items(sortedPays, key = { "unit_${it.id}" }) { payment ->
                            PaymentRow(
                                payment = payment,
                                showUnit = false,
                                unitName = unitLabel,
                                onEdit = { editingPayment = payment; showDialog = true },
                                onDelete = { deleteTarget = payment }
                            )
                        }
                    }

                } else {
                    // ── Vista per Mese ───────────────────────────────────────
                    val grouped = filteredPayments
                        .groupBy { payment ->
                            val cal = Calendar.getInstance().apply { timeInMillis = payment.date }
                            cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH) + 1  // es. 202604
                        }
                        .toSortedMap(compareByDescending { it })  // più recente prima

                    val monthNames = listOf("Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno",
                        "Luglio","Agosto","Settembre","Ottobre","Novembre","Dicembre")

                    grouped.forEach { (yearMonth, monthPayments) ->
                        val year = yearMonth / 100
                        val month = yearMonth % 100
                        val monthLabel = "${monthNames[month - 1]} $year"
                        val monthTotal = monthPayments.sumOf { it.amount }
                        val sortedPays = monthPayments.sortedByDescending { it.date }

                        item(key = "header_month_$yearMonth") {
                            PaymentGroupHeader(
                                label = monthLabel,
                                count = monthPayments.size,
                                total = monthTotal,
                                icon = Icons.Filled.CalendarToday
                            )
                        }
                        items(sortedPays, key = { "month_${it.id}" }) { payment ->
                            PaymentRow(
                                payment = payment,
                                showUnit = true,
                                unitName = viewModel.getUnitName(payment.unitId),
                                onEdit = { editingPayment = payment; showDialog = true },
                                onDelete = { deleteTarget = payment }
                            )
                        }
                    }
                }
            }
        }

        GradientFab(
            icon = Icons.Filled.Add,
            contentDescription = "Registra pagamento",
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

// ─── Header gruppo ────────────────────────────────────────────────────
@Composable
private fun PaymentGroupHeader(
    label: String,
    count: Int,
    total: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Green400, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    "$count ${if (count == 1) "pagamento" else "pagamenti"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Text(
                Formatters.currency(total),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Green400
            )
        }
    }
}

// ─── Riga pagamento ───────────────────────────────────────────────────
@Composable
private fun PaymentRow(
    payment: Payment,
    showUnit: Boolean,
    unitName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ItemCard(onEdit = onEdit, onDelete = onDelete) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                if (showUnit) {
                    Text(
                        unitName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Formatters.currency(payment.amount),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Green400
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(payment.method)
                }
                if (payment.reference.isNotBlank()) {
                    Text(
                        "Rif: ${payment.reference}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            Text(
                Formatters.date(payment.date),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

// ─── Form dialog ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentFormDialog(
    payment: Payment?,
    units: List<CondoUnit>,
    onDismiss: () -> Unit,
    onSave: (Payment) -> Unit
) {
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
                        value = buildString {
                            if (selectedUnit.scala.isNotBlank()) append("Sc.${selectedUnit.scala} · ")
                            append("Int. ${selectedUnit.number} - ${selectedUnit.ownerName}")
                        },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Unità") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) }
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.sortedBy { "${it.scala}_${it.number}" }.forEach { u ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(buildString {
                                            if (u.scala.isNotBlank()) append("Sc.${u.scala} · ")
                                            append("Int. ${u.number} - ${u.ownerName}")
                                        })
                                    }
                                },
                                onClick = { selectedUnit = u; unitExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Importo (€)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
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
                OutlinedTextField(
                    value = reference, onValueChange = { reference = it },
                    label = { Text("Riferimento cedolino") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = Payment(
                        id = payment?.id ?: 0,
                        unitId = selectedUnit.id,
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
