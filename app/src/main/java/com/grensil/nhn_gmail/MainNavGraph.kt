package com.grensil.nhn_gmail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grensil.detail.DetailScreen
import com.grensil.detail.DetailViewModel
import com.grensil.detail.DetailViewModelFactory
import com.grensil.network.HttpClient
import com.grensil.nhn_gmail.di.AppModule
import com.grensil.search.SearchScreen
import com.grensil.search.SearchViewModel
import com.grensil.search.SearchViewModelFactory

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    bottom = innerPadding.calculateBottomPadding(),
                    top = innerPadding.calculateTopPadding()
                )
        ) {
            MainNavGraph(
                navController = navController
            )
        }
    }
}


@Composable
fun MainNavGraph(navController: NavHostController) {

    val appModules = AppModule(HttpClient())
    val owner = LocalViewModelStoreOwner.current
    NavHost(
        navController = navController, startDestination = TabScreen.Search.route
    ) {
        composable(TabScreen.Search.route) { backStackEntry ->

            val searchViewModel = owner?.let {
                ViewModelProvider(
                    it, SearchViewModelFactory(
                        appModules.getSummaryUseCase(), appModules.getMediaListUseCase()
                    )
                ).get(SearchViewModel::class.java)
            }

            searchViewModel?.let {
                SearchScreen(viewModel = it, navController = navController)
            }
        }
        composable(route = TabScreen.Detail.route,
            arguments = listOf(
                navArgument("searchQuery") { type = NavType.StringType },
            ),) { backStackEntry ->

            val detailViewModel = owner?.let {
                ViewModelProvider(
                    it, DetailViewModelFactory(
                        appModules.getDetailPageUrlUseCase(),
                        appModules.getSearchKeywordExtractorUseCase()
                    )
                ).get(DetailViewModel::class.java)
            }

            val keyword = backStackEntry.arguments?.getString("searchQuery")
            detailViewModel?.let {
                DetailScreen(viewModel = it, navController = navController,keyword = keyword)
            }
        }
    }
}