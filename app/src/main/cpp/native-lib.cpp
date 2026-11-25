#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <vector>
#include <cstring>

#define LOG_TAG "UltraEmulator-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

class GameBoyCore {
public:
    uint8_t memory[0x10000];
    uint32_t framebuffer[160 * 144];

    GameBoyCore() {
        memset(memory, 0, 0x10000);
        memset(framebuffer, 0xFF, sizeof(framebuffer)); // White screen
    }

    void loadROM(uint8_t* data, size_t size) {
        size_t loadSize = (size > 0x8000) ? 0x8000 : size;
        memcpy(memory, data, loadSize);
        LOGI("ROM Loaded: %zu bytes", size);
    }

    void stepFrame() {
        // Mock rendering logic: Fill with random noise for testing
        for (int i = 0; i < 160 * 144; i++) {
            framebuffer[i] = 0xFF000000 | (rand() % 0xFFFFFF);
        }
    }
};

static GameBoyCore* core = nullptr;

extern "C" JNIEXPORT void JNICALL
Java_com_ultra_emulator_EmulatorEngine_initCore(JNIEnv* env, jobject thiz) {
    if (core) delete core;
    core = new GameBoyCore();
}

extern "C" JNIEXPORT void JNICALL
Java_com_ultra_emulator_EmulatorEngine_loadROM(JNIEnv* env, jobject thiz, jbyteArray rom_data) {
    jbyte* data = env->GetByteArrayElements(rom_data, nullptr);
    jsize size = env->GetArrayLength(rom_data);
    if (core) core->loadROM(reinterpret_cast<uint8_t*>(data), size);
    env->ReleaseByteArrayElements(rom_data, data, JNI_ABORT);
}

extern "C" JNIEXPORT void JNICALL
Java_com_ultra_emulator_EmulatorEngine_renderFrame(JNIEnv* env, jobject thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    if (core) {
        core->stepFrame();
        memcpy(pixels, core->framebuffer, 160 * 144 * 4);
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}
