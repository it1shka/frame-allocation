import kotlin.math.min
import kotlin.math.sqrt

private val colors = arrayOf(
    Colors.BLUE,
    Colors.CYAN, Colors.PURPLE,
    Colors.YELLOW, Colors.WHITE,
    Colors.GREEN, Colors.RED,
)

abstract class Allocator(protected val totalFrames: Int) {
    private val chunkSize = sqrt(totalFrames.toDouble()).times(1.5).toInt()
    private val minimalFrameRequirement = 2
    private val allocatedMemory = HashMap<Int, Int>()

    protected val processes = mutableListOf<Process>()

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

    private fun reallocate(allocation: Map<Process, Int>) {
        allocation
            .filter { (process, space) -> process.allocatedSpace > space }
            .forEach { (process, space) ->
                val delta = process.allocatedSpace - space
                // if (process.allocatedSpace - delta < minimalFrameRequirement) return
                val deallocationSize = min(process.allocatedSpace - minimalFrameRequirement, delta)
                process.deallocate(deallocationSize).forEach(allocatedMemory::remove)
            }

        allocation
            .filter { (process, space) -> process.allocatedSpace < space }
            .forEach { (process, space) ->
                val delta = space - process.allocatedSpace
                val frames = requestFreeFrames(delta)
                allocateForProcess(process, frames)
            }
    }

    fun balanceAllocatedFrames() =
        reallocate(estimateAllocation())

    abstract fun estimateAllocation(): Map<Process, Int>

    // helping functions

    private fun requestFreeFrames(amount: Int) = (0 until totalFrames)
        .filter { !allocatedMemory.contains(it) }
        .take(amount)

    private fun forceDeallocation(amount: Int): List<Int> {
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

    private fun allocateForProcess(process: Process, frames: List<Int>) {
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
                val sign = if (tag < 16) tag.toString(16).uppercase() else "0"
                "${colors[tag % colors.size]}$sign${Colors.RESET}"
            }
        }
        .chunked(chunkSize)
        .joinToString(separator = "\n") {
            it.joinToString(separator = "")
        }

    val processDump get() = processes
        .mapIndexed{ index, process -> "${colors[process.id % colors.size]}${index+1})${Colors.RESET} $process"}
        .joinToString(separator = "\n")
}
