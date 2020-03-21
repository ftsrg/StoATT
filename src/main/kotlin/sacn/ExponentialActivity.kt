package sacn

import gspn.rateexpressions.RateExpression

class ExponentialActivity(
        inputGates: ArrayList<InputGate>,
        cases: ArrayList<ActivityCase>,
        rate: RateExpression
) : Activity(inputGates, cases) {
}