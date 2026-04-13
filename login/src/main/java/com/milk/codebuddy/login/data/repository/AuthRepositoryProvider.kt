package com.milk.codebuddy.login.data.repository

import android.content.Context
import com.milk.codebuddy.base.datastore.AppPreferences
import com.milk.codebuddy.base.network.RetrofitFactory
import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.remote.LoginApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 认证仓库单例提供者
 *
 * 在引入 Hilt 之前用于跨 Navigation 扩展函数统一共享 [AuthRepository] 实例。
 * 必须在应用启动时（如 Application.onCreate 或 MainActivity.onCreate）调用 [init] 完成初始化，
 * 之后通过 [get] 获取实例。
 *
 * 使用示例（Application/MainActivity 初始化）：
 * ```kotlin
 * AuthRepositoryProvider.init(context, baseUrl = "https://api.example.com/", isDebug = BuildConfig.DEBUG)
 * ```
 *
 * 使用示例（Navigation 层获取）：
 * ```kotlin
 * val factory = AuthViewModelFactory(AuthRepositoryProvider.get())
 * val vm = viewModel<LoginViewModel>(factory = factory)
 * ```
 */
object AuthRepositoryProvider {

    @Volatile
    private var instance: AuthRepository? = null

    /**
     * 初始化认证仓库，应在应用启动时调用一次。
     *
     * @param context  Application Context
     * @param baseUrl  服务器基础地址
     * @param isDebug  是否开启网络日志
     */
    fun init(context: Context, baseUrl: String, isDebug: Boolean) {
        if (instance != null) return
        synchronized(this) {
            if (instance != null) return
            val prefs = AppPreferences(context.applicationContext)
            val sessionManager = SessionManager(prefs)
            val loginApi = RetrofitFactory(
                baseUrl = baseUrl,
                isDebug = isDebug,
                tokenProvider = { runBlocking { sessionManager.userSession.first().accessToken } },
                tokenRefresher = { null },
                onTokenExpired = {}
            ).createService<LoginApi>()
            instance = AuthRepositoryImpl(loginApi, sessionManager)
        }
    }

    /**
     * 获取 [AuthRepository] 单例，必须在 [init] 之后调用。
     */
    fun get(): AuthRepository = requireNotNull(instance) {
        "AuthRepositoryProvider 未初始化，请先调用 init()"
    }
}
