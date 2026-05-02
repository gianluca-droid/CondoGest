package com.condogest.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.condogest.app.data.model.Documento
import com.condogest.app.data.model.DocumentCategories
import com.condogest.app.data.model.FileTypes
import com.condogest.app.ui.components.CategoryChip
import com.condogest.app.ui.components.condoTextFieldColors
import com.condogest.app.ui.theme.*
import com.condogest.app.viewmodel.CondoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Contract per aprire file di tipi multipli
class GetMultiTypeContent : ActivityResultContract<Array<String>, Pair<Uri, String>?>() {
    override fun createIntent(context: android.content.Context, input: Array<String>) =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, input)
        }
    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Uri, String>? {
        if (resultCode != Activity.RESULT_OK) return null
        val uri = intent?.data ?: return null
        val type = intent.type ?: ""
        return Pair(uri, type)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentiScreen(viewModel: CondoViewModel) {
    val context = LocalContext.current
    val documenti by viewModel.documenti.collectAsState()
    val documentCount by viewModel.documentCount.collectAsState()

    var selectedCategoria by remember { mutableStateOf<String?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var documentoToDelete by remember { mutableStateOf<Documento?>(null) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedMimeType by remember { mutableStateOf("") }

    val documentiFiltrati = if (selectedCategoria == null) documenti
    else documenti.filter { it.categoria == selectedCategoria }

    val filePicker = rememberLauncherForActivityResult(GetMultiTypeContent()) { result ->
        result?.let { (uri, mime) ->
            pickedUri = uri
            pickedMimeType = mime.ifBlank {
                context.contentResolver.getType(uri) ?: ""
            }
            showAddSheet = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Archivio Documenti",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Text("$documentCount documento${if (documentCount != 1) "i" else ""}",
                        style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                // Chip tipi file
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FileTypes.supported.forEach { ft ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = try { Color(android.graphics.Color.parseColor(ft.colorHex)) } catch (e: Exception) { Cyan400 }.copy(alpha = 0.15f)
                        ) {
                            Text(ft.icon, modifier = Modifier.padding(6.dp), fontSize = 16.sp)
                        }
                    }
                }
            }

            // ── Filtro Categorie ──────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                item {
                    CategoryChip("Tutti", "📂", selectedCategoria == null, documenti.size, "#636E72") { selectedCategoria = null }
                }
                items(DocumentCategories.categories) { cat ->
                    CategoryChip(cat.name, cat.icon, selectedCategoria == cat.name,
                        documenti.count { it.categoria == cat.name }, cat.colorHex) {
                        selectedCategoria = if (selectedCategoria == cat.name) null else cat.name
                    }
                }
            }

            HorizontalDivider(color = DarkSurface, thickness = 1.dp)

            // ── Lista ──────────────────────────────────────────────────
            if (documentiFiltrati.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("📁", fontSize = 48.sp)
                        Text(
                            if (selectedCategoria == null) "Nessun documento ancora" else "Nessun documento in questa categoria",
                            color = TextMuted, style = MaterialTheme.typography.bodyMedium
                        )
                        if (selectedCategoria == null) {
                            FilledTonalButton(
                                onClick = { filePicker.launch(FileTypes.allMimeTypes) },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Cyan400.copy(alpha = 0.15f), contentColor = Cyan400)
                            ) {
                                Icon(Icons.Filled.UploadFile, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Carica il primo documento")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(documentiFiltrati, key = { it.id }) { doc ->
                        DocumentCard(
                            documento = doc,
                            onOpen = {
                                val file = File(doc.filePath)
                                if (file.exists()) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context, "${context.packageName}.provider", file)
                                    val mimeType = when (doc.fileType) {
                                        "Word" -> "application/msword"
                                        "Foto" -> "image/*"
                                        else -> "application/pdf"
                                    }
                                    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, mimeType)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
                            },
                            onDelete = { documentoToDelete = doc }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { filePicker.launch(FileTypes.allMimeTypes) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Cyan400, contentColor = DarkBg
        ) { Icon(Icons.Filled.Add, "Aggiungi documento") }
    }

    if (showAddSheet && pickedUri != null) {
        AddDocumentoSheet(
            uri = pickedUri!!,
            mimeType = pickedMimeType,
            onDismiss = { showAddSheet = false; pickedUri = null },
            onConfirm = { titolo, categoria, note ->
                viewModel.addDocumento(pickedUri!!, titolo, categoria, note, pickedMimeType)
                showAddSheet = false; pickedUri = null
            }
        )
    }

    documentoToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentoToDelete = null },
            containerColor = DarkSurface,
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = Color(0xFFFF6B6B)) },
            title = { Text("Elimina documento", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Eliminare \"${doc.titolo}\"?\nIl file verrà rimosso definitivamente.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteDocumento(doc); documentoToDelete = null }) {
                    Text("Elimina", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { documentoToDelete = null }) { Text("Annulla", color = TextSecondary) } }
        )
    }
}

