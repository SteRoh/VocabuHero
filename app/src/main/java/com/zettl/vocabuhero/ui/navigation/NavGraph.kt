package com.zettl.vocabuhero.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zettl.vocabuhero.VocabuHeroApplication
import com.zettl.vocabuhero.ui.deck.DeckListScreen
import com.zettl.vocabuhero.ui.deck.DeckListViewModel
import com.zettl.vocabuhero.ui.deck.DeckScreen
import com.zettl.vocabuhero.ui.deck.DeckViewModel
import com.zettl.vocabuhero.ui.edit.EditScreen
import com.zettl.vocabuhero.ui.edit.EditViewModel
import com.zettl.vocabuhero.ui.home.HomeScreen
import com.zettl.vocabuhero.ui.home.HomeViewModel
import com.zettl.vocabuhero.ui.import_.ImportScreen
import com.zettl.vocabuhero.ui.import_.ImportViewModel
import com.zettl.vocabuhero.ui.practice.PracticeScreen
import com.zettl.vocabuhero.ui.practice.PracticeViewModel
import com.zettl.vocabuhero.ui.settings.SettingsScreen
import com.zettl.vocabuhero.ui.settings.SettingsViewModel
import com.zettl.vocabuhero.ui.stats.StatsScreen
import com.zettl.vocabuhero.ui.stats.StatsViewModel
import com.zettl.vocabuhero.ViewModelFactory

@Composable
fun VocabuHeroNavGraph(
    app: VocabuHeroApplication,
    navController: NavHostController = rememberNavController()
) {
    val factory = remember(app) { ViewModelFactory(app) }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home
    ) {
        composable(NavRoutes.Home) {
            val vm = viewModel<HomeViewModel>(factory = factory)
            HomeScreen(
                viewModel = vm,
                onStartPractice = { navController.navigate(NavRoutes.Practice) },
                onDeck = { navController.navigate(NavRoutes.DeckList) },
                onStats = { navController.navigate(NavRoutes.Stats) },
                onSettings = { navController.navigate(NavRoutes.Settings) }
            )
        }
        composable(NavRoutes.Practice) {
            val vm = viewModel<PracticeViewModel>(factory = factory)
            PracticeScreen(
                viewModel = vm,
                onSessionComplete = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.StringType; defaultValue = "new" })
        ) { backStackEntry ->
            val cardIdStr = backStackEntry.arguments?.getString("cardId") ?: "new"
            val cardId = when (cardIdStr) {
                "new" -> 0L
                else -> cardIdStr.toLongOrNull() ?: 0L
            }
            val vm = viewModel<EditViewModel>(
                viewModelStoreOwner = backStackEntry,
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EditViewModel(app.cardRepository, backStackEntry.savedStateHandle) as T
                    }
                }
            )
            EditScreen(
                viewModel = vm,
                onSaved = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.Import) {
            val vm = viewModel<ImportViewModel>(factory = factory)
            ImportScreen(
                viewModel = vm,
                onDone = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.Stats) {
            val vm = viewModel<StatsViewModel>(factory = factory)
            StatsScreen(viewModel = vm)
        }
        composable(NavRoutes.Settings) {
            val vm = viewModel<SettingsViewModel>(factory = factory)
            SettingsScreen(viewModel = vm)
        }
        composable(
            route = NavRoutes.DeckDetail,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val vm = viewModel<DeckViewModel>(
                viewModelStoreOwner = backStackEntry,
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DeckViewModel(app.cardRepository, deckId) as T
                    }
                }
            )
            DeckScreen(
                viewModel = vm,
                onCardClick = { id -> navController.navigate(NavRoutes.edit(id)) },
                onAddCard = { navController.navigate(NavRoutes.EditNew) },
                onImport = { navController.navigate(NavRoutes.Import) }
            )
        }
        composable(NavRoutes.DeckList) {
            val vm = viewModel<DeckListViewModel>(factory = factory)
            DeckListScreen(
                viewModel = vm,
                onDeckClick = { deckId -> navController.navigate(NavRoutes.deckDetail(deckId)) }
            )
        }
    }
}
