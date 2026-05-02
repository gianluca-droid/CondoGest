package com.condogest.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.condogest.app.data.model.Documento
import com.condogest.app.data.model.DocumentCategories
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentiScreen(viewModel: CondoViewModel) {
    val context = LocalContext.current
    val documenti by viewModel.documenti.collectAsState()
    val documentCount by viewModel.documentCount.collectAsState()

    var selectedCategoria by remember { mutableStateOf<String?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var documentoToDelete by remember { mutableStateOf<Documento?>(null) }

    // Filtro applicato
    val documentiFiltrati = if (selectedCategoria == null) documenti
    else documenti.filter { it.categoria == selectedCategoria }

    // File picker per PDF
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { pickedUri = it; showAddSheet = true } }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header con contatore ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Archivio Documenti",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        "$documentCount documento${if (documentCount != 1) "i" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                FilledTonalButton(
                    onClick = { filePicker.launch("application/pdf") },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Cyan400.copy(alpha = 0.15f),
                        contentColor = Cyan400
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aggiungi PDF", style = MaterialTheme.typography.labelMedium)
                }
            }

            // ── Filtro Categorie ─────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    CategoryChip(
                        label = "Tutti",
                        icon = "📂",
                        selected = selectedCategoria == null,
                        count = documenti.size,
                        colorHex = "#636E72"
                    ) { selectedCategoria = null }
                }
                items(DocumentCategories.categories) { cat ->
                    val count = documenti.count { it.categoria == cat.name }
                    CategoryChip(
                        label = cat.name,
                        icon = cat.icon,
                        selected = selectedCategoria == cat.name,
                        count = count,
                        colorHex = cat.colorHex
                    ) { selectedCategoria = if (selectedCategoria == cat.name) null else cat.name }
                }
            }

            HorizontalDivider(color = DarkSurface, thickness = 1.dp)

            // ── Lista Documenti ──────────────────────────────────────
            if (documentiFiltrati.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📁", fontSize = 48.sp)
                        Text(
                            if (selectedCategoria == null) "Nessun documento ancora"
                            else "Nessun documento in questa categoria",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (selectedCategoria == null) {
                            FilledTonalButton(
                                onClick = { filePicker.launch("application/pdf") },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Cyan400.copy(alpha = 0.15f),
                                    contentColor = Cyan400
                                )
                            ) {
                                Icon(Icons.Filled.UploadFile, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Carica il primo PDF")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(documentiFiltrati, key = { it.id }) { doc ->
                        DocumentCard(
                            documento = doc,
                            onOpen = {
                                val file = File(doc.filePath)
                                if (file.exists()) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            onDelete = { documentoToDelete = doc }
                        )
                    }
                }
            }
        }

        // ── FAB ─────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { filePicker.launch("application/pdf") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Cyan400,
            contentColor = DarkBg
        ) {
            Icon(Icons.Filled.Add, "Aggiungi documento")
        }
    }

    // ── Bottom Sheet Aggiunta Documento ─────────────────────────────
    if (showAddSheet && pickedUri != null) {
        AddDocumentoSheet(
            uri = pickedUri!!,
            onDismiss = { showAddSheet = false; pickedUri = null },
            onConfirm = { titolo, categoria, note ->
                viewModel.addDocumento(pickedUri!!, titolo, categoria, note)
                showAddSheet = false
                pickedUri = null
            }
        )
    }

    // ── Dialog Conferma Eliminazione ─────────────────────────────────
    documentoToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentoToDelete = null },
            containerColor = DarkSurface,
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = Color(0xFFFF6B6B)) },
            title = {
                Text("Elimina documento", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Eliminare \"${doc.titolo}\"?\nIl file verrà rimosso definitivamente.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDocumento(doc)
                    documentoToDelete = null
                }) {
                    Text("Elimina", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentoToDelete = null }) {
                    Text("Annulla", color = TextSecondary)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Chip categoria
// ─────────────────────────────────────────────────────────────────────
@Composable
fun CategoryChip(
    label: String,
    icon: String,
    selected: Boolean,
    count: Int,
    colorHex: String,
    onClick: () -> Unit
) {
    val chipColor = try { Color(android.graphics.Color.parseColor(colorHex)) }
    catch (e: Exception) { Cyan400 }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) chipColor.copy(alpha = 0.18f) else DarkSurface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) chipColor else TextMuted.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 14.sp)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) chipColor else TextSecondary
                )
            )
            if (count > 0) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) chipColor.copy(alpha = 0.25f) else TextMuted.copy(alpha = 0.15f)
                ) {
                    Text(
                        "$count",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = if (selected) chipColor else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Card singolo documento
// ─────────────────────────────────────────────────────────────────────
@Composable
fun DocumentCard(
    documento: Documento,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val catColor = try {
        Color(android.graphics.Color.parseColor(DocumentCategories.getColorHex(documento.categoria)))
    } catch (e: Exception) { Cyan400 }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ITALIAN) }
    val dataStr = dateFormatter.format(Date(documento.dataInserimento))
    val sizeStr = formatFileSize(documento.fileSize)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, catColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icona categoria
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = catColor.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            DocumentCategories.getIcon(documento.categoria),
                            fontSize = 20.sp
                        )
                        Text(
                            "PDF",
                            fontSize = 8.sp,
                            color = catColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Testo
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    documento.titolo,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    documento.categoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = catColor
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dataStr, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("•", color = TextMuted, fontSize = 8.sp)
                    Text(sizeStr, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                if (documento.note.isNotBlank()) {
                    Text(
                        documento.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Azioni
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.OpenInNew,
                        "Apri",
                        tint = Cyan400,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        "Elimina",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Bottom Sheet aggiunta documento
// ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentoSheet(
    uri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (titolo: String, categoria: String, note: String) -> Unit
) {
    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "documento.pdf"
    var titolo by remember { mutableStateOf(fileName.removeSuffix(".pdf")) }
    var selectedCategoria by remember { mutableStateOf(DocumentCategories.names.first()) }
    var note by remember { mutableStateOf("") }
    var showCategoriaMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.padding(vertical = 12.dp).width(40.dp).height(4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = TextMuted.copy(alpha = 0.4f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Nuovo Documento",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            // File selezionato
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Cyan400.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Cyan400.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, null, tint = Cyan400, modifier = Modifier.size(20.dp))
                    Text(
                        fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Titolo
            OutlinedTextField(
                value = titolo,
                onValueChange = { titolo = it },
                label = { Text("Titolo documento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = condoTextFieldColors()
            )

            // Categoria dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoriaMenu,
                onExpandedChange = { showCategoriaMenu = it }
            ) {
                OutlinedTextField(
                    value = "${DocumentCategories.getIcon(selectedCategoria)} $selectedCategoria",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCategoriaMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = condoTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = showCategoriaMenu,
                    onDismissRequest = { showCategoriaMenu = false }
                ) {
                    DocumentCategories.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat.icon)
                                    Text(cat.name, color = TextPrimary)
                                }
                            },
                            onClick = { selectedCategoria = cat.name; showCategoriaMenu = false },
                            colors = MenuDefaults.itemColors(
                                textColor = TextPrimary,
                                leadingIconColor = TextPrimary,
                                trailingIconColor = TextPrimary,
                                disabledTextColor = TextMuted,
                                disabledLeadingIconColor = TextMuted,
                                disabledTrailingIconColor = TextMuted
                            )
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (opzionale)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                colors = condoTextFieldColors()
            )

            // Pulsanti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.4f))
                ) {
                    Text("Annulla", color = TextSecondary)
                }
                Button(
                    onClick = { onConfirm(titolo.trim().ifBlank { fileName }, selectedCategoria, note.trim()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = DarkBg),
                    enabled = titolo.isNotBlank()
                ) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Salva", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────
@Composable
fun condoTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = Cyan400,
    unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
    focusedLabelColor = Cyan400,
    unfocusedLabelColor = TextMuted,
    cursorColor = Cyan400
)

fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}
