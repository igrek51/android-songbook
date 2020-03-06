package igrek.songbook.settings.preferences

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.user.UserDataDao
import javax.inject.Inject
import kotlin.collections.set


class PreferencesService {
    @Inject
    lateinit var userDataDao: UserDataDao
    @Inject
    lateinit var sharedPreferencesService: SharedPreferencesService

    private val logger = LoggerFactory.logger
    private var entityValues = HashMap<String, Any>()

    init {
        DaggerIoc.factoryComponent.inject(this)
        loadAll()
    }

    fun loadAll() {
        entityValues = HashMap(readEntities())
        applyDefaults()
    }

    private fun readEntities(): Map<String, Any> {
        val primitives = userDataDao.preferencesDao!!.getPrimitiveEntries()
        if (!primitives.isNullOrEmpty())
            return primitives2entities(primitives)

        logger.warn("no user data preferences found, reading from shared preferences")
        return sharedPreferencesService.getEntities()
    }

    private fun primitives2entities(primitives: Map<String, Any>): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        for (prefDef in PreferencesField.values()) {
            val name = prefDef.preferenceName()
            primitives[name]?.let {
                val entityVal = prefDef.typeDef.primitive2entity(it)
                entities[name] = entityVal
            }
        }
        return entities
    }

    private fun entities2primitives(entities: Map<String, Any>): Map<String, Any> {
        val primitives = mutableMapOf<String, Any>()
        for (prefDef in PreferencesField.values()) {
            val name = prefDef.preferenceName()
            entities[name]?.let {
                val primitiveVal = prefDef.typeDef.entity2primitive(it)
                primitives[name] = primitiveVal
            }
        }
        return primitives
    }

    private fun applyDefaults() {
        for (prefDef in PreferencesField.values()) {
            val prefName = prefDef.preferenceName()
            if (prefName !in entityValues) {
                entityValues[prefName] = prefDef.typeDef.defaultValue
            }
        }
    }

    fun saveAll() {
        val primitiveValues = entities2primitives(entityValues)
        userDataDao.preferencesDao!!.setPrimitiveEntries(primitiveValues)
    }

    fun <T> getValue(prefDef: PreferencesField): T {
        val propertyName = prefDef.preferenceName()
        if (propertyName !in entityValues)
            return prefDef.typeDef.defaultValue as T

        val propertyValue = entityValues[propertyName]
        return propertyValue as T
    }

    fun setValue(prefDef: PreferencesField, value: Any?) {
        val propertyName = prefDef.preferenceName()

        if (value == null) {
            entityValues.remove(propertyName)
            return
        }

        // class type validation
        val validClazz = prefDef.typeDef.validClass().simpleName
        val givenClazz = value::class.simpleName
        require(givenClazz == validClazz) {
            "invalid value type, expected: $validClazz, but given: $givenClazz"
        }

        entityValues[propertyName] = value
    }

    fun clear() {
        userDataDao.preferencesDao?.factoryReset()
        entityValues.clear()
        saveAll()
        loadAll()
    }

    fun reload() {
        loadAll()
    }

}
