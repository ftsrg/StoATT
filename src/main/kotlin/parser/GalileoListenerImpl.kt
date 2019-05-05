package parser

import faulttree.*

class GalileoListenerImpl: GalileoBaseListener() {
    lateinit var faultTree: FaultTree private set

    private enum class GateType { OR, AND }
    private data class PendingNode(val name: String, val type: GateType, val inputs: Collection<String>)
    private var faultTreeName: String? = null
    private val pendingFTNodes = arrayListOf<PendingNode>()
    private val createdFaultTreeNodes = hashMapOf<String, FaultTreeNode>()

    override fun enterGate(ctx: GalileoParser.GateContext) {
        val newNode = when {
            ctx.operation().or() != null -> PendingNode(ctx.name.text, GateType.OR, ctx.inputs.map { it.text })
            ctx.operation().and() != null -> PendingNode(ctx.name.text, GateType.AND, ctx.inputs.map { it.text })
            else -> throw Exception("Unknown type \"${ctx.operation().text}\" for node ${ctx.name.text} ")
        }
        if(!tryToProcess(newNode)) pendingFTNodes.add(newNode)
    }

    override fun enterBasicevent(ctx: GalileoParser.BasiceventContext) {
        val name = ctx.name.text
        val lambda = ctx.property().find { it.lambda() != null }?.lambda()?.`val`?.text?.toDouble() ?: throw Exception("No lambda specified for basic event $name")
        addFTNode(name, BasicEvent(lambda, name))
    }

    //TODO: optimize recursion if possible
    private fun addFTNode(name: String, node: FaultTreeNode) {
        createdFaultTreeNodes[name] = node
        pendingFTNodes.filter { it.inputs.contains(name) }.forEach { pendingNode ->
            //needed because calling addFTNode in prev iterations can cause side effects TODO: better solution
            if(pendingFTNodes.contains(pendingNode)) tryToProcess(pendingNode)
        }
    }

    private fun tryToProcess(pendingNode: PendingNode): Boolean {
        if(pendingNode.inputs.all(createdFaultTreeNodes::containsKey)) {
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
    private fun instantiateNode(pendingNode: PendingNode): FaultTreeNode = when(pendingNode.type) {
        GateType.AND -> AndNode(*pendingNode.inputs.map {createdFaultTreeNodes[it]!!}.toTypedArray())
        GateType.OR -> OrNode(*pendingNode.inputs.map { createdFaultTreeNodes[it]!! }.toTypedArray())
    }

    override fun enterTop(ctx: GalileoParser.TopContext) {
        val name = ctx.name.text
        faultTreeName = name
    }

    override fun exitFaulttree(ctx: GalileoParser.FaulttreeContext) {
        val topnode = createdFaultTreeNodes[faultTreeName as String?] ?: throw Exception("Top event not found") //TODO: subclass exception
        faultTree = FaultTree(topnode)
    }
}