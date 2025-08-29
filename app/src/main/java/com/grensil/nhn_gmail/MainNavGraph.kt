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
import com.grensil.search.SearchScreen
import com.grensil.search.SearchViewModel
import com.grensil.navigation.Routes
import com.grensil.nhn_gmail.di.createViewModelFactory

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
        navController = navController, startDestination = Routes.Search.createInitialRoute()
    ) {
        // 통합된 검색 화면 (파라미터 있으면 검색, 없으면 빈 화면)
        composable(
            route = Routes.SEARCH_TEMPLATE,
            arguments = listOf(
                navArgument("searchQuery") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
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
                backStackEntry, 
                createViewModelFactory {
                    SearchViewModel(
                        appModules.getSummaryUseCase(),
                        appModules.getMediaListUseCase(),
                        keyword // 파라미터가 있으면 해당 키워드로 검색, 없으면 null
                    )
                }
            )[SearchViewModel::class.java]
            
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
                backStackEntry,
                createViewModelFactory {
                    DetailViewModel(appModules.getDetailPageUrlUseCase())
                }
            )[DetailViewModel::class.java]

            val encodedKeyword = backStackEntry.arguments?.getString("searchQuery")
            val keyword = Routes.extractSearchQuery(encodedKeyword)
            DetailScreen(viewModel = detailViewModel, navController = navController, keyword = keyword)
        }
    }
}