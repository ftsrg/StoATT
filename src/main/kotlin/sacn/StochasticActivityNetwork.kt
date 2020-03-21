package sacn

class StochasticActivityNetwork(val places: ArrayList<Place>, val activities: ArrayList<Activity>) {
    init {
        //TODO: references in expressions
        if(activities.any {
            it.inputGates.any {
                it.inputPlaces.any { it !in places }
            } ||
            it.cases.any {
                it.outputGates.any {
                    it.outputPlaces.any { it !in places }
                }
            }
        }) throw RuntimeException("Error: not all places referenced by activities are contained in the place list!")
    }
}