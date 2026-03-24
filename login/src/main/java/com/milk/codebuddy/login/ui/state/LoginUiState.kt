package com.milk.codebuddy.login.ui.state

data class LoginUiState(
    val account: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val accountError: String? = null,
    val passwordError: String? = null
)
