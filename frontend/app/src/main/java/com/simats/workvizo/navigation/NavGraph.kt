package com.simats.workvizo.navigation
import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.simats.workvizo.*

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.simats.workvizo.ui.theme.login.*
import com.simats.workvizo.ui.theme.welcome.*
import com.simats.workvizo.ui.theme.home.*
import com.simats.workvizo.ui.theme.rooms.*

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
        composable("subscribe") {
            SubscriptionScreen(
                navController = navController,
                onSubscribe = {
                    // Billing handled in Activity
                }
            )
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
            "ai_schedule_create/{userId}/{userName}/{roomId}/{roomCode}/{roomPassword}"
        ) { backStackEntry ->

            AiScheduleCreateScreen(
                navController = navController,
                userId = backStackEntry.arguments!!.getString("userId")!!,
                userName = backStackEntry.arguments!!.getString("userName")!!,
                roomId = backStackEntry.arguments!!.getString("roomId")!!,
                roomCode = backStackEntry.arguments!!.getString("roomCode")!!,
                roomPassword = backStackEntry.arguments!!.getString("roomPassword")!!
            )
        }



        composable(
            "ai_schedule_preview/{userId}/{userName}/{roomId}/{roomCode}/{roomPassword}"
        ) { backStackEntry ->

            AiSchedulePreviewScreen(
                navController = navController,
                userId = backStackEntry.arguments!!.getString("userId")!!,
                userName = backStackEntry.arguments!!.getString("userName")!!,
                roomId = backStackEntry.arguments!!.getString("roomId")!!
                ,
                roomCode = backStackEntry.arguments!!.getString("roomCode")!!,
                roomPassword = backStackEntry.arguments!!.getString("roomPassword")!!
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
            "task_scheduling/{userId}/{userName}/{roomId}/{roomCode}/{roomPassword}/{projectStart}/{projectEnd}"
        ) { backStackEntry ->

            TaskSchedulingScreen(
                navController = navController,
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: "",
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                roomCode = backStackEntry.arguments?.getString("roomCode") ?: "",
                roomPassword = backStackEntry.arguments?.getString("roomPassword") ?: "",
                projectStart = backStackEntry.arguments?.getString("projectStart") ?: "",
                projectEnd = backStackEntry.arguments?.getString("projectEnd") ?: ""
            )
        }




        /* ---------------- AI SCHEDULE FLOW ---------------- */

        composable(
            "ai_schedule_preview/{userId}/{userName}/{roomId}/{roomCode}/{roomPassword}"
        ) { backStackEntry ->

            AiSchedulePreviewScreen(
                navController = navController,
                userId = backStackEntry.arguments!!.getString("userId")!!,
                userName = backStackEntry.arguments!!.getString("userName")!!,
                roomId = backStackEntry.arguments!!.getString("roomId")!!,   // ✅ STRING
                roomCode = backStackEntry.arguments!!.getString("roomCode")!!,
                roomPassword = backStackEntry.arguments!!.getString("roomPassword")!!
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
        composable(
            "joined_room_success/{roomCode}/{userId}/{userName}"
        ) { backStackEntry ->

            JoinedRoomSuccessScreen(
                navController = navController,
                roomCode = backStackEntry.arguments?.getString("roomCode") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                userName = backStackEntry.arguments?.getString("userName") ?: ""
            )
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
        composable(
            "schedule_details/{roomId}/{roomCode}/{userId}/{creatorId}"
        ) { backStackEntry ->

            ScheduleDetailsScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                roomCode = backStackEntry.arguments?.getString("roomCode") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                creatorId = backStackEntry.arguments?.getString("creatorId") ?: ""
            )
        }
        composable(
            "edit_schedule/{roomId}/{roomCode}/{userId}/{creatorId}"
        ) { backStackEntry ->

            EditScheduleScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                roomCode = backStackEntry.arguments?.getString("roomCode") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                creatorId = backStackEntry.arguments?.getString("creatorId") ?: ""
            )
        }
        composable(
            "room_tasks/{roomId}/{userId}/{creatorId}"
        ) { backStackEntry ->
            RoomTasksScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                creatorId = backStackEntry.arguments?.getString("creatorId") ?: ""
            )
        }
        composable(
            route = "change_task_status/{taskId}/{userId}/{isCreator}"
        ) { backStackEntry ->

            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val isCreator = backStackEntry.arguments?.getString("isCreator") == "1"

            ChangeTaskStatusScreen(
                navController = navController,
                taskId = taskId,
                userId = userId,
                isCreator = isCreator
            )
        }





        composable("edit_proof/{taskId}/{userId}") { backStackEntry ->
            EditProofScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getString("taskId") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: ""
            )
        }



        composable("task_comment/{taskId}/{userId}") { backStackEntry ->
            TaskCommentScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getString("taskId") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: ""
            )
        }






        composable(
            route = "report_room/{roomId}/{userId}"
        ) { backStackEntry ->
            ReportRoomScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: ""
            )
        }

        composable(
            route = "edit_room/{roomCode}/{userId}"
        ) { backStackEntry ->
            EditRoomDetailsScreen(
                navController = navController,
                roomCode = backStackEntry.arguments?.getString("roomCode") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: ""
            )
        }



        composable(
            route = "remove_members/{roomId}"
        ) { backStackEntry ->
            RemoveMembersScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            )
        }
        composable(
            route = "room_members/{roomId}"
        ) { backStackEntry ->

            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""

            RoomMembersScreen(
                navController = navController,
                roomId = roomId
            )
        }


        composable("edit_tasks/{roomId}/{userId}") { backStackEntry ->

            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            EditTasksScreen(
                navController = navController,
                roomId = roomId,
                userId = userId
            )
        }

        composable("add_task/{roomId}/{userId}") { backStackEntry ->
            AddTaskScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                userId = backStackEntry.arguments?.getString("userId") ?: ""
            )
        }


        composable(
            route = "remove_tasks/{roomId}/{creatorId}"
        ) { backStackEntry ->

            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val creatorId = backStackEntry.arguments?.getString("creatorId") ?: ""

            RemoveTasksScreen(
                navController = navController,
                roomId = roomId,
                creatorId = creatorId
            )
        }

        composable(
            route = "room_ai_chat/{roomId}/{userId}"
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            RoomAiAssistantScreen(
                navController = navController,
                roomId = roomId,
                userId = userId
            )
        }


        composable(
            route = "room_chat/{roomId}/{userId}/{userName}/{creatorId}"
        ) { backStackEntry ->

            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val creatorId = backStackEntry.arguments?.getString("creatorId") ?: ""

            RoomChatScreen(
                navController = navController,
                roomId = roomId,
                userId = userId,
                userName = userName,
                creatorId = creatorId
            )
        }



    }
}
