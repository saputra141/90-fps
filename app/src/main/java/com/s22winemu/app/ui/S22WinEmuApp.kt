package com.s22winemu.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.s22winemu.app.core.HardwareTelemetry
import com.s22winemu.app.core.NativeBridge
import com.s22winemu.app.core.S22HardwareOptimizer
import com.s22winemu.app.data.DriverRepository
import com.s22winemu.app.model.*
import com.s22winemu.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun S22WinEmuApp(repository: DriverRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedBackend by remember { mutableStateOf(RuntimeBackend.WINE_BOX64) }
    val drivers by repository.drivers.collectAsState()
    val downloadState by repository.downloadState.collectAsState()

    var telemetry by remember {
        mutableStateOf(
            runCatching { NativeBridge.nativeProbeHardware() }.getOrElse {
                HardwareTelemetry("Adreno (TM) 730 (Fallback)", 8, "1.1.128", true, 39.0f, "Android Kernel")
            }
        )
    }

    var container by remember {
        mutableStateOf(
            WindowsContainer(
                id = "s22u_main",
                name = "Windows 11 ARM/x64 (S22 Ultra)",
                windowsVersion = "Windows 10/11 (Wine 9.x)",
                resolution = S22HardwareOptimizer.s22UltraResolutions[1], // 1600x720 20:9
                profile = S22PerformanceProfile.BALANCED_THERMAL,
                activeTurnipId = "turnip_24_3_0_r11_a730",
                activeDxvkId = "dxvk_2_4_1_gplasync",
                activeVkd3dId = "vkd3d_2_13",
                activeBox64Id = "box64_0_3_0_sd8g1"
            )
        )
    }

    var googleBrowserUrl by remember {
        mutableStateOf("https://www.google.com/search?q=mesa+turnip+adreno+730+driver+zip+github")
    }

    // Refresh suhu SoC tiap 3 detik
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val temp = runCatching { NativeBridge.nativeReadTemperature() }.getOrNull()
            if (temp != null) {
                telemetry = telemetry.copy(socTempCelsius = temp)
            }
        }
    }

    val zipPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
                } ?: "imported_driver.zip"

                repository.importLocalDriverZip(uri, displayName).onSuccess {
                    Toast.makeText(context, "Driver diimpor: ${it.name}", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Gagal impor: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
            } ?: "game.exe"
            container = container.copy(executablePath = "Z:\\sdcard\\Download\\$fileName")
            Toast.makeText(context, "Target EXE: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = S22Surface),
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "S22 WinEmu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = S22Cyan
                            )
                            Spacer(Modifier.width(8.dp))
                            BadgeChip(
                                text = if (telemetry.isAdreno730Detected) "Adreno 730 OK" else "SD8G1 Mode",
                                color = if (telemetry.isAdreno730Detected) S22Emerald else S22Amber
                            )
                        }
                        Text(
                            "Snapdragon 8 Gen 1 • ${String.format("%.1f", telemetry.socTempCelsius)}°C • Vulkan ${telemetry.vulkanApiVersion}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = S22Surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DesktopWindows, contentDescription = null) },
                    label = { Text("Emulator", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Memory, contentDescription = null) },
                    label = { Text("Driver A730", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null) },
                    label = { Text("Tuner S22U", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Public, contentDescription = null) },
                    label = { Text("Google", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Runtime", fontSize = 11.sp) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(S22BgDark)
                .padding(padding)
        ) {
            if (downloadState.isDownloading) {
                LinearProgressIndicator(
                    progress = { downloadState.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = S22Cyan
                )
                Text(
                    "${downloadState.fileName} (${downloadState.progressPercent}%) - ${downloadState.statusMessage}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = S22Cyan
                )
            }

            when (selectedTab) {
                0 -> ContainerLaunchScreen(
                    container = container,
                    telemetry = telemetry,
                    drivers = drivers,
                    onPickExe = { exePicker.launch(arrayOf("*/*")) },
                    onResolutionChange = { container = container.copy(resolution = it) },
                    onLaunch = {
                        // Terapkan CPU Core Pinning Snapdragon 8 Gen 1 sebelum launch
                        NativeBridge.nativeSetCpuAffinity(container.profile.cpuAffinityMask)

                        val turnip = drivers.find { it.id == container.activeTurnipId }
                        val dxvk = drivers.find { it.id == container.activeDxvkId }
                        val box64 = drivers.find { it.id == container.activeBox64Id }

                        val envMap = S22HardwareOptimizer.buildEnvironmentVariables(
                            container = container,
                            selectedTurnip = turnip,
                            selectedDxvk = dxvk,
                            selectedBox64 = box64,
                            driverRootPath = repository.driversDir.absolutePath,
                            winePrefixPath = repository.winePrefixDir.absolutePath,
                            runtimeBackend = selectedBackend
                        )

                        val intent = Intent(context, DisplayActivity::class.java).apply {
                            putExtra("container_name", container.name)
                            putExtra("res_w", container.resolution.width)
                            putExtra("res_h", container.resolution.height)
                            putExtra("exe_path", container.executablePath)
                            putExtra("turnip_name", turnip?.name ?: "Mesa Turnip Adreno 730")
                            putExtra("dxvk_name", dxvk?.name ?: "DXVK 2.4.1")
                            putExtra("profile_title", container.profile.title)
                            putExtra("runtime_backend", selectedBackend.name)
                            putExtra("cpu_mask", container.profile.cpuAffinityMask)
                            putStringArrayListExtra(
                                "env_pairs",
                                ArrayList(envMap.entries.map { "${it.key}=${it.value}" })
                            )
                        }
                        context.startActivity(intent)
                    }
                )

                1 -> DriversScreen(
                    drivers = drivers,
                    container = container,
                    onDownloadDriver = { pkg ->
                        scope.launch {
                            repository.downloadDriver(pkg).onSuccess {
                                Toast.makeText(context, "${pkg.name} siap digunakan!", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(context, "Unduhan gagal: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onSelectDriver = { pkg ->
                        container = when (pkg.category) {
                            DriverCategory.TURNIP_VULKAN -> container.copy(activeTurnipId = pkg.id)
                            DriverCategory.DXVK -> container.copy(activeDxvkId = pkg.id)
                            DriverCategory.VKD3D -> container.copy(activeVkd3dId = pkg.id)
                            DriverCategory.BOX64 -> container.copy(activeBox64Id = pkg.id)
                            DriverCategory.WINE_ROOTFS -> container
                        }
                        Toast.makeText(context, "Dipilih: ${pkg.name}", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteDriver = { repository.deleteDriver(it) },
                    onImportZip = { zipPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                    onOpenGoogleSearch = { query ->
                        googleBrowserUrl = "https://www.google.com/search?q=${Uri.encode(query)}"
                        selectedTab = 3
                    }
                )

                2 -> S22UltraTunerScreen(
                    telemetry = telemetry,
                    container = container,
                    onProfileSelected = { profile ->
                        container = container.copy(profile = profile)
                        val ok = NativeBridge.nativeSetCpuAffinity(profile.cpuAffinityMask)
                        Toast.makeText(
                            context,
                            if (ok) "CPU Core Mask 0x${profile.cpuAffinityMask.toString(16)} diterapkan!" else "Profil disimpan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                4 -> RuntimeManagerScreen(
                    selectedBackend = selectedBackend,
                    onBackendSelected = { selectedBackend = it }
                )

                3 -> GoogleDriverBrowserScreen(
                    initialUrl = googleBrowserUrl,
                    onUrlChanged = { googleBrowserUrl = it },
                    onInterceptDownload = { url, suggestedFileName ->
                        scope.launch {
                            Toast.makeText(context, "Mengunduh driver dari Google...", Toast.LENGTH_SHORT).show()
                            repository.downloadCustomFromGoogleUrl(url, suggestedFileName).onSuccess { file ->
                                Toast.makeText(
                                    context,
                                    "Driver terpasang: ${file.name}! Cek tab Driver A730.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure {
                                Toast.makeText(context, "Gagal unduh: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ContainerLaunchScreen(
    container: WindowsContainer,
    telemetry: HardwareTelemetry,
    drivers: List<DriverPackage>,
    onPickExe: () -> Unit,
    onResolutionChange: (S22ResolutionPreset) -> Unit,
    onLaunch: () -> Unit
) {
    val activeTurnip = drivers.find { it.id == container.activeTurnipId }
    val activeDxvk = drivers.find { it.id == container.activeDxvkId }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = S22Surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(S22Cyan, S22Violet)),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("SAMSUNG GALAXY S22 ULTRA EDITION", fontSize = 11.sp, color = S22Cyan, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(container.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "GPU Terdeteksi: ${telemetry.gpuName}\n" +
                        "Hardware: ${S22HardwareOptimizer.getDeviceIdentitySummary()}\n" +
                        "Driver Aktif: ${activeTurnip?.name ?: "Turnip A730"} + ${activeDxvk?.version ?: "DXVK"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = S22Surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target Aplikasi / Game (.EXE)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        container.executablePath,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = S22Cyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    OutlinedButton(onClick = onPickExe, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pilih File .EXE dari Penyimpanan HP")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = S22Surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resolusi Layar S22 Ultra", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    S22HardwareOptimizer.s22UltraResolutions.forEach { preset ->
                        val selected = preset == container.resolution
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) S22SurfaceVariant else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (selected) S22Cyan else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onResolutionChange(preset) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = { onResolutionChange(preset) })
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("${preset.label} (${preset.width}x${preset.height})", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(preset.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onLaunch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = S22Cyan, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("JALANKAN WINDOWS CONTAINER", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun DriversScreen(
    drivers: List<DriverPackage>,
    container: WindowsContainer,
    onDownloadDriver: (DriverPackage) -> Unit,
    onSelectDriver: (DriverPackage) -> Unit,
    onDeleteDriver: (DriverPackage) -> Unit,
    onImportZip: () -> Unit,
    onOpenGoogleSearch: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = S22SurfaceVariant)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Download Driver Sendiri di Google / Impor Lokal", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = S22Cyan)
                    Text(
                        "Cari driver Turnip Adreno 730, DXVK, atau Box64 terbaru di Google. Saat file .zip/.adpkg diklik, aplikasi otomatis mengunduh & memasangnya.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onOpenGoogleSearch("mesa turnip adreno 730 driver zip github") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = S22Violet)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cari di Google", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onImportZip,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Impor .ZIP", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        items(drivers, key = { it.id }) { pkg ->
            val isSelected = pkg.id in setOf(
                container.activeTurnipId,
                container.activeDxvkId,
                container.activeVkd3dId,
                container.activeBox64Id
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = S22Surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isSelected) S22Emerald else Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(pkg.category.label, fontSize = 11.sp, color = S22Cyan)
                        }
                        if (pkg.recommendedForS22Ultra) {
                            BadgeChip("S22U Ready", S22Emerald)
                        }
                    }
                    Text(pkg.compatibilityNote, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pkg.downloadUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onDownloadDriver(pkg) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (pkg.isInstalled) "Unduh Ulang" else "Download", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { onSelectDriver(pkg) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) S22Emerald else S22Cyan,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(if (isSelected) "AKTIF" else "PAKAI DRIVER", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (pkg.isInstalled) {
                            IconButton(onClick = { onDeleteDriver(pkg) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = S22Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun S22UltraTunerScreen(
    telemetry: HardwareTelemetry,
    container: WindowsContainer,
    onProfileSelected: (S22PerformanceProfile) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = S22Surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Telemetri Snapdragon 8 Gen 1 (SM8450)", fontWeight = FontWeight.Bold, color = S22Cyan)
                    Text("GPU Vulkan: ${telemetry.gpuName}", fontSize = 13.sp)
                    Text("Arsitektur CPU: 1x Cortex-X2 + 3x Cortex-A710 + 4x Cortex-A510 (${telemetry.cpuCores} Core)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Suhu SoC Saat Ini: ${String.format("%.1f", telemetry.socTempCelsius)} °C", fontSize = 13.sp, color = if (telemetry.socTempCelsius > 55f) S22Red else S22Emerald)
                    Text("Kernel: ${telemetry.kernelVersion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Text("Pilih Profil CPU & Box64 Dynarec Khusus S22 Ultra", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        items(S22PerformanceProfile.entries) { profile ->
            val selected = container.profile == profile
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) S22SurfaceVariant else S22Surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (selected) S22Cyan else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .clickable { onProfileSelected(profile) }
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected, onClick = { onProfileSelected(profile) })
                        Column {
                            Text(profile.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(profile.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        "BIGBLOCK=${profile.box64BigBlock} | STRONGMEM=${profile.box64StrongMem} | FASTNAN=${profile.box64FastNan} | TU_DEBUG=${profile.turnipDebug}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = S22Cyan
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GoogleDriverBrowserScreen(
    initialUrl: String,
    onUrlChanged: (String) -> Unit,
    onInterceptDownload: (url: String, fileName: String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentPageTitle by remember { mutableStateOf("Google Driver Finder") }

    val quickQueries = listOf(
        "Turnip A730" to "https://www.google.com/search?q=mesa+turnip+adreno+730+adpkg+zip+github",
        "K11MCH1 Releases" to "https://github.com/K11MCH1/AdrenoToolsDrivers/releases",
        "DXVK Async" to "https://www.google.com/search?q=dxvk+gplasync+release+github+download",
        "Box64 / Wine" to "https://www.google.com/search?q=winlator+box64+wcp+turnip+download"
    )

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(S22Surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari driver di Google (mis. Turnip Adreno 730)...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            IconButton(
                onClick = {
                    val target = if (searchQuery.startsWith("http")) searchQuery
                    else "https://www.google.com/search?q=${Uri.encode(searchQuery.ifBlank { "mesa turnip adreno 730 driver zip" })}"
                    onUrlChanged(target)
                    webViewRef?.loadUrl(target)
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Cari", tint = S22Cyan)
            }
            IconButton(onClick = { if (webViewRef?.canGoBack() == true) webViewRef?.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(S22SurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickQueries.forEach { (label, url) ->
                AssistChip(
                    onClick = {
                        onUrlChanged(url)
                        webViewRef?.loadUrl(url)
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Text(
            "Auto-Download Aktif: $currentPageTitle",
            fontSize = 10.sp,
            color = S22Emerald,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val u = request?.url?.toString() ?: return false
                            val lower = u.lowercase()
                            if (lower.endsWith(".zip") || lower.endsWith(".adpkg") ||
                                lower.endsWith(".tar.gz") || lower.endsWith(".tar.xz") ||
                                lower.endsWith(".tzst") || lower.endsWith(".wcp")
                            ) {
                                onInterceptDownload(u, URLUtil.guessFileName(u, null, null))
                                return true
                            }
                            return false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            if (!title.isNullOrBlank()) currentPageTitle = title
                        }
                    }

                    setDownloadListener { url, _, contentDisposition, mimeType, _ ->
                        val guessedName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                        onInterceptDownload(url, guessedName)
                    }

                    loadUrl(initialUrl)
                    webViewRef = this
                }
            }
        )
    }
}

@Composable
private fun BadgeChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
