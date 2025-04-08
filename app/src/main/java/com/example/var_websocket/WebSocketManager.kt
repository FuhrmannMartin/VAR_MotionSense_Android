package com.example.var_websocket

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val WEBSOCKET_IP = "10.138.25.78"
const val WEBSOCKET_PORT = 8765
const val WEBSOCKET_URL = "ws://$WEBSOCKET_IP:$WEBSOCKET_PORT/"

object WebSocketManager {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private val _eventFlow = MutableStateFlow<WebSocketEvent>(WebSocketEvent.StatusMessage("No data yet"))
    val eventFlow: StateFlow<WebSocketEvent> = _eventFlow

    private var session: WebSocketSession? = null

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    suspend fun connect(scope: CoroutineScope) {
        while (true) {
            try {
                session?.close()
            } catch (_: Exception) {}

            try {
                session = client.webSocketSession(WEBSOCKET_URL)
                _connectionState.value = true
                println("✅ Connected to WebSocket")

                // Handle incoming messages inside the same loop
                for (frame in session!!.incoming) {
                    if (frame is Frame.Text) {
                        val raw = frame.readText()
                        val event = parseMessage(raw)
                        _eventFlow.value = event
                    }
                }

                println("⚠️ Connection dropped (incoming closed)")

            } catch (e: Exception) {
                println("❌ WebSocket connection error: ${e.localizedMessage}")
            }

            _connectionState.value = false
            println("🔄 Reconnecting in 3 seconds...")
            delay(3000)
        }
    }

    suspend fun send(text: String) {
        try {
            session?.send(Frame.Text(text))
        } catch (e: Exception) {
            println("Send failed: ${e.localizedMessage}")
        }
    }

    private fun parseMessage(raw: String): WebSocketEvent {
        return when (raw.lowercase()) {
            "hit", "scored" -> {
                BasketballGame.registerShot(success = true)
                WebSocketEvent.StatusMessage("Scored!")
            }
            "miss" -> {
                BasketballGame.registerShot(success = false)
                WebSocketEvent.StatusMessage("Missed!")
            }
            else -> WebSocketEvent.Unknown(raw)
        }
    }

}
