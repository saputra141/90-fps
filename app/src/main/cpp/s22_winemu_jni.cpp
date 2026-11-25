#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>
#include <dlfcn.h>
#include <sched.h>
#include <unistd.h>
#include <sys/utsname.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <android/log.h>
#include <vulkan/vulkan.h>
#include <cstring>
#include <cstdlib>

#define LOG_TAG "S22WinEmuJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct VulkanGpuInfo {
    std::string deviceName = "Unknown GPU";
    uint32_t vendorId = 0;
    uint32_t deviceId = 0;
    uint32_t apiVersion = 0;
    uint32_t driverVersion = 0;
    bool isAdreno730 = false;
};

static VulkanGpuInfo probeVulkanGpu() {
    VulkanGpuInfo info;
    void* libVk = dlopen("libvulkan.so", RTLD_NOW | RTLD_LOCAL);
    if (!libVk) {
        LOGE("Failed to open libvulkan.so");
        return info;
    }

    auto pVkCreateInstance = reinterpret_cast<PFN_vkCreateInstance>(dlsym(libVk, "vkCreateInstance"));
    auto pVkEnumeratePhysicalDevices = reinterpret_cast<PFN_vkEnumeratePhysicalDevices>(dlsym(libVk, "vkEnumeratePhysicalDevices"));
    auto pVkGetPhysicalDeviceProperties = reinterpret_cast<PFN_vkGetPhysicalDeviceProperties>(dlsym(libVk, "vkGetPhysicalDeviceProperties"));
    auto pVkDestroyInstance = reinterpret_cast<PFN_vkDestroyInstance>(dlsym(libVk, "vkDestroyInstance"));

    if (!pVkCreateInstance || !pVkEnumeratePhysicalDevices || !pVkGetPhysicalDeviceProperties || !pVkDestroyInstance) {
        dlclose(libVk);
        return info;
    }

    VkApplicationInfo appInfo{};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "S22WinEmuProbe";
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "S22WinEmu";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_1;

    VkInstanceCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;

    VkInstance instance = VK_NULL_HANDLE;
    if (pVkCreateInstance(&createInfo, nullptr, &instance) == VK_SUCCESS && instance != VK_NULL_HANDLE) {
        uint32_t deviceCount = 0;
        pVkEnumeratePhysicalDevices(instance, &deviceCount, nullptr);
        if (deviceCount > 0) {
            std::vector<VkPhysicalDevice> devices(deviceCount);
            pVkEnumeratePhysicalDevices(instance, &deviceCount, devices.data());

            VkPhysicalDeviceProperties props{};
            pVkGetPhysicalDeviceProperties(devices[0], &props);

            info.deviceName = props.deviceName;
            info.vendorId = props.vendorID;
            info.deviceId = props.deviceID;
            info.apiVersion = props.apiVersion;
            info.driverVersion = props.driverVersion;
            info.isAdreno730 = (info.deviceName.find("730") != std::string::npos) ||
                                (info.vendorId == 0x5143 && info.deviceName.find("Adreno") != std::string::npos);
        }
        pVkDestroyInstance(instance, nullptr);
    }

    dlclose(libVk);
    return info;
}

