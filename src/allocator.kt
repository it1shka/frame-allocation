import kotlin.math.sqrt

private val colors = arrayOf(
    Colors.BLUE,
    Colors.CYAN, Colors.PURPLE,
    Colors.YELLOW, Colors.WHITE,
    Colors.GREEN, Colors.RED,
)

abstract class Allocator(protected val totalFrames: Int) {
    protected val minimalFrameRequirement = 2
    protected val processes = mutableListOf<Process>()
    protected val allocatedMemory = HashMap<Int, Int>()

    // main interface

    fun allocateProcess(process: Process): Boolean {
        val freeSpace = requestFreeFrames(minimalFrameRequirement).toMutableList()
        if (freeSpace.size < minimalFrameRequirement) {
            val forcedSpace = forceDeallocation(minimalFrameRequirement - freeSpace.size)
            freeSpace += forcedSpace
        }
        if (freeSpace.size < minimalFrameRequirement) {
            return false
        }
        allocateForProcess(process, freeSpace)
        processes.add(process)
        return true
    }

    fun deallocateProcess(process: Process) {
        process
            .deallocate(Int.MAX_VALUE)
            .forEach(allocatedMemory::remove)
        processes.remove(process)
    }

    protected fun reallocate(allocation: Map<Process, Int>) {
        allocation
            .filter { (process, space) -> process.allocatedSpace > space }
            .forEach { (process, space) ->
                val delta = process.allocatedSpace - space
                if (process.allocatedSpace - delta < minimalFrameRequirement) return
                process.deallocate(delta).forEach(allocatedMemory::remove)
            }

        allocation
            .filter { (process, space) -> process.allocatedSpace < space }
            .forEach { (process, space) ->
                val delta = space - process.allocatedSpace
                val frames = requestFreeFrames(delta)
                allocateForProcess(process, frames)
            }
    }

    abstract fun balanceAllocatedFrames()

    // helping functions

    protected fun requestFreeFrames(amount: Int) = (0 until totalFrames)
        .filter { !allocatedMemory.contains(it) }
        .take(amount)

    protected fun forceDeallocation(amount: Int): List<Int> {
        processes
            .filter { it.allocatedSpace > minimalFrameRequirement }
        val deallocated = mutableListOf<Int>()
        for (process in processes) {
            val expected = amount - deallocated.size
            if (expected <= 0) break
            deallocated += process.deallocate(expected)
        }
        return deallocated
    }

    protected fun allocateForProcess(process: Process, frames: List<Int>) {
        process.allocate(frames)
        frames.forEach { allocatedMemory[it] = process.id }
    }

    // for dumping purposes

    val memoryDump get() = (0 until totalFrames)
        .map { frame ->
            val tag = allocatedMemory[frame]
            if (tag == null) {
                "${Colors.BLACK}X${Colors.RESET}"
            } else {
                "${colors[tag % colors.size]}$tag${Colors.RESET}"
            }
        }
        .chunked(sqrt(totalFrames.toDouble()).toInt())
        .joinToString(separator = "\n") {
            it.joinToString(separator = "")
        }

    val processDump get() = processes
        .mapIndexed{ index, process -> "${index+1}) $process"}
        .joinToString(separator = "\n")
}
