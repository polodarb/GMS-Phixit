package ua.polodarb.gmsphixit.presentation.feature.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.polodarb.gmsphixit.core.phixit.InitRootDB

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val rootDB: InitRootDB
) : ViewModel() {

    private val _dbVersion = MutableStateFlow<Int?>(null)
    val dbVersion: StateFlow<Int?> = _dbVersion

    init {
        fetchDbVersion()
    }

    private fun fetchDbVersion() {
        viewModelScope.launch(Dispatchers.IO) {
            val version = rootDB.getDbVersion()
            _dbVersion.value = version
        }
    }
}