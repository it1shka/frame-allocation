import kotlin.random.Random

class EqualAllocator(totalFrames: Int): Allocator(totalFrames) {
    override fun estimateAllocation() = processes.associateWith {
        totalFrames / processes.size
    }.toMap()
}

class RandomAllocator(totalFrames: Int): Allocator(totalFrames) {
    override fun estimateAllocation() = processes.associateWith {
        Random.nextInt(totalFrames)
    }.toMap()
}

class ProportionalAllocator(totalFrames: Int): Allocator(totalFrames) {
    override fun estimateAllocation(): Map<Process, Int> {
        val totalSize = processes.sumOf { it.size }
        return processes.associateWith {
            (it.size.toFloat() / totalSize.toFloat() * totalFrames.toFloat()).toInt()
        }.toMap()
    }
}

class PageFaultFrequencyAllocator(totalFrames: Int): Allocator(totalFrames) {
    override fun estimateAllocation(): Map<Process, Int> {
        val totalFreq = processes.sumOf { it.faultRate }
        return processes.associateWith {
            (it.faultRate / totalFreq * totalFrames.toFloat()).toInt()
        }.toMap()
    }
}

class WorkingSetAllocator(totalFrames: Int): Allocator(totalFrames) {
    override fun estimateAllocation(): Map<Process, Int> {
        val totalSet = processes.sumOf { it.workingSetSize }
        return processes.associateWith {
            (it.workingSetSize.toFloat() / totalSet.toFloat() * totalFrames.toFloat()).toInt()
        }.toMap()
    }
}

