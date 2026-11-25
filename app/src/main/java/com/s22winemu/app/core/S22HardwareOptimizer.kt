package com.s22winemu.app.core

import android.os.Build
import com.s22winemu.app.model.DriverPackage
import com.s22winemu.app.model.S22ResolutionPreset
import com.s22winemu.app.model.WindowsContainer

object S22HardwareOptimizer {

    val s22UltraResolutions = listOf(
        S22ResolutionPreset("720p (16:9 Stabil)", 1280, 720, "FPS tertinggi & suhu paling adem di SD 8 Gen 1"),
        S22ResolutionPreset("S22U Fullscreen (20:9)", 1600, 720, "Pas penuh layar Dynamic AMOLED 2X S22 Ultra"),
        S22ResolutionPreset("900p (16:9 Tajam)", 1600, 900, "Keseimbangan visual & performa Adreno 730"),
        S22ResolutionPreset("1080p FHD (16:9)", 1920, 1080, "Kualitas maksimal untuk game ringan/sedang")
    )

    fun getDeviceIdentitySummary(): String {
        val model = Build.MODEL
        val board = Build.BOARD
        val hardware = Build.HARDWARE
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else "SM8450"
        return "$model | SoC: $soc ($board/$hardware)"
    }

    fun isSamsungS22UltraOrSnapdragon8Gen1(): Boolean {
        val model = Build.MODEL.uppercase()
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL.uppercase() else ""
        return model.contains("SM-S908") || soc.contains("SM8450") || soc.contains("TARO")
    }

    fun buildEnvironmentVariables(
        container: WindowsContainer,
        selectedTurnip: DriverPackage?,
        selectedDxvk: DriverPackage?,
        selectedBox64: DriverPackage?,
        driverRootPath: String,
        winePrefixPath: String
    ): Map<String, String> {
        val env = linkedMapOf<String, String>()

        // Core Wine & Display
        env["WINEPREFIX"] = winePrefixPath
        env["WINEESYNC"] = "1"
        env["WINEFSYNC"] = "1"
        env["WINEDEBUG"] = "-all"
        env["DISPLAY"] = ":0"
        env["WINEMU_RES"] = "${container.resolution.width}x${container.resolution.height}"

        // Snapdragon 8 Gen 1 / Adreno 730 Mesa Turnip Tuning
        env["MESA_VK_WSI_PRESENT_MODE"] = "mailbox"
        env["MESA_GL_VERSION_OVERRIDE"] = "4.6COMPAT"
        env["MESA_GLSL_VERSION_OVERRIDE"] = "460"
        env["TU_DEBUG"] = container.profile.turnipDebug
        env["ADRENO_TURNIP_730"] = "1"
        env["MESA_SHADER_CACHE_MAX_SIZE"] = "1G"
        env["VK_ICD_FILENAMES"] = selectedTurnip?.localPath ?: "$driverRootPath/turnip_adreno730/freedreno_icd.aarch64.json"

        // DXVK & VKD3D Optimization untuk 8GB/12GB RAM S22 Ultra
        env["DXVK_HUD"] = "fps,devinfo,gpuload,temps"
        env["DXVK_ASYNC"] = "1"
        env["DXVK_STATE_CACHE"] = "1"
        env["DXVK_LOG_LEVEL"] = "none"
        env["VKD3D_CONFIG"] = "dxr,force_host_cached"
        env["DXVK_FILTER_DEVICE_NAME"] = "Adreno"

        // Box64 Dynarec Khusus Cortex-X2 + A710 (ARMv9)
        env["BOX64_DYNAREC"] = "1"
        env["BOX64_DYNAREC_BIGBLOCK"] = container.profile.box64BigBlock
        env["BOX64_DYNAREC_STRONGMEM"] = container.profile.box64StrongMem
        env["BOX64_DYNAREC_FASTNAN"] = container.profile.box64FastNan
        env["BOX64_DYNAREC_FASTROUND"] = "1"
        env["BOX64_DYNAREC_SAFEFLAGS"] = "1"
        env["BOX64_DYNAREC_CALLRET"] = "1"
        env["BOX64_DYNAREC_BLEEDING_EDGE"] = "0"
        env["BOX64_NOBANNER"] = "1"
        env["BOX64_VULKAN"] = "1"

        // Custom overrides from selected driver packages
        selectedTurnip?.envOverrides?.let { env.putAll(it) }
        selectedDxvk?.envOverrides?.let { env.putAll(it) }
        selectedBox64?.envOverrides?.let { env.putAll(it) }

        return env
    }
}
