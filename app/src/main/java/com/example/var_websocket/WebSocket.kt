// WebSocket.kt
package com.example.var_websocket

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val client = HttpClient(CIO) {
    install(WebSockets)
}

var webSocketSession: WebSocketSession? = null

suspend fun maintainWebSocketConnection(
    scope: CoroutineScope,
    onMessage: (String) -> Unit,
    onConnected: (Boolean) -> Unit
) {
    while (true) {
        try {
            webSocketSession?.close()
        } catch (_: Exception) {}

        try {
            println("🔌 Trying to connect to WebSocket...")
            webSocketSession = client.webSocketSession(websocketUrl)
            onConnected(true)
            println("✅ Connected!")

            // Listen for messages
            for (frame in webSocketSession!!.incoming) {
                if (frame is Frame.Text) {
                    onMessage(frame.readText())
                }
            }

            // if `incoming` breaks, we are disconnected
            println("⚠️ Incoming channel closed")
        } catch (e: Exception) {
            println("❌ Connection error: ${e.localizedMessage}")
        }

        onConnected(false)
        println("🔄 Reconnecting in 3s...")
        delay(3000)
    }
}