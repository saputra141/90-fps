package com.ultra.emulator

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    private val engine = EmulatorEngine()
    private val bitmap = Bitmap.createBitmap(160, 144, Bitmap.Config.ARGB_8888)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine.initCore()
        
        // Mock ROM loading
        engine.loadROM(ByteArray(0x8000))

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                EmulatorScreen(engine, bitmap)
            }
        }
    }
}

@Composable
fun EmulatorScreen(engine: EmulatorEngine, bitmap: Bitmap) {
    var frameTick by remember { mutableLongStateOf(0L) }

    // Emulation Loop (60 FPS)
    LaunchedEffect(Unit) {
        while (isActive) {
            engine.renderFrame(bitmap)
            frameTick++
            delay(16) // ~60 FPS
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ULTRA EMULATOR v1.0", color = Color.Green, modifier = Modifier.padding(16.dp))

        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .aspectRatio(160f / 144f)
                .background(Color.DarkGray)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawContext.canvas.nativeCanvas.drawBitmap(
                    bitmap,
                    null,
                    android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt()),
                    null
                )
            }
        }

        // Virtual Controls
        Row(modifier = Modifier.padding(20.dp)) {
            Button(onClick = { /* D-Pad logic */ }) { Text("D-PAD") }
            Spacer(Modifier.width(20.dp))
            Button(onClick = { /* A/B logic */ }) { Text("A / B") }
        }
    }
}
