package faulttree

abstract class AbstractBasicEvent(val name: String, repairable: Boolean): FaultTreeNode(repairable) {

}