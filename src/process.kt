import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Process(private val name: String, val size: Int) {
    val memory = Memory()
    val workingSetSize get() = workingSet.toSet().size
    val faultRate get() = pageFaults.sum().toFloat() / pageFaults.size.toFloat()

    private val referenceString = ReferenceStringGenerator.getPageSequence()
    private val workingSet = mutableListOf<Int>()
    private val pageFaults = mutableListOf<Int>()
    private var currentPage = -1

    fun execute() {
        currentPage = referenceString.first()
        val maybeFault = memory.processPage(currentPage)
        pageFaults.add(maybeFault)
        workingSet.add(currentPage)
        // ensure size
        pageFaults.limitSize(10);
        workingSet.limitSize(5 + size)
    }

    override fun toString(): String {
        val nameLabel = "${Colors.PURPLE}$name${Colors.RESET}"
        val setLabel = "|WS| = ${Colors.RED}$workingSetSize${Colors.RESET}"
        val faultLabel = "Fault Rate = ${Colors.YELLOW}$faultRate${Colors.RESET}"
        return "$nameLabel: $memory, $setLabel, $faultLabel"
    }
}

class Memory {
    private val frames = mutableListOf<Int?>()
    private var clock = 0
    private val useTime = HashMap<Int, Int>()

    fun allocate(delta: Int) {
        frames += List(delta) { null }
    }

    fun deallocate(delta: Int): Int {
        val prioritised = frames.sortedBy {
            useTime.getOrDefault(it, -1)
        }.toMutableList()
        val deallocSize = min(prioritised.size, delta)
        repeat(deallocSize) {
            val toDeallocate = prioritised.removeAt(0)
            frames.remove(toDeallocate)
        }
        return deallocSize
    }

    fun processPage(page: Int): Int {
        useTime[page] = clock++
        if (frames.contains(page)) {
            return 0;
        }
        val bestReplace = frames.minBy {
            useTime.getOrDefault(it, -1)
        }
        val index = frames.indexOf(bestReplace)
        frames[index] = page
        return 1
    }

    override fun toString() =
        frames.joinToString(separator = "") {
            "[${Colors.CYAN}$it${Colors.RESET}]"
        }
}

private object ReferenceStringGenerator {
    private const val pageBound = 20

    private fun randDirection() =
        if (Random.nextBoolean()) -1 else 1

    fun getPageSequence() = sequence {
        var current = Random.nextInt(pageBound - 1) + 1
        while (true) {
            yield(current)
            val choice = Random.nextInt(100)
            current += when {
                choice < 90 -> randDirection()
                choice == 90 -> max(4, pageBound / 3) * randDirection()
                else -> 0
            }
            current = max(1, min(pageBound - 1, current))
        }
    }
}