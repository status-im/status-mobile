set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR})
find_package(Go REQUIRED)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES}
                                                     \"RCTStatus\" PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC}
                                              ${CMAKE_CURRENT_SOURCE_DIR}/rctstatus.cpp PARENT_SCOPE)

include(${CMAKE_ROOT}/Modules/ExternalProject.cmake)

if (WIN32 AND NOT CUSTOM_STATUSGO_BUILD_DIR_PATH)
  set(CUSTOM_STATUSGO_BUILD_DIR_PATH "C:/srd-build/StatusGo")
endif()
if (CUSTOM_STATUSGO_BUILD_DIR_PATH)
  set(StatusGo_ROOT ${CUSTOM_STATUSGO_BUILD_DIR_PATH})
else()
  set(StatusGo_ROOT "${CMAKE_CURRENT_BINARY_DIR}/StatusGo")
endif()
set(StatusGo_PREFIX "${StatusGo_ROOT}/src/github.com/status-im")
set(StatusGo_SOURCE_DIR  "${StatusGo_PREFIX}/status-go")
set(StatusGo_INCLUDE_DIR "${StatusGo_SOURCE_DIR}/build/bin")
set(StatusGo_STATIC_LIB
     "${StatusGo_SOURCE_DIR}/build/bin/${CMAKE_STATIC_LIBRARY_PREFIX}status${CMAKE_STATIC_LIBRARY_SUFFIX}")

include_directories(${StatusGo_INCLUDE_DIR})

if (WIN32)
  set(CONFIGURE_SCRIPT build-status-go.bat)
else()
  set(CONFIGURE_SCRIPT build-status-go.sh)
endif()

ExternalProject_Add(StatusGo_ep
  PREFIX ${StatusGo_PREFIX}
  SOURCE_DIR ${StatusGo_SOURCE_DIR}
  GIT_REPOSITORY https://github.com/status-im/status-go.git
  GIT_TAG develop-g19b53030
  BUILD_BYPRODUCTS ${StatusGo_STATIC_LIB}
  CONFIGURE_COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/${CONFIGURE_SCRIPT} ${GO_ROOT_PATH} ${StatusGo_ROOT} ${StatusGo_SOURCE_DIR}
  BUILD_COMMAND ""
  INSTALL_COMMAND ""
)

set(REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS ${REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS} StatusGo_ep PARENT_SCOPE)

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
