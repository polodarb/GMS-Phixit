package ua.polodarb.gmsphixit.presentation.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ua.polodarb.gmsphixit.presentation.feature.flagsChanger.FlagsChangerScreen
import ua.polodarb.gmsphixit.presentation.feature.flagsChanger.FlagsChangerViewModel
import ua.polodarb.gmsphixit.presentation.feature.onboarding.OnboardingScreen
import ua.polodarb.gmsphixit.presentation.feature.onboarding.OnboardingViewModel
import ua.polodarb.gmsphixit.presentation.feature.packagePicker.PackagePickerScreen
import ua.polodarb.gmsphixit.presentation.feature.packagePicker.PackagePickerViewModel
import ua.polodarb.gmsphixit.presentation.feature.settings.SettingsScreen
import ua.polodarb.gmsphixit.presentation.feature.settings.SettingsViewModel
import ua.polodarb.gmsphixit.presentation.navigation.anim.*
import java.util.Map.entry

@Composable
fun RootNavDisplay(
    backStack: MutableList<ScreenDestination>
) {
    NavDisplay(
        backStack = backStack,
        predictivePopTransitionSpec = {
            ContentTransform(
                ordinaryPopEnterTransition(),
                ordinaryPopExitTransition()
            )
        },
        transitionSpec = {
            ContentTransform(
                ordinaryEnterTransition(),
                ordinaryExitTransition()
            )
        },
        popTransitionSpec = {
            ContentTransform(
                ordinaryPopEnterTransition(),
                ordinaryPopExitTransition()
            )
        },
        entryProvider = entryProvider {
            entry<ScreenDestination.Onboarding> {
                val viewModel = hiltViewModel<OnboardingViewModel>()
                OnboardingScreen(
                    viewModel = viewModel,
                    onRootConfirm = {
                        backStack.apply {
                            clear()
                            add(ScreenDestination.PackagePicker)
                        }
                    }
                )
            }

            entry<ScreenDestination.PackagePicker> {
                val viewModel = hiltViewModel<PackagePickerViewModel>()
                PackagePickerScreen(
                    viewModel = viewModel,
                    onSettingsClick = {
                        backStack.apply {
                            add(ScreenDestination.Settings)
                        }
                    },
                    onClick = { packageName, appName ->
                        backStack.apply {
                            add(ScreenDestination.FlagsChanger(packageName, appName))
                        }
                    }
                )
            }

            entry<ScreenDestination.FlagsChanger> {
                val viewModel = hiltViewModel<FlagsChangerViewModel>()
                FlagsChangerScreen(
                    viewModel = viewModel,
                    appName = it.appName,
                    packageName = it.packageName,
                ) {
                    backStack.removeLastOrNull()
                }
            }

            entry<ScreenDestination.Settings> {
                val viewModel = hiltViewModel<SettingsViewModel>()
                SettingsScreen(
                    viewModel = viewModel
                ) {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}