cmake_minimum_required(VERSION 3.22.1)
project("ultraemulator")

add_library(ultraemulator SHARED
    native-lib.cpp
    gb_core.cpp
)

find_library(log-lib log)
find_library(jnigraphics jnigraphics)

target_link_libraries(ultraemulator
    ${log-lib}
    ${jnigraphics}
    android
)
