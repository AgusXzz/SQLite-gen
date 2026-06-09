package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CodePreviewScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImportExportScreen
import com.example.ui.screens.TableDetailScreen

@Composable
fun AppNavigation(viewModel: SchemaViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToTable = { tableId -> navController.navigate("table/$tableId") },
                onNavigateToCode = { navController.navigate("code") },
                onNavigateToImportExport = { navController.navigate("import_export") }
            )
        }
        composable(
            "table/{tableId}",
            arguments = listOf(navArgument("tableId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getLong("tableId") ?: return@composable
            TableDetailScreen(
                tableId = tableId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("code") {
            CodePreviewScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("import_export") {
            ImportExportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
