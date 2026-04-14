package com.milk.codebuddy.base.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 通用 PagingSource 封装，所有纯网络分页继承此类。
 *
 * 统一处理：
 * - [getRefreshKey]：基于 anchorPosition 恢复刷新页码
 * - [load] 框架：异常捕获包装，子类只需实现 [fetchPage]
 *
 * @param T 列表 Item 类型，必须为非空
 *
 * 使用示例：
 * ```kotlin
 * class UserPagingSource @Inject constructor(
 *     private val service: UserService
 * ) : BasePagingSource<UserEntity>() {
 *     override suspend fun fetchPage(page: Int, pageSize: Int): List<UserEntity> =
 *         service.getUsers(page, pageSize).map { it.toDomain() }
 * }
 * ```
 */
abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {

    /**
     * 获取刷新时的起始 key，基于当前可见位置最近的页码 +1。
     */
    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    /**
     * 加载指定页数据，由子类实现具体请求逻辑。
     *
     * @param page     当前页码，从 1 开始
     * @param pageSize 每页条数，由 [PagingConfig.pageSize] 决定
     * @return 当页数据列表；返回空列表表示已到末尾
     * @throws Exception 抛出任何异常均会被转为 [LoadResult.Error]
     */
    abstract suspend fun fetchPage(page: Int, pageSize: Int): List<T>

    final override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: INITIAL_PAGE
        return try {
            val items = fetchPage(page, params.loadSize)
            LoadResult.Page(
                data = items,
                prevKey = if (page == INITIAL_PAGE) null else page - 1,
                nextKey = if (items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        /** 起始页码 */
        const val INITIAL_PAGE = 1
    }
}
