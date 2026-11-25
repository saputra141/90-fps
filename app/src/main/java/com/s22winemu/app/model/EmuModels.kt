package com.s22winemu.app.model

enum class DriverCategory(val label: String) {
    TURNIP_VULKAN("Turnip GPU (Adreno 730)"),
    DXVK("DXVK (DirectX 9/10/11)"),
    VKD3D("VKD3D-Proton (DirectX 12)"),
    BOX64("Box64 Dynarec (x86_64)"),
    WINE_ROOTFS("Wine / RootFS Container")
}

data class DriverPackage(
    val id: String,
    val name: String,
    val version: String,
    val category: DriverCategory,
    val compatibilityNote: String,
    val downloadUrl: String,
    val fileName: String,
    val recommendedForS22Ultra: Boolean = true,
    val envOverrides: Map<String, String> = emptyMap(),
    val isInstalled: Boolean = false,
    val localPath: String? = null
)

enum class S22PerformanceProfile(
    val title: String,
    val subtitle: String,
    val cpuAffinityMask: Int, // Bitmask Core 0..7 (0..3=A510, 4..6=A710, 7=Cortex-X2)
    val box64BigBlock: String,
    val box64StrongMem: String,
    val box64FastNan: String,
    val turnipDebug: String
) {
    BALANCED_THERMAL(
        title = "S22U Thermal Guard (Rekomendasi)",
        subtitle = "Core 4-7 (A710 + X2) • Mencegah throttling 4nm SD8G1",
        cpuAffinityMask = 0b11110000, // Core 4,5,6,7
        box64BigBlock = "2",
        box64StrongMem = "1",
        box64FastNan = "1",
        turnipDebug = "noconform"
    ),
    MAX_CORTEX_X2(
        title = "Max Performance (Cortex-X2 Boost)",
        subtitle = "Semua 8 Core aktif • Prioritas tinggi DX11/DX12",
        cpuAffinityMask = 0b11111111, // Core 0..7
        box64BigBlock = "3",
        box64StrongMem = "0",
        box64FastNan = "1",
        turnipDebug = "noconform,sysmem"
    ),
    ACCURATE_COMPAT(
        title = "Strict Compatibility (Anti-Crash)",
        subtitle = "StrongMem=2 • Cocok untuk game Unity / Unreal Engine lawas",
        cpuAffinityMask = 0b11111100,
        box64BigBlock = "1",
        box64StrongMem = "2",
        box64FastNan = "0",
        turnipDebug = "noconform"
    )
}

data class S22ResolutionPreset(
    val label: String,
    val width: Int,
    val height: Int,
    val note: String
)

data class WindowsContainer(
    val id: String,
    val name: String,
    val windowsVersion: String,
    val resolution: S22ResolutionPreset,
    val profile: S22PerformanceProfile,
    val activeTurnipId: String,
    val activeDxvkId: String,
    val activeVkd3dId: String,
    val activeBox64Id: String,
    val executablePath: String = "C:\\Windows\\explorer.exe",
    val vramLimitMb: Int = 4096
)

data class DownloadProgressState(
    val driverId: String? = null,
    val fileName: String = "",
    val progressPercent: Int = 0,
    val isDownloading: Boolean = false,
    val statusMessage: String = ""
)
