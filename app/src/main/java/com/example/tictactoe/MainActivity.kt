package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var buttons: Array<Button>
    private lateinit var tvStatus: TextView
    private lateinit var tvScore: TextView
    private lateinit var etPlayerX: EditText
    private lateinit var etPlayerO: EditText
    private lateinit var spinnerBackground: Spinner
    private lateinit var spinnerMode: Spinner

    private var board = Array(9) { "" }
    private var currentPlayer = "X"
    private var gameOver = false

    private var scoreX = 0
    private var scoreO = 0
    private var ties = 0
    private var vsCPU = false

    // Para resaltar línea ganadora
    private var winLine: IntArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvScore = findViewById(R.id.tvScore)
        etPlayerX = findViewById(R.id.etPlayerX)
        etPlayerO = findViewById(R.id.etPlayerO)
        spinnerBackground = findViewById(R.id.spinnerBackground)
        spinnerMode = findViewById(R.id.spinnerMode)
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        buttons = Array(9) { i -> gridLayout.getChildAt(i) as Button }

        // Spinner de fondos
        val colors = arrayOf("Blanco", "Celeste", "Crema", "Gris Claro")
        val colorMap = mapOf(
            "Blanco" to Color.WHITE,
            "Celeste" to Color.parseColor("#B3E5FC"),
            "Crema" to Color.parseColor("#FFF9C4"),
            "Gris Claro" to Color.LTGRAY
        )
        spinnerBackground.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colors)
        spinnerBackground.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                gridLayout.setBackgroundColor(colorMap[colors[position]] ?: Color.WHITE)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Spinner modo de juego
        val modes = arrayOf("Jugador vs Jugador", "Jugador vs CPU")
        spinnerMode.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modes)
        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                vsCPU = position == 1
                resetGame()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!gameOver && board[index].isEmpty()) {
                    makeMove(index)
                    if (vsCPU && !gameOver && currentPlayer == "O") {
                        cpuMove()
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            resetGame()
        }
    }

    private fun makeMove(index: Int) {
        board[index] = currentPlayer
        buttons[index].text = currentPlayer
        buttons[index].setTextColor(if (currentPlayer == "X") Color.parseColor("#5D4037") else Color.parseColor("#1976D2"))

        val winnerLine = checkWinner(board, currentPlayer)
        if (winnerLine != null) {
            // Resaltar línea ganadora
            winLine = winnerLine
            winnerLine.forEach { i ->
                buttons[i].setBackgroundColor(if (currentPlayer == "X") Color.parseColor("#FFCDD2") else Color.parseColor("#BBDEFB"))
            }

            val winnerName =
                if (currentPlayer == "X") etPlayerX.text.toString().ifEmpty { "X" } else etPlayerO.text.toString().ifEmpty { "O" }
            tvStatus.text = "¡Ganó $winnerName!"
            tvStatus.setTextColor(Color.RED)
            tvStatus.textSize = 28f
            if (currentPlayer == "X") scoreX++ else scoreO++
            updateScore()
            gameOver = true
        } else if (board.all { it.isNotEmpty() }) {
            tvStatus.text = "¡Empate!"
            tvStatus.setTextColor(Color.GRAY)
            tvStatus.textSize = 24f
            ties++
            updateScore()
            gameOver = true
        } else {
            currentPlayer = if (currentPlayer == "X") "O" else "X"
            val playerName =
                if (currentPlayer == "X") etPlayerX.text.toString().ifEmpty { "X" } else etPlayerO.text.toString().ifEmpty { "O" }
            tvStatus.text = "Turno de $playerName"
            tvStatus.setTextColor(Color.BLACK)
            tvStatus.textSize = 20f
        }
    }

    private fun cpuMove() {
        val move = findBestMove()
        if (move != -1) makeMove(move)
    }

    // IA básica pero más inteligente: ganar o bloquear
    private fun findBestMove(): Int {
        // Ganar si puede
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                val tempBoard = board.copyOf()
                tempBoard[i] = "O"
                if (checkWinner(tempBoard, "O") != null) return i
            }
        }
        // Bloquear al jugador
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                val tempBoard = board.copyOf()
                tempBoard[i] = "X"
                if (checkWinner(tempBoard, "X") != null) return i
            }
        }
        // Si no hay movimiento crítico, elegir al azar
        val empty = board.mapIndexed { i, s -> if (s.isEmpty()) i else null }.filterNotNull()
        return if (empty.isNotEmpty()) empty.random() else -1
    }

    // Ahora checkWinner devuelve la línea ganadora si hay
    private fun checkWinner(b: Array<String>, player: String): IntArray? {
        val winPositions = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6)
        )
        for (pos in winPositions) {
            val (i1, i2, i3) = pos
            if (b[i1] == player && b[i2] == player && b[i3] == player) return intArrayOf(i1, i2, i3)
        }
        return null
    }

    private fun resetGame() {
        board = Array(9) { "" }
        currentPlayer = "X"
        gameOver = false
        tvStatus.text = "Turno de ${etPlayerX.text.toString().ifEmpty { "X" }}"
        tvStatus.setTextColor(Color.BLACK)
        tvStatus.textSize = 20f
        winLine = null
        buttons.forEach {
            it.text = ""
            it.setBackgroundResource(R.drawable.cell_border)
        }
    }

    private fun updateScore() {
        tvScore.text = "${scoreX} - ${scoreO}  (Empates: $ties)"
    }
}
