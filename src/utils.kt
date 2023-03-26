import kotlin.random.Random
import kotlin.math.max

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

tailrec fun getInteger(prompt: String?, bound: Int = 0): Int {
    print(prompt ?: "Please, enter integer: ")
    val input = readln().trim()
    val maybeInt = input.toIntOrNull()
    if (maybeInt != null && maybeInt > bound) {
        return maybeInt
    }
    if (maybeInt == null) {
        println("\"$input\" is not an integer at all. ")
    } else {
        println("\"$input\" is less than specified bound $bound. ")
    }
    return getInteger(prompt, bound)
}

fun <T> MutableList<T>.limitSize(limit: Int) {
    while (size > limit) {
        removeAt(0)
    }
}

tailrec fun <T> chooseFrom(options: Array<T>, message: String = "Please, choose one from the following options: "): T {
    println(message)
    val optionList = options
        .mapIndexed { i, v -> "${i+1}) $v" }
        .joinToString(separator = "\n")
    println(optionList)
    val choice = getInteger("Your choice (index): ")
    if (choice <= options.size) return options[choice - 1]
    println("Your choice \"$choice\" is not in range. ")
    return chooseFrom(options, message)
}

object RandomProcessGenerator {
    private val extensions = arrayOf(".py", ".rb", ".exs", ".kt", ".sh", ".js")
    private val filenames = arrayOf("program", "script", "file", "virus", "app", "main")
    private var currentId = 0

    private fun getRandomName(): String {
        val name = filenames.random()
        val index = Random.nextInt(10) + 1
        val ext = extensions.random()
        return "$name$index$ext"
    }

    fun get(): Process {
        val name = getRandomName()
        val id = currentId++
        val size = Random.nextInt(90) + 10
        return Process(name, id, size)
    }
}

inline fun maybe(chance: Int, action: () -> Unit) {
    if (Random.nextInt(100) < chance) {
        action()
    }
}

fun clearTerminal() {
    print("\u001B[H\u001B[2J")
}

fun String.pad(size: Int) =
    this + " ".repeat(max(size - length, 0))