package com.aerofocus.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aerofocus.app.ui.screen.ArrivalScreen
import com.aerofocus.app.ui.screen.DepartureScreen
import com.aerofocus.app.ui.screen.InFlightScreen
import com.aerofocus.app.ui.screen.LogbookScreen
import com.aerofocus.app.ui.screen.PreFlightScreen
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import com.aerofocus.app.ui.viewmodel.FlightViewModel
import com.aerofocus.app.ui.viewmodel.TimerViewModel
import com.aerofocus.app.util.Constants

/**
 * Root navigation composable for AeroFocus.
 *
 * - Two bottom nav items: Departure Lounge & Logbook
 * - Other screens (PreFlight, InFlight, Arrival) are pushed as full-screen routes
 *   and hide the bottom bar.
 */
@Composable
fun AeroFocusNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar during InFlight, PreFlight, and Arrival
    val showBottomBar = currentRoute in listOf(
        Constants.ROUTE_DEPARTURE,
        Constants.ROUTE_LOGBOOK
    )

    // Shared ViewModels scoped to the nav graph
    val flightViewModel: FlightViewModel = hiltViewModel()
    val timerViewModel: TimerViewModel = hiltViewModel()

    Scaffold(
        containerColor = DeepNight,
        bottomBar = {
            if (showBottomBar) {
                AeroFocusBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Constants.ROUTE_DEPARTURE,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Constants.ROUTE_DEPARTURE) {
                DepartureScreen(
                    flightViewModel = flightViewModel,
                    onBookFlight = {
                        navController.navigate(Constants.ROUTE_PRE_FLIGHT)
                    }
                )
            }

            composable(Constants.ROUTE_PRE_FLIGHT) {
                PreFlightScreen(
                    flightViewModel = flightViewModel,
                    timerViewModel = timerViewModel,
                    onBoardComplete = {
                        navController.navigate(Constants.ROUTE_IN_FLIGHT) {
                            popUpTo(Constants.ROUTE_PRE_FLIGHT) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Constants.ROUTE_IN_FLIGHT) {
                InFlightScreen(
                    timerViewModel = timerViewModel,
                    onFlightComplete = { cityName, earnedMiles, wasCompleted ->
                        navController.navigate(
                            "arrival/$cityName/$earnedMiles/$wasCompleted"
                        ) {
                            popUpTo(Constants.ROUTE_DEPARTURE) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Constants.ROUTE_ARRIVAL,
                arguments = listOf(
                    navArgument("cityName") { type = NavType.StringType },
                    navArgument("earnedMiles") { type = NavType.IntType },
                    navArgument("wasCompleted") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
                val earnedMiles = backStackEntry.arguments?.getInt("earnedMiles") ?: 0
                val wasCompleted = backStackEntry.arguments?.getBoolean("wasCompleted") ?: false

                ArrivalScreen(
                    cityName = cityName,
                    earnedMiles = earnedMiles,
                    wasCompleted = wasCompleted,
                    onReturnToLounge = {
                        flightViewModel.resetFlightConfig()
                        navController.navigate(Constants.ROUTE_DEPARTURE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Constants.ROUTE_LOGBOOK) {
                LogbookScreen()
            }
        }
    }
}

@Composable
private fun AeroFocusBottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = TextSecondary
    ) {
        NavigationBarItem(
            selected = currentRoute == Constants.ROUTE_DEPARTURE,
            onClick = {
                navController.navigate(Constants.ROUTE_DEPARTURE) {
                    popUpTo(Constants.ROUTE_DEPARTURE) { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Constants.ROUTE_DEPARTURE)
                        Icons.Filled.FlightTakeoff else Icons.Outlined.FlightTakeoff,
                    contentDescription = "Departure"
                )
            },
            label = { Text("Departure") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WarmGlow,
                selectedTextColor = WarmGlow,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = WarmGlow.copy(alpha = 0.12f)
            )
        )

        NavigationBarItem(
            selected = currentRoute == Constants.ROUTE_LOGBOOK,
            onClick = {
                navController.navigate(Constants.ROUTE_LOGBOOK) {
                    popUpTo(Constants.ROUTE_DEPARTURE) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Constants.ROUTE_LOGBOOK)
                        Icons.Filled.Book else Icons.Outlined.Book,
                    contentDescription = "Logbook"
                )
            },
            label = { Text("Logbook") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WarmGlow,
                selectedTextColor = WarmGlow,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = WarmGlow.copy(alpha = 0.12f)
            )
        )
    }
}
