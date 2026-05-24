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
import com.bbksapps.oksignal.ui.guardian.GuardianHomeViewModel
import com.bbksapps.oksignal.ui.guardian.GuardianHomeViewModelFactory
import com.bbksapps.oksignal.ui.member.MemberHomeViewModel
import com.bbksapps.oksignal.ui.member.MemberHomeViewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.bbksapps.oksignal.worker.HeartbeatScheduler
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
            appSessionRepository = dependencies.appSessionRepository,
            authRepository = dependencies.authRepository
        )
    )

    val startDestination by appStartViewModel.startDestination.collectAsStateWithLifecycle()
    val loginUiState by sessionViewModel.loginUiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(loginUiState.isSuccess, loginUiState.selectedMode) {
        if (loginUiState.isSuccess) {
            //HeartbeatScheduler.start(context.applicationContext)
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

    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("OKSignalFCM", "Current FCM token: $token")
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
                    sessionViewModel.clearError()
                    navController.navigate(Screen.SignUp.route)
                },
                serverErrorMessage = loginUiState.errorMessage
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpClick = { email, displayName, password ->
                    sessionViewModel.signup(email, displayName, password)
                },
                onGoogleSignUpClick = { },
                onNavigateToLogin = {
                    sessionViewModel.clearError()
                    navController.popBackStack()
                },
                serverErrorMessage = loginUiState.errorMessage
            )
        }

        composable(Screen.MemberHome.route) {
            val memberHomeViewModel: MemberHomeViewModel = viewModel(
                factory = MemberHomeViewModelFactory(
                    appSessionRepository = dependencies.appSessionRepository,
                    heartbeatRepository = dependencies.heartbeatRepository,
                    deviceStoreRepository = dependencies.deviceStoreRepository,
                    needHelpRepository = dependencies.needHelpRepository
                )
            )

            val memberUiState by memberHomeViewModel.uiState.collectAsStateWithLifecycle()

            MemberHomeScreen(
                uiState = memberUiState,
                onOkClick = {
                    memberHomeViewModel.checkIn()
                },
                onHelpClick = {
                    memberHomeViewModel.sendNeedHelp()
                }
            )
        }

        composable(Screen.GuardianHome.route) {
            val guardianHomeViewModel: GuardianHomeViewModel = viewModel(
                factory = GuardianHomeViewModelFactory(
                    appSessionRepository = dependencies.appSessionRepository
                )
            )

            val guardianUiState by guardianHomeViewModel.uiState.collectAsStateWithLifecycle()

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    coroutineScope.launch {
                        try {
                            val appSession = dependencies.appSessionRepository.appSessionState.first()
                            val deviceId = appSession.device.deviceId

                            Log.d("OKSignalFCM", "Uploading FCM token. deviceId=$deviceId")

                            if (!deviceId.isNullOrBlank()) {
                                val success = dependencies.fcmTokenRepository.updateFcmToken(
                                    deviceId = deviceId,
                                    fcmToken = token
                                )

                                Log.d("OKSignalFCM", "FCM token upload success=$success")
                            }
                        } catch (e: Exception) {
                            Log.e("OKSignalFCM", "Failed to upload FCM token", e)
                        }
                    }
                }
            }

            GuardianHomeScreen(
                uiState = guardianUiState,
                onRefresh = { guardianHomeViewModel.refreshMembers() },
                onInviteClick = {
                    guardianHomeViewModel.createInvite()
                },
                onDismissInviteDialog = {
                    guardianHomeViewModel.dismissInviteDialog()
                },
                onLogout = {
                    //HeartbeatScheduler.stop(context.applicationContext)
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
                    inviteRepository = InviteRepository(),
                    deviceStoreRepository = dependencies.deviceStoreRepository
                )
            )

            val inviteUiState by inviteAcceptViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(inviteUiState.isSuccess) {
                if (inviteUiState.isSuccess) {
                    HeartbeatScheduler.start(context.applicationContext)

                    inviteAcceptViewModel.consumeSuccess()
                    navController.navigate(Screen.MemberHome.route) {
                        popUpTo(Screen.InviteAccept.route) { inclusive = true }
                    }
                }
            }

            InviteAcceptScreen(
                inviteToken = token,
                uiState = inviteUiState,
                onDisplayNameChange = inviteAcceptViewModel::onDisplayNameChange,
                onAcceptClick = {
                    inviteAcceptViewModel.acceptInvite(token)
                }
            )
        }
    }
}