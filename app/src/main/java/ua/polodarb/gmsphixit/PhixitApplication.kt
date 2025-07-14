package ua.polodarb.gmsphixit

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import ua.polodarb.gmsphixit.BuildConfig
import ua.polodarb.gmsphixit.core.errors.general.CrashActivity
import ua.polodarb.gmsphixit.core.errors.general.ExceptionHandler

@HiltAndroidApp
class PhixitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG)
            ExceptionHandler.initialize(this, CrashActivity::class.java)

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}