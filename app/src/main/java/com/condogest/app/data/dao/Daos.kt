package com.condogest.app.data.dao

import androidx.room.*
import com.condogest.app.data.model.*
import kotlinx.coroutines.flow.Flow

// ─── Condominio DAO ─────────────────────────────────────────────────
@Dao
interface CondominioDao {
    @Query("SELECT * FROM condomini ORDER BY nome ASC")
    fun getAllCondomini(): Flow<List<Condominio>>

    @Query("SELECT * FROM condomini WHERE id = :id")
    suspend fun getCondominioById(id: Long): Condominio?

    @Query("SELECT COUNT(*) FROM condomini")
    fun getCondominioCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCondominio(condominio: Condominio): Long

    @Update
    suspend fun updateCondominio(condominio: Condominio)

    @Delete
    suspend fun deleteCondominio(condominio: Condominio)
}

// ─── Unit DAO ───────────────────────────────────────────────────────
@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE condominioId = :condominioId ORDER BY number ASC")
    fun getUnitsByCondominio(condominioId: Long): Flow<List<CondoUnit>>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getUnitById(id: Long): CondoUnit?

    @Query("SELECT SUM(millesimi) FROM units WHERE condominioId = :condominioId")
    fun getTotalMillesimi(condominioId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: CondoUnit): Long

    @Update
    suspend fun updateUnit(unit: CondoUnit)

    @Delete
    suspend fun deleteUnit(unit: CondoUnit)

    @Query("SELECT COUNT(*) FROM units WHERE condominioId = :condominioId")
    fun getUnitCount(condominioId: Long): Flow<Int>

    @Transaction
    @Query("SELECT * FROM units WHERE condominioId = :condominioId")
    fun getAllUnitsWithPayments(condominioId: Long): Flow<List<UnitWithPayments>>
}

// ─── Expense DAO ────────────────────────────────────────────────────
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE condominioId = :condominioId ORDER BY date DESC")
    fun getExpensesByCondominio(condominioId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE condominioId = :condominioId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(condominioId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE condominioId = :condominioId")
    fun getTotalExpenses(condominioId: Long): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE condominioId = :condominioId GROUP BY category ORDER BY total DESC")
    fun getExpensesByGroupedCategory(condominioId: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE condominioId = :condominioId ORDER BY date DESC LIMIT :limit")
    fun getRecentExpenses(condominioId: Long, limit: Int): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

data class CategoryTotal(val category: String, val total: Double)

// ─── Payment DAO ────────────────────────────────────────────────────
@Dao
interface PaymentDao {
    @Query("SELECT p.* FROM payments p JOIN units u ON p.unitId = u.id WHERE u.condominioId = :condominioId ORDER BY p.date DESC")
    fun getPaymentsByCondominio(condominioId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE unitId = :unitId ORDER BY date DESC")
    fun getPaymentsByUnit(unitId: Long): Flow<List<Payment>>

    @Query("SELECT SUM(p.amount) FROM payments p JOIN units u ON p.unitId = u.id WHERE u.condominioId = :condominioId")
    fun getTotalPayments(condominioId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE unitId = :unitId")
    fun getTotalPaymentsByUnit(unitId: Long): Flow<Double?>

    @Query("SELECT p.* FROM payments p JOIN units u ON p.unitId = u.id WHERE u.condominioId = :condominioId ORDER BY p.date DESC LIMIT :limit")
    fun getRecentPayments(condominioId: Long, limit: Int): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)
}

// ─── Cedolino DAO ───────────────────────────────────────────────────
@Dao
interface CedolinoDao {
    @Transaction
    @Query("SELECT c.* FROM cedolini c JOIN units u ON c.unitId = u.id WHERE u.condominioId = :condominioId ORDER BY c.issueDate DESC")
    fun getAllCedoliniWithItems(condominioId: Long): Flow<List<CedolinoWithItems>>

    @Query("SELECT c.* FROM cedolini c JOIN units u ON c.unitId = u.id WHERE u.condominioId = :condominioId ORDER BY c.issueDate DESC")
    fun getAllCedolini(condominioId: Long): Flow<List<Cedolino>>

    @Query("SELECT * FROM cedolini WHERE id = :id")
    suspend fun getCedolinoById(id: Long): Cedolino?

    @Transaction
    @Query("SELECT * FROM cedolini WHERE id = :id")
    suspend fun getCedolinoWithItems(id: Long): CedolinoWithItems?

    @Query("SELECT * FROM cedolini WHERE unitId = :unitId ORDER BY issueDate DESC")
    fun getCedoliniByUnit(unitId: Long): Flow<List<Cedolino>>

    @Query("SELECT COUNT(*) FROM cedolini c JOIN units u ON c.unitId = u.id WHERE u.condominioId = :condominioId AND (c.status = 'Emesso' OR c.status = 'Scaduto')")
    fun getPendingCedoliniCount(condominioId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCedolino(cedolino: Cedolino): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCedolinoItems(items: List<CedolinoItem>)

    @Update
    suspend fun updateCedolino(cedolino: Cedolino)

    @Delete
    suspend fun deleteCedolino(cedolino: Cedolino)

    @Query("DELETE FROM cedolino_items WHERE cedolinoId = :cedolinoId")
    suspend fun deleteCedolinoItems(cedolinoId: Long)

    @Transaction
    suspend fun insertCedolinoWithItems(cedolino: Cedolino, items: List<CedolinoItem>) {
        val cedolinoId = insertCedolino(cedolino)
        insertCedolinoItems(items.map { it.copy(cedolinoId = cedolinoId) })
    }
}

// ─── Documento DAO ──────────────────────────────────────────────────
@Dao
interface DocumentoDao {
    @Query("SELECT * FROM documents WHERE condominioId = :condominioId ORDER BY dataInserimento DESC")
    fun getDocumentiByCondominio(condominioId: Long): Flow<List<Documento>>

    @Query("SELECT * FROM documents WHERE condominioId = :condominioId AND categoria = :categoria ORDER BY dataInserimento DESC")
    fun getDocumentiByCategoria(condominioId: Long, categoria: String): Flow<List<Documento>>

    @Query("SELECT COUNT(*) FROM documents WHERE condominioId = :condominioId")
    fun getDocumentCount(condominioId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumento(documento: Documento): Long

    @Delete
    suspend fun deleteDocumento(documento: Documento)
}
