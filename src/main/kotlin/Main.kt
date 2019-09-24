
import faulttree.BasicEvent
import faulttree.FaultTree
import faulttree.FaultTreeNode
import faulttree.galileoParser
import org.ejml.simple.SimpleMatrix
import solver.*
import java.util.*
import kotlin.math.abs

fun main(args: Array<String>) {

//    val n = 3
//    val random = Random(1)
//    val mats = Array(n) {SimpleMatrix.random_DDRM(2, 2, 0.0, 10.0, random)}
//    val T = kronSumAsTT(mats.toList())
//    val TInv = approxInvertKronsum(mats.toList(), 500, 0.0)
//    T.printElements()
//    (TInv*T).printElements(numDecimals = 5)
//    val eye = TTSquareMatrix.eye(T.modes)
//    println((TInv * T - eye).frobenius()/eye.frobenius())
//    return

//    faultTreeGrowthTest(); return

    val shortTest =
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

    val longTest ="""
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


    val tallTest =
            """
                toplevel "MyTree";
                
                "MyTree" and "Latins" "Greeks";
                "Latins" and
                "EventA" 
                "EventB" 
                "EventC" 
                "EventD" 
                "EventE" 
                "EventF" 
                "EventG" 
                "EventH" 
                "EventI" 
                "EventJ" 
                "EventK" 
                "EventL" 
                "EventM" 
                "EventN" 
                "EventO" 
                "EventP" 
                "EventQ"
                "EventR" 
                "EventS" 
                "EventT"
                "EventU" 
                "EventV" 
                "EventW" 
                "EventX" 
                "EventY" 
                "EventZ";
                "Greeks" and
                "EventAlpha" 
                "EventBeta" 
                "EventGamma" 
                "EventDelta" 
                "EventEpsilon" 
                "EventPhi" ;
                
                "EventA" lambda=.2;
                "EventB" lambda=.1;
                "EventC" lambda=0.1;
                "EventD" lambda=.3;
                "EventE" lambda=.423;
                "EventF" lambda=0.5;
                "EventG" lambda=0.56;
                "EventH" lambda=0.1;
                "EventI" lambda=.4;
                "EventJ" lambda=.2;
                "EventK" lambda=0.2;
                "EventL" lambda=.6;
                "EventM" lambda=.543;
                "EventN" lambda=0.6;
                "EventO" lambda=0.16;
                "EventP" lambda=0.17;
                "EventQ" lambda=.2;
                "EventR" lambda=.1;
                "EventS" lambda=0.1;
                "EventT" lambda=.3;
                "EventU" lambda=.423;
                "EventV" lambda=0.5;
                "EventW" lambda=0.56;
                "EventX" lambda=0.1;
                "EventY" lambda=.4;
                "EventZ" lambda=.2;
                "EventAlpha" lambda=0.2;
                "EventBeta" lambda=.6;
                "EventGamma" lambda=.543;
                "EventDelta" lambda=0.6;
                "EventEpsilon" lambda=0.16;
                "EventPhi" lambda=0.17;
            """.trimIndent()

    val testTreeDesc = longTest;

    val Ft = galileoParser.parse(testTreeDesc.byteInputStream())
    println(Ft.mttfThroughKronsumMethod(10, 50, 0.0, 1e-10))
    return

//    val kronsumComponents = Ft.getKronsumComponents()
//    FileWriter("kronsumComponents.txt").use { kronsumFile ->
//        for (component in kronsumComponents) {
//            component.reshape(1, component.numElements)
//            kronsumFile.write(component.toString())
//        }
//    }
//    FileWriter("modifier.tt").use { modifierFile ->
//        val modifierForMTTF = Ft.getModifierForMTTF(Ft.getBaseGenerator())
//        modifierForMTTF.tt.round(1e-20)
//        println(modifierForMTTF.ttRanks())
//        modifierFile.write(modifierForMTTF.tt.dataAsString())
//    }
//    return

    val baseGenerator = Ft.getBaseGenerator()
    val stateMaskVector = Ft.getStateMaskVector()
    stateMaskVector.tt.round(0.0001)
    val residualThreshold = 0.00001 * stateMaskVector.norm()
    val perturbedGeneratorMatrix = Ft.getModifiedGenerator()
    perturbedGeneratorMatrix.tt.round(0.0001)
//
//    val matFile = FileWriter("modifiedGenerator.tt")
//    matFile.write(perturbedGeneratorMatrix.tt.dataAsString())
//    matFile.close()
//    val vectFile = FileWriter("stateMaskVector.tt")
//    vectFile.write(stateMaskVector.tt.dataAsString())
//    vectFile.close()
//
//
//    println(perturbedGeneratorMatrix.tt.dataAsString())
//    return

//    println("ALS:")
//    val r = 4
//    val ones = TTVector.ones(stateMaskVector.modes)
//    var x0 = TTVector.ones(stateMaskVector.modes)
//    for (i in 0 until r) {
//        x0 = x0+x0.hadamard(ones)
//    }
//    x0 /= r.toDouble()
//    x0 = TTVector.rand(x0.modes, x0.tt.ranks().toTypedArray())
//    val alsRes = ALSSolve(perturbedGeneratorMatrix, stateMaskVector, x0=x0, residualThreshold = residualThreshold, maxSweeps = 10)
//    report(perturbedGeneratorMatrix, stateMaskVector, alsRes, residualThreshold)

//    println("DMRG:")
//    var x0DMRG = TTVector.ones(stateMaskVector.modes)
//    for (i in 0 until r) {
//        x0DMRG = x0DMRG+ x0DMRG.hadamard(ones)
//    }
//    x0 /= r.toDouble()
//    val dmrgRes = DMRGSolve(perturbedGeneratorMatrix, stateMaskVector, x0=x0DMRG, residualThreshold = residualThreshold, maxSweeps = 10)
//    report(perturbedGeneratorMatrix, stateMaskVector, dmrgRes, residualThreshold)

    println("GMRES without preconditioner:")
    val res = TTGMRES(perturbedGeneratorMatrix, stateMaskVector, TTVector.zeros(stateMaskVector.modes), 0.00001, maxIter = 10000, verbose = true)
    report(perturbedGeneratorMatrix, stateMaskVector, res, residualThreshold)

    println("GMRES with Jacobi preconditioner:")
    val prec = jacobiPreconditioner(perturbedGeneratorMatrix, stateMaskVector)
//    val precdMtx = prec * perturbedGeneratorMatrix
//    precdMtx.tt.round(0.0001)
    val precdVect = prec*stateMaskVector
    precdVect.tt.round(0.0001)
    val resPrecd = TTGMRES(prec, perturbedGeneratorMatrix, precdVect, TTVector.zeros(stateMaskVector.modes), 0.0001)
    report(perturbedGeneratorMatrix, stateMaskVector, resPrecd, residualThreshold)

    println("TT-Jacobi: ")
    val res2 = TTJacobi(perturbedGeneratorMatrix, stateMaskVector, 0.0001 * stateMaskVector.norm(), 0.00001, stateMaskVector)
    report(perturbedGeneratorMatrix, stateMaskVector, res2, residualThreshold)

    println()
    println("MTFF = ${-res2[0]}")
    println()
}

fun faultTreeGrowthTest() {
    val rand = Random(123)
    var topNode: FaultTreeNode = BasicEvent("ev0", rand.nextDouble()*10.0)
    for (i in 1..40) {
        println("Number of leaves: ${i+1}")
        topNode = topNode and BasicEvent("ev$i", rand.nextDouble()*10.0)
        if(i < 28) continue
        val ft = FaultTree(topNode)
        val A = ft.getModifiedGenerator()
        A.tt.round(1e-30)
        val b = ft.getStateMaskVector()
        val r = 3
        val ones = TTVector.ones(b.modes)
        var x0 = TTVector.ones(b.modes)
        for (j in 0 until r) {
            x0 = x0+x0.hadamard(ones)
        }
        x0 /= r.toDouble()
        val relativeThreshold = 0.0001
        val residualThreshold = relativeThreshold * b.norm()
        println(residualThreshold)
//        val y = TTReGMRES(A, b, x0, 0.0001, verbose = true)
        val y = ALSSolve(A, b, x0, residualThreshold, 15)
        println()
    }
}

fun report(A: TTSquareMatrix, b: TTVector, x: TTVector, threshold: Double) =
        report(A::times, b, x, threshold)

fun report(linearMap: (TTVector)->TTVector, b: TTVector, x: TTVector, threshold: Double) {
    println("results:")
    val resNorm = (b-linearMap(x)).norm()
    println("residual norm: $resNorm ${if(resNorm<threshold) "<" else ">"} $threshold (threshold)")
    println("relative residual norm: ${resNorm/b.norm()}")
    print("solution vector: ")
    if(x.numElements < 100) {
        x.printElements(); println()
    }
    else
        println("First element: ${x[0]}")
    println("TT ranks of the result: ${x.tt.cores.map { it.rows }}")
    print("Non-nullness in absorbing states: ${x.tt.hadamard((TTVector.ones(x.modes) - b).tt).frobenius()}")
    println()
    println()
}

val rand = Random()
fun randSquareMtx(size: Int): SimpleMatrix {
    return SimpleMatrix.random_DDRM(size, size, 0.0, 10.0, rand)
}

fun SimpleMatrix.roundZeros(threshold: Double = 1E-14) {
    for (i in 0 until numRows()) {
        for (j in 0 until numCols()) {
            if (abs(this[i, j]) < threshold) this[i, j] = 0.0
        }
    }
}