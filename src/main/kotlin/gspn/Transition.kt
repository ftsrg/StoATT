package gspn

import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.TTSquareMatrix

abstract class Transition(val name: String, val inputs: ArrayList<Arc>, val outputs: ArrayList<Arc>, val inhibitors: ArrayList<Arc>) {
    abstract fun toTT(varOrder: MddVariableOrder, places: ArrayList<Place>): TTSquareMatrix
}