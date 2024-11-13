// WebSocket.kt
package com.example.var_websocket

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*

val client = HttpClient(CIO) {
    install(WebSockets)
}

var webSocketSession: WebSocketSession? = null

suspend fun connectWebSocket(ipAddress: String, viewModel: SharedViewModel) {
    try {
        webSocketSession = client.webSocketSession(host = ipAddress, port = 3000, path = "/ws")
        println("WebSocket connected")
        viewModel.isConnected.value = true
    } catch (e: Exception) {
        println("Error connecting WebSocket: ${e.localizedMessage}")
        viewModel.isConnected.value = false
    }
}

suspend fun sendData(rotX: Float, rotY: Float, rotZ: Float, speed: Float) {
    val jsonData = """
        {
            "rotX": $rotX,
            "rotY": $rotY,
            "rotZ": $rotZ,
            "speed": $speed
        }
    """
    try {
        webSocketSession?.send(Frame.Text(jsonData)) ?: println("WebSocket not connected")
    } catch (e: Exception) {
        println("Error sending data: ${e.localizedMessage}")
    }
}


suspend fun closeWebSocket() {
    try {
        webSocketSession?.close()
        println("WebSocket closed")
    } catch (e: Exception) {
        println("Error closing WebSocket: ${e.localizedMessage}")
    }
}
