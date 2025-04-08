package com.example.var_websocket

import android.content.pm.ActivityInfo
import android.health.connect.datatypes.units.Length
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.var_websocket.ui.theme.VAR_WebSocketTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            VAR_WebSocketTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Screen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Screen(modifier: Modifier = Modifier) {
    val context = LocalContext.current as ComponentActivity
    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    val scope = rememberCoroutineScope()

    val isConnected by WebSocketManager.connectionState.collectAsState()
    val isGameActive by BasketballGame.isGameActive.collectAsState()
    val currentPlayer by BasketballGame.currentPlayer.collectAsState()
    val shots by BasketballGame.shotsTaken.collectAsState()
    val scores by BasketballGame.scores.collectAsState()
    val lastAction by BasketballGame.lastAction.collectAsState()
    val winnerText by BasketballGame.winnerText.collectAsState()

    var statusText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        WebSocketManager.connect(scope)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Connection status
        Text(
            text = if (isConnected) "🟢 Connected" else "🔴 Disconnected",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )

        // Game UI
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isGameActive)
                    "🎮 ${currentPlayer.name}'s turn — Shot ${shots[currentPlayer]?.plus(1) ?: 1}/3"
                else
                    "Press ▶ Start to begin",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (winnerText.isNotEmpty()) {
                Text(
                    text = winnerText,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "🏀 Final Score\nP1: ${scores[BasketballGame.Player.PLAYER_1]} pts / Shots: ${shots[BasketballGame.Player.PLAYER_1] ?: 0} \n" +
                            "P2: ${scores[BasketballGame.Player.PLAYER_2]} pts / Shots: ${shots[BasketballGame.Player.PLAYER_2] ?: 0}",
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "🏀 Score\nP1: ${scores[BasketballGame.Player.PLAYER_1]} pts / Shots: ${shots[BasketballGame.Player.PLAYER_1] ?: 0} \n" +
                            "P2: ${scores[BasketballGame.Player.PLAYER_2]} pts / Shots: ${shots[BasketballGame.Player.PLAYER_2] ?: 0}",
                    textAlign = TextAlign.Center
                )
                Text(text = lastAction, textAlign = TextAlign.Center)
                Text(text = statusText, textAlign = TextAlign.Center)
            }


            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (isGameActive) {
                            scope.launch {
                                try {
                                    WebSocketManager.send("stop")
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error sending: ${e.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            BasketballGame.resetGame()
                        } else {
                            scope.launch {
                                try {
                                    WebSocketManager.send("start")
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error sending: ${e.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            BasketballGame.startGame()
                        }
                    }
                ) {
                    Text(if (isGameActive) "🔁 Restart" else "▶ Start")
                }


                if (isGameActive) {
                    Button(onClick = { BasketballGame.manualMiss() }) {
                        Text("❌ Manual Miss")
                    }
                }
            }
        }
    }
}
