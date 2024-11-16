// MainActivity.kt
package com.example.var_websocket

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.var_websocket.ui.theme.VAR_WebSocketTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale



class SharedViewModel : ViewModel() {
    // Der State, der von mehreren Composables geteilt wird
    val ipAddress = mutableStateOf("192.168.178.129");
    val port = mutableStateOf("3000");
    val isConnected = mutableStateOf(false)
}


class MainActivity : ComponentActivity() {
    private lateinit var gyroscopeManager: GyroscopeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gyroscopeManager = GyroscopeManager(this)

        enableEdgeToEdge()
        setContent {
            VAR_WebSocketTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
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
fun MainScreen(modifier: Modifier = Modifier, gyroscopeManager: GyroscopeManager) {

    // Erstelle eine NavController-Instanz
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()

    // NavHost: Definiert die verfügbaren Routen (Screens) und ihre zugehörigen Composables
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home Screen
        composable("home") { DataSender(navController, modifier, gyroscopeManager, sharedViewModel) }

        // Settings Screen
        composable("settings") { SettingsScreen(navController, sharedViewModel) }
    }
}

@Composable
fun SettingsScreen(navController: NavHostController, viewModel: SharedViewModel){
    val activity = LocalContext.current as ComponentActivity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 60.dp, end = 16.dp)
            // Abstand von 16 dp zum Bildschirmrand
    ) {
        TextField(
            value = viewModel.ipAddress.value,
            onValueChange = { viewModel.ipAddress.value = it },
            label = { Text("IP Address") }
        )

        TextField(
            value = viewModel.port.value,
            onValueChange = { viewModel.port.value = it },
            label = { Text("Port") }
        )

        // Reconnect Button
        Button(onClick = {
            scope.launch {

                reconnectWebSocket(viewModel.ipAddress.value,viewModel)
                navController.navigate("home")
            }
        }) {
            Text("Connect")
        }
    }
}



@Composable
fun DataSender(navController: NavHostController, modifier: Modifier = Modifier, gyroscopeManager: GyroscopeManager, viewModel: SharedViewModel) {

    val activity = LocalContext.current as ComponentActivity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE


    val screenHeight = (LocalConfiguration.current.screenHeightDp - 30) // Bildschirmbreite in dp
    val offsetY = (screenHeight / 2)


    var speed by remember { mutableStateOf(0.0f) } // Initialize speed control variable
    Box(
        modifier = Modifier.fillMaxSize()  // Box füllt den gesamten Bildschirm
    ) {

        Image(
            painter = painterResource(id = R.drawable.cockpit),  // Bild aus den Ressourcen
            contentDescription = null,  // Keine Beschreibung für rein dekorative Bilder
            modifier = Modifier.fillMaxSize(),  // Bild füllt die gesamte Größe aus
            contentScale = ContentScale.Crop  // Bild wird skaliert, um den Bildschirm zu füllen
        )


        Row(modifier = Modifier.fillMaxWidth()) {

            Column() {

                Row(modifier = Modifier.padding(30.dp).background(Color.White)) {
                    SettingsIcon(
                        onSettingsClick = {
                            navController.navigate("settings")
                        }
                    )
                    // Display current orientation
                    Column {
                        Row {
                            Text("is connected:")
                            StatusIndicator(viewModel.isConnected.value)
                        }
                        Text("Thrust: ${String.format("%.1f", speed * 100)} %")
                    }

                }


                // Continuous data sending using LaunchedEffect
                LaunchedEffect(viewModel.isConnected.value) {
                    if (!viewModel.isConnected.value) {
                        //connectWebSocket("192.168.178.129")
                        //viewModel.isConnected.value = true
                    }
                    while (viewModel.isConnected.value) {
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

            /*
        Image(
            bitmap = ImageBitmap.imageResource(id = R.drawable.cockpit),
            contentDescription = "Beschreibung des Bildes",
            modifier = Modifier.size(100.dp), // Größe des Bildes anpassen
            contentScale = ContentScale.Crop  // Zuschneide-Optionen
        )

*/


            Spacer(modifier = Modifier.weight(1f))

            // Speed Control
            Box(modifier = Modifier.rotate(-90f).offset(x = -(offsetY).dp, y = (offsetY - 55).dp)) {
                Slider(
                    value = speed,
                    onValueChange = { speed = it },
                    valueRange = 0.0f..1.0f, // Example speed range
                    modifier = Modifier.width((LocalConfiguration.current.screenHeightDp - 30).dp)
                )
            }
            //Spacer(modifier = Modifier.height(16.dp))
        }
    }
}



// Reconnect function to safely reconnect WebSocket
suspend fun reconnectWebSocket(ipAddress: String, viewModel: SharedViewModel) {
    closeWebSocket() // Close existing connection if any
    connectWebSocket(ipAddress,viewModel.port.value.toInt(), viewModel) // Reconnect to the WebSocket server
}

@Composable
fun SettingsIcon(onSettingsClick: () -> Unit) {
    IconButton(onClick = onSettingsClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings Icon",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp) // Größe des Icons
        )
    }
}

@Composable
fun StatusIndicator(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                if (isActive) Color.Green else Color.Red,
                shape = androidx.compose.foundation.shape.CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Weitere Inhalte können hier hinzugefügt werden
    }
}