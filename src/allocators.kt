import kotlin.random.Random

class EqualAllocator(totalFrames: Int): Allocator(totalFrames) {
    override fun balanceAllocatedFrames() =
        reallocate(processes
            .associateWith { totalFrames / processes.size }
        )
}

class RandomAllocator(totalFrames: Int): Allocator(totalFrames) {
    override fun balanceAllocatedFrames() =
        reallocate(processes
            .associateWith { Random.nextInt(totalFrames) }
        )
}