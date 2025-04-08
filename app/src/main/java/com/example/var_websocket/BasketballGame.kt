package com.example.var_websocket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BasketballGame {
    enum class Player { PLAYER_1, PLAYER_2 }

    private val _isGameActive = MutableStateFlow(false)
    val isGameActive: StateFlow<Boolean> = _isGameActive

    private val _currentPlayer = MutableStateFlow(Player.PLAYER_1)
    val currentPlayer: StateFlow<Player> = _currentPlayer

    private val _shotsTaken = MutableStateFlow(mapOf(Player.PLAYER_1 to 0, Player.PLAYER_2 to 0))
    val shotsTaken: StateFlow<Map<Player, Int>> = _shotsTaken

    private val _scores = MutableStateFlow(mapOf(Player.PLAYER_1 to 0, Player.PLAYER_2 to 0))
    val scores: StateFlow<Map<Player, Int>> = _scores

    private val _lastAction = MutableStateFlow("Press START to begin")
    val lastAction: StateFlow<String> = _lastAction

    private val _winnerText = MutableStateFlow("")
    val winnerText: StateFlow<String> = _winnerText

    fun startGame() {
        _isGameActive.value = true
        _currentPlayer.value = Player.PLAYER_1
        _scores.value = mapOf(Player.PLAYER_1 to 0, Player.PLAYER_2 to 0)
        _shotsTaken.value = mapOf(Player.PLAYER_1 to 0, Player.PLAYER_2 to 0)
        _lastAction.value = "Game started: PLAYER 1 begins"
        _winnerText.value = ""
    }

    fun registerShot(success: Boolean) {
        if (!_isGameActive.value) return

        val player = _currentPlayer.value
        val other = if (player == Player.PLAYER_1) Player.PLAYER_2 else Player.PLAYER_1

        val updatedShots = _shotsTaken.value.toMutableMap()
        val updatedScores = _scores.value.toMutableMap()

        val currentShots = updatedShots.getOrDefault(player, 0)
        if (currentShots >= 3) {
            _lastAction.value = "${player.name} has no shots left"
            return
        }

        updatedShots[player] = currentShots + 1
        _shotsTaken.value = updatedShots

        if (success) {
            updatedScores[player] = updatedScores.getOrDefault(player, 0) + 2
            _lastAction.value = "${player.name} scored! 🏀"
        } else {
            _lastAction.value = "${player.name} missed ❌"
        }
        _scores.value = updatedScores

        val bothPlayersDone = updatedShots[Player.PLAYER_1]!! >= 3 && updatedShots[Player.PLAYER_2]!! >= 3
        if (bothPlayersDone) {
            _isGameActive.value = false

            val p1Score = updatedScores[Player.PLAYER_1] ?: 0
            val p2Score = updatedScores[Player.PLAYER_2] ?: 0

            _winnerText.value = when {
                p1Score > p2Score -> "🏆 Player 1 wins!"
                p2Score > p1Score -> "🏆 Player 2 wins!"
                else -> "🤝 It's a draw!"
            }

            _lastAction.value += " → Game over"
        } else {
            _currentPlayer.value = other
            _lastAction.value += " → ${other.name}'s turn"
        }
    }

    fun manualMiss() {
        registerShot(success = false)
    }

    fun resetGame() {
        _isGameActive.value = false
        _scores.value = mapOf(Player.PLAYER_1 to 0, Player.PLAYER_2 to 0)
        _shotsTaken.value = mapOf(Player.PLAYER_1 to 0, Player.PLAYER_2 to 0)
        _lastAction.value = "Game reset"
    }
}
