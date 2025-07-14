package ua.polodarb.gmsphixit.core.phixit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.topjohnwu.superuser.ipc.RootService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ua.polodarb.gmsphixit.core.phixit.service.IPhixitFlagsService
import ua.polodarb.gmsphixit.core.phixit.service.PhixitFlagsService
import javax.inject.Inject

data class DatabaseInitializationState(val isInitialized: Boolean)

class InitRootDB @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _databaseInitializationStateFlow =
        MutableStateFlow(DatabaseInitializationState(false))
    val databaseInitializationStateFlow: Flow<DatabaseInitializationState> =
        _databaseInitializationStateFlow

    fun setDatabaseInitialized(isInitialized: Boolean) {
        _databaseInitializationStateFlow.value = DatabaseInitializationState(isInitialized)
    }

    private var rootDatabase: IPhixitFlagsService? = null

    fun initDB() {
        Log.d("InitRootDB", "initDB called")
        val intent = Intent(context, PhixitFlagsService::class.java)
        val service = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                Log.d("InitRootDB", "onServiceConnected: $name, binder=$binder")
                try {
                    rootDatabase = IPhixitFlagsService.Stub.asInterface(binder)
                    Log.d("InitRootDB", "rootDatabase assigned: $rootDatabase")
                    setDatabaseInitialized(true)
                    Log.d("InitRootDB", "setDatabaseInitialized(true)")
                } catch (e: Exception) {
                    Log.e("InitRootDBImpl", "Error connecting to database", e)
                    throw e
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d("InitRootDB", "onServiceDisconnected: $name")
                rootDatabase = null
                setDatabaseInitialized(false)
                Log.d("InitRootDB", "setDatabaseInitialized(false)")
            }
        }
        RootService.bind(intent, service)
        Log.d("InitRootDB", "RootService.bind called")
    }

    fun getRootDatabase(): IPhixitFlagsService {
        return checkNotNull(rootDatabase) { "DB not init" }
    }

    suspend fun getDbVersion(): Int {
        return try {
            getRootDatabase().getDbVersion()
        } catch (e: Exception) {
            Log.e("InitRootDB", "Failed to get DB version", e)
            -1
        }
    }
}