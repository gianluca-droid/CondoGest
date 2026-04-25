package com.condogest.app.data.dao

import androidx.room.*
import com.condogest.app.data.model.*
import kotlinx.coroutines.flow.Flow

// ─── Unit DAO ───────────────────────────────────────────────────────
@Dao
interface UnitDao {
    @Query("SELECT * FROM units ORDER BY number ASC")
    fun getAllUnits(): Flow<List<CondoUnit>>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getUnitById(id: Long): CondoUnit?

    @Query("SELECT SUM(millesimi) FROM units")
    fun getTotalMillesimi(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: CondoUnit): Long

    @Update
    suspend fun updateUnit(unit: CondoUnit)

    @Delete
    suspend fun deleteUnit(unit: CondoUnit)

    @Query("SELECT COUNT(*) FROM units")
    fun getUnitCount(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM units WHERE id = :unitId")
    fun getUnitWithPayments(unitId: Long): Flow<UnitWithPayments?>

    @Transaction
    @Query("SELECT * FROM units")
    fun getAllUnitsWithPayments(): Flow<List<UnitWithPayments>>
}

// ─── Expense DAO ────────────────────────────────────────────────────
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses GROUP BY category ORDER BY total DESC")
    fun getExpensesByGroupedCategory(): Flow<List<CategoryTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int): Flow<List<Expense>>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)

// ─── Payment DAO ────────────────────────────────────────────────────
@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): Payment?

    @Query("SELECT * FROM payments WHERE unitId = :unitId ORDER BY date DESC")
    fun getPaymentsByUnit(unitId: Long): Flow<List<Payment>>

    @Query("SELECT SUM(amount) FROM payments")
    fun getTotalPayments(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE unitId = :unitId")
    fun getTotalPaymentsByUnit(unitId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalPaymentsByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM payments WHERE method = :method ORDER BY date DESC")
    fun getPaymentsByMethod(method: String): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM payments ORDER BY date DESC LIMIT :limit")
    fun getRecentPayments(limit: Int): Flow<List<Payment>>
}

// ─── Cedolino DAO ───────────────────────────────────────────────────
@Dao
interface CedolinoDao {
    @Transaction
    @Query("SELECT * FROM cedolini ORDER BY issueDate DESC")
    fun getAllCedoliniWithItems(): Flow<List<CedolinoWithItems>>

    @Query("SELECT * FROM cedolini ORDER BY issueDate DESC")
    fun getAllCedolini(): Flow<List<Cedolino>>

    @Query("SELECT * FROM cedolini WHERE id = :id")
    suspend fun getCedolinoById(id: Long): Cedolino?

    @Transaction
    @Query("SELECT * FROM cedolini WHERE id = :id")
    suspend fun getCedolinoWithItems(id: Long): CedolinoWithItems?

    @Query("SELECT * FROM cedolini WHERE unitId = :unitId ORDER BY issueDate DESC")
    fun getCedoliniByUnit(unitId: Long): Flow<List<Cedolino>>

    @Query("SELECT * FROM cedolini WHERE status = :status ORDER BY issueDate DESC")
    fun getCedoliniByStatus(status: String): Flow<List<Cedolino>>

    @Query("SELECT COUNT(*) FROM cedolini WHERE status = 'Emesso' OR status = 'Scaduto'")
    fun getPendingCedoliniCount(): Flow<Int>

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
        val itemsWithId = items.map { it.copy(cedolinoId = cedolinoId) }
        insertCedolinoItems(itemsWithId)
    }
}
