import kotlin.random.Random

val allocatorTypes = arrayOf("EQ", "RAND")

fun createAllocator(allocatorType: String, frames: Int) =
    when (allocatorType) {
        "EQ" -> EqualAllocator(frames)
        "RAND" -> RandomAllocator(frames)
        else -> throw RuntimeException("No such allocator called \"$allocatorType\"")
    }

object Scheduler {
    private const val minAmount = 5
    private const val maxAmount = 10
    private const val changeChance = 2

    fun initializeProcesses(allocator: Allocator) =
        List(minAmount) {
            val process = RandomProcessGenerator.get()
            allocator.allocateProcess(process)
            process

        }.toMutableList()

    fun schedule(processes: MutableList<Process>, allocator: Allocator) {
        if (processes.size < maxAmount) maybe(changeChance) {
            val newProcess = RandomProcessGenerator.get()
            processes.add(newProcess)
            allocator.allocateProcess(newProcess)
        }
        if (processes.size > minAmount) maybe(changeChance) {
            val oldProcess = processes.random()
            processes.remove(oldProcess)
            allocator.deallocateProcess(oldProcess)
        }
    }
}

fun main() {
    val framesAmount = getNaturalNumber("Total frames number: ")
    val allocatorType = chooseFrom(allocatorTypes)
    val allocator = createAllocator(allocatorType, framesAmount)

    val processes = Scheduler.initializeProcesses(allocator)

    while (true) {
        clearTerminal()
        println(allocator.memoryDump)
        println()
        println(allocator.processDump)

        allocator.balanceAllocatedFrames()
        processes.forEach(Process::execute)
        Scheduler.schedule(processes, allocator)
        Thread.sleep(50)
    }
}