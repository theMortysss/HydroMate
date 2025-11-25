package sdf.bitt.hydromate.ui.navigation

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import sdf.bitt.hydromate.ui.screens.history.HistoryScreen
import sdf.bitt.hydromate.ui.screens.home.HomeScreen
import sdf.bitt.hydromate.ui.screens.settings.SettingsScreen
import sdf.bitt.hydromate.ui.screens.statistics.StatisticsScreen
import sdf.bitt.hydromate.ui.theme.VeryThirstyRed

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Statistics : Screen("statistics", "Stats", Icons.Outlined.BarChart)
    object History : Screen("history", "History", Icons.Outlined.History)
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
fun HydroMateNavigation() {
    val navController = rememberNavController()
    val hazeState = remember { HazeState() }

    Scaffold(
        topBar = {
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        bottomBar = {
            val tabs = listOf(
                BottomBarTab.Home,
                BottomBarTab.Statistics,
                BottomBarTab.History,
                BottomBarTab.Settings,
            )
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
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
                                saveState = true // Save the state of the destination
                            }
                            launchSingleTop = true // Avoid multiple copies of the same destination
                            restoreState = true // Restore the previous state if it exists
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
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .hazeSource(
                    state = hazeState,
                )
                .padding(
                    top = innerPadding.calculateTopPadding(),
                )
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
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
