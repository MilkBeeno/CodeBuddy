package com.milk.codebuddy.base.datastore.room

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.milk.codebuddy.base.datastore.room.converter.CommonConverters

/**
 * 全局 Room 数据库单例
 *
 * 规范：
 * - 禁止业务模块自行调用 `Room.databaseBuilder`，统一通过此类访问
 * - 新增 Entity 时同步更新 `entities` 数组并递增 `version`，补充对应 Migration
 * - 线上版本禁止使用 `fallbackToDestructiveMigration()`，必须编写 Migration
 *
 * 接入新 Entity 步骤：
 * 1. 在 `@Database(entities = [...])` 中追加新 Entity 类
 * 2. 递增 `version`
 * 3. 在 `companion object` 中添加对应 `MIGRATION_x_y`
 * 4. 在 `addMigrations(...)` 中注册
 * 5. 添加对应 DAO 抽象方法
 *
 * Migration 示例：
 * ```kotlin
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE user ADD COLUMN bio TEXT NOT NULL DEFAULT ''")
 *     }
 * }
 * ```
 */

/** 内部占位 Entity，保证数据库可编译，业务接入时替换为真实 Entity */
@Entity(tableName = "_placeholder")
internal data class PlaceholderEntity(
    @PrimaryKey val id: Int = 0
)

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(CommonConverters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {

        /** 数据库文件名 */
        const val NAME = "app_database"

        /**
         * 创建数据库实例（仅供 DI 框架调用，业务代码禁止直接调用）
         *
         * @param context ApplicationContext
         */
        internal fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, NAME)
                .fallbackToDestructiveMigrationOnDowngrade()
                // 线上版本在此注册 Migration：.addMigrations(MIGRATION_1_2)
                .build()
    }
}
