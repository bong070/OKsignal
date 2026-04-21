package com.bbksapps.oksignal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bbksapps.oksignal.data.local.model.AppMode
import com.bbksapps.oksignal.data.repository.InviteRepository
import com.bbksapps.oksignal.ui.invite.InviteAcceptViewModel
import com.bbksapps.oksignal.ui.invite.InviteAcceptViewModelFactory
import com.bbksapps.oksignal.ui.screens.GuardianHomeScreen
import com.bbksapps.oksignal.ui.screens.InviteAcceptScreen
import com.bbksapps.oksignal.ui.screens.LoginScreen
import com.bbksapps.oksignal.ui.screens.MemberHomeScreen
import com.bbksapps.oksignal.ui.screens.SignUpScreen
import com.bbksapps.oksignal.ui.screens.SplashScreen
import com.bbksapps.oksignal.ui.session.SessionViewModel
import com.bbksapps.oksignal.ui.session.SessionViewModelFactory
import com.bbksapps.oksignal.ui.start.AppStartViewModel
import com.bbksapps.oksignal.ui.start.AppStartViewModelFactory
import com.bbksapps.oksignal.data.local.model.AppSessionState

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    initialInviteToken: String? = null
) {
    val dependencies = rememberAppDependencies()

    val appStartViewModel: AppStartViewModel = viewModel(
        factory = AppStartViewModelFactory(
            appSessionRepository = dependencies.appSessionRepository,
            deviceStoreRepository = dependencies.deviceStoreRepository
        )
    )

    val sessionViewModel: SessionViewModel = viewModel(
        factory = SessionViewModelFactory(
            dependencies.appSessionRepository
        )
    )

    val startDestination by appStartViewModel.startDestination.collectAsStateWithLifecycle()
    val loginUiState by sessionViewModel.loginUiState.collectAsStateWithLifecycle()
    val appSession by dependencies.appSessionRepository.appSessionState.collectAsStateWithLifecycle(
        initialValue = AppSessionState()
    )

    LaunchedEffect(loginUiState.isSuccess, loginUiState.selectedMode) {
        if (loginUiState.isSuccess) {
            when (loginUiState.selectedMode) {
                AppMode.GUARDIAN -> {
                    navController.navigate(Screen.GuardianHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                AppMode.MEMBER, AppMode.GROUP, null -> {
                    navController.navigate(Screen.MemberHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            sessionViewModel.consumeSuccess()
        }
    }

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
                onLoginClick = { email, password ->
                    sessionViewModel.login(email, password)
                },
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

        composable(Screen.MemberHome.route) {
            MemberHomeScreen()
        }

        composable(Screen.GuardianHome.route) {
            val guardianUserId = appSession.user.userId

            if (guardianUserId.isNullOrBlank()) {
                // 아직 준비 안됐으면 아무것도 안 그리거나 loading
                return@composable
            }

            GuardianHomeScreen(
                guardianUserId = guardianUserId,
                onLogout = {
                    sessionViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.InviteAccept.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token").orEmpty()

            val inviteAcceptViewModel: InviteAcceptViewModel = viewModel(
                factory = InviteAcceptViewModelFactory(
                    appSessionRepository = dependencies.appSessionRepository,
                    inviteRepository = InviteRepository()
                )
            )

            val inviteUiState by inviteAcceptViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(inviteUiState.isSuccess) {
                if (inviteUiState.isSuccess) {
                    inviteAcceptViewModel.consumeSuccess()
                    navController.navigate(Screen.MemberHome.route) {
                        popUpTo(Screen.InviteAccept.route) { inclusive = true }
                    }
                }
            }

            InviteAcceptScreen(
                inviteToken = token,
                uiState = inviteUiState,
                onAcceptClick = {
                    inviteAcceptViewModel.acceptInvite(token)
                }
            )
        }
    }
}