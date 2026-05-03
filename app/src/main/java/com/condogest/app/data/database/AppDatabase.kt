package com.condogest.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.condogest.app.data.dao.*
import com.condogest.app.data.model.*

@Database(
    entities = [
        Condominio::class,
        CondoUnit::class,
        Expense::class,
        Payment::class,
        Cedolino::class,
        CedolinoItem::class,
        Documento::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun condominioDao(): CondominioDao
    abstract fun unitDao(): UnitDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun cedolinoDao(): CedolinoDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "condogest_v3"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
