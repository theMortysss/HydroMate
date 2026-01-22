package dev.techm1nd.hydromate.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.techm1nd.hydromate.domain.entities.AuthState
import dev.techm1nd.hydromate.ui.screens.auth.AuthViewModel
import dev.techm1nd.hydromate.ui.screens.auth.navigation.authScreen
import dev.techm1nd.hydromate.ui.screens.history.navigation.historyScreen
import dev.techm1nd.hydromate.ui.screens.home.navigation.homeScreen
import dev.techm1nd.hydromate.ui.screens.onboarding.navigation.onboardingScreen
import dev.techm1nd.hydromate.ui.screens.profile.navigation.profileScreen
import dev.techm1nd.hydromate.ui.screens.settings.navigation.settingsScreen
import dev.techm1nd.hydromate.ui.screens.statistics.navigation.statisticsScreen
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarHost
import javax.inject.Inject

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Auth : Screen("auth", "Auth", Icons.Filled.Person)
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Filled.Person)
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Statistics : Screen("statistics", "Stats", Icons.Outlined.BarChart)
    object History : Screen("history", "History", Icons.Outlined.History)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

sealed class BottomBarTab(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    data object Home : BottomBarTab(
        route = Screen.Home.route,
        title = "Home",
        icon = Icons.Filled.Home,
        color = Color(0xFF81D4FA)
    )

    data object Statistics : BottomBarTab(
        route = Screen.Statistics.route,
        title = "Statistics",
        icon = Icons.Outlined.BarChart,
        color = Color(0xFFFA6FFF)
    )

    data object History : BottomBarTab(
        route = Screen.History.route,
        title = "History",
        icon = Icons.Outlined.History,
        color = Color(0xFFFFE082)
    )

    data object Settings : BottomBarTab(
        route = Screen.Settings.route,
        title = "Settings",
        icon = Icons.Filled.Settings,
        color = Color(0xFFAED581)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeApi::class)
@Composable
fun HydroMateNavigation(
    globalSnackbarController: GlobalSnackbarController
) {
    val hazeState = remember { HazeState() }

    // Get auth state from ViewModel
    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.state.collectAsStateWithLifecycle()

    // Don't render navigation until we know the auth state
    // This prevents the flicker of auth screen
    if (authUiState.isLoading) {
        // Show nothing while loading - splash screen is still visible
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // Determine auth state and start destination
    val isAuthenticated = authUiState.currentUser != null
    val needsOnboarding = isAuthenticated && authUiState.needsOnboarding

    val startDestination = when {
        !isAuthenticated -> Screen.Auth.route
        needsOnboarding -> Screen.Onboarding.route
        else -> Screen.Home.route
    }

    // Create navController only after we know the start destination
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle auth state changes after initial load
    LaunchedEffect(isAuthenticated) {
        val currentDestination = navController.currentDestination?.route
        when {
            isAuthenticated && currentDestination == Screen.Auth.route -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
            !isAuthenticated && currentDestination != Screen.Auth.route -> {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (isAuthenticated && !needsOnboarding) {
                TopAppBar(
                    modifier = Modifier
                        .hazeEffect(
                            state = hazeState,
                        ),
                    title = {
                        Text(
                            text = "HydroMate",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.width(48.dp)) {
                            AnimatedVisibility(
                                visible = currentRoute == Screen.Profile.route,
                                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 }),
                                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 2 })
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .clickable {
                                            navController.popBackStack()
                                        },
                                    imageVector = Icons.Filled.ArrowBackIosNew,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        AnimatedVisibility(
                            visible = currentRoute != Screen.Profile.route,
                            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 })
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .clickable {
                                        navController.navigate(Screen.Profile.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                imageVector = Icons.Filled.Person,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Profile"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    )
                )
            }
        },
        bottomBar = {
            if (isAuthenticated && !needsOnboarding) {
                val tabs = listOf(
                    BottomBarTab.Home,
                    BottomBarTab.Statistics,
                    BottomBarTab.History,
                    BottomBarTab.Settings,
                )
                var selectedTabIndex by remember { mutableIntStateOf(0) }

                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(vertical = 24.dp, horizontal = 64.dp)
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(CircleShape)
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = MaterialTheme.colorScheme.background,
                                tint = HazeTint(
                                    color = MaterialTheme.colorScheme.background.copy(alpha = .7f),
                                ),
                                blurRadius = 30.dp,
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = .2f)
                                ),
                            ),
                            shape = CircleShape
                        )
                ) {
                    BottomBarTabs(
                        tabs = tabs,
                        selectedTab = selectedTabIndex,
                        onTabSelected = {
                            selectedTabIndex = tabs.indexOf(it)
                            navController.navigate(it.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    val animatedSelectedTabIndex by animateFloatAsState(
                        targetValue = selectedTabIndex.toFloat(),
                        label = "animatedSelectedTabIndex",
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow,
                            dampingRatio = Spring.DampingRatioLowBouncy,
                        )
                    )

                    val animatedColor by animateColorAsState(
                        targetValue = tabs[selectedTabIndex].color,
                        label = "animatedColor",
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow,
                        )
                    )
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    ) {
                        val path = Path().apply {
                            addRoundRect(RoundRect(size.toRect(), CornerRadius(size.height)))
                        }
                        val length = PathMeasure().apply { setPath(path, false) }.length

                        val tabWidth = size.width / tabs.size
                        drawPath(
                            path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    animatedColor.copy(alpha = 0f),
                                    animatedColor.copy(alpha = 1f),
                                    animatedColor.copy(alpha = 1f),
                                    animatedColor.copy(alpha = 0f),
                                ),
                                startX = tabWidth * animatedSelectedTabIndex,
                                endX = tabWidth * (animatedSelectedTabIndex + 1),
                            ),
                            style = Stroke(
                                width = 6f,
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(length / 2, length)
                                )
                            )
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // Global Snackbar Host - поверх всего контента
        GlobalSnackbarHost(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding()),
            snackbarController = globalSnackbarController,
            hazeState = hazeState
        )
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .hazeSource(
                    state = hazeState,
                )
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            authScreen(
                modifier = Modifier,
                navController = navController,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                })
            onboardingScreen(
                modifier = Modifier,
                navController = navController,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
            homeScreen(modifier = Modifier, navController = navController)
            statisticsScreen(modifier = Modifier, navController = navController)
            historyScreen(modifier = Modifier, navController = navController)
            profileScreen(
                modifier = Modifier,
                navController = navController,
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
            settingsScreen(modifier = Modifier, navController = navController)
        }
    }
}

@Composable
fun BottomBarTabs(
    tabs: List<BottomBarTab>,
    selectedTab: Int,
    onTabSelected: (BottomBarTab) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
        LocalContentColor provides Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            for (tab in tabs) {
                val alpha by animateFloatAsState(
                    targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .35f,
                    label = "alpha"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .98f,
                    visibilityThreshold = .000001f,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                    ),
                    label = "scale"
                )
                Column(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .fillMaxHeight()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onTabSelected(tab)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "tab ${tab.title}"
                    )
                    Text(
                        text = tab.title,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}