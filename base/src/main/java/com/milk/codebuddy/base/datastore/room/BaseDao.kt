package com.milk.codebuddy.base.datastore.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

/**
 * 通用 CRUD 基础 DAO 接口
 *
 * 业务 DAO 只需继承此接口并添加业务查询方法，无需重复定义 insert/update/delete。
 *
 * 规范：
 * - 所有方法必须是 `suspend` 或返回 `Flow<T>`，禁止同步方法
 * - 多表关联查询加 `@Transaction`
 *
 * 使用示例：
 * ```kotlin
 * @Dao
 * interface UserDao : BaseDao<UserEntity> {
 *     @Query("SELECT * FROM user ORDER BY created_at DESC")
 *     fun observeAll(): Flow<List<UserEntity>>
 * }
 * ```
 *
 * @param T Entity 类型，必须是被 `@Entity` 标注的 data class
 */
interface BaseDao<T> {

    /**
     * 插入单条记录。若主键冲突则忽略（返回 -1L）。
     *
     * @return 新插入行的 rowId，冲突时返回 -1L
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: T): Long

    /**
     * 更新单条记录（按主键匹配）。
     */
    @Update
    suspend fun update(entity: T)

    /**
     * 删除单条记录（按主键匹配）。
     */
    @Delete
    suspend fun delete(entity: T)

    /**
     * 插入或更新：先尝试 insert，若冲突（返回 -1L）则执行 update。
     *
     * 适用于"不知道记录是否已存在"的场景。
     */
    @Transaction
    suspend fun upsert(entity: T) {
        if (insert(entity) == -1L) update(entity)
    }
}
