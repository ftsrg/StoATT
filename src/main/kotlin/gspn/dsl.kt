package gspn

import gspn.rateexpressions.Constant
import gspn.rateexpressions.PlaceRef
import gspn.rateexpressions.RateExpression

object PetriNet {
    class PreGSPN {
        val _t = arrayListOf<Transition>()
        val _p = arrayListOf<Place>()
        fun build() = GSPN(_p, _t)
        fun p(name: String, initial: Int = 0, capacity: Int = Int.MAX_VALUE): Place {
            val place = Place(name, capacity, initial)
            _p.add(place)
            return place
        }
        fun timed(name: String, rate: RateExpression, construct: PreTransition.() -> Unit): ExponentialTransition {
            val pret = PreTransition().apply(construct)
            val t = ExponentialTransition(name, pret._i, pret._o, pret._inh, rate)
            _t.add(t)
            return t
        }
        fun immediate(name: String, priority: Int = 1, construct: PreTransition.() -> Unit): ImmediateTransition {
            val pret = PreTransition().apply(construct)
            val t = ImmediateTransition(name, pret._i, pret._o, pret._inh, priority)
            _t.add(t)
            return t
        }
        fun timed(name: String, rate: Double, construct: PreTransition.() -> Unit) = timed(name, Constant(rate), construct)
    }
    class PreTransition {
        val _i = arrayListOf<Arc>()
        val _o = arrayListOf<Arc>()
        val _inh = arrayListOf<Arc>()
        fun input(vararg arcs: Arc) = _i.addAll(arcs)
        fun out(vararg arcs: Arc) = _o.addAll(arcs)
        fun inhibit(vararg arcs: Arc) = _inh.addAll(arcs)
    }
    operator fun invoke(construct: PreGSPN.() -> Unit) = PreGSPN().apply(construct).build()



}
operator fun RateExpression.plus(d: Double) = this + Constant(d)
operator fun Double.plus(r: RateExpression) = r + Constant(this)
operator fun RateExpression.minus(d: Double) = this + Constant(-d)
operator fun RateExpression.times(d: Double) = this*Constant(d)
operator fun Double.times(r: RateExpression) = r*this
operator fun Double.times(place: Place) = this*PlaceRef(place)
operator fun Place.times(d: Double) = PlaceRef(this)*d
operator fun RateExpression.times(place: Place)  = this * PlaceRef(place)
fun arc(place: Place, weight: Int = 1) = Arc.ConstantArc(place, weight)
fun reset(place: Place, resetTo: Int) = Arc.ResetArc(place, resetTo)