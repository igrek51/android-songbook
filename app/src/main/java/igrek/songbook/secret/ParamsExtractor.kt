package igrek.songbook.secret

fun extractParameters(cmd: String): List<String> {
    val matches = """"[\S\s]*?"|\S+""".toRegex().findAll(cmd)
    return matches.map { match ->
        clearOutQuotes(match.groups[0]!!.value)
    }.toList()
}

private fun clearOutQuotes(param: String): String {
    if (param.length >= 2 && param.startsWith("\"") && param.endsWith("\""))
        return param.substring(1, param.length - 1)
    return param
}