package com.example.var_websocket

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.var_websocket.ui.theme.VAR_WebSocketTheme
import io.ktor.websocket.Frame
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            VAR_WebSocketTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DataSender(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DataSender(modifier: Modifier = Modifier) {
    val context = LocalContext.current as ComponentActivity
    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    val scope = rememberCoroutineScope()
    var isConnected by remember { mutableStateOf(false) }
    var lastMessage by remember { mutableStateOf("No message yet") }

    LaunchedEffect(Unit) {
        scope.launch {
            maintainWebSocketConnection(
                scope = scope,
                onMessage = { msg -> lastMessage = msg },
                onConnected = { connected -> isConnected = connected }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isConnected) "✅ Connected to WebSocket" else "🔄 Connecting...",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            webSocketSession?.send(Frame.Text("shoot!"))
                        } catch (e: Exception) {
                            lastMessage = "Error sending: ${e.localizedMessage}"
                        }
                    }
                },
                enabled = isConnected
            ) {
                Text("🔥 Shoot!")
            }
        }
    }
}
