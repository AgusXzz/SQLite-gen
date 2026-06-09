package com.example.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CodePreviewScreen
import com.example.ui.screens.ImportExportScreen
import com.example.ui.screens.SchemaListDetailScreen

@Composable
fun AppNavigation(viewModel: SchemaViewModel) {
    val navController = rememberNavController()
    val anim = 300

    NavHost(
        navController = navController,
        startDestination = "list_detail",
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(anim)) + fadeIn(tween(anim))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(anim)) + fadeOut(tween(anim))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(anim)) + fadeIn(tween(anim))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(anim)) + fadeOut(tween(anim))
        }
    ) {
        composable("list_detail") {
            SchemaListDetailScreen(
                viewModel = viewModel,
                onNavigateToCode = { navController.navigate("code") },
                onNavigateToImportExport = { navController.navigate("import_export") }
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
