# S22 WinEmu

Aplikasi Android eksperimental yang ditargetkan khusus untuk **Samsung Galaxy S22 Ultra Snapdragon 8 Gen 1 (SM8450 / Adreno 730)**.

## Target perangkat

- ABI: ARM64 (arm64-v8a)
- SoC: Qualcomm Snapdragon 8 Gen 1
- GPU: Adreno 730
- Android minimum: Android 8.0 (API 26)
- UI: Kotlin + Jetpack Compose
- Native layer: C++17 + Android NDK + JNI + Vulkan

## Fitur aplikasi saat ini

- Dashboard perangkat dan informasi GPU/Vulkan.
- Katalog driver dan impor paket driver dari penyimpanan.
- Profil resolusi dan optimasi yang ditujukan untuk layar S22 Ultra.
- Antarmuka desktop, log, dan kontrol sentuh virtual.
- Kerangka JNI untuk pemeriksaan hardware dan menjalankan binary runtime ARM64.

## Status penting: runtime Windows

APK Android ini **belum menyertakan distribusi Wine, Box64, root filesystem Windows compatibility, Mesa Turnip, DXVK, atau VKD3D-Proton yang siap pakai**. UI desktop dan katalog driver bukan berarti game Windows sudah bisa dijalankan.

Untuk menjalankan program Windows x86/x64 secara lokal, aplikasi masih membutuhkan paket runtime ARM64 yang kompatibel, integrasi loader/Box64 dan Wine yang benar, root filesystem/prefix, serta lapisan grafis yang cocok dengan Adreno 730. Game berat juga tidak dijamin berjalan lancar pada Snapdragon 8 Gen 1; kompatibilitas dan performa bergantung pada game, API grafis, RAM, driver, dan suhu perangkat.

Gunakan hanya file runtime dan game yang kamu miliki hak untuk menggunakannya. Jangan menganggap semua driver ZIP dapat langsung dipakai sebagai Vulkan driver Android.

## Build APK via GitHub Actions

1. Buka tab Actions di repository.
2. Jalankan workflow Build S22 WinEmu APK atau push commit ke branch main.
3. Jika workflow sukses, unduh artifact S22WinEmu-SD8Gen1-APK.
4. Instal APK debug di Galaxy S22 Ultra.

Build workflow mengompilasi target native ARM64. Build berhasil hanya membuktikan APK berhasil dibuat; bukan bukti bahwa runtime Windows/game sudah terintegrasi.
