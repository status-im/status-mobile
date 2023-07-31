@echo off

:: Determine user directory path
set USER_PATH=%USERPROFILE%

:: Construct the path to emulator
set PATH_TO_EMULATOR=%USER_PATH%\AppData\Local\Android\Sdk\emulator\emulator.exe

:: Get the list of AVDs. This assumes only one AVD is present.
for /f "delims=" %%i in ('%PATH_TO_EMULATOR% -list-avds') do set AVD_NAME=%%i

:: Start the AVD
%PATH_TO_EMULATOR% -avd %AVD_NAME%

:: End of script
