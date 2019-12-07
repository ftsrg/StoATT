package parser

import faulttree.*
import org.ejml.simple.SimpleMatrix

class GalileoListenerImpl : GalileoBaseListener() {
    lateinit var faultTree: FaultTree private set

    sealed class GateType {
        object OR : GateType()
        object AND : GateType()
        data class K_OF_N(val k: Int, val n: Int) : GateType()
    }

    private data class PendingNode(val name: String, val type: GateType, val inputs: Collection<String>)

    private var faultTreeName: String? = null
    private val pendingFTNodes = arrayListOf<PendingNode>()
    private val createdFaultTreeNodes = hashMapOf<String, FaultTreeNode>()

    override fun enterGate(ctx: GalileoParser.GateContext) {
        val newNode = when {
            ctx.operation().or() != null -> PendingNode(ctx.name.text, GateType.OR, ctx.inputs.map { it.text })
            ctx.operation().and() != null -> PendingNode(ctx.name.text, GateType.AND, ctx.inputs.map { it.text })
            ctx.operation().of() != null -> {
                val k = ctx.operation().of().k.text.toInt()
                val n = ctx.operation().of().n.text.toInt()
                PendingNode(ctx.name.text, GateType.K_OF_N(k, n), ctx.inputs.map { it.text })
            }
            else -> throw Exception("Unknown type \"${ctx.operation().text}\" for node ${ctx.name.text} ")
        }
        if (!tryToProcess(newNode)) pendingFTNodes.add(newNode)
    }

    override fun enterBasicevent(ctx: GalileoParser.BasiceventContext) {
        val name = ctx.name.text
        val lambda = ctx.property().find { it.lambda() != null }?.lambda()?.`val`?.text?.toDouble()
        val mu = ctx.property().find { it.repair() != null }?.repair()?.`val`?.text?.toDouble() ?: 0.0
        val dorm = ctx.property().find { it.dormancy() != null }?.dormancy()?.`val`?.text?.toDouble() ?: 1.0
        val phase = ctx.property().find { it.phase() != null }?.phase()?.`val`
        val numFailureStates = (ctx.property().find { it.numFailureStates() != null }?.numFailureStates()?.`val`?.text
                                ?: "1").toInt()
        if (lambda != null)
            addFTNode(name, BasicEvent(name, lambda, dorm, repairRate = mu))
        else if (phase != null) {
            val rateMatrix = parseMatrix(phase)
            if (rateMatrix.numRows() < numFailureStates)
                throw RuntimeException("Error when parsing event $name: number of failure states cannot be larger than " +
                                       "the number of states given by the rate matrix")
            addFTNode(name, PHBasicEvent(name, rateMatrix, numFailureStates))
        } else throw RuntimeException("No failure distribution specified for event $name!")
    }

    private fun parseMatrix(matrixCtx: GalileoParser.RateMatrixContext): SimpleMatrix {
        val rowCtxs = matrixCtx.matrixRow()
        val ret = SimpleMatrix(rowCtxs.size, rowCtxs.size)
        for ((i, rowCtx) in rowCtxs.withIndex()) {
            if (rowCtx.NUMBER().size != ret.numCols())
                throw RuntimeException("Error when parsing rate matrix: number of columns in a row must be the " +
                                       "same as the number of rows")
            for ((j, node) in rowCtx.NUMBER().withIndex()) {
                ret[i, j] = node.text.toDouble()
            }
        }
        return ret
    }

    private fun addFTNode(name: String, node: FaultTreeNode) {
        createdFaultTreeNodes[name] = node
        pendingFTNodes.filter { it.inputs.contains(name) }.forEach { pendingNode ->
            //needed because calling addFTNode in prev iterations can change the pending nodes collection TODO: better solution
            if (pendingFTNodes.contains(pendingNode)) tryToProcess(pendingNode)
        }
    }

    private fun tryToProcess(pendingNode: PendingNode): Boolean {
        if (pendingNode.inputs.all(createdFaultTreeNodes::containsKey)) {
            pendingFTNodes.remove(pendingNode)
            addFTNode(pendingNode.name, instantiateNode(pendingNode))
            return true
        }
        return false
    }

    /**
     * Creates a FaultTreeNode instance described by a PendingNodeInstance. Must be called only when all inputs have
     * been created, and added to the createdFaulTreeNodes map!!!
     */
    private fun instantiateNode(pendingNode: PendingNode): FaultTreeNode = when (pendingNode.type) {
        is GateType.AND -> AndGate(*pendingNode.inputs.map { createdFaultTreeNodes[it]!! }.toTypedArray())
        is GateType.OR -> OrGate(*pendingNode.inputs.map { createdFaultTreeNodes[it]!! }.toTypedArray())
        is GateType.K_OF_N -> VotingGate(pendingNode.type.k, *pendingNode.inputs.map { createdFaultTreeNodes[it]!! }.toTypedArray())
    }

    override fun enterTop(ctx: GalileoParser.TopContext) {
        val name = ctx.name.text
        faultTreeName = name
    }

    override fun exitFaulttree(ctx: GalileoParser.FaulttreeContext) {
        val topnode = createdFaultTreeNodes[faultTreeName as String?]
                      ?: throw Exception("Top event not found")
        faultTree = FaultTree(topnode)
    }
}