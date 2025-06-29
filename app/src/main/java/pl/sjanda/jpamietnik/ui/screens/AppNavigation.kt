package pl.sjanda.jpamietnik.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.sjanda.jpamietnik.data.JournalRepository
import pl.sjanda.jpamietnik.ui.viewmodel.JournalDetailViewModel
import pl.sjanda.jpamietnik.ui.viewmodel.JournalDetailViewModelFactory
import pl.sjanda.jpamietnik.ui.viewmodel.JournalEntryEditViewModel
import pl.sjanda.jpamietnik.ui.viewmodel.JournalEntryEditViewModelFactory
import pl.sjanda.jpamietnik.ui.viewmodel.JournalListViewModel
import pl.sjanda.jpamietnik.ui.viewmodel.JournalListViewModelFactory
import pl.twojprojekt.dziennik.ui.screens.JournalEditScreen

sealed class Screen(val route: String) {
    data object Lock : Screen("lock")

    data object JournalList : Screen("journal_list")
    data object JournalAdd : Screen("add_journal_entry")
    data object JournalEdit : Screen("edit_entry/{entryId}") {
        fun createRoute(entryId: String) = "edit_entry/$entryId"
    }

    data object JournalDetail : Screen("entry_detail/{entryId}") {
        fun createRoute(entryId: String) = "entry_detail/$entryId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val journalRepository = remember { JournalRepository() }

    NavHost(navController = navController, startDestination = Screen.Lock.route) {
        composable(Screen.Lock.route) {
            LockScreen(
                onUnlocked = {
                    navController.navigate(Screen.JournalList.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.JournalList.route) {
            val journalListViewModel: JournalListViewModel = viewModel(
                factory = JournalListViewModelFactory(journalRepository)
            )

            JournalListScreen(
                viewModel = journalListViewModel,
                onAddJournalClick = {
                    navController.navigate(Screen.JournalAdd.route)
                },
                onJournalClick = { journalId ->
                    navController.navigate(Screen.JournalDetail.createRoute(journalId))
                },
                onJournalLongClick = { journalId ->
                    navController.navigate(Screen.JournalEdit.createRoute(journalId))
                }
            )
        }

        composable(
            Screen.JournalDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val journalId = backStackEntry.arguments?.getString("entryId")
            journalId?.let {
                val journalDetailViewModel: JournalDetailViewModel = viewModel(
                    factory = JournalDetailViewModelFactory(
                        journalRepository, it
                    )
                )
                JournalDetailScreen(
                    viewModel = journalDetailViewModel,
                    onNavigateUp = { navController.popBackStack() }
                )

            } ?: navController.popBackStack()
        }

        composable(
            Screen.JournalEdit.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            entryId?.let {
                val journalEditViewModel: JournalEntryEditViewModel = viewModel(
                    factory = JournalEntryEditViewModelFactory(
                        journalRepository, it
                    )
                )
                JournalEditScreen(
                    viewModel = journalEditViewModel,
                    onNavigateUp = { navController.popBackStack() },
                    onSaveComplete = {

                    }
                )
            } ?: navController.popBackStack()
        }

    }
}