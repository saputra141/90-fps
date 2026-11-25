# S22 WinEmu — Windows Emulator Khusus Samsung Galaxy S22 Ultra (Snapdragon 8 Gen 1)

Emulator Windows berbasis Android (Kotlin + Jetpack Compose + Native C++ NDK/JNI) yang dioptimalkan langsung untuk **Samsung Galaxy S22 Ultra (SM-S908x / Snapdragon 8 Gen 1 / GPU Adreno 730)**.

## Fitur Utama
- **Optimasi Snapdragon 8 Gen 1 & Adreno 730**:
  - Deteksi GPU Adreno 730 via Vulkan (`libvulkan.so`) dan sensor suhu SoC langsung dari JNI C++.
  - *CPU Core Pinning* (`sched_setaffinity`) ke cluster Prime **Cortex-X2** dan **Cortex-A710** dengan profil *S22U Thermal Guard*.
  - Konfigurasi otomatis **Box64 Dynarec** + **Mesa Turnip** (`TU_DEBUG=noconform`, `MESA_VK_WSI_PRESENT_MODE=mailbox`, `DXVK_ASYNC=1`).
- **Pilihan Driver Adreno 730 Tertanam**:
  - Mesa Turnip v24.3.0-R11 (Adreno 730)
  - Mesa Turnip v24.1.0-R18 Stable
  - Qualcomm Stock Vulkan v676.53
  - DXVK-GPLAsync v2.4.1 & v1.10.3
  - VKD3D-Proton v2.13 (DirectX 12)
  - Box64 v0.3.0 (ARMv9/ARMv8.2+ dotprod)
- **Download Driver Sendiri di Google**:
  - Tab **Google** di dalam aplikasi dilengkapi WebView dengan pintasan pencarian driver Adreno 730 di Google & GitHub.
  - Setiap link `.zip`, `.adpkg`, `.tar.gz`, `.tzst`, atau `.wcp` yang diklik akan **otomatis diunduh & diekstrak** ke folder driver emulator.

## Cara Build APK Otomatis via GitHub
1. Salin seluruh file proyek ini ke repository GitHub Anda (`main` atau `master`).
2. Buka tab **Actions** di GitHub, workflow **Build S22 WinEmu APK** akan berjalan otomatis.
3. Setelah selesai, unduh artifact **S22WinEmu-SD8Gen1-APK** dan instal di Samsung Galaxy S22 Ultra Anda.
