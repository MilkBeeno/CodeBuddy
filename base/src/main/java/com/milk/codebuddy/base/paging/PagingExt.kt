package com.milk.codebuddy.base.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.RemoteMediator
import kotlinx.coroutines.flow.Flow

/** 默认每页加载条数 */
const val DEFAULT_PAGE_SIZE = 20

/** 默认预加载距离（距底部多少条时触发下一页） */
const val DEFAULT_PREFETCH_DISTANCE = DEFAULT_PAGE_SIZE

/**
 * 构建纯网络分页 [Flow]，统一 [PagingConfig] 默认参数。
 *
 * @param pageSize         每页条数，默认 [DEFAULT_PAGE_SIZE]
 * @param prefetchDistance 预加载距离，默认等于 [pageSize]
 * @param enablePlaceholders 是否启用占位符，默认关闭
 * @param source           [PagingSource] 工厂函数
 *
 * 使用示例：
 * ```kotlin
 * val pagingFlow = buildPager { UserPagingSource(service) }
 *     .cachedIn(viewModelScope)
 * ```
 */
fun <T : Any> buildPager(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    prefetchDistance: Int = pageSize,
    enablePlaceholders: Boolean = false,
    source: () -> PagingSource<Int, T>
): Flow<PagingData<T>> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        enablePlaceholders = enablePlaceholders
    ),
    pagingSourceFactory = source
).flow

/**
 * 构建离线优先分页 [Flow]（Room + RemoteMediator）。
 *
 * UI 始终观察本地数据库，网络数据经 [mediator] 写入 Room 后自动刷新。
 *
 * @param pageSize  每页条数，默认 [DEFAULT_PAGE_SIZE]
 * @param mediator  [RemoteMediator] 实例，负责网络请求 + 数据库写入
 * @param source    Room DAO 的 `PagingSource` 工厂函数
 *
 * 使用示例：
 * ```kotlin
 * val pagingFlow = buildPagerWithMediator(
 *     mediator = userRemoteMediator
 * ) { userDao.pagingSource() }.cachedIn(viewModelScope)
 * ```
 */
@OptIn(ExperimentalPagingApi::class)
fun <T : Any> buildPagerWithMediator(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    mediator: RemoteMediator<Int, T>,
    source: () -> PagingSource<Int, T>
): Flow<PagingData<T>> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = pageSize,
        enablePlaceholders = false
    ),
    remoteMediator = mediator,
    pagingSourceFactory = source
).flow
