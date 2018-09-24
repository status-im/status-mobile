set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR})

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES}
                                                     \"RCTStatus\" PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC}
                                              ${CMAKE_CURRENT_SOURCE_DIR}/rctstatus.cpp PARENT_SCOPE)

include(${CMAKE_ROOT}/Modules/ExternalProject.cmake)

set(STATUSGO_GIT_TAG v0.14.1-29-g2676f506)
set(StatusGo_ROOT "${CMAKE_CURRENT_BINARY_DIR}")

STRING(TOLOWER "${CMAKE_SYSTEM_NAME}" CMAKE_SYSTEM_NAME_LOWER)
set(StatusGo_Package_URL "http://status-im.ams3.digitaloceanspaces.com/status-go-${CMAKE_SYSTEM_NAME_LOWER}-${STATUSGO_GIT_TAG}.zip")

ExternalProject_Add(StatusGo
  PREFIX ${StatusGo_ROOT}
  URL ${StatusGo_Package_URL}
  CONFIGURE_COMMAND ""
  BUILD_COMMAND ""
  INSTALL_COMMAND ""
)
ExternalProject_Get_Property(StatusGo source_dir SOURCE_DIR)

set(StatusGo_INCLUDE_DIR "${SOURCE_DIR}/include")
set(StatusGo_STATIC_LIB
     "${SOURCE_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}status${CMAKE_STATIC_LIBRARY_SUFFIX}")

include_directories(${StatusGo_INCLUDE_DIR})

set(REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS ${REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS} StatusGo PARENT_SCOPE)

if (APPLE)
  set(STATUSGO_DEPS_LIBS "-framework Foundation"
                         "-framework CoreServices"
                         "-framework IOKit"
                         "-framework Security" pthread)
elseif (WIN32)
  set(STATUSGO_DEPS_LIBS -lWinMM -lWS2_32 -lsetupapi)
else()
  set(STATUSGO_DEPS_LIBS pthread)
endif()

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS}
  ${StatusGo_STATIC_LIB} ${STATUSGO_DEPS_LIBS} PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS}
  ${StatusGo_INCLUDE_DIR} PARENT_SCOPE)
