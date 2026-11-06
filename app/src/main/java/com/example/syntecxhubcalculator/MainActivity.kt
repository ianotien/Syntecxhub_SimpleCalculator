package com.example.syntecxhubcalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var tvExpression: TextView

    private var expression = ""
    private var isNewNumber = true
    private var hasCalculatedResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvExpression = findViewById(R.id.tvExpression)

        // Number buttons
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2,
            R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8,
            R.id.btn9
        )

        for (id in numberButtons) {
            findViewById<Button>(id).setOnClickListener {
                val number = (it as Button).text.toString()
                enterNumber(number)
            }
        }

        // Decimal button
        findViewById<Button>(R.id.btnDecimal).setOnClickListener {
            enterDecimal()
        }

        // Mathematical operators
        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            selectOperator("+")
        }

        findViewById<Button>(R.id.btnSubtract).setOnClickListener {
            selectOperator("-")
        }

        findViewById<Button>(R.id.btnMultiply).setOnClickListener {
            selectOperator("*")
        }

        findViewById<Button>(R.id.btnDivide).setOnClickListener {
            selectOperator("/")
        }

        // Equals button
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            calculateResult()
        }

        // Clear button
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            clearCalculator()
        }

        // Backspace button
        findViewById<Button>(R.id.btnBackspace).setOnClickListener {
            backspace()
        }
    }

    // Enter numbers
    private fun enterNumber(number: String) {

        if (hasCalculatedResult) {
            expression = ""
            tvExpression.text = ""
            hasCalculatedResult = false
        }

        if (isNewNumber) {
            tvDisplay.text = number
            expression += number
            isNewNumber = false

        } else if (tvDisplay.text.toString() == "0"
            && !tvDisplay.text.contains(".")) {

            tvDisplay.text = number
            expression = expression.dropLast(1) + number

        } else {
            tvDisplay.append(number)
            expression += number
        }

        tvExpression.text = expression
    }

    // Enter decimal points
    private fun enterDecimal() {

        if (hasCalculatedResult) {
            expression = ""
            tvExpression.text = ""
            tvDisplay.text = "0"
            hasCalculatedResult = false
            isNewNumber = true
        }

        if (isNewNumber) {
            tvDisplay.text = "0."
            expression += "0."
            isNewNumber = false

        } else if (!tvDisplay.text.contains(".")) {
            tvDisplay.append(".")
            expression += "."
        }

        tvExpression.text = expression
    }

    // Select mathematical operators
    private fun selectOperator(operator: String) {

        val currentValue =
            tvDisplay.text.toString().toDoubleOrNull() ?: return

        if (hasCalculatedResult) {
            expression = formatResult(currentValue)
            hasCalculatedResult = false
        }

        if (expression.isEmpty()) {
            expression = formatResult(currentValue)
        }

        // Replace the previous operator if necessary
        if (expression.endsWith(" + ") ||
            expression.endsWith(" - ") ||
            expression.endsWith(" × ") ||
            expression.endsWith(" ÷ ")) {

            expression = expression.dropLast(3)
        }

        val symbol = when (operator) {
            "*" -> "×"
            "/" -> "÷"
            else -> operator
        }

        expression += " $symbol "

        tvExpression.text = expression

        isNewNumber = true
    }

    // Calculate the result
    private fun calculateResult() {

        val result = evaluateExpression(expression)

        if (result == null) {
            if (expression.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter a complete expression",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        tvExpression.text = "$expression ="
        tvDisplay.text = formatResult(result)

        isNewNumber = true
        hasCalculatedResult = true
    }

    // Evaluate expressions using BODMAS
    private fun evaluateExpression(input: String): Double? {

        val tokens = Regex("(?<=[+\\-×÷])|(?=[+\\-×÷])")
            .split(input.replace(" ", ""))
            .filter { it.isNotEmpty() }

        // Validate the expression
        if (tokens.size < 3 || tokens.size % 2 == 0) {
            return null
        }

        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<String>()

        val first = tokens[0].toDoubleOrNull() ?: return null

        numbers.add(first)

        var index = 1

        // Process multiplication and division first
        while (index < tokens.size) {

            val operator = tokens[index]

            val number = tokens.getOrNull(index + 1)
                ?.toDoubleOrNull() ?: return null

            when (operator) {

                "×" -> {
                    val last = numbers.lastIndex
                    numbers[last] = numbers[last] * number
                }

                "÷" -> {

                    if (number == 0.0) {
                        Toast.makeText(
                            this,
                            "Cannot divide by zero",
                            Toast.LENGTH_SHORT
                        ).show()

                        return null
                    }

                    val last = numbers.lastIndex
                    numbers[last] = numbers[last] / number
                }

                "+", "-" -> {
                    operators.add(operator)
                    numbers.add(number)
                }

                else -> return null
            }

            index += 2
        }

        // Process addition and subtraction
        var result = numbers[0]

        for (i in operators.indices) {

            result = when (operators[i]) {
                "+" -> result + numbers[i + 1]
                "-" -> result - numbers[i + 1]
                else -> return null
            }
        }

        return result
    }

    // Format the displayed result
    private fun formatResult(result: Double): String {

        return if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString()
        }
    }

    // Clear the calculator
    private fun clearCalculator() {

        tvDisplay.text = "0"
        tvExpression.text = ""

        expression = ""
        isNewNumber = true
        hasCalculatedResult = false
    }

    // Delete the last entered digit
    private fun backspace() {

        if (isNewNumber) return

        val currentValue = tvDisplay.text.toString()

        if (currentValue.length <= 1) {

            tvDisplay.text = "0"

            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
            }

            isNewNumber = true

        } else {

            tvDisplay.text = currentValue.dropLast(1)

            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
            }
        }

        tvExpression.text = expression
    }
}