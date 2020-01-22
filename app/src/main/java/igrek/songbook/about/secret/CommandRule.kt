package igrek.songbook.about.secret

import com.google.common.base.Predicate

class CommandRule {

    val condition: Predicate<String>
    val activator: () -> Unit

    constructor(condition: Predicate<String>, activator: () -> Unit) {
        this.condition = condition
        this.activator = activator
    }

    constructor(exactKey: String, activator: () -> Unit) {
        this.condition = Predicate { input -> input == exactKey }
        this.activator = activator
    }

    constructor(vararg exactKeys: String, activator: () -> Unit) {
        this.condition = Predicate { input -> exactKeys.contains(input) }
        this.activator = activator
    }
}
