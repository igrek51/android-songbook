package igrek.songbook.about.secret

import com.google.common.base.Predicate

class CommandRule {

    val condition: Predicate<String>
    val activator: (key: String) -> Unit

    constructor(condition: Predicate<String>, activator: (key: String) -> Unit) {
        this.condition = condition
        this.activator = activator
    }

    constructor(exactKey: String, activator: (key: String) -> Unit) {
        this.condition = Predicate { input -> input == exactKey }
        this.activator = activator
    }

    constructor(vararg exactKeys: String, activator: (key: String) -> Unit) {
        this.condition = Predicate { input -> exactKeys.contains(input) }
        this.activator = activator
    }
}
