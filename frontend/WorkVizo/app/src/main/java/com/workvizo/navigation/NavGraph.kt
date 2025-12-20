package com.workvizo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.workvizo.ui.theme.login.*
import com.workvizo.ui.theme.welcome.*
import com.workvizo.ui.theme.home.*
import com.workvizo.ui.theme.rooms.*

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        /* ---------------- SPLASH / AUTH ---------------- */

        composable("splash") {
            SplashScreen(navController)
        }

        composable("get_started") {
            GetStartedScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("forgot") {
            ForgotPasswordScreen(navController)
        }

        composable("verify_otp/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOtpScreen(navController, email)
        }

        composable("reset_password/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(navController, email)
        }
        composable("feedback/{userId}") {
            val userId = it.arguments?.getString("userId") ?: ""
            FeedbackScreen(navController, userId)
        }
        composable("edit_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditProfileScreen(navController, userId)
        }

        composable("change_email/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ChangeEmailScreen(navController, userId)
        }
        composable("change_password/{userId}") {
            val userId = it.arguments?.getString("userId") ?: ""
            ChangePasswordScreen(navController, userId)
        }
        composable("change_dob/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ChangeDobScreen(navController, userId)
        }
        composable("delete_profile/{userId}") {
            val userId = it.arguments?.getString("userId") ?: ""
            DeleteProfileScreen(navController, userId)
        }


        composable("password_changed") {
            PasswordChangedScreen(navController)
        }

        /* ---------------- HOME ---------------- */

        composable("home/{userId}/{userName}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            HomeScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }

        /* ---------------- CREATE ROOM (MANUAL) ---------------- */

        composable(
            "ai_schedule_preview/{userId}/{userName}/{roomCode}/{roomPassword}"
        ) { backStackEntry ->
            AiSchedulePreviewScreen(
                navController,
                backStackEntry.arguments!!.getString("userId")!!,
                backStackEntry.arguments!!.getString("userName")!!,
                backStackEntry.arguments!!.getString("roomCode")!!,
                backStackEntry.arguments!!.getString("roomPassword")!!
            )
        }

        composable(
            "room_created_success/{userId}/{userName}/{roomCode}/{roomPassword}"
        ) { backStackEntry ->
            RoomCreatedSuccessScreen(
                navController,
                backStackEntry.arguments!!.getString("userId")!!,
                backStackEntry.arguments!!.getString("userName")!!,
                backStackEntry.arguments!!.getString("roomCode")!!,
                backStackEntry.arguments!!.getString("roomPassword")!!
            )
        }




        /* ---------------- CREATE ROOM (AI) ---------------- */

        composable("create_room_ai/{userId}/{userName}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            CreateRoomAIScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }

        /* ---------------- ROOM CREATED SUCCESS ---------------- */


        composable("create_room_manual/{userId}/{userName}") { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            CreateRoomManualScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }



        /* ---------------- TASK SCHEDULING ---------------- */
        composable(
            "task_scheduling/{userId}/{userName}/{roomId}/{roomCode}/{roomPassword}"
        ) { backStackEntry ->

            TaskSchedulingScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: "",
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                roomCode = backStackEntry.arguments?.getString("roomCode") ?: "",
                roomPassword = backStackEntry.arguments?.getString("roomPassword") ?: ""
            )
        }



        /* ---------------- AI SCHEDULE FLOW ---------------- */

        composable(
            "ai_schedule_create/{userId}/{userName}/{roomCode}/{roomPassword}"
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val roomPassword = backStackEntry.arguments?.getString("roomPassword") ?: ""

            AiScheduleCreateScreen(
                navController = navController,
                userId = userId,
                userName = userName,
                roomCode = roomCode,
                roomPassword = roomPassword
            )
        }



        /* ---------------- JOIN ROOM ---------------- */

        composable(
            "join_room/{userId}/{userName}"
        ) { backStackEntry ->

            JoinRoomScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: ""
            )
        }


        /* ---------------- ONBOARD ---------------- */

        composable("onboard/{userId}/{userName}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            OnboardingScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }
        composable("joined_room_success") {
            JoinedRoomSuccessScreen(navController)
        }
        composable("profile/{userId}/{userName}") { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            ProfileScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(navController)
        }
        composable("terms_conditions") {
            TermsConditionsScreen(navController)
        }
        composable("about_workvizo") {
            AboutWorkVizoScreen(navController)
        }

        composable("rooms_overview/{userId}/{userName}") { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            RoomsOverviewScreen(
                navController = navController,
                userId = userId,
                userName = userName,
                onClick = {
                    // TODO: Add navigation logic or another action here
                    println("Button was clicked!") // Example action
                }
            )
        }


        composable("created_rooms/{userId}/{userName}") { backStackEntry ->
            CreatedRoomsScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: ""
            )
        }
        composable(
            route = "joined_rooms/{userId}/{userName}"
        ) { backStackEntry ->
            JoinedRoomsScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: ""
            )
        }
        composable(
            route = "active_rooms/{userId}/{userName}"
        ) { backStackEntry ->
            ActiveRoomsScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: ""
            )
        }
        composable(
            route = "completed_rooms/{userId}/{userName}"
        ) { backStackEntry ->
            CompletedRoomsScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: ""
            )
        }
        composable(
            route = "recent_rooms/{userId}/{userName}"
        ) { backStackEntry ->
            RecentRoomsScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: ""
            )
        }
        composable(
            route = "room_details/{roomCode}/{userId}/{userName}"
        ) { backStackEntry ->

            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            RoomDetailsScreen(
                navController = navController,
                roomCode = roomCode,
                userId = userId,
                userName = userName
            )
        }
        composable(
            route = "room_settings/{roomId}/{roomCode}/{userId}/{creatorId}"
        ) { backStackEntry ->

            RoomSettingsScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                roomCode = backStackEntry.arguments?.getString("roomCode") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                creatorId = backStackEntry.arguments?.getString("creatorId") ?: ""
            )
        }


    }
}
