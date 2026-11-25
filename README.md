# 90-FPS — Complete Windows Runtime for Android

90-FPS is an independent ARM64 rebranded build based on the **Winlator v11.2.0** Android engine. It targets Android devices with Vulkan-capable GPUs, with the primary test target being Samsung Galaxy S22 Ultra Snapdragon 8 Gen 1 / Adreno 730.

> 90-FPS is not an official Winlator release. Winlator and its contributors retain their original copyrights and licenses.

## What is included in the APK

The GitHub Actions build fetches the pinned Winlator v11.2.0 source and all of its pinned submodules, then builds one ARM64 APK containing the runtime assets:

- Ubuntu-based RootFS and Wine environment
- Box64 ARM64 translation runtime
- Mesa Turnip and supporting graphics drivers
- DXVK for Direct3D 9/10/11
- VKD3D for Direct3D 12
- D8VK and legacy DirectDraw/Direct3D compatibility components
- PulseAudio and supporting audio assets
- Container templates, input controls, and supporting Windows components

The workflow checks that key runtime archives are actually present inside the APK before publishing it. This avoids producing another UI-only shell that merely lists drivers without having the runtime.

## First launch

1. Install the 90-FPS ARM64 APK.
2. Open the app and allow the initial system-file installation to finish.
3. Keep the device powered and do not force-stop the app while RootFS is being extracted.
4. Keep several GB of free internal storage available for the installed RootFS, Wine prefixes, containers, and Windows applications.
5. Create a container, then select or import the Windows `.exe` you want to run.

The large runtime archives are bundled in the APK. The initial RootFS setup extracts the included system files into app storage; it is not necessary to download Box64, DXVK, or Turnip one by one.

## Device and compatibility notes

- Build ABI: `arm64-v8a`
- Primary target: Snapdragon 8 Gen 1 / Adreno 730
- Android minimum: inherited from the upstream Winlator engine (Android 8.0 / API 26)
- Vulkan support is required for Turnip/DXVK paths.
- Not every Windows application or PC game is guaranteed to work. Compatibility depends on the app/game, Windows APIs, graphics backend, memory use, driver version, and device temperature.
- Proton is not treated as a drop-in replacement for Wine. Proton-on-Android requires a compatible port and additional integration; the bundled Winlator engine uses its supported Wine/Box64 stack.

## Build and releases

Push to `main` or run the **Build 90-FPS Complete Windows Runtime** workflow from GitHub Actions.

The workflow:
1. Checks out Winlator v11.2.0 with its pinned submodules.
2. Rebrands the Android package to `com.s22winemu.app` and the launcher label to 90-FPS.
3. Builds the ARM64 APK with the upstream native runtime and bundled assets.
4. Verifies RootFS, Box64, DXVK, and Turnip archives are present in the APK.
5. Publishes the APK and a corresponding upstream source archive as a GitHub pre-release.

## Open-source notices

The Winlator Android engine is licensed under LGPL-2.1. The release includes the corresponding source archive and license notices. Third-party components (Wine, Box64, Mesa/Turnip, DXVK, VKD3D, D8VK, PRoot, and others) retain their respective upstream licenses and copyrights. See the attached source archive and the upstream project for complete notices:

- [Winlator upstream](https://github.com/brunodev85/winlator)
- [Winlator Android application source](https://github.com/brunodev85/winlator-app)
- [Winlator license](https://github.com/brunodev85/winlator/blob/main/LICENSE)

## Important

A successful GitHub Actions build confirms that the APK was built and that required runtime archives were bundled. It does not prove that every EXE or game works on every device. Test compatibility with the actual application and device.
