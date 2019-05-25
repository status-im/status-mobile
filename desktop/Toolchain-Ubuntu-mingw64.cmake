# Toolchain file for building for Windows from an Ubuntu Linux system.
#
# Typical usage:
#    *) install cross compiler: `sudo apt-get install mingw-w64 g++-mingw-w64`
#    *) cmake -DCMAKE_TOOLCHAIN_FILE=~/Toolchain-Ubuntu-mingw64.cmake ..

message(STATUS "Cross-compiling for Windows")

set(CMAKE_SYSTEM_NAME Windows)
if(NOT "${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Linux")
  message(FATAL_ERROR "Can only cross-compile to Windows from Linux")
endif()

set(USE_QTWEBKIT OFF)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/toolchain/")
include(conanbuildinfo)
#conan_basic_setup()
conan_global_flags()
