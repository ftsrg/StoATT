package parser

import faulttree.*

class GalileoListenerImpl: GalileoBaseListener() {
    lateinit var faultTree: FaultTree private set

    private data class PendingNode(val name: String, val type: String, val inputs: List<String>)
    private var faultTreeName: String? = null
    private val pendingFTNodes = arrayListOf<PendingNode>()
    private val createdFaultTreeNodes = hashMapOf<String, FaultTreeNode>()

    override fun enterBasicevent(ctx: GalileoParser.BasiceventContext) {
        val name = ctx.name.text
        val lambda = ctx.property().find { it.lambda() != null }?.lambda()?.`val`?.text?.toDouble() ?: throw Exception("No lambda specified for basic event $name")
        addFTNode(name, BasicEvent(lambda, name))
    }

    //TODO: optimize recursion if possible
    private fun addFTNode(name: String, node: FaultTreeNode) {
        createdFaultTreeNodes[name] = node
        pendingFTNodes.filter { it.inputs.contains(name) }.forEach { pendingNode ->
            if(     pendingFTNodes.contains(pendingNode) && //needed because calling addFTNode in prev iterations can cause side effects TODO: better solution
                    pendingNode.inputs.all { pendingInput -> createdFaultTreeNodes.containsKey(pendingInput) }) {
                pendingFTNodes.remove(pendingNode)
                addFTNode(pendingNode.name, instantiateNode(pendingNode))
            }
        }
    }

    /**
     * Creates a FaultTreeNode instance described by a PendingNodeInstance. Must be called only when all inputs have
     * been created, and added to the createdFaulTreeNodes map!!!
     */
    private fun instantiateNode(pendingNode: PendingNode): FaultTreeNode = when(pendingNode.type) {
        "and" -> AndNode(*pendingNode.inputs.map {createdFaultTreeNodes[it]!!}.toTypedArray())
        "or" -> OrNode(*pendingNode.inputs.map { createdFaultTreeNodes[it]!! }.toTypedArray())
        else -> throw Exception("Unknown type \"${pendingNode.type}\" for node ${pendingNode.name} ") //TODO: subclass exception
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