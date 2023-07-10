package igrek.songbook.settings.preferences

import kotlin.reflect.KClass


abstract class FieldDefinition<T : Any, P: Any>(
    val preferenceName: String,
    val defaultValue: T,
) {

    abstract fun primitive2entity(primitive: P): T

    abstract fun entity2primitive(entity: T): P

    fun validClass(): KClass<out T> {
        return defaultValue::class
    }
}

class StringField(
    preferenceName: String,
    defaultValue: String,
) : FieldDefinition<String, String>(preferenceName, defaultValue) {

    override fun primitive2entity(primitive: String): String {
        return primitive
    }

    override fun entity2primitive(entity: String): String {
        return entity
    }
}

class LongField(
    preferenceName: String,
    defaultValue: Long,
) : FieldDefinition<Long, Long>(preferenceName, defaultValue) {

    override fun primitive2entity(primitive: Long): Long {
        return primitive
    }

    override fun entity2primitive(entity: Long): Long {
        return entity
    }
}

class FloatField(
    preferenceName: String,
    defaultValue: Float,
) : FieldDefinition<Float, Float>(preferenceName, defaultValue) {

    override fun primitive2entity(primitive: Float): Float {
        return primitive
    }

    override fun entity2primitive(entity: Float): Float {
        return entity
    }
}

class BooleanField(
    preferenceName: String,
    defaultValue: Boolean,
) : FieldDefinition<Boolean, Boolean>(preferenceName, defaultValue) {

    override fun primitive2entity(primitive: Boolean): Boolean {
        return primitive
    }

    override fun entity2primitive(entity: Boolean): Boolean {
        return entity
    }
}

class GenericStringIdField<T : Any>(
    preferenceName: String,
    defaultValue: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T?,
) : FieldDefinition<T, String>(preferenceName, defaultValue) {

    override fun primitive2entity(primitive: String): T {
        return deserializer(primitive) ?: defaultValue
    }

    override fun entity2primitive(entity: T): String {
        return serializer(entity)
    }
}

class GenericLongIdField<T : Any>(
    preferenceName: String,
    defaultValue: T,
    private val serializer: (T) -> Long,
    private val deserializer: (Long) -> T?,
) : FieldDefinition<T, Long>(preferenceName, defaultValue) {

    override fun primitive2entity(primitive: Long): T {
        return deserializer(primitive) ?: defaultValue
    }

    override fun entity2primitive(entity: T): Long {
        return serializer(entity)
    }
}
