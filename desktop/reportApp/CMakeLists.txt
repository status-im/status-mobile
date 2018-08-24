# Copyright (c) 2017-present, Status Research and Development GmbH.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_INCLUDE_CURRENT_DIR ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c11")

set(APP_NAME "reportApp")

find_package(Qt5Core REQUIRED)
find_package(Qt5Qml REQUIRED)
find_package(Qt5Quick REQUIRED)
find_package(Qt5WebSockets REQUIRED)
find_package(Qt5Svg REQUIRED)

set(MAIN_CPP_SOURCE reportpublisher.cpp
                    reportpublisher.cpp
                    main.cpp)

add_executable(
  ${APP_NAME}
  ${MAIN_CPP_SOURCE}
  main.qrc
)

set(USED_QT_MODULES Core Qml Quick WebSockets Svg)

qt5_use_modules(${APP_NAME} ${USED_QT_MODULES})

set(REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS ${REACT_NATIVE_DESKTOP_EXTERNAL_PROJECT_DEPS} reportApp PARENT_SCOPE)
