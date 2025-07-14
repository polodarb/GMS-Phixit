package ua.polodarb.gmsphixit.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import dagger.hilt.android.AndroidEntryPoint
import ua.polodarb.gmsphixit.presentation.core.ui.theme.GMSPhixitTheme
import ua.polodarb.gmsphixit.presentation.navigation.NavigationManager
import ua.polodarb.gmsphixit.presentation.navigation.RootNavDisplay
import ua.polodarb.gmsphixit.presentation.navigation.ScreenDestination
import ua.polodarb.gmsphixit.core.phixit.InitRootDB
import ua.polodarb.gmsphixit.core.shell.InitShell
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootDBInitializer: InitRootDB

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val config = ClarityConfig(
            projectId = "sewjadu3ol", // yeah yeah hardcode moment
            logLevel = LogLevel.Error
        )
        Clarity.initialize(applicationContext, config)

        val startDestination = navigationManager.getInitialDestination()

        if (startDestination == ScreenDestination.PackagePicker) {
            InitShell.initShell()
            rootDBInitializer.initDB()
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )

        setContent {
            val backStack = rememberSaveable { mutableStateListOf<ScreenDestination>(startDestination) }
            GMSPhixitTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    RootNavDisplay(backStack = backStack)
                }
            }
        }
    }
}