package com.condogest.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.condogest.app.data.dao.*
import com.condogest.app.data.model.*

@Database(
    entities = [
        CondoUnit::class,
        Expense::class,
        Payment::class,
        Cedolino::class,
        CedolinoItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun unitDao(): UnitDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun cedolinoDao(): CedolinoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "condogest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
