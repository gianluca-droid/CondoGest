package com.condogest.app.data.repository

import com.condogest.app.data.dao.*
import com.condogest.app.data.model.*
import kotlinx.coroutines.flow.Flow

class CondoRepository(
    private val unitDao: UnitDao,
    private val expenseDao: ExpenseDao,
    private val paymentDao: PaymentDao,
    private val cedolinoDao: CedolinoDao
) {
    val allUnits: Flow<List<CondoUnit>> = unitDao.getAllUnits()
    val unitCount: Flow<Int> = unitDao.getUnitCount()
    val totalMillesimi: Flow<Double?> = unitDao.getTotalMillesimi()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val totalExpenses: Flow<Double?> = expenseDao.getTotalExpenses()
    val expensesByCategory: Flow<List<CategoryTotal>> = expenseDao.getExpensesByGroupedCategory()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val totalPayments: Flow<Double?> = paymentDao.getTotalPayments()
    val allCedolini: Flow<List<Cedolino>> = cedolinoDao.getAllCedolini()
    val allCedoliniWithItems: Flow<List<CedolinoWithItems>> = cedolinoDao.getAllCedoliniWithItems()
    val pendingCedoliniCount: Flow<Int> = cedolinoDao.getPendingCedoliniCount()

    suspend fun getUnitById(id: Long) = unitDao.getUnitById(id)
    suspend fun insertUnit(unit: CondoUnit) = unitDao.insertUnit(unit)
    suspend fun updateUnit(unit: CondoUnit) = unitDao.updateUnit(unit)
    suspend fun deleteUnit(unit: CondoUnit) = unitDao.deleteUnit(unit)
    fun getAllUnitsWithPayments() = unitDao.getAllUnitsWithPayments()

    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    fun getRecentExpenses(limit: Int) = expenseDao.getRecentExpenses(limit)
    fun getExpensesByDateRange(s: Long, e: Long) = expenseDao.getExpensesByDateRange(s, e)

    suspend fun insertPayment(payment: Payment) = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)
    fun getPaymentsByUnit(unitId: Long) = paymentDao.getPaymentsByUnit(unitId)
    fun getRecentPayments(limit: Int) = paymentDao.getRecentPayments(limit)

    suspend fun insertCedolinoWithItems(c: Cedolino, items: List<CedolinoItem>) =
        cedolinoDao.insertCedolinoWithItems(c, items)
    suspend fun updateCedolino(cedolino: Cedolino) = cedolinoDao.updateCedolino(cedolino)
    suspend fun deleteCedolino(cedolino: Cedolino) = cedolinoDao.deleteCedolino(cedolino)
    fun getCedoliniByUnit(unitId: Long) = cedolinoDao.getCedoliniByUnit(unitId)
}
