package sacn.markingpredicates

import sacn.Place

class OrPredicate(vararg val inputs: MarkingPredicate): MarkingPredicate() {
    override fun getReferencedPlaces(): Set<Place> =
            inputs.fold(setOf()) {acc, next -> acc.union(next.getReferencedPlaces())}

}