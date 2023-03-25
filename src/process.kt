import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Process(private val name: String, val id: Int, val size: Int) {
    val workingSetSize get() = workingSet.toSet().size
    val faultRate get() = pageFaults.sum().toFloat() / pageFaults.size.toFloat()

    private val referenceString = ReferenceStringGenerator.getPageSequence()
    private val workingSet = mutableListOf<Int>()
    private val pageFaults = mutableListOf<Int>()
    private var currentPage = -1

    private val memory = Memory()
    fun allocate(frames: Iterable<Int>) = memory.allocate(frames)
    fun deallocate(delta: Int) = memory.deallocate(delta)
    val allocatedSpace get() = memory.size

    fun execute() {
        currentPage = referenceString.first()
        val maybeFault = memory.processPage(currentPage)
        pageFaults.add(maybeFault)
        workingSet.add(currentPage)
        // ensure size
        pageFaults.limitSize(10)
        workingSet.limitSize(5 + size)
    }

    override fun toString(): String {
        val nameLabel = "${Colors.PURPLE}$name (ID $id)${Colors.RESET}"
        val setLabel = "|WS| = ${Colors.RED}$workingSetSize${Colors.RESET}"
        val faultRateFormatted = "%.2f".format(faultRate * 100).plus("5")
        val faultLabel = "Fault Rate = ${Colors.YELLOW}$faultRateFormatted${Colors.RESET}"
        return "$nameLabel: $memory, $setLabel, $faultLabel"
    }
}

private class Memory {
    private val frames = mutableListOf<Pair<Int, Int?>>()
    val size get() = frames.size
    private var clock = 0
    private val useTime = HashMap<Int, Int>()
    private fun getUseTime(frame: Pair<Int, Int?>) =
        useTime.getOrDefault(frame.second, -1)

    fun allocate(alloc: Iterable<Int>) {
        frames += alloc.map { Pair(it, null) }
    }

    fun deallocate(delta: Int): List<Int> {
        val leastUsed = frames
            .sortedBy(this::getUseTime)
            .take(delta)
            .map { it.first }
        frames.removeIf { leastUsed.contains(it.first) }
        return leastUsed
    }

    fun processPage(page: Int): Int {
        useTime[page] = clock++
        if (frames.map{ it.second }.contains(page)) {
            return 0
        }
        val bestReplace = frames
            .minBy(this::getUseTime)
            .first
        val index = frames.indexOfFirst { it.first == bestReplace }
        frames[index] = frames[index].copy(second = page)
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