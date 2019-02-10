macro(import_qt_modules)
  set(REQUIRED_QT_VERSION "5.9.1")

  set(QTCONFIGROOT ${QTROOT}/lib/cmake/Qt5)

  foreach(COMP ${USED_QT_MODULES})
    set(mod Qt5${COMP})

    # look for the config files in the QtConfigRoot defined above
    set(${mod}_DIR ${QTCONFIGROOT}${COMP})

    # look for the actual package
    find_package(${mod} ${REQUIRED_QT_VERSION} REQUIRED)

    #message("${mod}_INCLUDE_DIRS: include_directories(${${mod}_INCLUDE_DIRS})")
    include_directories(${${mod}_INCLUDE_DIRS})
    if (${COMP} STREQUAL "Quick")
      # We need to include the private headers for QZipWriter. If in the future we can't use that class anymore, we can always resort to the QuaZIP OSS library
      include_directories(${${mod}_PRIVATE_INCLUDE_DIRS})
    endif()

    list(APPEND QT5_LIBRARIES ${${mod}_LIBRARIES})
    list(APPEND QT5_CFLAGS ${${mod}_EXECUTABLE_COMPILE_FLAGS})
  endforeach(COMP ${USED_QT_MODULES})
endmacro(import_qt_modules)

if(WIN32)
  # Download automatically, you can also just copy the conan.cmake file
  # TODO: Create packages of qt5 for Linux and MacOS too, so that we can rely strictly on this branch of code 
  if(NOT EXISTS "${CMAKE_BINARY_DIR}/conan.cmake")
      message(STATUS "Downloading conan.cmake from https://github.com/conan-io/cmake-conan")
      file(DOWNLOAD "https://raw.githubusercontent.com/conan-io/cmake-conan/9cc97acda619b7917f140415241785a864482b11/conan.cmake"
                    "${CMAKE_BINARY_DIR}/conan.cmake")
  endif()

  include(${CMAKE_BINARY_DIR}/conan.cmake)

  conan_check()

  if(USE_QTWEBKIT)
    set(_QT_PACKAGE_OPTIONS "qt5-mxe:webkit=True")
  endif()
  conan_cmake_run(REQUIRES qt5-mxe/5.11.2@status-im/stable
                  PROFILE ../node_modules/status-conan/profiles/status-mingw32-x86_64
                  SETTINGS "qt5-mxe:os=Windows" "qt5-mxe:arch=x86_64"
                  OPTIONS ${_QT_PACKAGE_OPTIONS}
                  BUILD never)

  set(QTROOT "${CONAN_QT5-MXE_ROOT}")
else(WIN32)
  set(QTROOT "$ENV{QT_PATH}")
endif(WIN32)

if(NOT EXISTS ${QTROOT}/bin/qt.conf)
  if(EXISTS ${QTROOT}/gcc_64/bin/qt.conf)
    set(QTROOT "${QTROOT}/gcc_64")
  elseif(EXISTS ${QTROOT}/clang_64/bin/qt.conf)
    set(QTROOT "${QTROOT}/clang_64")
  else()
    message(FATAL_ERROR "Could not find qt.conf in ${QTROOT}/bin nor in ${QTROOT}/clang_64/bin nor in ${QTROOT}/gcc_64/bin. Is QTROOT correctly defined?")
  endif()
endif()

if(WIN32)
  set(WINARCHSTR ARCHSTR windows-x86_64)
endif(WIN32)

message(STATUS "Qt root directory: ${QTROOT}")

list(APPEND CMAKE_FIND_ROOT_PATH ${QTROOT})
list(APPEND CMAKE_PREFIX_PATH ${QTROOT})
include_directories(${QTROOT}/include)

import_qt_modules()

if(QT5_CFLAGS)
  list(REMOVE_DUPLICATES QT5_CFLAGS)
  if(WIN32)
    list(REMOVE_ITEM QT5_CFLAGS -fPIC)
  endif(WIN32)
endif(QT5_CFLAGS)

message(STATUS "Qt version: ${Qt5Core_VERSION_STRING}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${QT5_CFLAGS}")

set(CMAKE_REQUIRED_LIBRARIES ${QT5_LIBRARIES})
