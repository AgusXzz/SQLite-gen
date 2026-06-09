package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import com.example.ui.SchemaViewModel

/**
 * Hosts the table list and the table detail editor in an adaptive master-detail
 * layout. On compact widths (phones) only one pane shows at a time and the back
 * gesture returns from detail to list; on medium/expanded widths (tablets,
 * landscape) both panes are visible side by side.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SchemaListDetailScreen(
    viewModel: SchemaViewModel,
    onNavigateToCode: () -> Unit,
    onNavigateToImportExport: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()

    BackHandler(enabled = navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                TableListPane(
                    viewModel = viewModel,
                    selectedTableId = navigator.currentDestination?.content,
                    onNavigateToTable = { id ->
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    },
                    onNavigateToCode = onNavigateToCode,
                    onNavigateToImportExport = onNavigateToImportExport
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val selectedId = navigator.currentDestination?.content
                if (selectedId != null) {
                    TableDetailPane(
                        tableId = selectedId,
                        viewModel = viewModel,
                        showBack = navigator.canNavigateBack(),
                        onBack = { navigator.navigateBack() }
                    )
                } else {
                    DetailPlaceholder("Select a table to edit its columns.")
                }
            }
        }
    )
}
