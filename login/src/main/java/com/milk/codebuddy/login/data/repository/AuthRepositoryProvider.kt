package com.milk.codebuddy.login.data.repository

import com.milk.codebuddy.base.datastore.AppPreferences
import com.milk.codebuddy.base.network.RetrofitFactory
import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.remote.LoginApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

/**
 * 认证模块 Hilt 依赖注入模块
 *
 * 提供 [SessionManager]、[LoginApi]、[AuthRepository] 的单例。
 *
 * 配置方式：在 Application 所在模块的 Hilt 组件中自动生效，
 * 无需手动调用初始化方法。
 *
 * 注入示例（ViewModel）：
 * ```kotlin
 * @HiltViewModel
 * class LoginViewModel @Inject constructor(
 *     private val authRepository: AuthRepository
 * ) : ViewModel()
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideSessionManager(prefs: AppPreferences): SessionManager = SessionManager(prefs)

    @Provides
    @Singleton
    fun provideLoginApi(sessionManager: SessionManager): LoginApi {
        return RetrofitFactory(
            baseUrl = "https://api.example.com/",     // TODO: 替换为 BuildConfig.BASE_URL
            isDebug = false,                          // TODO: 替换为 BuildConfig.DEBUG
            appVersion = "1.0",                       // TODO: 替换为 BuildConfig.VERSION_NAME
            tokenProvider = {
                runBlocking { sessionManager.userSession.firstOrNull()?.accessToken }
            },
            tokenRefresher = { null },
            onTokenExpired = {}
        ).createService()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        loginApi: LoginApi,
        sessionManager: SessionManager
    ): AuthRepository = AuthRepositoryImpl(loginApi, sessionManager)
}
