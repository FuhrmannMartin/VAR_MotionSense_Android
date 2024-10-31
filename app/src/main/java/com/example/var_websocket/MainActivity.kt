// MainActivity.kt
package com.example.var_websocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.var_websocket.ui.theme.VAR_WebSocketTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var accelerometerManager: AccelerometerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accelerometerManager = AccelerometerManager(this)

        enableEdgeToEdge()
        setContent {
            VAR_WebSocketTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DataSender(
                        modifier = Modifier.padding(innerPadding),
                        accelerometerManager = accelerometerManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometerManager.stopListening()
    }
}

@Composable
fun DataSender(modifier: Modifier = Modifier, accelerometerManager: AccelerometerManager) {
    val scope = rememberCoroutineScope()
    val x by accelerometerManager.posX
    val y by accelerometerManager.posY
    val z by accelerometerManager.posZ
    var color by remember { mutableStateOf(Color.Red) }

    // Connect WebSocket on initial load
    LaunchedEffect(Unit) {
        connectWebSocket()
    }

    // UI Elements
    Column(modifier = modifier) {
        Text("X: ${x.roundToInt()}, Y: ${y.roundToInt()}, Z: ${z.roundToInt()}")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Choose Color:")
        ColorPicker { color = it }

        Spacer(Modifier.height(16.dp))

        // Continuous data sending using LaunchedEffect
        LaunchedEffect(Unit) {
            while (true) {
                scope.launch {
                    sendData(x, y, z, color)
                }
                delay(500) // Adjust delay as needed for sending frequency
            }
        }

        Button(onClick = {
            scope.launch {
                sendData(x, y, z, color)
            }
        }) {
            Text("DRAW")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            accelerometerManager.resetPosition()
        }) {
            Text("Reset Origin")
        }
    }

    // Close WebSocket on Dispose
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                closeWebSocket()
            }
        }
    }
}

@Composable
fun ColorPicker(onColorChange: (Color) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan).forEach { color ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color)
                    .padding(8.dp)
                    .clickable { onColorChange(color) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SenderPreview() {
    VAR_WebSocketTheme {
        DataSender(accelerometerManager = AccelerometerManager(context = LocalContext.current))
    }
}
