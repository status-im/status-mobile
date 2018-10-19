set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES}
                                                     \"DesktopNotification\" PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC}
                                              ${CMAKE_CURRENT_SOURCE_DIR}/desktopnotification.cpp PARENT_SCOPE)

include(${CMAKE_ROOT}/Modules/ExternalProject.cmake)

set(SN_PREFIX ${CMAKE_CURRENT_BINARY_DIR}/SnoreNotify_ep-prefix/src/SnoreNotify_ep)
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


ExternalProject_Add(SnoreNotify_ep
  GIT_REPOSITORY https://github.com/status-im/snorenotify.git
  CMAKE_ARGS ${SnoreNotify_CMAKE_ARGS}
  BUILD_BYPRODUCTS ${SnoreNotify_STATIC_LIB} ${SnoreNotify_LIBS} ${SnoreNotifyBackend_STATIC_LIB}
                    ${SnoreNotifyBackendSettings_STATIC_LIB} ${SnoreNotifySettings_STATIC_LIB}
  LOG_DOWNLOAD 1
)

set(REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS ${REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS} SnoreNotify_ep PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS}
     ${SnoreNotify_LIBS} ${SnoreNotify_STATIC_LIB} ${SnoreNotifyBackend_STATIC_LIB}
     ${SnoreNotifyBackendSettings_STATIC_LIB} ${SnoreNotifySettings_STATIC_LIB} PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS}
  ${SnoreNotify_INCLUDE_DIR} PARENT_SCOPE)

