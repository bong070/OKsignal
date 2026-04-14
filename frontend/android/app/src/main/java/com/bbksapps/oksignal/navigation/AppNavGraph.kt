package com.bbksapps.oksignal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bbksapps.oksignal.ui.screens.LoginScreen
import com.bbksapps.oksignal.ui.screens.SignUpScreen
import com.bbksapps.oksignal.ui.screens.SplashScreen
import com.bbksapps.oksignal.ui.screens.GuardianHomeScreen
import com.bbksapps.oksignal.ui.screens.MemberHomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToGuardian = {
                    navController.navigate(Screen.GuardianHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMember = {
                    navController.navigate(Screen.MemberHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onGoogleLoginClick = { },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpClick = { _, _, _ -> },
                onGoogleSignUpClick = { },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.GuardianHome.route) {
            GuardianHomeScreen()
        }

        composable(Screen.MemberHome.route) {
            MemberHomeScreen()
        }
    }
}