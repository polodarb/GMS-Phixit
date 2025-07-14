package ua.polodarb.gmsphixit.presentation.navigation

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class NavigationManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    fun getInitialDestination(): ScreenDestination {
        val hasCompletedOnboarding = sharedPreferences.getBoolean("has_completed_onboarding", false)
        return if (hasCompletedOnboarding) {
            ScreenDestination.PackagePicker
        } else {
            ScreenDestination.Onboarding
        }
    }

    fun markOnboardingCompleted() {
        sharedPreferences.edit { putBoolean("has_completed_onboarding", true) }
    }
} 