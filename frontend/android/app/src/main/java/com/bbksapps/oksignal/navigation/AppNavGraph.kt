package com.bbksapps.oksignal.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.RelationshipStoreRepository
import com.bbksapps.oksignal.data.local.repository.SessionStoreRepository
import com.bbksapps.oksignal.data.local.repository.UserStoreRepository
import com.bbksapps.oksignal.ui.screens.GuardianHomeScreen
import com.bbksapps.oksignal.ui.screens.InviteAcceptScreen
import com.bbksapps.oksignal.ui.screens.LoginScreen
import com.bbksapps.oksignal.ui.screens.MemberHomeScreen
import com.bbksapps.oksignal.ui.screens.SignUpScreen
import com.bbksapps.oksignal.ui.screens.SplashScreen
import com.bbksapps.oksignal.ui.start.AppStartViewModel
import com.bbksapps.oksignal.ui.start.AppStartViewModelFactory


private fun provideAppStartViewModelFactory(context: Context): AppStartViewModelFactory {
    val appContext = context.applicationContext

    val deviceStoreRepository = DeviceStoreRepository(appContext)
    val userStoreRepository = UserStoreRepository(appContext)
    val sessionStoreRepository = SessionStoreRepository(appContext)
    val relationshipStoreRepository = RelationshipStoreRepository(appContext)

    val appSessionRepository = AppSessionRepository(
        deviceStoreRepository = deviceStoreRepository,
        userStoreRepository = userStoreRepository,
        sessionStoreRepository = sessionStoreRepository,
        relationshipStoreRepository = relationshipStoreRepository
    )

    return AppStartViewModelFactory(appSessionRepository)
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    initialInviteToken: String? = null
) {
    val context = LocalContext.current
    val appStartViewModel: AppStartViewModel = viewModel(
        factory = provideAppStartViewModelFactory(context)
    )
    val startDestination by appStartViewModel.startDestination.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                startDestination = startDestination,
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
                },
                initialInviteToken = initialInviteToken,
                onNavigateToInviteAccept = { token ->
                    navController.navigate(Screen.InviteAccept.createRoute(token)) {
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

        composable(
            route = Screen.InviteAccept.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token").orEmpty()

            InviteAcceptScreen(
                inviteToken = token,
                onAcceptSuccess = {
                    navController.navigate(Screen.MemberHome.route) {
                        popUpTo(Screen.InviteAccept.route) { inclusive = true }
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

        composable(
            route = Screen.InviteAccept.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token").orEmpty()

            InviteAcceptScreen(
                inviteToken = token,
                onAcceptSuccess = {
                    navController.navigate(Screen.MemberHome.route) {
                        popUpTo(Screen.InviteAccept.route) { inclusive = true }
                    }
                }
            )
        }
    }
}