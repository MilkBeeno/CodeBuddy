package com.milk.codebuddy.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.milk.codebuddy.login.data.repository.AuthRepository

/**
 * 认证模块 ViewModel 工厂
 *
 * 职责：将 [AuthRepository] 注入到认证相关的 ViewModel，避免在 ViewModel 内部手动 new 依赖。
 * 支持含 [androidx.lifecycle.SavedStateHandle] 参数的 ViewModel（如 [ResetPasswordViewModel]）。
 *
 * 使用示例（Navigation 层）：
 * ```kotlin
 * val factory = AuthViewModelFactory(repository)
 * composable<Login> {
 *     val vm = viewModel<LoginViewModel>(factory = factory)
 * }
 * composable<ResetPassword> {
 *     val vm = viewModel<ResetPasswordViewModel>(factory = factory)
 * }
 * ```
 */
class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(authRepository) as T
        modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) ->
            ForgotPasswordViewModel(authRepository) as T
        modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
            RegisterViewModel(authRepository) as T
        modelClass.isAssignableFrom(ResetPasswordViewModel::class.java) ->
            ResetPasswordViewModel(authRepository, extras.createSavedStateHandle()) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
