package com.milk.codebuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.milk.codebuddy.base.ui.navigation.ProvideNavHostController
import com.milk.codebuddy.base.ui.navigation.Splash
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.login.ui.navigation.forgotPasswordScreen
import com.milk.codebuddy.login.ui.navigation.loginScreen
import com.milk.codebuddy.login.ui.navigation.registerScreen
import com.milk.codebuddy.login.ui.navigation.resetPasswordScreen
import com.milk.codebuddy.login.ui.navigation.splashScreen
import com.milk.codebuddy.main.ui.navigation.addTransactionScreen
import com.milk.codebuddy.main.ui.navigation.mainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    ProvideNavHostController(navController) {
        NavHost(
            navController = navController,
            startDestination = Splash,
            modifier = Modifier.fillMaxSize(),
            builder = {
                splashScreen()
                loginScreen()
                registerScreen()
                forgotPasswordScreen()
                resetPasswordScreen()
                mainScreen()
                addTransactionScreen()
            }
        )
    }
}
