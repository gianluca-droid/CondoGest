package com.condogest.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.condogest.app.CondominioManager
import com.condogest.app.data.SampleData
import com.condogest.app.data.database.AppDatabase
import com.condogest.app.data.model.*
import com.condogest.app.data.repository.CondoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CondoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = CondoRepository(
        db.condominioDao(), db.unitDao(), db.expenseDao(),
        db.paymentDao(), db.cedolinoDao(), db.documentoDao()
    )

    // ─── Condominio Attivo ───────────────────────────────────────
    private val _activeCondominioId = MutableStateFlow(
        CondominioManager.getActiveCondominioId(application)
    )
    val activeCondominioId: StateFlow<Long> = _activeCondominioId

    val activeCondominio: StateFlow<Condominio?> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { id -> flow { emit(repository.getCondominioById(id)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCondomini: StateFlow<List<Condominio>> = repository.allCondomini
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setActiveCondominio(id: Long) {
        CondominioManager.setActiveCondominioId(getApplication(), id)
        _activeCondominioId.value = id
    }

    fun clearActiveCondominio() {
        CondominioManager.clearActiveCondominio(getApplication())
        _activeCondominioId.value = -1L
    }

    // ─── State Flows (dipendono dal condominio attivo) ───────────
    val units: StateFlow<List<CondoUnit>> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getUnitsByCondominio(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getExpensesByCondominio(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getPaymentsByCondominio(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cedolini: StateFlow<List<Cedolino>> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getAllCedolini(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cedoliniWithItems: StateFlow<List<CedolinoWithItems>> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getAllCedoliniWithItems(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenses: StateFlow<Double> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getTotalExpenses(it).map { v -> v ?: 0.0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPayments: StateFlow<Double> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getTotalPayments(it).map { v -> v ?: 0.0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingCedolini: StateFlow<Int> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getPendingCedoliniCount(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expensesByCategory = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getExpensesByGroupedCategory(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documenti: StateFlow<List<Documento>> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getDocumentiByCondominio(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documentCount: StateFlow<Int> = _activeCondominioId
        .filter { it > 0 }
        .flatMapLatest { repository.getDocumentCount(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            val count = repository.condominioCount.first()
            if (count == 0) {
                val defaultCondoId = SampleData.populateDatabase(repository)
                setActiveCondominio(defaultCondoId)
            }
            _isLoading.value = false
        }
    }

    // ─── Condominio CRUD ─────────────────────────────────────────
    fun addCondominio(condominio: Condominio, andSelect: Boolean = false) =
        viewModelScope.launch {
            val id = repository.insertCondominio(condominio)
            if (andSelect) setActiveCondominio(id)
        }

    fun updateCondominio(condominio: Condominio) = viewModelScope.launch {
        repository.updateCondominio(condominio)
    }

    fun deleteCondominio(condominio: Condominio) = viewModelScope.launch {
        repository.deleteCondominio(condominio)
        if (_activeCondominioId.value == condominio.id) clearActiveCondominio()
    }

    // ─── Unit CRUD ───────────────────────────────────────────────
    fun addUnit(unit: CondoUnit) = viewModelScope.launch { repository.insertUnit(unit) }
    fun updateUnit(unit: CondoUnit) = viewModelScope.launch { repository.updateUnit(unit) }
    fun deleteUnit(unit: CondoUnit) = viewModelScope.launch { repository.deleteUnit(unit) }

    // ─── Expense CRUD ────────────────────────────────────────────
    fun addExpense(expense: Expense) = viewModelScope.launch { repository.insertExpense(expense) }
    fun updateExpense(expense: Expense) = viewModelScope.launch { repository.updateExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch { repository.deleteExpense(expense) }

    // ─── Payment CRUD ────────────────────────────────────────────
    fun addPayment(payment: Payment) = viewModelScope.launch { repository.insertPayment(payment) }
    fun updatePayment(payment: Payment) = viewModelScope.launch { repository.updatePayment(payment) }
    fun deletePayment(payment: Payment) = viewModelScope.launch { repository.deletePayment(payment) }

    // ─── Cedolino CRUD ───────────────────────────────────────────
    fun addCedolinoWithItems(cedolino: Cedolino, items: List<CedolinoItem>) =
        viewModelScope.launch { repository.insertCedolinoWithItems(cedolino, items) }

    fun updateCedolino(cedolino: Cedolino) = viewModelScope.launch { repository.updateCedolino(cedolino) }
    fun deleteCedolino(cedolino: Cedolino) = viewModelScope.launch { repository.deleteCedolino(cedolino) }

    fun markCedolinoPaid(cedolino: Cedolino) = viewModelScope.launch {
        repository.updateCedolino(cedolino.copy(status = "Pagato", paidAmount = cedolino.total, paidDate = System.currentTimeMillis()))
    }

    fun generateCedoliniForAllUnits(period: String, dueDate: Long) = viewModelScope.launch {
        val condId = _activeCondominioId.value.takeIf { it > 0 } ?: return@launch
        val currentUnits = units.value
        val currentExpenses = expenses.value
        val totalMillesimi = currentUnits.sumOf { it.millesimi }
        if (totalMillesimi <= 0 || currentExpenses.isEmpty()) return@launch
        for (unit in currentUnits) {
            val share = unit.millesimi / totalMillesimi
            val items = currentExpenses.map { exp ->
                CedolinoItem(cedolinoId = 0, description = "${exp.category}: ${exp.description}",
                    amount = Math.round(exp.amount * share * 100.0) / 100.0)
            }
            repository.insertCedolinoWithItems(
                Cedolino(unitId = unit.id, period = period,
                    issueDate = System.currentTimeMillis(), dueDate = dueDate,
                    total = items.sumOf { it.amount }, status = "Emesso"),
                items
            )
        }
    }

    // ─── Documento CRUD ──────────────────────────────────────────
    fun addDocumento(uri: Uri, titolo: String, categoria: String, note: String, mimeType: String) =
        viewModelScope.launch {
            val condId = _activeCondominioId.value.takeIf { it > 0 } ?: return@launch
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>()
                val docsDir = File(context.filesDir, "documents").also { it.mkdirs() }
                val originalName = uri.lastPathSegment?.substringAfterLast('/') ?: "documento"
                val destFile = File(docsDir, "${System.currentTimeMillis()}_$originalName")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                repository.insertDocumento(Documento(
                    condominioId = condId,
                    titolo = titolo, categoria = categoria,
                    fileType = FileTypes.fromMimeType(mimeType),
                    filePath = destFile.absolutePath, fileName = originalName,
                    fileSize = destFile.length(), note = note
                ))
            }
        }

    fun deleteDocumento(documento: Documento) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            File(documento.filePath).takeIf { it.exists() }?.delete()
            repository.deleteDocumento(documento)
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────
    fun getUnitName(unitId: Long) =
        units.value.find { it.id == unitId }?.let { "Int. ${it.number} - ${it.ownerName}" } ?: "Sconosciuto"
}
