package com.grensil.nhn_gmail

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grensil.detail.DetailScreen
import com.grensil.detail.DetailViewModel
import com.grensil.detail.DetailViewModelFactory
import com.grensil.search.SearchScreen
import com.grensil.search.SearchViewModel
import com.grensil.search.SearchViewModelFactory
import com.grensil.navigation.Routes

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
    val context = LocalContext.current
    val appModules = (context.applicationContext as NhnApplication).getWikipediaModule()

    val owner = LocalViewModelStoreOwner.current
    NavHost(
        navController = navController, startDestination = Routes.SEARCH
    ) {
        composable(route = Routes.SEARCH,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }) { backStackEntry ->

            val searchViewModel = owner?.let {
                ViewModelProvider(
                    it, SearchViewModelFactory(
                        appModules.getSummaryUseCase(),
                        appModules.getMediaListUseCase()
                    )
                ).get(SearchViewModel::class.java)
            }

            searchViewModel?.let {
                SearchScreen(viewModel = it, navController = navController)
            }
        }
        composable(route = Routes.DETAIL_TEMPLATE,
            arguments = listOf(
                navArgument("searchQuery") { type = NavType.StringType },
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }) { backStackEntry ->

            val detailViewModel = owner?.let {
                ViewModelProvider(
                    it, DetailViewModelFactory(
                        appModules.getDetailPageUrlUseCase()
                    )
                ).get(DetailViewModel::class.java)
            }

            val encodedKeyword = backStackEntry.arguments?.getString("searchQuery")
            val keyword = Routes.Detail.extractSearchQuery(encodedKeyword)
            detailViewModel?.let {
                DetailScreen(viewModel = it, navController = navController, keyword = keyword)
            }
        }
    }
}