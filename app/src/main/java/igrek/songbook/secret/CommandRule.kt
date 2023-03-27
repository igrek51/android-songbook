package igrek.songbook.secret

import igrek.songbook.system.locale.StringSimplifier


open class CommandRule(
    val condition: (key: String) -> Boolean,
    val activator: suspend (key: String) -> Unit,
)


class SimpleKeyRule(
    vararg exactKeys: String,
    activator: suspend (key: String) -> Unit,
) : CommandRule(condition = { key ->
    val simplified = StringSimplifier.simplify(key)
    exactKeys.contains(simplified)
}, activator)


class SubCommandRule(
    prefix: String,
    subcommandActivator: suspend (key: String) -> Unit,
) : CommandRule(condition = { key ->
    val simpleKey = StringSimplifier.simplify(key)
    simpleKey.startsWith("$prefix ")
}, activator = { key: String ->
    subcommandActivator(key.drop(prefix.length + 1))
})


class NestedSubcommandRule(
    vararg val prefixes: String,
    val rules: List<CommandRule>,
) : CommandRule(condition = { key ->
    val simpleKey = StringSimplifier.simplify(key)
    prefixes.any { prefix -> simpleKey.startsWith("$prefix ") }
}, activator = { key: String ->
})


class ActivationResult(
    val activator: suspend (key: String) -> Unit,
    private val key: String,
) {
    suspend fun run() {
        activator(key)
    }
}


fun findActivator(rules: List<CommandRule>, key: String): ActivationResult? {
    for (rule in rules) {

        if (rule is NestedSubcommandRule) {
            val simpleKey = StringSimplifier.simplify(key)

            for (prefix in rule.prefixes) {
                if (simpleKey.startsWith("$prefix ")) {
                    val subkey = key.drop(prefix.length + 1)
                    findActivator(rule.rules, subkey)?.let { activation ->
                        return activation
                    }
                }
            }

        } else {
            if (rule.condition(key)) {
                return ActivationResult(rule.activator, key)
            }
        }
    }
    return null
}
