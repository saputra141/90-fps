cmake_minimum_required(VERSION 3.22.1)
project("s22winemu")

add_library(s22winemu SHARED
        s22_winemu_jni.cpp)

find_library(log-lib log)

target_link_libraries(s22winemu
        ${log-lib}
        dl)
