package igrek.songbook.secret

import igrek.songbook.system.locale.StringSimplifier

open class CommandRule(
        val condition: (key: String) -> Boolean,
        val activator: (key: String) -> Unit,
)

class ExactKeyRule(
        exactKey: String,
        activator: (key: String) -> Unit,
) : CommandRule(condition = { it == exactKey }, activator)

class SubCommandRule(
        prefix: String,
        subcommandActivator: (key: String) -> Unit,
) : CommandRule(condition = {
    it.startsWith("$prefix ")
}, activator = { key: String ->
    subcommandActivator(key.drop(prefix.length + 1))
})

class SimplifiedKeyRule(
        vararg exactKeys: String,
        activator: (key: String) -> Unit,
) : CommandRule(condition = { key ->
    val simplified = StringSimplifier.simplify(key)
    exactKeys.contains(simplified)
}, activator)
