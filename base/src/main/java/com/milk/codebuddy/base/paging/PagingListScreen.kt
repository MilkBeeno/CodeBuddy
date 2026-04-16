package com.milk.codebuddy.base.paging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey

/**
 * 通用分页列表容器，自动处理四种状态：
 * - **首次加载中**：全屏 [loadingContent]
 * - **首次加载失败**：全屏 [errorContent]
 * - **列表为空**：全屏 [emptyContent]
 * - **追加加载中/失败**：列表底部 [appendLoadingContent] / [appendErrorContent]
 *
 * @param T            列表 Item 类型
 * @param items        通过 `collectAsLazyPagingItems()` 获取的分页数据
 * @param modifier     Modifier
 * @param listState    LazyColumn 滚动状态，外部可控制滚动
 * @param contentPadding LazyColumn 内边距
 * @param verticalArrangement LazyColumn 垂直间距
 * @param loadingContent  首次加载中 UI，默认居中 [CircularProgressIndicator]
 * @param errorContent    首次加载失败 UI，默认文字 + 重试按钮；参数为重试回调
 * @param emptyContent    空列表 UI，默认提示文字
 * @param appendLoadingContent 追加加载中 UI，默认列表底部小圆圈
 * @param appendErrorContent   追加加载失败 UI，默认文字 + 重试按钮；参数为重试回调
 * @param itemContent  单条 Item 渲染，item 可能为 null（占位符），需自行处理
 *
 * 使用示例：
 * ```kotlin
 * val items = viewModel.pagingFlow.collectAsLazyPagingItems()
 *
 * PagingListScreen(
 *     items = items,
 *     itemContent = { item ->
 *         if (item != null) UserCard(user = item)
 *     }
 * )
 * ```
 */
@Composable
fun <T : Any> PagingListScreen(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (retry: () -> Unit) -> Unit = { retry ->
        DefaultErrorContent(retry = retry)
    },
    emptyContent: @Composable () -> Unit = { DefaultEmptyContent() },
    appendLoadingContent: @Composable () -> Unit = { DefaultAppendLoadingContent() },
    appendErrorContent: @Composable (retry: () -> Unit) -> Unit = { retry ->
        DefaultAppendErrorContent(retry = retry)
    },
    extraItems: (LazyListScope.() -> Unit)? = null,
    itemContent: @Composable (item: T?) -> Unit
) {
    val refreshState = items.loadState.refresh

    when {
        // 首次加载中
        refreshState is LoadState.Loading && items.itemCount == 0 -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                loadingContent()
            }
        }
        // 首次加载失败
        refreshState is LoadState.Error && items.itemCount == 0 -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                errorContent { items.refresh() }
            }
        }
        // 空列表（加载成功但无数据）
        refreshState is LoadState.NotLoading && items.itemCount == 0 -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                emptyContent()
            }
        }
        // 正常列表
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                state = listState,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement
            ) {
                items(
                    count = items.itemCount,
                    key = items.itemKey()
                ) { index ->
                    itemContent(items[index])
                }

                // 额外 Item（Header/Footer 等）
                extraItems?.invoke(this)

                // 追加加载状态
                item {
                    when (items.loadState.append) {
                        is LoadState.Loading -> appendLoadingContent()
                        is LoadState.Error   -> appendErrorContent { items.retry() }
                        else                 -> Unit
                    }
                }
            }
        }
    }
}

// ─── 默认 UI 组件 ────────────────────────────────────────────────────────────

/**
 * 默认首次加载中：全屏居中圆形进度条。
 * 可通过 [PagingListScreen] 的 `loadingContent` 参数替换。
 */
@Composable
fun DefaultLoadingContent() {
    CircularProgressIndicator()
}

/**
 * 默认首次加载失败：居中文字 + 重试按钮。
 * 可通过 [PagingListScreen] 的 `errorContent` 参数替换。
 */
@Composable
fun DefaultErrorContent(
    message: String = "加载失败，请重试",
    retry: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = retry) {
            Text(text = "重试")
        }
    }
}

/**
 * 默认空列表：居中提示文字。
 * 可通过 [PagingListScreen] 的 `emptyContent` 参数替换。
 */
@Composable
fun DefaultEmptyContent(message: String = "暂无数据") {
    Text(text = message, style = MaterialTheme.typography.bodyMedium)
}

/**
 * 默认追加加载中：列表底部居中小圆圈。
 * 可通过 [PagingListScreen] 的 `appendLoadingContent` 参数替换。
 */
@Composable
fun DefaultAppendLoadingContent() {
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}

/**
 * 默认追加加载失败：列表底部文字 + 重试按钮。
 * 可通过 [PagingListScreen] 的 `appendErrorContent` 参数替换。
 */
@Composable
fun DefaultAppendErrorContent(
    message: String = "加载更多失败",
    retry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = retry) {
            Text(text = "重试")
        }
    }
}
