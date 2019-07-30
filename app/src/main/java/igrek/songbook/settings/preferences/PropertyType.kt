package igrek.songbook.settings.preferences

enum class PropertyType constructor(val clazz: Class<*>) {

    STRING(String::class.javaObjectType),

    BOOLEAN(Boolean::class.javaObjectType),

    INTEGER(Int::class.javaObjectType),

    LONG(Long::class.javaObjectType),

    FLOAT(Float::class.javaObjectType)
}
