package com.condogest.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.condogest.app.data.SampleData
import com.condogest.app.data.database.AppDatabase
import com.condogest.app.data.model.*
import com.condogest.app.data.repository.CondoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CondoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = CondoRepository(
        database.unitDao(), database.expenseDao(),
        database.paymentDao(), database.cedolinoDao(),
        database.documentoDao()
    )

    // ─── State Flows ────────────────────────────────────────────
    val units: StateFlow<List<CondoUnit>> = repository.allUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cedolini: StateFlow<List<Cedolino>> = repository.allCedolini
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cedoliniWithItems: StateFlow<List<CedolinoWithItems>> = repository.allCedoliniWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPayments: StateFlow<Double> = repository.totalPayments
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingCedolini: StateFlow<Int> = repository.pendingCedoliniCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expensesByCategory = repository.expensesByCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documenti: StateFlow<List<Documento>> = repository.allDocumenti
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documentCount: StateFlow<Int> = repository.documentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            val count = repository.unitCount.first()
            if (count == 0) {
                SampleData.populateDatabase(repository)
            }
            _isLoading.value = false
        }
    }

    // ─── Unit Operations ────────────────────────────────────────
    fun addUnit(unit: CondoUnit) = viewModelScope.launch {
        repository.insertUnit(unit)
    }

    fun updateUnit(unit: CondoUnit) = viewModelScope.launch {
        repository.updateUnit(unit)
    }

    fun deleteUnit(unit: CondoUnit) = viewModelScope.launch {
        repository.deleteUnit(unit)
    }

    // ─── Expense Operations ─────────────────────────────────────
    fun addExpense(expense: Expense) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // ─── Payment Operations ─────────────────────────────────────
    fun addPayment(payment: Payment) = viewModelScope.launch {
        repository.insertPayment(payment)
    }

    fun updatePayment(payment: Payment) = viewModelScope.launch {
        repository.updatePayment(payment)
    }

    fun deletePayment(payment: Payment) = viewModelScope.launch {
        repository.deletePayment(payment)
    }

    // ─── Cedolino Operations ────────────────────────────────────
    fun addCedolinoWithItems(cedolino: Cedolino, items: List<CedolinoItem>) =
        viewModelScope.launch {
            repository.insertCedolinoWithItems(cedolino, items)
        }

    fun updateCedolino(cedolino: Cedolino) = viewModelScope.launch {
        repository.updateCedolino(cedolino)
    }

    fun deleteCedolino(cedolino: Cedolino) = viewModelScope.launch {
        repository.deleteCedolino(cedolino)
    }

    fun markCedolinoPaid(cedolino: Cedolino) = viewModelScope.launch {
        repository.updateCedolino(
            cedolino.copy(
                status = "Pagato",
                paidAmount = cedolino.total,
                paidDate = System.currentTimeMillis()
            )
        )
    }

    // ─── Generazione Cedolini per tutte le unità ────────────────
    fun generateCedoliniForAllUnits(period: String, dueDate: Long) =
        viewModelScope.launch {
            val currentUnits = units.value
            val currentExpenses = expenses.value
            val totalMillesimi = currentUnits.sumOf { it.millesimi }
            if (totalMillesimi <= 0 || currentExpenses.isEmpty()) return@launch

            val totalExpenseAmount = currentExpenses.sumOf { it.amount }

            for (unit in currentUnits) {
                val share = unit.millesimi / totalMillesimi
                val items = currentExpenses.map { exp ->
                    CedolinoItem(
                        cedolinoId = 0,
                        description = "${exp.category}: ${exp.description}",
                        amount = Math.round(exp.amount * share * 100.0) / 100.0
                    )
                }
                val total = items.sumOf { it.amount }
                val cedolino = Cedolino(
                    unitId = unit.id, period = period,
                    issueDate = System.currentTimeMillis(), dueDate = dueDate,
                    total = total, status = "Emesso"
                )
                repository.insertCedolinoWithItems(cedolino, items)
            }
        }

    // ─── Documento Operations ────────────────────────────────────
    fun addDocumento(uri: Uri, titolo: String, categoria: String, note: String) =
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>()
                val docsDir = File(context.filesDir, "documents").also { it.mkdirs() }
                val originalName = uri.lastPathSegment?.substringAfterLast('/') ?: "documento.pdf"
                val destFile = File(docsDir, "${System.currentTimeMillis()}_$originalName")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                val documento = Documento(
                    titolo = titolo,
                    categoria = categoria,
                    filePath = destFile.absolutePath,
                    fileName = originalName,
                    fileSize = destFile.length(),
                    note = note
                )
                repository.insertDocumento(documento)
            }
        }

    fun deleteDocumento(documento: Documento) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            File(documento.filePath).takeIf { it.exists() }?.delete()
            repository.deleteDocumento(documento)
        }
    }

    fun getDocumentiByCategoria(categoria: String) =
        repository.getDocumentiByCategoria(categoria)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Helpers ────────────────────────────────────────────────
    fun getUnitName(unitId: Long): String {
        return units.value.find { it.id == unitId }?.let {
            "Int. ${it.number} - ${it.ownerName}"
        } ?: "Sconosciuto"
    }
}
