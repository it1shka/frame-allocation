object Colors {
    const val RESET = "\u001B[0m"
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"
}

tailrec fun getNaturalNumber(prompt: String?): Int {
    print(prompt ?: "Please, enter a natural number: ")
    val input = readln().trim()
    val maybeInt = input.toIntOrNull()
    if (maybeInt != null && maybeInt > 0) {
        return maybeInt;
    }
    println("\"$input\" is not a natural number. ")
    return getNaturalNumber(prompt)
}

fun <T> MutableList<T>.limitSize(limit: Int) {
    while (size > limit) {
        removeAt(0)
    }
}