package com.s22winemu.app.model

/**
 * Runtime choices are configuration targets. A backend is only runnable after
 * its matching ARM64 binaries/root filesystem have been installed and validated.
 */
enum class RuntimeBackend(
    val title: String,
    val subtitle: String,
    val status: String,
    val description: String
) {
    WINE_BOX64(
        "Wine + Box64",
        "Baseline Android Windows compatibility",
        "Baseline",
        "Runs x86/x64 Windows apps through Wine and Box64. Requires a compatible ARM64 runtime/rootfs."
    ),
    PROTON(
        "Proton (Experimental)",
        "Wine-based gaming compatibility stack",
        "Experimental",
        "Proton is not a standalone Android emulator. Android builds need a compatible Proton/Wine package, Box64 integration, pressure-vessel/runtime adaptations and graphics drivers."
    ),
    WINE(
        "Wine (ARM64)",
        "Windows API compatibility layer",
        "Alternative",
        "Useful for Windows applications and games that work without the Proton-specific patches."
    ),
    BOX64(
        "Box64 + Wine",
        "x86_64 translation focused",
        "Alternative",
        "Box64 translates x86_64 Linux binaries to ARM64; Wine supplies Windows API compatibility."
    )
}

enum class NativeToolchain(
    val title: String,
    val detail: String
) {
    NDK("Android NDK", "ARM64 compiler, linker, sysroot and native Android build support"),
    CLANG("Clang / LLVM", "C and C++ compiler toolchain supplied by Android NDK"),
    CMAKE("CMake + Ninja", "Native project configuration and fast builds"),
    LLDB("LLDB", "Native debugging for ARM64 code"),
    LIBCXX("libc++ / libc++_shared", "C++ standard library for Android"),
    JNI("JNI + Android headers", "Kotlin/Java to C/C++ bridge"),
    VULKAN("Vulkan headers + loader", "Graphics API integration; device driver remains device-provided")
}
