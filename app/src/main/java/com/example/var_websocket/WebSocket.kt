package com.example.var_websocket

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

val client = HttpClient(CIO) {
    install(WebSockets)
}

var webSocketSession: WebSocketSession? = null

suspend fun connectWebSocket() {
    try {
        webSocketSession = client.webSocketSession(host = "10.0.2.2", port = 3000, path = "/ws")
        println("WebSocket connected")
    } catch (e: Exception) {
        println("Error connecting WebSocket: ${e.localizedMessage}")
    }
}

suspend fun sendData(x: Float, y: Float, z: Float, color: Color) {
    // Extract RGB values and format them to a hex string
    val r = (color.red * 255).roundToInt().toString(16).padStart(2, '0')
    val g = (color.green * 255).roundToInt().toString(16).padStart(2, '0')
    val b = (color.blue * 255).roundToInt().toString(16).padStart(2, '0')
    val colorHex = "#$r$g$b"

    val jsonData = "{\"x\":$x,\"y\":$y,\"z\":$z,\"color\":\"$colorHex\"}"
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
