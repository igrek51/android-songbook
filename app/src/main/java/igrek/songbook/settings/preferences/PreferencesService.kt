package igrek.songbook.settings.preferences


import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.preferences.TypedPrimitiveEntries
import kotlin.collections.set

class PreferencesService(
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
) {
    private val userDataDao by LazyExtractor(userDataDao)

    private val logger = LoggerFactory.logger
    private var entityValues: MutableMap<String, Any> = mutableMapOf()

    init {
        loadAll()
    }

    private fun loadAll() {
        entityValues = HashMap(readEntities())
        applyDefaults()
    }

    fun reload() {
        loadAll()
    }

    fun dumpAll() {
        val primitiveValues = entities2primitives(entityValues)

        userDataDao.preferencesDao.setPrimitiveEntries(primitiveValues)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValue(fieldDef: FieldDefinition<T, *>): T {
        val propertyName = fieldDef.preferenceName
        if (propertyName !in entityValues)
            return fieldDef.defaultValue

        val propertyValue = entityValues[propertyName]
        return propertyValue as T
    }

    fun <T : Any> setValue(fieldDef: FieldDefinition<T, *>, value: Any?) {
        val propertyName = fieldDef.preferenceName

        if (value == null) {
            entityValues.remove(propertyName)
            return
        }

        // class type validation
        val validClazz = fieldDef.validClass().simpleName
        val givenClazz = value::class.simpleName
        require(givenClazz == validClazz) {
            "invalid value type, expected: $validClazz, but given: $givenClazz"
        }

        entityValues[propertyName] = value
    }

    fun clear() {
        userDataDao.preferencesDao.factoryReset()
        entityValues.clear()
        dumpAll()
        loadAll()
    }

    private fun readEntities(): Map<String, Any> {
        if (SettingsState.knownSettingFields.isEmpty()) {
            logger.warn("no known setting fields")
        }
        val primitives: TypedPrimitiveEntries = userDataDao.preferencesDao.getPrimitiveEntries()
        if (primitives.isEmpty()) {
            logger.info("no user data preferences found, loading defaults")
            return emptyMap()
        }
        return primitives2entities(primitives)
    }

    private fun applyDefaults() {
        SettingsState.knownSettingFields.forEach { (prefName: String, fieldDef: FieldDefinition<*, *>) ->
            if (prefName !in entityValues) {
                entityValues[prefName] = fieldDef.defaultValue
            }
        }
    }

    private fun primitives2entities(primitives: TypedPrimitiveEntries): Map<String, Any> {
        val entities = mutableMapOf<String, Any>()
        SettingsState.knownSettingFields.forEach { (prefName: String, fieldDef: FieldDefinition<*, *>) ->
            primitives.get(prefName)?.let { primitive: Any ->
                val entityVal = anyPrimitiveToEntity(primitive, fieldDef)
                entities[prefName] = entityVal
            }
        }
        return entities
    }

    private fun entities2primitives(entities: Map<String, Any>): TypedPrimitiveEntries {
        val primitives = TypedPrimitiveEntries()
        SettingsState.knownSettingFields.forEach { (prefName: String, fieldDef: FieldDefinition<*, *>) ->
            entities[prefName]?.let { entity: Any ->
                val primitiveVal = anyEntityToPrimitive(entity, fieldDef)
                primitives.set(prefName, primitiveVal)
            }
        }
        return primitives
    }

    @Suppress("UNCHECKED_CAST")
    private fun <P : Any> anyPrimitiveToEntity(primitive: Any, fieldDef: FieldDefinition<*, P>): Any {
        return fieldDef.primitive2entity(primitive as P)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> anyEntityToPrimitive(entity: Any, fieldDef: FieldDefinition<T, *>): Any {
        return fieldDef.entity2primitive(entity as T)
    }
}
