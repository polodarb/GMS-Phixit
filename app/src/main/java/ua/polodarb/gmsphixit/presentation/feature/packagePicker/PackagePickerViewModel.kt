package ua.polodarb.gmsphixit.presentation.feature.packagePicker

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.milliseconds
import ua.polodarb.gmsphixit.core.phixit.InitRootDB
import javax.inject.Inject

@HiltViewModel
class PackagePickerViewModel @Inject constructor(
    application: Application,
    private val rootDB: InitRootDB
) : AndroidViewModel(application) {

    var state by mutableStateOf(PackagePickerState())
        private set

    var expandedSections by mutableStateOf(setOf<String>())
        private set

    var loadingSections by mutableStateOf(setOf<String>())
        private set

    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        state = state.copy(isLoading = true)
        observeDBInit()
    }

    private fun observeDBInit() {
        viewModelScope.launch {
            rootDB.databaseInitializationStateFlow.collectLatest { dbState ->
                if (dbState.isInitialized) {
                    loadPackages()
                } else {
                    state = state.copy(isLoading = true)
                }
            }
        }
    }

    fun onSearch(query: String) {
        state = state.copy(searchQuery = query)
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300.milliseconds)
            filterPackages()
        }
    }

    fun toggleManualInput() {
        state = state.copy(showManualInput = !state.showManualInput)
    }

    fun onManualPackageChange(packageName: String) {
        state = state.copy(manualPackageName = packageName)
    }

    fun onManualPackageSubmit(onClick: (packageName: String, appName: String) -> Unit) {
        val packageName = state.manualPackageName.trim()
        if (packageName.isNotEmpty()) {
            onClick(packageName, "Manual Package")
        }
    }

    fun toggleSection(packageName: String) {
        if (expandedSections.contains(packageName)) {
            expandedSections = expandedSections - packageName
        } else {
            expandedSections = expandedSections + packageName
            if (!loadingSections.contains(packageName)) {
                loadingSections = loadingSections + packageName
                viewModelScope.launch(Dispatchers.Default) {
                    kotlinx.coroutines.delay(500)
                    loadingSections = loadingSections - packageName
                }
            }
        }
    }

    private fun loadPackages() {
        viewModelScope.launch(Dispatchers.IO) {
            state = state.copy(isLoading = true)
            try {
                val phixitService = rootDB.getRootDatabase()
                val allPackages = withTimeout(20.seconds) {
                    phixitService.getAllConfigPackages()
                }
                val pm = getApplication<Application>().packageManager
                val allPackagesInfo = pm.getInstalledPackages(PackageManager.GET_META_DATA)
                val installedApps = allPackagesInfo.mapNotNull { it.applicationInfo }
                val appMap = installedApps
                    .filter {
                        val pkg = it.packageName.lowercase()
                        pkg.contains("android") || pkg.contains("bkb") || pkg.contains("google")
                    }
                    .associateBy { it.packageName }
                val used = mutableSetOf<String>()
                val sections = mutableListOf<AppSection>()
                for ((pkgName, appInfo) in appMap) {
                    val matched = allPackages.filter { it.substringAfter('#', it) == pkgName }
                    if (matched.isNotEmpty()) {
                        sections.add(
                            AppSection(
                                appName = pm.getApplicationLabel(appInfo).toString(),
                                appIcon = pm.getApplicationIcon(appInfo),
                                packageName = pkgName,
                                packages = matched
                            )
                        )
                        used.addAll(matched)
                    }
                }
                val others = allPackages.filter { it !in used }
                if (others.isNotEmpty()) {
                    sections.add(AppSection("Others", null, "others", others))
                }
                val sortedSections = sections.sortedWith(
                    compareBy<AppSection> { it.appName == "Others" }.thenBy { it.appName }
                )
                state = state.copy(
                    isLoading = false,
                    sections = sortedSections,
                    filteredSections = sortedSections
                )
            }
            catch (e: TimeoutCancellationException) {
                throw RuntimeException("Loading packages timed out, current timeout is 20 seconds", e)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                throw e
            }
        }
    }

    private fun filterPackages() {
        val query = state.searchQuery.trim().lowercase()
        if (query.isBlank()) {
            state = state.copy(filteredSections = state.sections)
            return
        }
        val filtered = state.sections.mapNotNull { section ->
            val filteredPkgs = section.packages.filter { it.contains(query, true) }
            if (section.appName.contains(query, true) || filteredPkgs.isNotEmpty()) {
                section.copy(packages = filteredPkgs)
            } else null
        }.filter { it.packages.isNotEmpty() }
        state = state.copy(filteredSections = filtered)
    }
}

data class AppSection(
    val appName: String,
    val appIcon: Drawable?,
    val packageName: String,
    val packages: List<String>
)

data class PackagePickerState(
    val isLoading: Boolean = false,
    val sections: List<AppSection> = emptyList(),
    val filteredSections: List<AppSection> = emptyList(),
    val searchQuery: String = "",
    val showManualInput: Boolean = false,
    val manualPackageName: String = ""
)
