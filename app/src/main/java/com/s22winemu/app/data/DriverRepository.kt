package com.s22winemu.app.data

import android.content.Context
import android.net.Uri
import com.s22winemu.app.model.DownloadProgressState
import com.s22winemu.app.model.DriverCategory
import com.s22winemu.app.model.DriverPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class DriverRepository(private val context: Context) {

    val driversDir: File = File(context.filesDir, "drivers").apply { mkdirs() }
    val customDriversDir: File = File(driversDir, "custom").apply { mkdirs() }
    val winePrefixDir: File = File(context.filesDir, "wine_prefix").apply { mkdirs() }
    val logsDir: File = File(context.filesDir, "logs").apply { mkdirs() }

    private val _drivers = MutableStateFlow<List<DriverPackage>>(emptyList())
    val drivers: StateFlow<List<DriverPackage>> = _drivers.asStateFlow()

    private val _downloadState = MutableStateFlow(DownloadProgressState())
    val downloadState: StateFlow<DownloadProgressState> = _downloadState.asStateFlow()

    init {
        refreshDriverCatalog()
    }

    fun refreshDriverCatalog() {
        val builtInCatalog = listOf(
            // === GPU DRIVERS KHUSUS ADRENO 730 (SNAPDRAGON 8 GEN 1) ===
            DriverPackage(
                id = "turnip_24_3_0_r11_a730",
                name = "Mesa Turnip v24.3.0-R11 (Adreno 730)",
                version = "24.3.0-R11",
                category = DriverCategory.TURNIP_VULKAN,
                compatibilityNote = "Terbaik untuk S22 Ultra (Adreno 730). Mendukung Vulkan 1.3 + gmem/sysmem optimal.",
                downloadUrl = "https://github.com/K11MCH1/AdrenoToolsDrivers/releases/download/v24.3.0_r11/Turnip-24.3.0.adpkg_R11.zip",
                fileName = "Turnip-24.3.0-R11-A730.zip",
                recommendedForS22Ultra = true,
                envOverrides = mapOf("TU_DEBUG" to "noconform", "MESA_VK_WSI_PRESENT_MODE" to "mailbox")
            ),
            DriverPackage(
                id = "turnip_24_1_0_stable_a730",
                name = "Mesa Turnip v24.1.0-R18 Stable (Adreno 730)",
                version = "24.1.0-R18",
                category = DriverCategory.TURNIP_VULKAN,
                compatibilityNote = "Sangat stabil untuk GTA V, Skyrim, Resident Evil di Snapdragon 8 Gen 1.",
                downloadUrl = "https://github.com/K11MCH1/AdrenoToolsDrivers/releases/download/v24.1.0_r18/Turnip-24.1.0.adpkg_R18.zip",
                fileName = "Turnip-24.1.0-R18-A730.zip",
                recommendedForS22Ultra = true,
                envOverrides = mapOf("TU_DEBUG" to "noconform")
            ),
            DriverPackage(
                id = "qualcomm_676_53_a730",
                name = "Qualcomm Adreno 730 Stock Vulkan v676.53",
                version = "676.53",
                category = DriverCategory.TURNIP_VULKAN,
                compatibilityNote = "Driver proprietary Qualcomm. Gunakan jika Turnip mengalami glitch tekstur.",
                downloadUrl = "https://github.com/K11MCH1/AdrenoToolsDrivers/releases/download/v676.53/Qualcomm_v676.53_Vulkan.zip",
                fileName = "Qualcomm-v676.53-A730.zip",
                recommendedForS22Ultra = false,
                envOverrides = mapOf("TU_DEBUG" to "0")
            ),

            // === DXVK (DirectX 9/10/11 to Vulkan) ===
            DriverPackage(
                id = "dxvk_2_4_1_gplasync",
                name = "DXVK-GPLAsync v2.4.1 (Anti-Stutter)",
                version = "2.4.1-async",
                category = DriverCategory.DXVK,
                compatibilityNote = "Mengurangi shader stutter di Adreno 730 secara drastis.",
                downloadUrl = "https://github.com/doitsujin/dxvk/releases/download/v2.4.1/dxvk-2.4.1.tar.gz",
                fileName = "dxvk-2.4.1.tar.gz",
                recommendedForS22Ultra = true,
                envOverrides = mapOf("DXVK_ASYNC" to "1")
            ),
            DriverPackage(
                id = "dxvk_1_10_3_legacy",
                name = "DXVK v1.10.3 (Ringan DX9/DX10)",
                version = "1.10.3",
                category = DriverCategory.DXVK,
                compatibilityNote = "Performa tertinggi untuk game DX9/DX10 ringan & memori hemat.",
                downloadUrl = "https://github.com/doitsujin/dxvk/releases/download/v1.10.3/dxvk-1.10.3.tar.gz",
                fileName = "dxvk-1.10.3.tar.gz",
                recommendedForS22Ultra = true,
                envOverrides = mapOf("DXVK_ASYNC" to "0")
            ),

            // === VKD3D-PROTON (DirectX 12) ===
            DriverPackage(
                id = "vkd3d_2_13",
                name = "VKD3D-Proton v2.13 (DirectX 12)",
                version = "2.13",
                category = DriverCategory.VKD3D,
                compatibilityNote = "Wajib untuk game DirectX 12 pada Turnip Adreno 730.",
                downloadUrl = "https://github.com/HansKristian-Work/vkd3d-proton/releases/download/v2.13/vkd3d-proton-2.13.tar.zst",
                fileName = "vkd3d-proton-2.13.tar.zst",
                recommendedForS22Ultra = true
            ),

            // === BOX64 DYNAREC ===
            DriverPackage(
                id = "box64_0_3_0_sd8g1",
                name = "Box64 v0.3.0 (ARMv9 Cortex-X2 Tuned)",
                version = "0.3.0",
                category = DriverCategory.BOX64,
                compatibilityNote = "Translator x86_64 -> ARM64 dikompilasi khusus flag dotprod ARMv8.2+/ARMv9.",
                downloadUrl = "https://github.com/ptitSeb/box64/archive/refs/tags/v0.3.0.zip",
                fileName = "box64-v0.3.0.zip",
                recommendedForS22Ultra = true
            )
        )

        val customFiles = customDriversDir.listFiles()?.map { file ->
            DriverPackage(
                id = "custom_${file.name.hashCode()}",
                name = "Custom Driver: ${file.nameWithoutExtension}",
                version = "Google/Local",
                category = guessCategory(file.name),
                compatibilityNote = "Diunduh langsung dari Google / Penyimpanan HP (${file.length() / 1024} KB)",
                downloadUrl = "",
                fileName = file.name,
                recommendedForS22Ultra = true,
                isInstalled = true,
                localPath = file.absolutePath
            )
        }.orEmpty()

        val merged = builtInCatalog.map { pkg ->
            val targetFile = File(driversDir, pkg.fileName)
            val unpackedDir = File(driversDir, pkg.id)
            val installed = targetFile.exists() || unpackedDir.exists()
            pkg.copy(
                isInstalled = installed,
                localPath = if (unpackedDir.exists()) unpackedDir.absolutePath else if (targetFile.exists()) targetFile.absolutePath else null
            )
        } + customFiles

        _drivers.value = merged
    }

    private fun guessCategory(fileName: String): DriverCategory {
        val lower = fileName.lowercase()
        return when {
            lower.contains("dxvk") -> DriverCategory.DXVK
            lower.contains("vkd3d") -> DriverCategory.VKD3D
            lower.contains("box") -> DriverCategory.BOX64
            lower.contains("wine") || lower.contains("rootfs") -> DriverCategory.WINE_ROOTFS
            else -> DriverCategory.TURNIP_VULKAN
        }
    }

    suspend fun downloadDriver(pkg: DriverPackage): Result<File> = withContext(Dispatchers.IO) {
        downloadFromUrl(
            driverId = pkg.id,
            urlStr = pkg.downloadUrl,
            targetFile = File(driversDir, pkg.fileName),
            unpackDir = File(driversDir, pkg.id)
        )
    }

    suspend fun downloadCustomFromGoogleUrl(urlStr: String, suggestedName: String?): Result<File> =
        withContext(Dispatchers.IO) {
            val cleanName = (suggestedName?.takeIf { it.isNotBlank() }
                ?: urlStr.substringAfterLast('/').substringBefore('?').ifBlank {
                    "google_driver_${System.currentTimeMillis()}.zip"
                }).replace(Regex("[^a-zA-Z0-9._-]"), "_")

            val targetFile = File(customDriversDir, cleanName)
            downloadFromUrl(
                driverId = "google_${cleanName.hashCode()}",
                urlStr = urlStr,
                targetFile = targetFile,
                unpackDir = File(customDriversDir, targetFile.nameWithoutExtension)
            )
        }

    suspend fun importLocalDriverZip(uri: Uri, displayName: String): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val safeName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val dest = File(customDriversDir, safeName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(dest).use { output ->
                        input.copyTo(output)
                    }
                } ?: error("Tidak dapat membaca file driver dari penyimpanan.")

                if (safeName.endsWith(".zip", ignoreCase = true) || safeName.endsWith(".adpkg", ignoreCase = true)) {
                    extractZipSafely(dest, File(customDriversDir, dest.nameWithoutExtension))
                }
                refreshDriverCatalog()
                dest
            }
        }

    private fun downloadFromUrl(
        driverId: String,
        urlStr: String,
        targetFile: File,
        unpackDir: File
    ): Result<File> {
        return runCatching {
            _downloadState.value = DownloadProgressState(
                driverId = driverId,
                fileName = targetFile.name,
                progressPercent = 0,
                isDownloading = true,
                statusMessage = "Menghubungkan ke server..."
            )

            var currentUrl = urlStr
            var connection: HttpURLConnection
            var redirects = 0
            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; SM-S908B) S22WinEmu/1.0")
                }
                val code = connection.responseCode
                if (code in 300..399 && redirects < 6) {
                    val loc = connection.getHeaderField("Location") ?: break
                    connection.disconnect()
                    currentUrl = loc
                    redirects++
                } else {
                    break
                }
            }

            if (connection.responseCode !in 200..299) {
                error("HTTP ${connection.responseCode}: Gagal mengunduh ${targetFile.name}")
            }

            val totalBytes = connection.contentLengthLong
            BufferedInputStream(connection.inputStream).use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        val pct = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 50
                        _downloadState.value = DownloadProgressState(
                            driverId = driverId,
                            fileName = targetFile.name,
                            progressPercent = pct,
                            isDownloading = true,
                            statusMessage = "Mengunduh ${downloaded / 1024} KB..."
                        )
                    }
                }
            }
            connection.disconnect()

            if (targetFile.name.endsWith(".zip", ignoreCase = true) ||
                targetFile.name.endsWith(".adpkg", ignoreCase = true)
            ) {
                _downloadState.value = _downloadState.value.copy(statusMessage = "Mengekstrak driver...")
                extractZipSafely(targetFile, unpackDir)
            }

            _downloadState.value = DownloadProgressState(
                driverId = null,
                fileName = targetFile.name,
                progressPercent = 100,
                isDownloading = false,
                statusMessage = "Berhasil diinstal: ${targetFile.name}"
            )
            refreshDriverCatalog()
            targetFile
        }.onFailure { err ->
            _downloadState.value = DownloadProgressState(
                driverId = null,
                fileName = targetFile.name,
                progressPercent = 0,
                isDownloading = false,
                statusMessage = "Gagal: ${err.localizedMessage}"
            )
        }
    }

    private fun extractZipSafely(zipFile: File, destDir: File) {
        if (!destDir.exists()) destDir.mkdirs()
        val canonicalDest = destDir.canonicalPath
        ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zis ->
            var entry = zis.nextEntry
            val buffer = ByteArray(8192)
            while (entry != null) {
                val outFile = File(destDir, entry.name)
                if (!outFile.canonicalPath.startsWith(canonicalDest)) {
                    throw SecurityException("Zip Slip terdeteksi: ${entry.name}")
                }
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        var count: Int
                        while (zis.read(buffer).also { count = it } != -1) {
                            fos.write(buffer, 0, count)
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    fun deleteDriver(pkg: DriverPackage) {
        File(driversDir, pkg.fileName).delete()
        File(driversDir, pkg.id).deleteRecursively()
        File(customDriversDir, pkg.fileName).delete()
        File(customDriversDir, File(pkg.fileName).nameWithoutExtension).deleteRecursively()
        refreshDriverCatalog()
    }
}
