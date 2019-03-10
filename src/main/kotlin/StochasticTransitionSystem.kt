data class GuardedCommand(val guard: ()->Boolean, val rate: Double)

fun createTT(guardedCommands: List<GuardedCommand>): TensorTrain {

    TODO()
}
