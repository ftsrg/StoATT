package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class TTReliabilityTool : CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) =
        TTReliabilityTool()
                .subcommands(
                        Gen(),
                        Calc(),
                        GSPN().subcommands(
                            Kanban()
                        )
                ).main(args)