@Composable
fun DocumentCard(documento: Documento, onOpen: () -> Unit, onDelete: () -> Unit) {
    val catColor = try { Color(android.graphics.Color.parseColor(DocumentCategories.getColorHex(documento.categoria))) }
    catch (e: Exception) { Cyan400 }
    val fileColor = try { Color(android.graphics.Color.parseColor(FileTypes.getColorHex(documento.fileType))) }
    catch (e: Exception) { Cyan400 }
    val dateStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.ITALIAN).format(Date(documento.dataInserimento)) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, catColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Anteprima immagine o icona
            Surface(shape = RoundedCornerShape(12.dp), color = fileColor.copy(alpha = 0.15f), modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (documento.fileType == "Foto" && File(documento.filePath).exists()) {
                        AsyncImage(
                            model = File(documento.filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(FileTypes.getIcon(documento.fileType), fontSize = 22.sp)
                            Text(documento.fileType, fontSize = 8.sp, color = fileColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(documento.titolo,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(documento.categoria, style = MaterialTheme.typography.labelSmall, color = catColor)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("•", color = TextMuted, fontSize = 8.sp)
                    Text(formatFileSize(documento.fileSize), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                if (documento.note.isNotBlank()) {
                    Text(documento.note, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onOpen, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.OpenInNew, "Apri", tint = Cyan400, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Delete, "Elimina", tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentoSheet(uri: Uri, mimeType: String, onDismiss: () -> Unit, onConfirm: (titolo: String, categoria: String, note: String) -> Unit) {
    val detectedFileType = FileTypes.fromMimeType(mimeType)
    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "documento"
    var titolo by remember { mutableStateOf(fileName.substringBeforeLast('.')) }
    var selectedCategoria by remember { mutableStateOf(DocumentCategories.names.first()) }
    var note by remember { mutableStateOf("") }
    var showCategoriaMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DarkSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Nuovo Documento",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary))

            Surface(shape = RoundedCornerShape(10.dp), color = Cyan400.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Cyan400.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(FileTypes.getIcon(detectedFileType), fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fileName, style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(detectedFileType, style = MaterialTheme.typography.labelSmall, color = Cyan400)
                    }
                }
            }

            OutlinedTextField(titolo, { titolo = it }, label = { Text("Titolo documento") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = condoTextFieldColors())

            ExposedDropdownMenuBox(expanded = showCategoriaMenu, onExpandedChange = { showCategoriaMenu = it }) {
                OutlinedTextField(
                    "${DocumentCategories.getIcon(selectedCategoria)} $selectedCategoria", {},
                    readOnly = true, label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCategoriaMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = condoTextFieldColors()
                )
                ExposedDropdownMenu(expanded = showCategoriaMenu, onDismissRequest = { showCategoriaMenu = false }) {
                    DocumentCategories.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text(cat.icon); Text(cat.name, color = TextPrimary) } },
                            onClick = { selectedCategoria = cat.name; showCategoriaMenu = false },
                            colors = MenuDefaults.itemColors(textColor = TextPrimary, leadingIconColor = TextPrimary,
                                trailingIconColor = TextPrimary, disabledTextColor = TextMuted,
                                disabledLeadingIconColor = TextMuted, disabledTrailingIconColor = TextMuted)
                        )
                    }
                }
            }

            OutlinedTextField(note, { note = it }, label = { Text("Note (opzionale)") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, colors = condoTextFieldColors())

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.4f))) {
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

fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}
