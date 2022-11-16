package igrek.songbook.system.filesystem


import java.util.*

internal class FirstRuleChecker<T> {
    // remembers inserting order
    private val rules = LinkedList<Rule>()

    fun addRule(condition: () -> Boolean, then: () -> (T?)): FirstRuleChecker<T> {
        rules.add(Rule(condition, then))
        return this
    }

    fun addRule(then: () -> (T?)): FirstRuleChecker<T> {
        return addRule({ true }, then)
    }

    fun find(): T? {
        for (rule in rules) {
            val condition = rule.condition
            if (condition.invoke()) {
                val then = rule.then
                val value = then.invoke()
                // accept only not null values
                if (value != null)
                    return value
            }
        }
        return null
    }

    private inner class Rule(
        var condition: () -> Boolean,
        var then: () -> (T?)
    )
}
