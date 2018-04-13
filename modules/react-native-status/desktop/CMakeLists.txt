set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_TYPE_NAMES}
                                                     \"RCTStatus\" PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_SRC}
                                              ${CMAKE_CURRENT_SOURCE_DIR}/rctstatus.cpp PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_LIBS}
                                               "/Users/vkjr/work/projects/github/status-im-status-go/status-go/src/github.com/status-im/status-go/build/bin/libstatus.a"
                                                "-framework Foundation"
                                                "-framework CoreServices"
                                                "-framework IOKit"
                                                "-framework Security" pthread PARENT_SCOPE)

set(REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS ${REACT_NATIVE_DESKTOP_EXTERNAL_MODULES_INCLUDE_DIRS}
                                               "/Users/vkjr/work/projects/github/status-im-status-go/status-go/src/github.com/status-im/status-go/build/bin/" PARENT_SCOPE)
