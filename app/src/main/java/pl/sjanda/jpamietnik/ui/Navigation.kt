package pl.sjanda.jpamietnik.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.sjanda.jpamietnik.ui.screens.CreateEditEntryScreen
import pl.sjanda.jpamietnik.ui.screens.DiaryListScreen
import pl.sjanda.jpamietnik.ui.screens.EntryDetailScreen
import pl.sjanda.jpamietnik.ui.screens.LockScreen
import pl.sjanda.jpamietnik.ui.screens.MapScreen

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "diary_list"
    ) {
        composable("lock") {
            LockScreen(
                onUnlocked = {
                    navController.navigate("diary_list")
                }
            )
        }

        composable("diary_list") {
            DiaryListScreen(
                onCreateEntry = { navController.navigate("create_entry") },
                onEditEntry = { entryId -> navController.navigate("edit_entry/$entryId") },
                onViewEntry = { entryId -> navController.navigate("entry_detail/$entryId") },
                onOpenMap = { navController.navigate("map") }
            )
        }

        composable("create_entry") {
            CreateEditEntryScreen(
                entryId = null,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("edit_entry/{entryId}") { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            CreateEditEntryScreen(
                entryId = entryId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("entry_detail/{entryId}") { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            EntryDetailScreen(
                entryId = entryId ?: "",
                onEdit = { navController.navigate("edit_entry/$entryId") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("map") {
            MapScreen(
                onBack = { navController.popBackStack() },
                onEntryClick = { entryId -> navController.navigate("entry_detail/$entryId") }
            )
        }
    }
}