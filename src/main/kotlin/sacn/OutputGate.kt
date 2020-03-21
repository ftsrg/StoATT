package sacn

import sacn.markingfunctions.MarkingFunction

class OutputGate(val outputPlaces: ArrayList<Place>, val outputFunction: Map<Place, MarkingFunction>) {
}