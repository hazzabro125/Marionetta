package org.valkyrienskies.tournament

class PIDController(private val kp: Double, private val ki: Double, private val kd: Double) {
    private var integral: Double = 0.0
    private var prevError: Double = 0.0

    fun calculateOutput(sp: Double, pv: Double): Double {
        val error = (sp - pv) * -1
        integral += error
        val derivative = error - prevError

        val output = kp * error + ki * integral + kd * derivative

        prevError = error

        return output
    }
}

fun main() {
    // Example usage
    val pidController = PIDController(kp = 0.1, ki = 0.01, kd = 0.05)

    val setpoint = 100.0
    var processVariable = 50.0

    for (i in 1..100) {
        val output = pidController.calculateOutput(setpoint, processVariable)

        // Update process variable using the control output
        processVariable += output

        println("Iteration $i - Setpoint: $setpoint, Process Variable: $processVariable, Control Output: $output")
    }
}