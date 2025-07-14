package ua.polodarb.gmsphixit.presentation.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ScreenDestination : NavKey, Parcelable {

    @Parcelize
    data object Onboarding : ScreenDestination()

    @Parcelize
    data object Settings : ScreenDestination()

    @Parcelize
    data object PackagePicker : ScreenDestination()

    @Parcelize
    data class FlagsChanger(val packageName: String, val appName: String) : ScreenDestination()
}