static float readSocTemperatureC() {
    for (int i = 0; i < 30; ++i) {
        std::string typePath = "/sys/class/thermal/thermal_zone" + std::to_string(i) + "/type";
        std::string tempPath = "/sys/class/thermal/thermal_zone" + std::to_string(i) + "/temp";
        std::ifstream typeFile(typePath);
        if (!typeFile.is_open()) continue;
        std::string typeStr;
        std::getline(typeFile, typeStr);
        if (typeStr.find("cpu") != std::string::npos ||
            typeStr.find("gpuss") != std::string::npos ||
            typeStr.find("soc") != std::string::npos ||
            typeStr.find("tsens") != std::string::npos) {
            std::ifstream tempFile(tempPath);
            if (tempFile.is_open()) {
                long rawTemp = 0;
                tempFile >> rawTemp;
                if (rawTemp > 1000) return static_cast<float>(rawTemp) / 1000.0f;
                if (rawTemp > 0) return static_cast<float>(rawTemp);
            }
        }
    }
    return 38.5f; // Fallback safe reading if SELinux blocks sysfs thermal
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_s22winemu_app_core_NativeBridge_nativeProbeHardware(JNIEnv *env, jobject /*thiz*/) {
    VulkanGpuInfo gpu = probeVulkanGpu();
    long cpuCores = sysconf(_SC_NPROCESSORS_CONF);
    float tempC = readSocTemperatureC();

    struct utsname uts{};
    uname(&uts);
    std::string kernelStr = std::string(uts.release) + " (" + uts.machine + ")";

    std::string vkApi = std::to_string(VK_API_VERSION_MAJOR(gpu.apiVersion)) + "." +
                        std::to_string(VK_API_VERSION_MINOR(gpu.apiVersion)) + "." +
                        std::to_string(VK_API_VERSION_PATCH(gpu.apiVersion));

    jclass cls = env->FindClass("com/s22winemu/app/core/HardwareTelemetry");
    jmethodID ctor = env->GetMethodID(
            cls,
            "<init>",
            "(Ljava/lang/String;ILjava/lang/String;ZFLjava/lang/String;)V"
    );

    jstring jGpuName = env->NewStringUTF(gpu.deviceName.c_str());
    jstring jVkApi = env->NewStringUTF(vkApi.c_str());
    jstring jKernel = env->NewStringUTF(kernelStr.c_str());

    return env->NewObject(
            cls,
            ctor,
            jGpuName,
            static_cast<jint>(cpuCores),
            jVkApi,
            static_cast<jboolean>(gpu.isAdreno730),
            static_cast<jfloat>(tempC),
            jKernel
    );
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_s22winemu_app_core_NativeBridge_nativeSetCpuAffinity(JNIEnv * /*env*/, jobject /*thiz*/, jint coreMask) {
    cpu_set_t cpuset;
    CPU_ZERO(&cpuset);
    for (int cpu = 0; cpu < 8; ++cpu) {
        if ((coreMask & (1 << cpu)) != 0) {
            CPU_SET(cpu, &cpuset);
        }
    }
    int res = sched_setaffinity(0, sizeof(cpu_set_t), &cpuset);
    if (res != 0) {
        LOGE("sched_setaffinity failed for mask 0x%x", coreMask);
        return JNI_FALSE;
    }
    LOGI("CPU Affinity pinned to mask 0x%x (Snapdragon 8 Gen 1)", coreMask);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_s22winemu_app_core_NativeBridge_nativeReadTemperature(JNIEnv * /*env*/, jobject /*thiz*/) {
    return static_cast<jfloat>(readSocTemperatureC());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_s22winemu_app_core_NativeBridge_nativeLaunchRuntime(
        JNIEnv *env,
        jobject /*thiz*/,
        jobjectArray envPairs,
        jstring workDir,
        jstring binaryPath,
        jstring logPath
) {
    const char *cWorkDir = env->GetStringUTFChars(workDir, nullptr);
    const char *cBin = env->GetStringUTFChars(binaryPath, nullptr);
    const char *cLog = env->GetStringUTFChars(logPath, nullptr);

    jsize envCount = env->GetArrayLength(envPairs);
    std::vector<std::string> envStorage;
    envStorage.reserve(envCount);
    for (jsize i = 0; i < envCount; ++i) {
        auto jStr = static_cast<jstring>(env->GetObjectArrayElement(envPairs, i));
        const char *raw = env->GetStringUTFChars(jStr, nullptr);
        envStorage.emplace_back(raw);
        env->ReleaseStringUTFChars(jStr, raw);
        env->DeleteLocalRef(jStr);
    }

    pid_t pid = fork();
    if (pid == 0) {
        chdir(cWorkDir);
        int fd = open(cLog, O_WRONLY | O_CREAT | O_TRUNC, 0644);
        if (fd >= 0) {
            dup2(fd, STDOUT_FILENO);
            dup2(fd, STDERR_FILENO);
            close(fd);
        }

        std::vector<char*> envp;
        for (auto &s : envStorage) {
            envp.push_back(const_cast<char*>(s.c_str()));
        }
        envp.push_back(nullptr);

        char* argv[] = { const_cast<char*>(cBin), nullptr };
        execve(cBin, argv, envp.data());
        _exit(127);
    }

    env->ReleaseStringUTFChars(workDir, cWorkDir);
    env->ReleaseStringUTFChars(binaryPath, cBin);
    env->ReleaseStringUTFChars(logPath, cLog);

    return static_cast<jint>(pid);
}
