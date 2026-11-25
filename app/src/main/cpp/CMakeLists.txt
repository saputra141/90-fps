cmake_minimum_required(VERSION 3.22.1)

project(s22winemu LANGUAGES CXX)

add_library(
    s22winemu
    SHARED
    s22_winemu_jni.cpp
)

find_library(log-lib log)
find_library(android-lib android)
find_library(vulkan-lib vulkan)
find_library(dl-lib dl)

target_compile_features(s22winemu PRIVATE cxx_std_17)
target_compile_options(s22winemu PRIVATE -Wall -Wextra -Wno-unused-parameter)

target_link_libraries(
    s22winemu
    ${log-lib}
    ${android-lib}
    ${vulkan-lib}
    ${dl-lib}
)
