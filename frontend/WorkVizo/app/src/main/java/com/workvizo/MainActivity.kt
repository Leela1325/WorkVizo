package com.workvizo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.workvizo.ui.theme.WorkVizoTheme
import com.workvizo.navigation.NavGraph   // ✅ this must match function name

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WorkVizoTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)   // ✅ correct
            }
        }
    }
}
