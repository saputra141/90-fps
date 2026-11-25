package com.s22winemu.app.core

data class HardwareTelemetry(
    val gpuName: String,
    val cpuCores: Int,
    val vulkanApiVersion: String,
    val isAdreno730Detected: Boolean,
    val socTempCelsius: Float,
    val kernelVersion: String
)

object NativeBridge {
    init {
        System.loadLibrary("s22winemu")
    }

    external fun nativeProbeHardware(): HardwareTelemetry
    external fun nativeSetCpuAffinity(coreMask: Int): Boolean
    external fun nativeReadTemperature(): Float
    external fun nativeLaunchRuntime(
        envPairs: Array<String>,
        workDir: String,
        binaryPath: String,
        logPath: String
    ): Int
}
