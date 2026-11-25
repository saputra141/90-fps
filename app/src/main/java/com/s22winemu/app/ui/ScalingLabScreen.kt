package com.s22winemu.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.s22winemu.app.ui.theme.*

@Composable
fun ScalingLabScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = S22SurfaceVariant)
            ) {
                Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF102B50), Color(0xFF251449)))).padding(18.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = S22Cyan, modifier = Modifier.size(32.dp))
                        Text("SCALING LAB", color = S22Cyan, style = MaterialTheme.typography.labelLarge)
                        Text("Lebih tajam. Lebih efisien.", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text("Pusat eksperimen upscaling dan frame pacing untuk game Windows di Android.", color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
                    }
                }
            }
        }
        item {
            FeatureStatusCard(
                icon = { Icon(Icons.Default.Tune, contentDescription = null, tint = S22Cyan) },
                title = "Render resolution scaling",
                status = "TERSEDIA DI PENGATURAN GAME",
                detail = "Gunakan preset resolusi lebih rendah pada layar/container untuk mengurangi beban GPU. Ini bukan AI upscaling dan hasilnya bergantung pada game."
            )
        }
        item {
            FeatureStatusCard(
                icon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = S22Violet) },
                title = "Lossless Scaling / LSFG",
                status = "BELUM TERINTEGRASI",
                detail = "LSFG adalah frame generation dari aplikasi Lossless Scaling di PC. Android membutuhkan pipeline frame capture, motion estimation, GPU shader, dan frame pacing tersendiri. Memilih menu ini belum akan menambah FPS."
            )
        }
        item {
            FeatureStatusCard(
                icon = { Icon(Icons.Default.Memory, contentDescription = null, tint = S22Emerald) },
                title = "Native GPU upscaling",
                status = "TAHAP RANCANGAN",
                detail = "Rencana jalur implementasi: Vulkan compute shader untuk spatial upscaling (misalnya FSR-style), dengan fallback bila driver Adreno tidak mendukung fitur yang diperlukan."
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = S22Surface), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = S22Amber)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Catatan performa", style = MaterialTheme.typography.titleSmall)
                        Text("Frame generation tidak selalu menaikkan FPS asli. Ia dapat menambah frame visual tetapi juga menambah latency, panas, dan penggunaan GPU. Snapdragon 8 Gen 1 perlu pengujian suhu serta kompatibilitas per game.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureStatusCard(
    icon: @Composable () -> Unit,
    title: String,
    status: String,
    detail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = S22Surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                icon()
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Text(status, color = S22Amber, style = MaterialTheme.typography.labelSmall)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}
