/*
 *
 *   Copyright 2021 Budapest University of Technology and Economics
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package benchmark

import gspn.PetriNet
import gspn.Place
import gspn.arc

val largeTreeString = """
    toplevel "SystemC0_failed";
    "SystemC0_failed" and "McuC0_controlFailed" "McuC1_controlFailed" "McuC4_controlFailed" "McuC2_controlFailed" "McuC3_controlFailed";
    "McuC2_controlFailed" or "McuC2_mcuFailed" "McuC2_hasNoValidSensor" "McuC2_cannotDetermineValidSensor";
    "McuC3_controlFailed" or "McuC3_mcuFailed" "McuC3_hasNoValidSensor" "McuC3_cannotDetermineValidSensor";
    "McuC3_cannotDetermineValidSensor" and "McuC3_sensorDiscrepancy" "ConnectionC5_referenceOutFailed" "ConnectionC8_referenceOutFailed";
    "ConnectionC5_referenceOutFailed" or "ConnectionC5_connectionFailed" "McuC2_referenceOutFailed";
    "McuC2_referenceOutFailed" or "McuC2_mcuFailed" "McuC2_sensorDiscrepancy";
    "McuC3_hasNoValidSensor" and "SensorC3_sensorFailed" "SensorC6_sensorFailed";
    "ConnectionC8_referenceOutFailed" or "ConnectionC8_connectionFailed" "McuC1_referenceOutFailed";
    "McuC2_cannotDetermineValidSensor" and "McuC2_sensorDiscrepancy" "ConnectionC1_referenceOutFailed" "ConnectionC4_referenceOutFailed";
    "McuC2_sensorDiscrepancy" or "SensorC4_faultyMeasurement" "SensorC8_faultyMeasurement";
    "SensorC4_faultyMeasurement" and "SensorC4_sensorFailed" "SensorC4_selfCheckFailed";
    "ConnectionC4_referenceOutFailed" or "ConnectionC4_connectionFailed" "McuC3_referenceOutFailed";
    "ConnectionC1_referenceOutFailed" or "ConnectionC1_connectionFailed" "McuC0_referenceOutFailed";
    "McuC0_controlFailed" or "McuC0_mcuFailed" "McuC0_hasNoValidSensor" "McuC0_cannotDetermineValidSensor";
    "McuC0_hasNoValidSensor" and "SensorC1_sensorFailed" "SensorC5_sensorFailed";
    "SensorC8_faultyMeasurement" and "SensorC8_sensorFailed" "SensorC8_selfCheckFailed";
    "McuC1_controlFailed" or "McuC1_mcuFailed" "McuC1_hasNoValidSensor" "McuC1_cannotDetermineValidSensor";
    "McuC1_cannotDetermineValidSensor" and "McuC1_sensorDiscrepancy" "ConnectionC6_referenceOutFailed" "ConnectionC7_referenceOutFailed";
    "ConnectionC7_referenceOutFailed" or "ConnectionC7_connectionFailed" "McuC3_referenceOutFailed";
    "ConnectionC6_referenceOutFailed" or "ConnectionC6_connectionFailed" "McuC4_referenceOutFailed";
    "McuC1_hasNoValidSensor" and "SensorC0_sensorFailed" "SensorC7_sensorFailed";
    "McuC4_controlFailed" or "McuC4_mcuFailed" "McuC4_hasNoValidSensor" "McuC4_cannotDetermineValidSensor";
    "McuC4_hasNoValidSensor" and "SensorC2_sensorFailed" "SensorC9_sensorFailed";
    "McuC2_hasNoValidSensor" and "SensorC4_sensorFailed" "SensorC8_sensorFailed";
    "McuC4_cannotDetermineValidSensor" and "McuC4_sensorDiscrepancy" "ConnectionC2_referenceOutFailed" "ConnectionC9_referenceOutFailed";
    "ConnectionC9_referenceOutFailed" or "ConnectionC9_connectionFailed" "McuC0_referenceOutFailed";
    "McuC0_referenceOutFailed" or "McuC0_mcuFailed" "McuC0_sensorDiscrepancy";
    "McuC0_cannotDetermineValidSensor" and "McuC0_sensorDiscrepancy" "ConnectionC0_referenceOutFailed" "ConnectionC3_referenceOutFailed";
    "ConnectionC0_referenceOutFailed" or "ConnectionC0_connectionFailed" "McuC1_referenceOutFailed";
    "McuC1_referenceOutFailed" or "McuC1_mcuFailed" "McuC1_sensorDiscrepancy";
    "McuC1_sensorDiscrepancy" or "SensorC0_faultyMeasurement" "SensorC7_faultyMeasurement";
    "SensorC7_faultyMeasurement" and "SensorC7_sensorFailed" "SensorC7_selfCheckFailed";
    "SensorC0_faultyMeasurement" and "SensorC0_sensorFailed" "SensorC0_selfCheckFailed";
    "McuC0_sensorDiscrepancy" or "SensorC1_faultyMeasurement" "SensorC5_faultyMeasurement";
    "SensorC5_faultyMeasurement" and "SensorC5_sensorFailed" "SensorC5_selfCheckFailed";
    "SensorC1_faultyMeasurement" and "SensorC1_sensorFailed" "SensorC1_selfCheckFailed";
    "ConnectionC2_referenceOutFailed" or "ConnectionC2_connectionFailed" "McuC3_referenceOutFailed";
    "McuC3_referenceOutFailed" or "McuC3_mcuFailed" "McuC3_sensorDiscrepancy";
    "McuC3_sensorDiscrepancy" or "SensorC3_faultyMeasurement" "SensorC6_faultyMeasurement";
    "SensorC6_faultyMeasurement" and "SensorC6_sensorFailed" "SensorC6_selfCheckFailed";
    "SensorC3_faultyMeasurement" and "SensorC3_sensorFailed" "SensorC3_selfCheckFailed";
    "ConnectionC3_referenceOutFailed" or "ConnectionC3_connectionFailed" "McuC4_referenceOutFailed";
    "McuC4_referenceOutFailed" or "McuC4_mcuFailed" "McuC4_sensorDiscrepancy";
    "McuC4_sensorDiscrepancy" or "SensorC2_faultyMeasurement" "SensorC9_faultyMeasurement";
    "SensorC9_faultyMeasurement" and "SensorC9_sensorFailed" "SensorC9_selfCheckFailed";
    "SensorC2_faultyMeasurement" and "SensorC2_sensorFailed" "SensorC2_selfCheckFailed";
    "McuC2_mcuFailed" lambda=4.0 dorm=0.0;
    "ConnectionC5_connectionFailed" lambda=2.0 dorm=0.0;
    "ConnectionC8_connectionFailed" lambda=2.0 dorm=0.0;
    "ConnectionC4_connectionFailed" lambda=2.0 dorm=0.0;
    "ConnectionC1_connectionFailed" lambda=2.0 dorm=0.0;
    "SensorC4_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC8_selfCheckFailed" lambda=6.25 dorm=0.0;
    "ConnectionC7_connectionFailed" lambda=2.0 dorm=0.0;
    "ConnectionC6_connectionFailed" lambda=2.0 dorm=0.0;
    "SensorC8_sensorFailed" lambda=25.0 dorm=0.0;
    "SensorC4_sensorFailed" lambda=25.0 dorm=0.0;
    "ConnectionC9_connectionFailed" lambda=2.0 dorm=0.0;
    "SensorC7_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC7_sensorFailed" lambda=25.0 dorm=0.0;
    "SensorC0_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC0_sensorFailed" lambda=25.0 dorm=0.0;
    "McuC1_mcuFailed" lambda=4.0 dorm=0.0;
    "ConnectionC0_connectionFailed" lambda=2.0 dorm=0.0;
    "SensorC5_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC5_sensorFailed" lambda=25.0 dorm=0.0;
    "SensorC1_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC1_sensorFailed" lambda=25.0 dorm=0.0;
    "SensorC6_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC6_sensorFailed" lambda=25.0 dorm=0.0;
    "SensorC3_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC3_sensorFailed" lambda=25.0 dorm=0.0;
    "McuC3_mcuFailed" lambda=4.0 dorm=0.0;
    "ConnectionC2_connectionFailed" lambda=2.0 dorm=0.0;
    "McuC0_mcuFailed" lambda=4.0 dorm=0.0;
    "SensorC9_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC9_sensorFailed" lambda=25.0 dorm=0.0;
    "SensorC2_selfCheckFailed" lambda=6.25 dorm=0.0;
    "SensorC2_sensorFailed" lambda=25.0 dorm=0.0;
    "McuC4_mcuFailed" lambda=4.0 dorm=0.0;
    "ConnectionC3_connectionFailed" lambda=2.0 dorm=0.0;
    """.trimIndent()

val smallTreeString =
        """
                toplevel "MyTree";
                "MyTree" or "EAndD" "AAndFAndBOrC" "MultiAnd";

                "EventA" lambda=.2;
                "EventB" lambda=.1;
                "EventC" lambda=0.1;
                "EventD" lambda=.3;
                "EventE" lambda=.423;
                "EventF" lambda=0.5;
                "EventG" lambda=0.56;

                "EAndD" and "EventE" "EventD";
                /* (((a and f) and (b or c)) or (b and c and e and g)) */
                "AAndFAndBOrC" and "EventA" "EventF" "BOrC";
                "BOrC" or "EventB" "EventC";
                "MultiAnd" and "EventB" "EventC" "EventE" "EventG";
            """.trimIndent()

val gspnPrioExample = PetriNet {
    val p0 = p("p0", 4)
    val p1 = p("p1")
    val p2 = p("p2")
    val p3 = p("p3")
    val p4 = p("p4")

    timed("t0", 1.0) {
        input(arc(p0))
        out(arc(p1))
    }
    timed("t1", 0.5) {
        input(arc(p1))
        out(arc(p4))
    }
    timed("t2", 2.0) {
        input(arc(p4))
        out(arc(p0))
    }

    immediate("t3", 2) {
        input(arc(p1, 3))
        out(arc(p2), arc(p1, 2))
    }
    immediate("t4") {
        input(arc(p1, 2))
        out(arc(p3), arc(p1))
        inhibit(arc(p4))
    }
}

val gspnSmallerExample = PetriNet {
    val first = p("first", 2)
    val middle = p("middle")
    val last = p("last")

    timed("t0", 1.0) {
        input(arc(first))
        out(arc(middle))
    }
    immediate("t1", 1) {
        input(arc(middle))
        out(arc(last))
        inhibit(arc(last))
    }
    immediate("sink", 2) {
        input(arc(middle, 2))
    }
    timed("back", 0.5) {
        input(arc(last))
        out(arc(middle))
    }
}

fun generateKanban(N: Int, getNextRate: ()->Double) = PetriNet {
    val p= Array(4) { Place("p$it", initialMarking = N) }
    val pm= Array(4) { Place("pm$it")}
    val pback= Array(4) { Place("pback$it")}
    val pout = Array(4) { Place("pout$it")}

    val tin = timed("tin1", getNextRate()) {
        input(arc(p[0]))
        out(arc(pm[0]))
    }

    val tredo = Array(4) {
        timed("tredo$it", getNextRate()) {
            input(arc(pm[it]))
            out(arc(pback[it]))
        }
    }
    val tok = Array(4) {
        timed("tok$it", getNextRate()) {
            input(arc(pm[it]))
            out(arc(pout[it]))
        }
    }
    val tback = Array(4) {
        timed("tback$it", getNextRate()) {
            input(arc(pback[it]))
            out(arc(pm[it]))
        }
    }

    val tout = timed("tout4", getNextRate()) {
        input(arc(pout[3]))
        out(arc(p[3]))
    }

    val tsynch1_2 = immediate("tsynch1_2") {
        input(arc(pout[0]), arc(p[1]), arc(p[2]))
        out(arc(p[0]), arc(pm[1]), arc(pm[2]))
    }
    val tsynch23_4 = immediate("tsyncs23_4") {
        input(arc(pout[1]), arc(pout[2]), arc(p[3]))
        out(arc(p[2]), arc(p[3]), arc(pm[3]))
    }

    _p.clear()
    _p.addAll(
            (0 until 4).flatMap<Int, Place> { listOf(p[it], pout[it], pm[it], pback[it]) }.reversed()
    )
}

fun generateLongKanban(numLargeBlocks: Int, N: Int, getNextRate: () -> Double) = PetriNet {
    val p0 = Place("start_p", initialMarking = N)
    val pm0 = Place("start_pm")
    val pback0 = Place("start_pback")
    val pout0 = Place("start_pout")

    val tin = timed("tin", getNextRate()) {
        input(arc(p0))
        out(arc(pm0))
    }
    timed("start_tok", getNextRate()) {
        input(arc(pm0))
        out(arc(pout0))
    }
    timed("start_tredo", getNextRate()) {
        input(arc(pm0))
        out(arc(pback0))
    }
    timed("start_tback", getNextRate()) {
        input(arc(pback0))
        out(arc(pm0))
    }

    class LargeBlock(id: Int) {
        val p = Array(3) { Place("b${id}_p$it", initialMarking = N) }
        val pm = Array(3) { Place("b${id}_pm$it") }
        val pback = Array(3) { Place("b${id}_pback$it") }
        val pout = Array(3) { Place("b${id}_pout$it") }

        val tredo = Array(3) {
            timed("b${id}_tredo$it", getNextRate()) {
                input(arc(pm[it]))
                out(arc(pback[it]))
            }
        }
        val tok = Array(3) {
            timed("b${id}_tok$it", getNextRate()) {
                input(arc(pm[it]))
                out(arc(pout[it]))
            }
        }
        val tback = Array(3) {
            timed("b${id}_tback$it", getNextRate()) {
                input(arc(pback[it]))
                out(arc(pm[it]))
            }
        }

        val tsynch23_4 = immediate("tsyncs23_4") {
            input(arc(pout[0]), arc(pout[1]), arc(p[2]))
            out(arc(p[0]), arc(p[1]), arc(pm[2]))
        }
    }

    val largeBlocks = Array(numLargeBlocks) { LargeBlock(it) }

    val outs = listOf(pout0) + largeBlocks.map { it.pout[2] }
    val plasts = listOf(p0) + largeBlocks.map { it.p[2] }
    val pparallels = largeBlocks.map { Pair(it.p[0], it.p[1]) }
    val pmparallels = largeBlocks.map { Pair(it.pm[0], it.pm[1]) }

    repeat(outs.size-1) {
        val tsynch1_2 = immediate("b${it}_tsynch1_2") {
            input(arc(outs[it]), arc(pparallels[it].first), arc(pparallels[it].second))
            out(arc(plasts[it]), arc(pmparallels[it].first), arc(pmparallels[it].second))
        }
    }

    val tout = timed("tout4", getNextRate()) {
        input(arc(largeBlocks.last().pout[2]))
        out(arc(largeBlocks.last().p[2]))
    }


    // reorder places for better variable ordering
    _p.clear()
    _p.addAll(
            (listOf(p0, pout0, pm0, pback0) +
            largeBlocks.flatMap { block ->
                (0 until 3).flatMap<Int, Place> { listOf(block.p[it], block.pout[it], block.pm[it], block.pback[it]) }
            }).reversed()
    )
}