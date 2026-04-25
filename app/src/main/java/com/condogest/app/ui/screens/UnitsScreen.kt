package com.condogest.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.condogest.app.data.model.*
import com.condogest.app.ui.components.*
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel

@Composable
fun UnitsScreen(viewModel: CondoViewModel) {
    val units by viewModel.units.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingUnit by remember { mutableStateOf<CondoUnit?>(null) }
    var deleteTarget by remember { mutableStateOf<CondoUnit?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (units.isEmpty()) {
            EmptyState("Nessuna unità registrata", Icons.Filled.Apartment)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SummaryCard(
                        title = "Totale Unità", value = "${units.size}",
                        icon = Icons.Filled.Apartment, accentColor = Cyan400,
                        subtitle = "Millesimi: ${units.sumOf { it.millesimi }.toInt()}/1000"
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(units, key = { it.id }) { unit ->
                    ItemCard(
                        onEdit = { editingUnit = unit; showDialog = true },
                        onDelete = { deleteTarget = unit }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Int. ${unit.number}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Cyan400)
                            Spacer(Modifier.width(8.dp))
                            StatusBadge(unit.type)
                            Spacer(Modifier.weight(1f))
                            Text("Piano ${unit.floor}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(unit.ownerName, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        if (unit.ownerPhone.isNotBlank()) {
                            Text("📞 ${unit.ownerPhone}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row {
                            Text("${unit.areaMq} m²", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Spacer(Modifier.width(16.dp))
                            Text("Millesimi: ${unit.millesimi.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Purple400)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        GradientFab(
            icon = Icons.Filled.Add, contentDescription = "Aggiungi unità",
            onClick = { editingUnit = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (showDialog) {
        UnitFormDialog(
            unit = editingUnit,
            onDismiss = { showDialog = false; editingUnit = null },
            onSave = { unit ->
                if (editingUnit != null) viewModel.updateUnit(unit) else viewModel.addUnit(unit)
                showDialog = false; editingUnit = null
            }
        )
    }

    deleteTarget?.let { unit ->
        ConfirmDeleteDialog(
            itemName = "Int. ${unit.number} - ${unit.ownerName}",
            onConfirm = { viewModel.deleteUnit(unit); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitFormDialog(unit: CondoUnit?, onDismiss: () -> Unit, onSave: (CondoUnit) -> Unit) {
    var number by remember { mutableStateOf(unit?.number ?: "") }
    var floor by remember { mutableStateOf(unit?.floor?.toString() ?: "0") }
    var type by remember { mutableStateOf(unit?.type ?: "Appartamento") }
    var areaMq by remember { mutableStateOf(unit?.areaMq?.toString() ?: "") }
    var millesimi by remember { mutableStateOf(unit?.millesimi?.toString() ?: "") }
    var ownerName by remember { mutableStateOf(unit?.ownerName ?: "") }
    var ownerEmail by remember { mutableStateOf(unit?.ownerEmail ?: "") }
    var ownerPhone by remember { mutableStateOf(unit?.ownerPhone ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (unit != null) "Modifica Unità" else "Nuova Unità") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Interno") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = floor, onValueChange = { floor = it }, label = { Text("Piano") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                        OutlinedTextField(value = type, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) })
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            UnitTypes.types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false }) }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = areaMq, onValueChange = { areaMq = it }, label = { Text("m²") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        OutlinedTextField(value = millesimi, onValueChange = { millesimi = it }, label = { Text("Millesimi") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    }
                }
                item { OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Proprietario") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = ownerEmail, onValueChange = { ownerEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)) }
                item { OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Telefono") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val u = CondoUnit(
                        id = unit?.id ?: 0, number = number, floor = floor.toIntOrNull() ?: 0,
                        type = type, areaMq = areaMq.toDoubleOrNull() ?: 0.0,
                        millesimi = millesimi.toDoubleOrNull() ?: 0.0,
                        ownerName = ownerName, ownerEmail = ownerEmail, ownerPhone = ownerPhone
                    )
                    onSave(u)
                },
                enabled = number.isNotBlank() && ownerName.isNotBlank()
            ) { Text("Salva") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
        containerColor = DarkSurface
    )
}
