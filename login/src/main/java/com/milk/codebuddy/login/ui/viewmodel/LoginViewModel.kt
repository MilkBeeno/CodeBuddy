package com.milk.codebuddy.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.login.ui.state.LoginUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onAccountChange(account: String) {
        _uiState.update { it.copy(account = account, accountError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onLoginClick(onLoginSuccess: () -> Unit) {
        // 验证输入
        if (_uiState.value.account.isBlank()) {
            _uiState.update { it.copy(accountError = "请输入账号") }
            return
        }
        if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "请输入密码") }
            return
        }

        // 登录逻辑
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // 模拟网络请求
            delay(1000)
            _uiState.update { it.copy(isLoading = false) }
            onLoginSuccess()
        }
    }

    fun clearErrors() {
        _uiState.update {
            it.copy(
                accountError = null,
                passwordError = null
            )
        }
    }
}
