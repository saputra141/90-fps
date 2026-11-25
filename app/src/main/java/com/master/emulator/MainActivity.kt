package com.master.emulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.master.emulator.engine.Chip8Core
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dummy ROM: Program yang menggambar pola sederhana
        val dummyRom = byteArrayOf(
            0x60.toByte(), 0x00.toByte(), // V0 = 0
            0x61.toByte(), 0x00.toByte(), // V1 = 0
            0xA2.toByte(), 0x00.toByte(), // I = 0 (Font '0')
            0xD0.toByte(), 0x15.toByte(), // Draw V0, V1, height 5
            0x70.toByte(), 0x08.toByte(), // V0 += 8
            0x12.toByte(), 0x06.toByte()  // Jump to 0x206
        )

        val engine = Chip8Core().apply { loadRom(dummyRom) }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EmulatorScreen(engine)
                }
            }
        }
    }
}

@Composable
fun EmulatorScreen(engine: Chip8Core) {
    var tick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            repeat(10) { engine.cycle() }
            engine.updateTimers()
            tick++
            delay(16) // ~60 FPS
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("CHIP-8 ULTIMATE EMULATOR", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().aspectRatio(2f).background(Color.Black)) {
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight
            val pixelWidth = canvasWidth / 64
            val pixelHeight = canvasHeight / 32

            Canvas(modifier = Modifier.fillMaxSize()) {
                for (y in 0 until 32) {
                    for (x in 0 until 64) {
                        if (engine.display[x + (y * 64)] == 1) {
                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(x * pixelWidth.toPx(), y * pixelHeight.toPx()),
                                size = Size(pixelWidth.toPx(), pixelHeight.toPx())
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Status: Running at 60Hz", color = Color.Gray)
        Text("PC: 0x${engine.pc.toString(16).uppercase()}", color = Color.White)
    }
}
