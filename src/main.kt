val allocatorTypes = arrayOf("EQ", "RAND", "PROP", "FAULT", "WORKSET")

fun createAllocator(allocatorType: String, frames: Int) =
    when (allocatorType) {
        "EQ" -> EqualAllocator(frames)
        "RAND" -> RandomAllocator(frames)
        "PROP" -> ProportionalAllocator(frames)
        "FAULT" -> PageFaultFrequencyAllocator(frames)
        "WORKSET" -> WorkingSetAllocator(frames)
        else -> throw RuntimeException("No such allocator called \"$allocatorType\"")
    }

object Scheduler {
    private const val minAmount = 5
    private const val maxAmount = 15
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
    clearTerminal()
    val framesAmount = getInteger("Total frames number: ", 49)
    val allocatorType = chooseFrom(allocatorTypes, "Select allocator: ")
    val allocator = createAllocator(allocatorType, framesAmount)
    val processes = Scheduler.initializeProcesses(allocator)

    val display = getDisplay()
    while (true) {
        display.update(allocator)

        allocator.balanceAllocatedFrames()
        processes.forEach(Process::execute)
        Scheduler.schedule(processes, allocator)
        Thread.sleep(50)
    }
}