package igrek.songbook.settings.preferences


import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import java.util.*
import kotlin.collections.set

class SharedPreferencesService(
        sharedPreferences: LazyInject<SharedPreferences> = appFactory.sharedPreferences,
) {
    private val sharedPreferences by LazyExtractor(sharedPreferences)

    private val logger = LoggerFactory.logger
    private val propertyValues = HashMap<String, Any>()

    companion object {
        private const val sharedPreferencesName = "SongBook-UserPreferences"

        val sharedPreferencesCreator: (activity: AppCompatActivity) -> SharedPreferences = { activity -> activity.applicationContext.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE) }
    }

    fun getEntities(): Map<String, Any> {
        loadAll()
        return propertyValues.toMutableMap()
    }

    private fun loadAll() {
        propertyValues.clear()
        for (prefDef in PreferencesField.values()) {
            loadProperty(prefDef)
        }
    }

    private fun saveAll() {
        val editor = sharedPreferences.edit()
        for (prefDef in PreferencesField.values()) {
            saveProperty(prefDef, editor)
        }
        editor.apply()
    }

    private fun loadProperty(prefDef: PreferencesField) {
        val propertyName = prefDef.preferenceName()
        var value: Any
        if (exists(prefDef)) {
            try {
                value = prefDef.typeDef.load(sharedPreferences, propertyName)
            } catch (e: ClassCastException) {
                value = prefDef.typeDef.defaultValue
                logger.warn("Invalid property type, loading default value: $propertyName = $value")
            }
        } else {
            value = prefDef.typeDef.defaultValue
            logger.debug("Missing preferences property, loading default value: $propertyName = $value")
        }
        // logger.debug("Property loaded: $propertyName = $value")
        propertyValues[propertyName] = value
    }

    private fun saveProperty(prefDef: PreferencesField, editor: SharedPreferences.Editor) {
        val propertyName = prefDef.preferenceName()
        if (!propertyValues.containsKey(propertyName)) {
            logger.warn("No shared preferences property found in map")
        }

        val propertyValue = propertyValues[propertyName]
        // logger.debug("Saving property: $propertyName = $propertyValue")

        if (propertyValue == null) {
            editor.remove(propertyName)
            return
        }

        prefDef.typeDef.save(editor, propertyName, propertyValue)
    }

    fun <T> getValue(prefDef: PreferencesField): T {
        val propertyName = prefDef.preferenceName()
        if (!propertyValues.containsKey(propertyName))
            return prefDef.typeDef.defaultValue as T

        val propertyValue = propertyValues[propertyName]
        return propertyValue as T
    }

    fun setValue(prefDef: PreferencesField, value: Any) {
        val propertyName = prefDef.preferenceName()
        // class type validation
        if (value != null) {
            val validClazz = prefDef.typeDef.validClass().simpleName
            val givenClazz = value::class.simpleName
            require(givenClazz == validClazz) {
                "invalid value type, expected: $validClazz, but given: $givenClazz"
            }
        }
        propertyValues[propertyName] = value
    }

    fun clear() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        loadAll()
    }

    private fun exists(prefDef: PreferencesField): Boolean {
        return sharedPreferences.contains(prefDef.preferenceName())
    }

}
