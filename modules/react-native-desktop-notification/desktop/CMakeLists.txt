set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES}
                                                     \"DesktopNotification\" PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC}
                                              ${CMAKE_CURRENT_SOURCE_DIR}/desktopnotification.cpp PARENT_SCOPE)

include(${CMAKE_ROOT}/Modules/ExternalProject.cmake)

set(SN_PREFIX ${CMAKE_CURRENT_BINARY_DIR}/SnoreNotify_ep-prefix/src/SnoreNotify_ep)
if (WIN32)
  set(SnoreNotifyWindowsToast_STATIC_LIB ${SN_PREFIX}/lib${SN_LIBPATHSUFFIX}/${CMAKE_STATIC_LIBRARY_PREFIX}snore_backend_windowstoast${CMAKE_STATIC_LIBRARY_SUFFIX})

  set(SnoreNotify_LIBS ${SnoreNotifyWindowsToast_STATIC_LIB})

  set(SnoreNotify_CMAKE_ARGS -DMINGW=1 -DBUILD_osxnotificationcenter=OFF -DBUILD_sound=OFF -DBUILD_speech=OFF -DBUILD_toasty=OFF
                             -DBUILD_freedesktop_backend=OFF -DBUILD_snarl=OFF -DBUILD_growl=OFF -DBUILD_trayicon=OFF -DBUILD_pushover_backend=OFF)
endif()

if (UNIX AND NOT APPLE)
  set(SN_LIBPATHSUFFIX /${CMAKE_LIBRARY_ARCHITECTURE})
  set(SnoreNotifyFreedesktop_STATIC_LIB ${SN_PREFIX}/lib${SN_LIBPATHSUFFIX}/${CMAKE_STATIC_LIBRARY_PREFIX}snore_backend_freedesktop${CMAKE_STATIC_LIBRARY_SUFFIX})

  set(SnoreNotify_LIBS ${SnoreNotifyFreedesktop_STATIC_LIB})

  set(SnoreNotify_CMAKE_ARGS -DBUILD_osxnotificationcenter=OFF -DBUILD_sound=OFF -DBUILD_speech=OFF -DBUILD_toasty=OFF
                             -DBUILD_snarl=OFF -DBUILD_growl=OFF -DBUILD_trayicon=OFF -DBUILD_pushover_backend=OFF)
endif()

if (APPLE)
  set(SnoreNotifyOSXNotificationCenter_STATIC_LIB ${SN_PREFIX}/lib${SN_LIBPATHSUFFIX}/${CMAKE_STATIC_LIBRARY_PREFIX}snore_backend_osxnotificationcenter${CMAKE_STATIC_LIBRARY_SUFFIX})
  set(SnoreNotify_LIBS ${SnoreNotifyOSXNotificationCenter_STATIC_LIB})
  set(SnoreNotify_CMAKE_ARGS -DBUILD_sound=OFF -DBUILD_speech=OFF -DBUILD_toasty=OFF -DBUILD_snarl=OFF -DBUILD_growl=OFF
                             -DBUILD_freedesktop_backend=OFF -DBUILD_trayicon=OFF -DBUILD_pushover_backend=OFF)
endif()

set(SnoreNotify_INCLUDE_DIR ${SN_PREFIX}/include)
set(SnoreNotify_STATIC_LIB ${SN_PREFIX}/lib${SN_LIBPATHSUFFIX}/${CMAKE_STATIC_LIBRARY_PREFIX}snore-qt5${CMAKE_STATIC_LIBRARY_SUFFIX})
set(SnoreNotifyBackend_STATIC_LIB ${SN_PREFIX}/lib${SN_LIBPATHSUFFIX}/${CMAKE_STATIC_LIBRARY_PREFIX}snore_backend_snore${CMAKE_STATIC_LIBRARY_SUFFIX})
set(SnoreNotifyBackendSettings_STATIC_LIB ${SN_PREFIX}/lib${SN_LIBPATHSUFFIX}/${CMAKE_STATIC_LIBRARY_PREFIX}snore_settings_backend_snore${CMAKE_STATIC_LIBRARY_SUFFIX})
set(SnoreNotifySettings_STATIC_LIB ${SN_PREFIX}/lib${SN_LIBPATHSUFFIX}/${CMAKE_STATIC_LIBRARY_PREFIX}snoresettings-qt5${CMAKE_STATIC_LIBRARY_SUFFIX})
set(SnoreNotify_CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${SN_PREFIX} -DSNORE_STATIC=ON -DBUILD_daemon=OFF -DBUILD_settings=OFF
                           -DBUILD_snoresend=OFF ${SnoreNotify_CMAKE_ARGS})
