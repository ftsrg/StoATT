package gspn

sealed class Arc(val place: Place, val weightFunction: (Int) -> Int) {
    constructor(place: Place, weight: Int) : this(place, {weight})
    //TODO: these classes are a temporary solution for compatibility with Turnout
    class ConstantArc(place: Place, val value: Int): Arc(place, {value})
    class ResetArc(place: Place, val resetTo: Int): Arc(place, {resetTo-it})
    class ArbitraryArc(place: Place, weightFunction: (Int) -> Int): Arc(place,  weightFunction)
}
