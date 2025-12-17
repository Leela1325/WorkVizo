package com.workvizo.navigation
import com.workvizo.ui.theme.login.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.workvizo.ui.theme.welcome.SplashScreen
import com.workvizo.ui.theme.welcome.GetStartedScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // Splash Screen
        composable("splash") {
            SplashScreen(navController)
        }
        composable("reset_password/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                navController = navController,
                email = email
            )
        }

        composable("password_changed") {
            PasswordChangedScreen(navController)
        }

        composable("forgot") {
            ForgotPasswordScreen(navController)
        }



        // Get Started Screen (inside welcome package)
        composable("get_started") {
            GetStartedScreen(navController)
        }
        composable("register") { RegisterScreen(navController) }

        // Temporary Home Screen
        composable("home") {
            HomeScreenPlaceholder()
        }
        composable("onboard") {
            OnboardingScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

    }
}

@Composable
fun HomeScreenPlaceholder() {
    Text(text = "Home Screen Coming Soon…")
}