if (CMAKE_CROSSCOMPILING)
  set(SnoreNotify_CMAKE_ARGS ${SnoreNotify_CMAKE_ARGS}
                            "-DCMAKE_C_FLAGS=${CMAKE_C_FLAGS}"
                            "-DCMAKE_CXX_FLAGS=${CMAKE_CXX_FLAGS}"
                            "-DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}"
                            "-DBUILD_SHARED_LIBS=${BUILD_SHARED_LIBS}"
                            # These are only useful if you're cross-compiling.
                            # They, however, will not hurt regardless.
                            "-DCMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME}"
                            "-DCMAKE_SYSTEM_PROCESSOR=${CMAKE_SYSTEM_PROCESSOR}"
                            "-DCMAKE_AR=${CMAKE_AR}"
                            "-DCMAKE_C_COMPILER=${CMAKE_C_COMPILER}"
                            "-DCMAKE_C_COMPILER_AR=${CMAKE_C_COMPILER_AR}"
                            "-DCMAKE_C_COMPILER_RANLIB=${CMAKE_C_COMPILER_RANLIB}"
                            "-DCMAKE_CXX_COMPILER=${CMAKE_CXX_COMPILER}"
                            "-DCMAKE_CXX_COMPILER_AR=${CMAKE_CXX_COMPILER_AR}"
                            "-DCMAKE_CXX_COMPILER_RANLIB=${CMAKE_CXX_COMPILER_RANLIB}"
                            "-DCMAKE_LINKER=${CMAKE_LINKER}"
                            "-DCMAKE_RC_COMPILER=${CMAKE_RC_COMPILER}"
                            "-DCMAKE_EXE_LINKER_FLAGS=${CMAKE_EXE_LINKER_FLAGS}"
                            "-DCMAKE_COMPILER_PREFIX=${CMAKE_COMPILER_PREFIX}"
                            "-DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}")
endif()

ExternalProject_Add(SnoreNotify_ep
  GIT_REPOSITORY https://github.com/status-im/snorenotify.git
  GIT_TAG 9d54904e4896ab6c3c3a52f97381e5948b455970
  GIT_SHALLOW TRUE
  CMAKE_ARGS ${SnoreNotify_CMAKE_ARGS}
  BUILD_BYPRODUCTS ${SnoreNotify_STATIC_LIB} ${SnoreNotify_LIBS} ${SnoreNotifyBackend_STATIC_LIB}
                   ${SnoreNotifyBackendSettings_STATIC_LIB} ${SnoreNotifySettings_STATIC_LIB}
  LOG_DOWNLOAD 1
)

set(REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS ${REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS} SnoreNotify_ep PARENT_SCOPE)

if (APPLE)
  set(SNORENOTIFY_DEPS_LIBS "-framework Cocoa")
endif()

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS}
     ${SnoreNotify_LIBS} ${SnoreNotify_STATIC_LIB} ${SnoreNotifyBackend_STATIC_LIB}
     ${SnoreNotifyBackendSettings_STATIC_LIB} ${SnoreNotifySettings_STATIC_LIB} ${SNORENOTIFY_DEPS_LIBS} PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS}
  ${SnoreNotify_INCLUDE_DIR} PARENT_SCOPE)

