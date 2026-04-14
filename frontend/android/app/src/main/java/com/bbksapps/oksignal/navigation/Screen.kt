package com.bbksapps.oksignal.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object GuardianHome : Screen("guardian_home")
    data object MemberHome : Screen("member_home")
}