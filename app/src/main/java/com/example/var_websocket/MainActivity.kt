// MainActivity.kt
package com.example.var_websocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.var_websocket.ui.theme.VAR_WebSocketTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var gyroscopeManager: GyroscopeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gyroscopeManager = GyroscopeManager(this)

        enableEdgeToEdge()
        setContent {
            VAR_WebSocketTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DataSender(
                        modifier = Modifier.padding(innerPadding),
                        gyroscopeManager = gyroscopeManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gyroscopeManager.stopListening()
        //closeWebSocket() // Ensure WebSocket is closed when activity is destroyed
    }
}

@Composable
fun DataSender(modifier: Modifier = Modifier, gyroscopeManager: GyroscopeManager) {
    val scope = rememberCoroutineScope()
    var isConnected by remember { mutableStateOf(false) }
    var speed by remember { mutableStateOf(0.0f) } // Initialize speed control variable

    Column(modifier = modifier) {
        // Display current orientation
        Text("Rotation X: ${gyroscopeManager.rotX.value}, Y: ${gyroscopeManager.rotY.value}, Z: ${gyroscopeManager.rotZ.value}")

        // Speed Control
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = 0.0f..1.0f, // Example speed range
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text("Speed: ${speed}")

        Spacer(modifier = Modifier.height(16.dp))

        // Reconnect Button
        Button(onClick = {
            scope.launch {
                reconnectWebSocket()
                isConnected = true
            }
        }) {
            Text("Reconnect")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Continuous data sending using LaunchedEffect
        LaunchedEffect(isConnected) {
            if (!isConnected) {
                connectWebSocket()
                isConnected = true
            }
            while (isConnected) {
                sendData(
                    gyroscopeManager.rotX.value,
                    gyroscopeManager.rotY.value,
                    gyroscopeManager.rotZ.value,
                    speed
                )
                delay(16) // Approx. 60 updates per second
            }
        }
    }
}


// Reconnect function to safely reconnect WebSocket
suspend fun reconnectWebSocket() {
    closeWebSocket() // Close existing connection if any
    connectWebSocket() // Reconnect to the WebSocket server
}
