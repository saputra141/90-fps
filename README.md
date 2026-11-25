# 90-FPS — S22 WinEmu

Aplikasi Android eksperimental yang ditargetkan khusus untuk **Samsung Galaxy S22 Ultra Snapdragon 8 Gen 1 (SM8450 / Adreno 730)**.

## Target perangkat

- ABI: ARM64 (arm64-v8a)
- SoC: Qualcomm Snapdragon 8 Gen 1
- GPU: Adreno 730
- Android minimum: Android 8.0 (API 26)
- UI: Kotlin + Jetpack Compose
- Native layer: C++17 + Android NDK + JNI + Vulkan

## Branding dan tampilan\n\n- Nama aplikasi: **90-FPS** dengan ikon launcher neon khusus dan adaptive icon Android.\n- Dashboard gelap bergaya gaming dengan aksen cyan/ungu, indikator perangkat, serta tab Runtime dan Scaling Lab.\n\n## Fitur aplikasi saat ini

- Dashboard perangkat dan informasi GPU/Vulkan.
- Katalog driver dan impor paket driver dari penyimpanan.
- Profil resolusi dan optimasi yang ditujukan untuk layar S22 Ultra.
- Antarmuka desktop, log, dan kontrol sentuh virtual.
- Kerangka JNI untuk pemeriksaan hardware dan menjalankan binary runtime ARM64.

## Runtime Windows dan backend

Menu **Runtime** menyediakan pilihan konfigurasi Wine + Box64, Proton (Experimental), Wine ARM64, dan Box64 + Wine. Pilihan backend diteruskan sebagai environment variable saat membuka desktop container. Ini adalah pemilihan konfigurasi, bukan klaim bahwa binary Proton/Wine sudah terpasang.

**Proton di Android:** Proton upstream ditujukan untuk Linux/Steam. Android memerlukan build/port yang kompatibel, integrasi ARM64/Box64, root filesystem, serta graphics stack yang sesuai. Karena itu Proton ditandai Experimental dan tidak otomatis diunduh atau dijalankan sebagai Proton desktop biasa.

## Scaling Lab: upscaling dan frame generation\n\nTab **Scaling** menjelaskan jalur peningkatan kualitas/performa grafis. Render resolution scaling melalui preset resolusi yang sudah ada dapat mengurangi beban GPU, tetapi bukan AI upscaling. **Lossless Scaling Frame Generation (LSFG) belum terintegrasi**: aplikasi PC Lossless Scaling tidak dapat langsung dipakai sebagai library Android. Implementasi Android memerlukan capture frame, motion estimation/interpolation, Vulkan compute shader, dan frame pacing. UI menandai fitur ini sebagai belum tersedia agar tidak memberi kesan FPS sudah meningkat.\n\n## C/C++ native toolchain

Build APK memakai Android NDK 26.3.11579264 dan CMake 3.22.1 melalui GitHub Actions. NDK menyediakan Clang/LLVM, linker, ARM64 sysroot, libc++, JNI headers, dan LLDB tooling; CMake mengatur build native. CMakeLists mengompilasi kode C++17 untuk ABI `arm64-v8a`. Komponen ini adalah toolchain build untuk APK, bukan IDE/compiler C++ lengkap yang otomatis terinstal di ponsel.

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
