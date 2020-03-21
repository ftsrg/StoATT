package sacn.markingpredicates

import sacn.Place

abstract class MarkingPredicate {
    abstract fun getReferencedPlaces(): Set<Place>
}