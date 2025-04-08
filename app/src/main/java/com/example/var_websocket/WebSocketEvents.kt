package com.example.var_websocket

sealed class WebSocketEvent {
    data class StatusMessage(val text: String) : WebSocketEvent()
    data class Unknown(val raw: String) : WebSocketEvent()
}
