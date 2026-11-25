package com.s22winemu.app.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.s22winemu.app.core.NativeBridge
import com.s22winemu.app.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt

class DisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val containerName = intent.getStringExtra("container_name") ?: "S22U Windows"
        val resW = intent.getIntExtra("res_w", 1600)
        val resH = intent.getIntExtra("res_h", 720)
        val exePath = intent.getStringExtra("exe_path") ?: "C:\\Windows\\explorer.exe"
        val turnipName = intent.getStringExtra("turnip_name") ?: "Mesa Turnip A730"
        val dxvkName = intent.getStringExtra("dxvk_name") ?: "DXVK 2.4.1"
        val profileTitle = intent.getStringExtra("profile_title") ?: "Balanced"
        val runtimeBackend = intent.getStringExtra("runtime_backend") ?: "WINE_BOX64"
        val cpuMask = intent.getIntExtra("cpu_mask", 0xF0)
        val envPairs = intent.getStringArrayListExtra("env_pairs") ?: arrayListOf()

        // Terapkan CPU affinity untuk thread display & emulator
        NativeBridge.nativeSetCpuAffinity(cpuMask)

        // Box64 tanpa argumen Wine/PRoot dan rootfs bukan runtime Windows yang siap dipakai.
        val runtimeRoot = File(filesDir, "rootfs")
        val box64Bin = File(runtimeRoot, "usr/local/bin/box64")
        val wineBin = File(runtimeRoot, "usr/bin/wine")
        val runtimeReady = box64Bin.canExecute() && wineBin.canExecute()
        val runtimePid = -1

        setContent {
            S22WinEmuTheme {
                WindowsDesktopDisplayScreen(
                    containerName = containerName,
                    resW = resW,
                    resH = resH,
                    exePath = exePath,
                    turnipName = turnipName,
                    dxvkName = dxvkName,
                    profileTitle = profileTitle,
                    runtimeBackend = runtimeBackend,
                    envPairs = envPairs,
                    runtimePid = runtimePid,
                    onExit = { finish() }
                )
            }
        }
    }
}

