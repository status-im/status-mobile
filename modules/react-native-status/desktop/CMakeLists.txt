set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR})

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES}
                                                     \"RCTStatus\" PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC}
                                              ${CMAKE_CURRENT_SOURCE_DIR}/rctstatus.cpp PARENT_SCOPE)

if (WIN32)
  #
  # Right now we only build status-go from source for Windows, since that needs to be cross-compiled with the toolchain in Conan
  #
  include(${CMAKE_ROOT}/Modules/ExternalProject.cmake)
  include(JSONParser.cmake)

  find_package(Go REQUIRED)

  set(versionJSONFilePath "../../../status-go-version.json")
  file(READ ${versionJSONFilePath} versionJSON)
  sbeParseJson(json versionJSON)
  set(owner ${json.owner})
  if(NOT owner)
    set(owner "status-im")
    message(WARNING "Repository owner name missing from ${versionJSONFilePath}, defaulting to ${owner}")
  endif()
  set(version ${json.version})
  if(NOT version)
    message(FATAL_ERROR "Version name missing from ${versionJSONFilePath}")
  endif()
  set(commit ${json.commit-sha1})
  if(NOT commit)
    message(FATAL_ERROR "Commit SHA1 missing from ${versionJSONFilePath}")
  endif()

  if (CUSTOM_STATUSGO_BUILD_DIR_PATH)
    set(StatusGo_ROOT ${CUSTOM_STATUSGO_BUILD_DIR_PATH})
  else()
    set(StatusGo_ROOT "${CMAKE_CURRENT_BINARY_DIR}/StatusGo")
  endif()
  set(StatusGo_PREFIX "${StatusGo_ROOT}/src/github.com/${owner}")
  set(StatusGo_SOURCE_DIR  "${StatusGo_PREFIX}/status-go")
  set(StatusGo_INCLUDE_DIR "${StatusGo_SOURCE_DIR}/build/bin")
  set(StatusGo_STATIC_LIB
      "${StatusGo_SOURCE_DIR}/build/bin/${CMAKE_STATIC_LIBRARY_PREFIX}status${CMAKE_STATIC_LIBRARY_SUFFIX}")

  include_directories(${StatusGo_INCLUDE_DIR})

  set(CONFIGURE_SCRIPT build-status-go.sh)

  ExternalProject_Add(StatusGo_ep
    PREFIX ${StatusGo_PREFIX}
    SOURCE_DIR ${StatusGo_SOURCE_DIR}
    URL https://status-go.ams3.digitaloceanspaces.com/status-go-desktop-${version}.zip
        https://github.com/${owner}/status-go/archive/${commit}.zip
    BUILD_BYPRODUCTS ${StatusGo_STATIC_LIB}
    CONFIGURE_COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/${CONFIGURE_SCRIPT} ${CMAKE_SYSTEM_NAME} ${GO_ROOT_PATH} ${StatusGo_ROOT} ${StatusGo_SOURCE_DIR} ${CMAKE_C_COMPILER} ${CMAKE_CXX_COMPILER}
    BUILD_COMMAND ""
    INSTALL_COMMAND ""
  )

  set(REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS ${REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS} StatusGo_ep PARENT_SCOPE)
else (WIN32)
  # For Linux and Darwin just use the Nix build of status-go
  set(StatusGo_INCLUDE_DIR $ENV{STATUS_GO_DESKTOP_INCLUDEDIR})
  if (APPLE)
    set(StatusGo_PLATFORM "x86_64-darwin")
  else()
    set(StatusGo_PLATFORM "x86_64-linux")
  endif()
  set(StatusGo_STATIC_LIB
      "$ENV{STATUS_GO_DESKTOP_LIBDIR}/${StatusGo_PLATFORM}/${CMAKE_STATIC_LIBRARY_PREFIX}status${CMAKE_STATIC_LIBRARY_SUFFIX}")

  include_directories(${StatusGo_INCLUDE_DIR})

  message(STATUS "StatusGo_STATIC_LIB=${StatusGo_STATIC_LIB}")
  message(STATUS "StatusGo_INCLUDE_DIR=${StatusGo_INCLUDE_DIR}")
endif (WIN32)

if (APPLE)
  set(STATUSGO_DEPS_LIBS "-framework Foundation"
                         "-framework CoreServices"
                         "-framework IOKit"
                         "-framework Security" pthread)
elseif (WIN32)
  set(STATUSGO_DEPS_LIBS -lwinmm -lws2_32 -lsetupapi)
else()
  set(STATUSGO_DEPS_LIBS pthread)
endif()

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS}
  ${StatusGo_STATIC_LIB} ${STATUSGO_DEPS_LIBS} PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS}
  ${StatusGo_INCLUDE_DIR} PARENT_SCOPE)
