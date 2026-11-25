package com.s22winemu.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.s22winemu.app.model.NativeToolchain
import com.s22winemu.app.model.RuntimeBackend
import com.s22winemu.app.ui.theme.*

@Composable
fun RuntimeManagerScreen(
    selectedBackend: RuntimeBackend,
    onBackendSelected: (RuntimeBackend) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = S22SurfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("WINDOWS GAMING RUNTIMES", color = S22Cyan, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Pilih compatibility backend. Pilihan ini mengatur konfigurasi launch; binary runtime tetap harus dipasang secara terpisah sebelum game Windows benar-benar dapat dijalankan.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(RuntimeBackend.entries) { backend ->
            val selected = selectedBackend == backend
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (selected) S22SurfaceVariant else S22Surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(backend.title, color = if (selected) S22Cyan else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        if (selected) Icon(Icons.Default.CheckCircle, contentDescription = "Dipilih", tint = S22Emerald)
                    }
                    Text(backend.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(backend.description, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AssistChip(onClick = {}, label = { Text(backend.status) })
                        Button(
                            onClick = { onBackendSelected(backend) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selected) S22Emerald else S22Cyan, contentColor = Color.Black)
                        ) { Text(if (selected) "AKTIF DIPILIH" else "PILIH BACKEND") }
                    }
                }
            }
        }

        item {
            Text("C / C++ NATIVE TOOLCHAIN", color = S22Cyan, style = MaterialTheme.typography.titleMedium)
            Text(
                "Komponen build untuk menghasilkan library ARM64. Toolchain ini dipakai saat build APK di CI/Android Studio; bukan compiler IDE lengkap yang otomatis terpasang di Android.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        items(NativeToolchain.entries) { tool ->
            Card(colors = CardDefaults.cardColors(containerColor = S22Surface)) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = S22Cyan)
                    Column {
                        Text(tool.title, style = MaterialTheme.typography.titleSmall)
                        Text(tool.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = S22SurfaceVariant)) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = S22Amber)
                    Text(
                        "Catatan S22 Ultra: Snapdragon 8 Gen 1 memakai ARM64 + Adreno 730. Proton desktop Linux tidak bisa langsung dipasang begitu saja di Android. Integrasi membutuhkan build yang memang ditargetkan untuk Android, rootfs, Box64/Wine, Turnip, DXVK dan/atau VKD3D-Proton yang cocok.",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
