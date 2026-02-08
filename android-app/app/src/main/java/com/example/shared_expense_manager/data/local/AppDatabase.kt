package com.example.shared_expense_manager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.shared_expense_manager.data.local.dao.CategoryDao
import com.example.shared_expense_manager.data.local.dao.ExpenseDao
import com.example.shared_expense_manager.data.local.dao.PendingCategoryDeleteDao
import com.example.shared_expense_manager.data.local.dao.PendingDeleteDao
import com.example.shared_expense_manager.data.local.entity.CategoryEntity
import com.example.shared_expense_manager.data.local.entity.ExpenseEntity
import com.example.shared_expense_manager.data.local.entity.PendingCategoryDeleteEntity
import com.example.shared_expense_manager.data.local.entity.PendingDeleteEntity

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS pending_deletes (expenseId TEXT NOT NULL PRIMARY KEY)")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN pendingUpdate INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS pending_category_deletes (categoryId TEXT NOT NULL PRIMARY KEY)")
    }
}

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class, PendingDeleteEntity::class, PendingCategoryDeleteEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun pendingDeleteDao(): PendingDeleteDao
    abstract fun pendingCategoryDeleteDao(): PendingCategoryDeleteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
