@rem Copyright (c) 2017-present, Status Research and Development GmbH.
@rem All rights reserved.
@rem
@rem This source code is licensed under the BSD-style license found in the
@rem LICENSE file in the root directory of this source tree. An additional grant
@rem of patent rights can be found in the PATENTS file in the same directory.

@echo off
setlocal EnableDelayedExpansion

set "option="
for %%a in (%*) do (
   if not defined option (
      set arg=%%a
      if "!arg:~0,1!" equ "-" set "option=!arg!"
   ) else (
      set "option!option!=%%a"
      set "option="
   )
)

SET option
@echo on

echo "build.bat external modules paths: "%option-e%
echo "build.bat JS bundle path: "%option-j%
echo "build.bat desktop fonts: "%option-f%
echo "build.bat cmake generator: "%option-g%

@rem Workaround
@rem rm -rf CMakeFiles CMakeCache.txt cmake_install.cmake Makefile

@rem Build project
echo %CD%
cmake -DCMAKE_BUILD_TYPE=Debug -G %option-g% -DEXTERNAL_MODULES_DIR=%option-e% -DJS_BUNDLE_PATH=%option-j% -DDESKTOP_FONTS=%option-f% . && cmake --build .
