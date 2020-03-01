package igrek.songbook.admin

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import javax.inject.Inject

class AdminService {
    @Inject
    lateinit var preferencesService: Lazy<PreferencesService>
    @Inject
    lateinit var navigationMenuController: Lazy<NavigationMenuController>
    @Inject
    lateinit var preferencesState: PreferencesState

    var userAuthToken: String
        get() = preferencesState.userAuthToken
        set(value) {
            preferencesState.userAuthToken = value
        }

    fun login(key: String) {
        val match = Regex("login (.*)").matchEntire(key)
        match?.let {
            userAuthToken = it.groupValues[1]
            LoggerFactory.logger.debug("Admin token entered: [$userAuthToken]")
            checkMenuVisibility()
        }
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun init() {
        checkMenuVisibility()
    }

    private fun checkMenuVisibility() {
        if (isAdminEnabled()) {
            LoggerFactory.logger.debug("Enabling admin tools")
            navigationMenuController.get().setAdminMenu()
        }
    }

    fun isAdminEnabled(): Boolean {
        return userAuthToken.isNotBlank()
    }
}