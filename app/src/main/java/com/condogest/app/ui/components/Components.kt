package com.condogest.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.condogest.app.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ─── Formatters ─────────────────────────────────────────────────────
object Formatters {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.ITALY)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private val shortDateFormat = SimpleDateFormat("dd MMM", Locale.ITALY)

    fun currency(amount: Double): String = currencyFormat.format(amount)
    fun date(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun shortDate(timestamp: Long): String = shortDateFormat.format(Date(timestamp))
}

// ─── Summary Card ───────────────────────────────────────────────────
@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        }
    }
}

// ─── Section Header ─────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = Cyan400, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ─── Status Badge ───────────────────────────────────────────────────
@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Pagato" -> Green500.copy(alpha = 0.15f) to Green400
        "Emesso" -> Cyan500.copy(alpha = 0.15f) to Cyan400
        "Scaduto" -> Red500.copy(alpha = 0.15f) to Red400
        "Parziale" -> Amber500.copy(alpha = 0.15f) to Amber400
        else -> TextMuted.copy(alpha = 0.15f) to TextSecondary
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

// ─── Category Chip ──────────────────────────────────────────────────
@Composable
fun CategoryChip(category: String, icon: String) {
    val color = CategoryColors[category] ?: TextSecondary
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            Text(category, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ─── Item Card (for lists) ──────────────────────────────────────────
@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
            if (onEdit != null || onDelete != null) {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Edit, "Modifica", tint = Cyan400, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Delete, "Elimina", tint = Red400, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Empty State ────────────────────────────────────────────────────
@Composable
fun EmptyState(message: String, icon: ImageVector) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
    }
}

// ─── Confirm Delete Dialog ──────────────────────────────────────────
@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conferma eliminazione") },
        text = { Text("Sei sicuro di voler eliminare \"$itemName\"? Questa azione non può essere annullata.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Elimina", color = Red400) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
        containerColor = DarkSurface
    )
}

// ─── Gradient FAB ───────────────────────────────────────────────────
@Composable
fun GradientFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = Cyan500,
        contentColor = DarkBg
    ) {
        Icon(icon, contentDescription, modifier = Modifier.size(28.dp))
    }
}
