package ua.polodarb.gmsphixit.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ua.polodarb.gmsphixit.core.phixit.InitRootDB
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {
    @Provides
    @Singleton
    fun provideInitRootDB(@ApplicationContext context: Context): InitRootDB = InitRootDB(context)

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("gms_phixit_prefs", Context.MODE_PRIVATE)
    }
}

