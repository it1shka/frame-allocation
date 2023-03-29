import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JFrame
import javax.swing.WindowConstants

interface Display {
    fun update(allocator: Allocator)
}

@Suppress("MoveVariableDeclarationIntoWhen")
fun getDisplay(): Display {
    val choice = chooseFrom(arrayOf("Console", "Window"), "Please, select a display: ")
    return when(choice) {
        "Console" -> ConsoleDisplay()
        "Window" -> WindowDisplay()
        else -> throw RuntimeException("This should never happen")
    }
}

class ConsoleDisplay: Display {
    override fun update(allocator: Allocator) {
        clearTerminal()
        println(allocator.memoryDump)
        println()
        println(allocator.processDump)
    }
}

class WindowDisplay: JFrame("Your memory: "), Display {
    private var matrix: List<List<Int?>> = listOf()
    private val colors = arrayOf(
        Color.BLUE,
        Color.CYAN, Color.MAGENTA,
        Color.YELLOW, Color.WHITE,
        Color.GREEN, Color.RED,
    )

    init {
//        val width = getInteger("Window width: ", 100)
//        val height = getInteger("Window height: ", 100)
        size = Dimension(300, 300)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        isVisible = true
    }

    override fun paint(g: Graphics?) {
        if (g == null) return
        g.color = Color.black
        g.fillRect(0, 0, width, height)
        if (matrix.isEmpty()) return

        val cellHeight = height / matrix.size
        val cellWidth = width / matrix.maxOf { it.size }
        matrix.forEachIndexed { i, row ->
            row.forEachIndexed { j, tag ->
                if (tag == null) return
                g.color = colors[tag % colors.size]
                g.fillRect(cellWidth * j, cellHeight * i, cellWidth, cellHeight)
            }
        }
    }

    override fun update(allocator: Allocator) {
        matrix = allocator.memoryMatrix
        clearTerminal()
        println(allocator.processDump)
        repaint()
    }
}