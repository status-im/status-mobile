@echo off

:: Determine user directory path
set USER_PATH=%USERPROFILE%

:: Construct the path to adb based on typical Android SDK installation
set ADB_PATH=%USER_PATH%\AppData\Local\Android\Sdk\platform-tools\adb.exe

:: Check if adb exists at the constructed path
if not exist "%ADB_PATH%" (
    echo "adb not found. Please ensure adb is installed."
    exit /b
)

:: Run the adb commands
%ADB_PATH% kill-server
%ADB_PATH% -a nodaemon server start

:: End of script
