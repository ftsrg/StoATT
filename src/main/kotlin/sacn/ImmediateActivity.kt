package sacn

class ImmediateActivity(inputGates: ArrayList<InputGate>, cases: ArrayList<ActivityCase>, val priority: Int) :
        Activity(inputGates, cases) {
}