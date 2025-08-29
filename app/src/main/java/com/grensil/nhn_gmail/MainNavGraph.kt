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

    NavHost(
        navController = navController, startDestination = Routes.SEARCH
    ) {
        // 초기 검색 화면 (검색어 없음)
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

            val searchViewModel = ViewModelProvider(
                backStackEntry, SearchViewModelFactory(
                    appModules.getSummaryUseCase(),
                    appModules.getMediaListUseCase(),
                    null // 초기 화면은 검색어 없음
                )
            ).get(SearchViewModel::class.java)

            SearchScreen(viewModel = searchViewModel, navController = navController)
        }

        // 검색어가 있는 검색 화면 (새로운 검색)
        composable(route = Routes.SEARCH_TEMPLATE,
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

            val encodedKeyword = backStackEntry.arguments?.getString("searchQuery")
            val keyword = Routes.extractSearchQuery(encodedKeyword)
            
            val searchViewModel = ViewModelProvider(
                backStackEntry, SearchViewModelFactory(
                    appModules.getSummaryUseCase(),
                    appModules.getMediaListUseCase(),
                    keyword // 초기 검색어를 ViewModel에 전달
                )
            ).get(SearchViewModel::class.java)
            
            SearchScreen(viewModel = searchViewModel, navController = navController)
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

            val detailViewModel = ViewModelProvider(
                backStackEntry, DetailViewModelFactory(
                    appModules.getDetailPageUrlUseCase()
                )
            ).get(DetailViewModel::class.java)

            val encodedKeyword = backStackEntry.arguments?.getString("searchQuery")
            val keyword = Routes.extractSearchQuery(encodedKeyword)
            DetailScreen(viewModel = detailViewModel, navController = navController, keyword = keyword)
        }
    }
}