@Composable
private fun WindowsDesktopDisplayScreen(
    containerName: String,
    resW: Int,
    resH: Int,
    exePath: String,
    turnipName: String,
    dxvkName: String,
    profileTitle: String,
    runtimeBackend: String,
    envPairs: List<String>,
    runtimePid: Int,
    onExit: () -> Unit
) {
    var cursorPos by remember { mutableStateOf(Offset(500f, 300f)) }
    var showGamepad by remember { mutableStateOf(true) }
    var showConsole by remember { mutableStateOf(false) }
    var showStartMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var tempC by remember { mutableFloatStateOf(40.0f) }
    
    var lastPressedButton by remember { mutableStateOf("Siap") }

    val bootLog = remember(envPairs, runtimePid) {
        buildList {
            add("[S22-WinEmu] Booting $containerName ($resW x $resH)...")
            add("[HW] SoC: Snapdragon 8 Gen 1 (SM8450) | GPU: Adreno 730")
            add("[DRV] Vulkan ICD: $turnipName")
            add("[DRV] DirectX Layer: $dxvkName")
            add("[CPU] Affinity Profile: $profileTitle")
            add("[RUNTIME] Backend selected: $runtimeBackend")
            if (runtimeBackend == "PROTON") add("[WARN] Proton backend is experimental and requires a compatible Android Proton/Wine package.")
            add("[WINE] Target Executable: $exePath")
            if (runtimeReady) {
                add("[CHECK] Box64 dan Wine ditemukan; launcher PRoot/container belum dikonfigurasi.")
            } else {
                add("[ERROR] Runtime Windows belum terpasang lengkap.")
                add("[CHECK] Box64: ${if (box64Bin.canExecute()) "OK" else "TIDAK ADA"}")
                add("[CHECK] Wine: ${if (wineBin.canExecute()) "OK" else "TIDAK ADA"}")
                add("[INFO] Layar ini adalah shell UI, bukan desktop Windows yang sedang berjalan.")
            }
            add("[INFO] File EXE tidak akan dijalankan sampai runtime Wine + Box64 + rootfs + PRoot tersedia.")
            add("--- Environment Variables ---")
            addAll(envPairs.take(12))
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            tempC = runCatching { NativeBridge.nativeReadTemperature() }.getOrDefault(41.2f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C2340)) // Classic Windows Desktop Blue-Teal
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    cursorPos = Offset(
                        (cursorPos.x + dragAmount.x).coerceIn(0f, size.width.toFloat()),
                        (cursorPos.y + dragAmount.y).coerceIn(0f, size.height.toFloat())
                    )
                }
            }
    ) {
        // HUD Performa S22 Ultra di kiri atas
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ADRENO 730", color = S22Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("FPS —", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("${String.format("%.1f", tempC)}°C", color = if (tempC > 55f) S22Red else S22Amber, fontSize = 11.sp)
            Text("${resW}x${resH}", color = Color.White, fontSize = 11.sp)
            Text("Input: $lastPressedButton", color = Color.LightGray, fontSize = 11.sp)
        }

        // Toolbar Kontrol di kanan atas
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallActionChip(
                label = if (showGamepad) "Gamepad ON" else "Gamepad OFF",
                onClick = { showGamepad = !showGamepad }
            )
            SmallActionChip(
                label = if (showConsole) "Tutup Log" else "Buka Log",
                onClick = { showConsole = !showConsole }
            )
            SmallActionChip(label = "Keluar", isDanger = true, onClick = onExit)
        }

        // Jendela Desktop / Log Container
        if (showConsole) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.62f)
                    .fillMaxHeight(0.58f),
                colors = CardDefaults.cardColors(containerColor = Color(0xEE0F141E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2A3D))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Windows Container Console - $exePath", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showConsole = false }
                        )
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(bootLog) { line ->
                            Text(
                                line,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (line.startsWith("[DRV]") || line.startsWith("[HW]")) S22Cyan else Color(0xFFD5E1F2)
                            )
                        }
                    }
                }
            }
        }

        // Taskbar Windows di bagian bawah
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(38.dp)
                .background(Color(0xE60B111C))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showStartMenu = !showStartMenu }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Window, contentDescription = "Start", tint = S22Cyan, modifier = Modifier.size(20.dp))
                    Text("Start", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("|", color = Color.Gray)
                Text(turnipName, color = S22Emerald, fontSize = 11.sp)
            }
            Text("S22 Ultra • Snapdragon 8 Gen 1", color = Color.LightGray, fontSize = 11.sp)
        }

        if (showStartMenu) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 46.dp)
                    .widthIn(min = 220.dp, max = 300.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF21A2638)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("90-FPS • Start", color = S22Cyan, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Text("Container: $containerName", color = Color.White, fontSize = 11.sp)
                    Text(
                        if (runtimeReady) "Runtime ditemukan, tetapi launcher belum siap."
                        else "Runtime Wine/Box64 belum terpasang.",
                        color = S22Amber,
                        fontSize = 11.sp
                    )
                    TextButton(onClick = { showConsole = true; showStartMenu = false }) {
                        Icon(Icons.Default.Terminal, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Buka log runtime")
                    }
                    TextButton(onClick = {
                        Toast.makeText(context, "File Explorer Windows belum tersedia karena runtime belum terpasang.", Toast.LENGTH_LONG).show()
                        showStartMenu = false
                    }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("File Explorer (belum tersedia)")
                    }
                }
            }
        }

        // Virtual XInput Gamepad Overlay
        if (showGamepad) {
            // D-Pad Kiri
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 54.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PadButton("▲") { lastPressedButton = "D-PAD UP" }
                Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    PadButton("◀") { lastPressedButton = "D-PAD LEFT" }
                    PadButton("▶") { lastPressedButton = "D-PAD RIGHT" }
                }
                PadButton("▼") { lastPressedButton = "D-PAD DOWN" }
            }

            // Tombol A B X Y Kanan
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 54.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PadButton("Y", S22Amber) { lastPressedButton = "XINPUT Y" }
                Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    PadButton("X", S22Cyan) { lastPressedButton = "XINPUT X" }
                    PadButton("B", S22Red) { lastPressedButton = "XINPUT B" }
                }
                PadButton("A", S22Emerald) { lastPressedButton = "XINPUT A" }
            }
        }

        // Kursor Mouse Virtual
        Icon(
            Icons.Default.NearMe,
            contentDescription = "Cursor",
            tint = Color.White,
            modifier = Modifier
                .offset { IntOffset(cursorPos.x.roundToInt(), cursorPos.y.roundToInt()) }
                .size(22.dp)
        )
    }
}

@Composable
private fun PadButton(label: String, tint: Color = Color.White, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.5.dp, tint.copy(alpha = 0.7f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = tint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun SmallActionChip(label: String, isDanger: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDanger) S22Red.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.7f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
