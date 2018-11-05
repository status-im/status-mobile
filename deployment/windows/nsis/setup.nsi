;--------------------------------
; Build environment
;--------------------------------
 
  ;Unicode true
  ;!define top_srcdir @top_srcdir@
  ;!define srcdir @srcdir@
  !define VERSION       ${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_BUILD}
  !define VERSION_FULL  ${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_BUILD}.0
  ;!define PUBLISHER     "Status.im"
  ;!define WEBSITE_URL   "https://status.im/"
  !define               AppUserModelId              "StatusIm.Status.Desktop.1" ; app.id must match ID in modules/react-native-desktop-notification/desktop/desktopnotification.cpp
  !define               SnoreToastExe               "$INSTDIR\SnoreToast.exe"
  !define               AppLinkFileName             "Status.lnk"
  !define               AppExeName                  "Status.exe"
  !define               UninstallExeName            "Uninstall.exe"
  !define               UninstallLinkName           "Uninstall.lnk"
  !define               NodeJsServerExeName         "ubuntu-server.exe"
  !define               SetupExeFileName            "Status-x86_64-setup.exe"
  !define               OrgRegistryKeyPath          "Software\${PUBLISHER}"
  !define               AppRegistryKeyPath          "${OrgRegistryKeyPath}\Status Desktop"
  !define               UninstallRegKeyPath         "Software\Microsoft\Windows\CurrentVersion\Uninstall\Status Desktop"

  !addplugindir         plugins/x86-ansi
 
;--------------------------------
;General
;--------------------------------
 
  ;Name and file
  Name    "Status Desktop ${VERSION}"
  OutFile "${top_srcdir}/${SetupExeFileName}"
  
  SetCompressor /FINAL ${COMPRESSION_TYPE} ${COMPRESSION_ALGO}

  ; Default installation folder
  InstallDir "$PROGRAMFILES64\${PUBLISHER}"

  ; Get installation folder from registry if available
  InstallDirRegKey HKLM "${AppRegistryKeyPath}" ""

  RequestExecutionLevel user

  !define DUMP_KEY "SOFTWARE\Microsoft\Windows\Windows Error Reporting\LocalDumps"

;--------------------------------
; Include Modern UI and functions
;--------------------------------

  !include "MUI2.nsh"
  !include "WordFunc.nsh"
  !include "Library.nsh"
  !include "WinVer.nsh"
  !include "FileFunc.nsh"
;  !include "Memento.nsh"
  !include "StrFunc.nsh"
  !include "include/UAC.nsh"
  !include "include/SnoreNotify.nsh"
  !include "include/nsProcess.nsh"

  ${StrRep}

;--------------------------------
; Installer's VersionInfo
;--------------------------------

  VIProductVersion                   "${VERSION_FULL}"
  VIAddVersionKey "CompanyName"      "${PUBLISHER}"
  VIAddVersionKey "ProductName"      "Status Desktop"
  VIAddVersionKey "ProductVersion"   "${VERSION}"
  VIAddVersionKey "FileDescription"  "Status Desktop Client"
  VIAddVersionKey "FileVersion"      "${VERSION}"
  VIAddVersionKey "LegalCopyright"   "${PUBLISHER}"
  VIAddVersionKey "OriginalFilename" "${SetupExeFileName}"

;--------------------------------
; Required functions
;--------------------------------

  !insertmacro GetParameters
  !insertmacro GetOptions
  !insertmacro un.GetParameters
  !insertmacro un.GetOptions

;--------------------------------
;Variables
;--------------------------------
 
  Var MUI_TEMP
  Var STARTMENU_FOLDER
  Var PREVIOUS_INSTALLDIR
  Var PREVIOUS_VERSION
  Var PREVIOUS_VERSION_STATE
  Var REINSTALL_UNINSTALL
  Var ALL_USERS_DEFAULT
  Var ALL_USERS
  Var IS_ADMIN
  Var USERNAME
  Var PERFORM_UPDATE
  Var SKIPLICENSE
  Var SKIPUAC
  Var GetInstalledSize.total
  Var OldRunDir
  Var CommandLine
  Var Quiet

;--------------------------------
;Interface Settings
;--------------------------------
 
  !define MUI_ICON "${top_srcdir}/deployment/windows/status.ico"
  ;!define MUI_UNICON "${srcdir}/uninstall.ico"
 
  !define MUI_ABORTWARNING

;--------------------------------
;Memento settings
;--------------------------------
 
;!define MEMENTO_REGISTRY_ROOT SHELL_CONTEXT
;!define MEMENTO_REGISTRY_KEY "${AppRegistryKeyPath}"

;--------------------------------
;Pages

  !define MUI_PAGE_CUSTOMFUNCTION_PRE PageDirectoryPre
  !insertmacro MUI_PAGE_DIRECTORY

  ; Start Menu Folder Page Configuration
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "SHCTX"
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "${AppRegistryKeyPath}"
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Startmenu"
  !define MUI_STARTMENUPAGE_DEFAULTFOLDER "Status Desktop"

  !define MUI_PAGE_CUSTOMFUNCTION_PRE PageStartmenuPre
  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER

  !define MUI_PAGE_CUSTOMFUNCTION_LEAVE PostInstPage
  !insertmacro MUI_PAGE_INSTFILES

  !define MUI_PAGE_CUSTOMFUNCTION_PRE un.ConfirmPagePre
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !define MUI_PAGE_CUSTOMFUNCTION_PRE un.FinishPagePre
  !insertmacro MUI_UNPAGE_FINISH

Function GetUserInfo
  ClearErrors
  UserInfo::GetName
  ${If} ${Errors}
    StrCpy $IS_ADMIN 1
    Return
  ${EndIf}
  Pop $USERNAME

  ${If} ${UAC_IsAdmin}
    StrCpy $IS_ADMIN 1
  ${Else}
    StrCpy $IS_ADMIN 0
  ${EndIf}
 
FunctionEnd

Function UpdateShellVarContext
 
  ${If} $ALL_USERS == 1
    SetShellVarContext all
    DetailPrint "Installing for all users"
  ${Else}
    SetShellVarContext current
    DetailPrint "Installing for current user"
  ${EndIf}

FunctionEnd

!Macro MessageBoxImpl options text sdreturn checks
  ${If} $Quiet == 1
    MessageBox ${options} `${text}` ${checks}
  ${Else}
    MessageBox ${options} `${text}` /SD ${sdreturn} ${checks}
  ${Endif}

!MacroEnd

!define MessageBox `!insertmacro MessageBoxImpl`

Function ReadAllUsersCommandline

  ${GetOptions} $CommandLine "/user" $R1

  ${Unless} ${Errors}
    ${If} $R1 == "current"
    ${OrIf} $R1 == "=current"
      StrCpy $ALL_USERS 0
    ${ElseIf} $R1 == "all"
    ${OrIf} $R1 == "=all"
      StrCpy $ALL_USERS 1
    ${Else}
      ${MessageBox} MB_ICONSTOP "Invalid option for /user. Has to be either /user=all or /user=current" IDOK ''
      Abort
    ${EndIf}
  ${EndUnless}
  Call UpdateShellVarContext

FunctionEnd

Function CheckPrevInstallDirExists

  ${If} $PREVIOUS_INSTALLDIR != ""

    ; Make sure directory is valid
    Push $R0
    Push $R1
    StrCpy $R0 "$PREVIOUS_INSTALLDIR" "" -1
    ${If} $R0 == '\'
    ${OrIf} $R0 == '/'
      StrCpy $R0 $PREVIOUS_INSTALLDIR*.*
    ${Else}
      StrCpy $R0 $PREVIOUS_INSTALLDIR\*.*
    ${EndIf}
    ${IfNot} ${FileExists} $R0
      StrCpy $PREVIOUS_INSTALLDIR ""
    ${EndIf}
    Pop $R1
    Pop $R0

  ${EndIf}

FunctionEnd

Function ReadPreviousVersion

  ReadRegStr $PREVIOUS_INSTALLDIR HKLM "${AppRegistryKeyPath}" ""

  Call CheckPrevInstallDirExists

  ${If} $PREVIOUS_INSTALLDIR != ""
    ; Detect version
    ReadRegStr $PREVIOUS_VERSION HKLM "${AppRegistryKeyPath}" "Version"
    ${If} $PREVIOUS_VERSION != ""
      StrCpy $ALL_USERS 1
      SetShellVarContext all
      return
    ${EndIf}
  ${EndIf}

  ReadRegStr $PREVIOUS_INSTALLDIR HKCU "${AppRegistryKeyPath}" ""

  Call CheckPrevInstallDirExists

  ${If} $PREVIOUS_INSTALLDIR != ""
    ; Detect version
    ReadRegStr $PREVIOUS_VERSION HKCU "${AppRegistryKeyPath}" "Version"
    ${If} $PREVIOUS_VERSION != ""
      StrCpy $ALL_USERS 0
      SetShellVarContext current
      return
    ${EndIf}
  ${EndIf}

FunctionEnd

Function LoadPreviousSettings
 
  ; Component selection
  ;${MementoSectionRestore}
 
  ; Startmenu
  !define ID "Application"
 
  !ifdef MUI_STARTMENUPAGE_${ID}_REGISTRY_ROOT & MUI_STARTMENUPAGE_${ID}_REGISTRY_KEY & MUI_STARTMENUPAGE_${ID}_REGISTRY_VALUENAME
 
    ReadRegStr $mui.StartMenuPage.RegistryLocation "${MUI_STARTMENUPAGE_${ID}_REGISTRY_ROOT}" "${MUI_STARTMENUPAGE_${ID}_REGISTRY_KEY}" "${MUI_STARTMENUPAGE_${ID}_REGISTRY_VALUENAME}"
 
    ${if} $mui.StartMenuPage.RegistryLocation != ""
      StrCpy "$STARTMENU_FOLDER" $mui.StartMenuPage.RegistryLocation
    ${else}
      StrCpy "$STARTMENU_FOLDER" ""
    ${endif}
 
    !undef ID
 
  !endif
 
  ${If} $PREVIOUS_INSTALLDIR != ""
    StrCpy $INSTDIR $PREVIOUS_INSTALLDIR
  ${EndIf}
 
FunctionEnd

Function ReadUpdateCommandline

  ${GetOptions} $CommandLine "/update" $R1

  ${If} ${Errors}
    StrCpy $PERFORM_UPDATE 0
  ${Else}
    StrCpy $PERFORM_UPDATE 1
  ${EndIf}

FunctionEnd

Function ReadSkipLicense

  ${GetOptions} $CommandLine "/skiplicense" $R1

  ${If} ${Errors}
    StrCpy $SKIPLICENSE 0
  ${Else}
    StrCpy $SKIPLICENSE 1
  ${EndIf}

FunctionEnd

Function ReadSkipUAC

  ${GetOptions} $CommandLine "/skipuac" $R1

  ${If} ${Errors}
    StrCpy $SKIPUAC 0
  ${Else}
    StrCpy $SKIPUAC 1
  ${EndIf}

FunctionEnd

Function ReadQuiet

  ${GetOptions} $CommandLine "/quiet" $R1

  ${If} ${Errors}
    StrCpy $Quiet 0
  ${Else}
    StrCpy $Quiet 1
    SetSilent silent
  ${EndIf}

FunctionEnd

Function .onInit
 
  Pop $OldRunDir

  ; Store command line
  ${GetParameters} $CommandLine

  Call ReadQuiet

  ${Unless} ${AtLeastWin7}
    ${MessageBox} MB_YESNO|MB_ICONSTOP "Unsupported operating system.$\nStatus Desktop ${VERSION} requires at least Windows 7 and may not work correctly on your system.$\nDo you really want to continue with the installation?" IDNO 'IDYES installonoldwindows'
    Abort
installonoldwindows:
  ${EndUnless}
  ${Unless} ${RunningX64}
    ${MessageBox} MB_OK|MB_ICONSTOP "Unsupported operating system.$\nThis is the installer for the 64bit version of Status Desktop ${VERSION} and does not run on your operating system which is only 32bit." IDOK ''
    Abort
  ${EndUnless}

  Call ReadSkipUAC

  ${If} $SKIPUAC != 1
uac_tryagain:
    !insertmacro UAC_RunElevated

    ${Switch} $0
    ${Case} 0
      ${IfThen} $1 = 1 ${|} Quit ${|} ;we are the outer process, the inner process has done its work, we are done.
      ${IfThen} $3 <> 0 ${|} ${Break} ${|} ;we are admin, let the show go on
      ${If} $2 = 3 ;RunAs completed successfully, but with a non-admin user
        MessageBox MB_YESNO|MB_ICONEXCLAMATION|MB_TOPMOST|MB_SETFOREGROUND "Status Desktop setup requires admin privileges, try again" /SD IDNO IDYES uac_tryagain IDNO 0
      ${EndIf}
      ;fall-through and die
    ${Case} 1223
      ; User aborted elevation, continue regardless
      ${Break}
    ${Default}
      ${MessageBox} mb_iconstop "Could not elevate process (errorcode $0), continuing with normal user privileges." IDOK ''
      ${Break}
    ${EndSwitch}

    ; The UAC plugin changes the error level even in the inner process, reset it.
    SetErrorLevel -1
  ${EndIf}
 
 ; /update argument
  Call ReadUpdateCommandline

  Call ReadSkipLicense

  Call GetUserInfo
 
  ; Initialize $ALL_USERS with default value
  ${If} $IS_ADMIN == 1
    StrCpy $ALL_USERS 1
  ${Else}
    StrCpy $ALL_USERS 0
  ${EndIf}
  Call UpdateShellVarContext
 
  ; See if previous version exists
  ; This can change ALL_USERS
  Call ReadPreviousVersion
 
  ${If} $PREVIOUS_VERSION != ""
    StrCpy $REINSTALL_UNINSTALL 1
  ${EndIf}

  ; Load _all_ previous settings.
  ; Need to do it now as up to now, $ALL_USERS was possibly reflecting a
  ; previous installation. After this call, $ALL_USERS reflects the requested
  ; installation mode for this installation.
  Call LoadPreviousSettings

  Call ReadAllUsersCommandline

  ${If} $ALL_USERS == 1
    ${If} $IS_ADMIN == 0

      ${If} $PREVIOUS_VERSION != ""
        ${MessageBox} MB_ICONSTOP "Status Desktop has been previously installed for all users.$\nPlease restart the installer with Administrator privileges." IDOK ''
        Abort
      ${Else}
        ${MessageBox} MB_ICONSTOP "Cannot install for all users.$\nPlease restart the installer with Administrator privileges." IDOK ''
        Abort
      ${EndIf}
    ${EndIf}
  ${EndIf}

  ${If} $PREVIOUS_VERSION == ""

    StrCpy $PERFORM_UPDATE 0
    DetailPrint "No previous version of Status Desktop was found"

  ${Else}

    Push "${VERSION}"
    Push $PREVIOUS_VERSION
    Call StatusVersionCompare

    DetailPrint "Found previous version: $PREVIOUS_VERSION"
    DetailPrint "Installing $PREVIOUS_VERSION_STATE version ${VERSION}"

  ${EndIf}

  StrCpy $ALL_USERS_DEFAULT $ALL_USERS
 
FunctionEnd

Function StatusVersionCompare
 
  Exch $1
  Exch
  Exch $0
 
  Push $2
  Push $3
  Push $4
 
versioncomparebegin:
  ${If} $0 == ""
  ${AndIf} $1 == ""
    StrCpy $PREVIOUS_VERSION_STATE "same"
    goto versioncomparedone
  ${EndIf}
 
  StrCpy $2 0
  StrCpy $3 0
 
  ; Parse rc / beta suffixes for segments
  StrCpy $4 $0 2
  ${If} $4 == "rc"
    StrCpy $2 100
    StrCpy $0 $0 "" 2
  ${Else}
    StrCpy $4 $0 4
    ${If} $4 == "beta"
      StrCpy $0 $0 "" 4
    ${Else}
      StrCpy $2 10000
    ${EndIf}
  ${EndIf}
 
  StrCpy $4 $1 2
  ${If} $4 == "rc"
    StrCpy $3 100
    StrCpy $1 $1 "" 2
  ${Else}
    StrCpy $4 $1 4
    ${If} $4 == "beta"
      StrCpy $1 $1 "" 4
    ${Else}
      StrCpy $3 10000
    ${EndIf}
  ${EndIf}
 
split1loop:
 
  StrCmp $0 "" split1loopdone
  StrCpy $4 $0 1
  StrCpy $0 $0 "" 1
  StrCmp $4 "." split1loopdone
  StrCmp $4 "-" split1loopdone
  StrCpy $2 $2$4
  goto split1loop
split1loopdone:
 
split2loop:
 
  StrCmp $1 "" split2loopdone
  StrCpy $4 $1 1
  StrCpy $1 $1 "" 1
  StrCmp $4 "." split2loopdone
  StrCmp $4 "-" split2loopdone
  StrCpy $3 $3$4
  goto split2loop
split2loopdone:
 
  ${If} $2 > $3
    StrCpy $PREVIOUS_VERSION_STATE "newer"
  ${ElseIf} $3 > $2
    StrCpy $PREVIOUS_VERSION_STATE "older"
  ${Else}
    goto versioncomparebegin
  ${EndIf}
 
 
versioncomparedone:
 
  Pop $4
  Pop $3
  Pop $2
  Pop $1
  Pop $0
 
FunctionEnd

Function PageDirectoryPre

  ${If} $PERFORM_UPDATE == 1
    Abort
  ${EndIf}

  ${If} $REINSTALL_UNINSTALL == "1"
  ${AndIf} $PREVIOUS_VERSION_STATE != "same"

    Abort

  ${EndIf}

FunctionEnd

Function PageStartmenuPre

  ${If} $PERFORM_UPDATE == 1
    Abort
  ${EndIf}

  ${If} $REINSTALL_UNINSTALL == "1"
  ${AndIf} $PREVIOUS_VERSION_STATE != "same"

    ${If} "$STARTMENU_FOLDER" == ""

      StrCpy "$STARTMENU_FOLDER" ">"

    ${EndIf}

    Abort

  ${EndIf}

FunctionEnd

Function .OnInstFailed
FunctionEnd
 
Function .onInstSuccess
 
  ;${MementoSectionSave}
 

  ; Detect multiple install directories
  ${If} $OldRunDir != ''
    ${GetFileVersion} $OldRunDir $R0
    ${GetFileVersion} "$INSTDIR\${AppExeName}" $R1

    StrCpy $R2 $OldRunDir -14

    ${If} $R0 != ''
    ${AndIf} $R1 != ''
    ${AndIf} $R0 != $R1
      ${MessageBox} MB_ICONEXCLAMATION 'Multiple installations of Status Desktop detected.$\n$\nStatus Desktop ${VERSION} has been installed to "$InstDir".$\nAn old installation of Status Desktop $R0 still exists in the "$R2" directory.$\n$\nPlease delete the old version in the "$R2" directory.' IDOK ''
    ${EndIf}
  ${EndIf}

FunctionEnd

;--------------------------------
; Languages
;--------------------------------

  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections
;--------------------------------

Section "Status Desktop" SecMain

  ;SectionIn 1 RO

  ${nsProcess::FindProcess} "${NodeJsServerExeName}" $R0

  ${If} $R0 == 0
    DetailPrint "${NodeJsServerExeName} is running. Closing it down"
    ${nsProcess::CloseProcess} "${NodeJsServerExeName}" $R0
    DetailPrint "Waiting for ${NodeJsServerExeName} to close"
    Sleep 2000  
  ${Else}
    DetailPrint "${NodeJsServerExeName} was not found to be running"        
  ${EndIf}    

  ${nsProcess::Unload}

  SetOutPath "$INSTDIR"

  File "${top_srcdir}\.env"
  File "${top_srcdir}\node_modules\node-notifier\vendor\snoreToast\SnoreToast.exe"
  File /r "${top_srcdir}\desktop\bin\"
  File /r "${top_srcdir}\StatusImPackage\Windows\"

  SetOutPath "$INSTDIR\notifier"
  File "${top_srcdir}\node_modules\node-notifier\vendor\notifu\*.exe"

  SetOutPath "$INSTDIR\assets\resources\fonts"
  File /r "${top_srcdir}\resources\fonts\"

  SetOutPath "$INSTDIR\assets\resources\icons"
  File /r "${top_srcdir}\resources\icons\"

  SetOutPath "$INSTDIR\assets\resources\images"
  File /r "${top_srcdir}\resources\images\"

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\${UninstallExeName}"

  WriteRegStr SHCTX "${AppRegistryKeyPath}" "" $INSTDIR
  WriteRegStr SHCTX "${AppRegistryKeyPath}" "Version" "${VERSION}"

  WriteRegDWORD SHCTX "${AppRegistryKeyPath}" "Updated" $PERFORM_UPDATE

  ${StrRep} $R0 "$INSTDIR\${UninstallExeName}" '"' '""'
  WriteRegExpandStr SHCTX "${UninstallRegKeyPath}" "UninstallString" '"$R0"'
  WriteRegExpandStr SHCTX "${UninstallRegKeyPath}" "InstallLocation" "$INSTDIR"
  WriteRegStr       SHCTX "${UninstallRegKeyPath}" "DisplayName"     "Status Desktop ${VERSION}"
  WriteRegStr       SHCTX "${UninstallRegKeyPath}" "DisplayIcon"     "$INSTDIR\${AppExeName}"
  WriteRegStr       SHCTX "${UninstallRegKeyPath}" "DisplayVersion"  "${VERSION}"
  WriteRegStr       SHCTX "${UninstallRegKeyPath}" "URLInfoAbout"    "${WEBSITE_URL}"
  WriteRegStr       SHCTX "${UninstallRegKeyPath}" "URLUpdateInfo"   "${WEBSITE_URL}"
  WriteRegStr       SHCTX "${UninstallRegKeyPath}" "HelpLink"        "${WEBSITE_URL}"
  WriteRegStr       SHCTX "${UninstallRegKeyPath}" "Publisher"       "${PUBLISHER}"
  WriteRegDWORD     SHCTX "${UninstallRegKeyPath}" "VersionMajor"    "${VERSION_MAJOR}"
  WriteRegDWORD     SHCTX "${UninstallRegKeyPath}" "VersionMinor"    "${VERSION_MINOR}"
  WriteRegDWORD     SHCTX "${UninstallRegKeyPath}" "NoModify"        "1"
  WriteRegDWORD     SHCTX "${UninstallRegKeyPath}" "NoRepair"        "1"

  Call GetInstalledSize
  WriteRegDWORD SHCTX "${UninstallRegKeyPath}" "EstimatedSize" "$GetInstalledSize.total" ; Create/Write the reg key with the dword value

  ;Add applications to the firewall exception list - All Networks - All IP Version - Enabled
  SimpleFC::IsApplicationAdded "$INSTDIR\${AppExeName}"
  Pop $0 ; return error(1)/success(0)
  ${if} $0 == "0"
    Pop $1 ; return 1=Added/0=Not added
    ${if} $1 == "0"
      ; SimpleFC::AddApplication [name] [path] [scope] [ip_version] [remote_addresses] [status]
      SimpleFC::AddApplication "Status Desktop Ethereum Node"   "$INSTDIR\${AppExeName}" 0 2 "" 1
      SimpleFC::AddApplication "Status Desktop Node.js Server"  "$INSTDIR\${NodeJsServerExeName}" 1 2 "" 1
    ${endif}
  ${endif}

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application

  ;Create shortcuts
  SetOutPath      "$INSTDIR"
  CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
  CreateShortCut  "$SMPROGRAMS\$STARTMENU_FOLDER\${UninstallLinkName}" "$INSTDIR\${UninstallExeName}"

  ;CreateShortCut  "$SMPROGRAMS\$STARTMENU_FOLDER\${AppLinkFileName}" "$INSTDIR\${AppExeName}"
  !insertmacro SnoreShortcut "$SMPROGRAMS\$STARTMENU_FOLDER\${AppLinkFileName}" "$INSTDIR\${AppExeName}" "${AppUserModelId}"

  !insertmacro MUI_STARTMENU_WRITE_END

  ; Push $R0
  ; StrCpy $R0 "$STARTMENU_FOLDER" 1
  ; ${if} $R0 == ">"
  ;   ;Write folder to registry
  ;   WriteRegStr "${MUI_STARTMENUPAGE_Application_REGISTRY_ROOT}" "${MUI_STARTMENUPAGE_Application_REGISTRY_KEY}" "${MUI_STARTMENUPAGE_Application_REGISTRY_VALUENAME}" ">"
  ; ${endif}
  ; Pop $R0

  ${If} $ALL_USERS == 1
    ; Enable mini dumps
    ${If} ${RunningX64}
      SetRegView 64
    ${EndIf}
    WriteRegDWORD HKLM "${DUMP_KEY}\${AppExeName}"           "DumpType" "1"
    WriteRegDWORD HKLM "${DUMP_KEY}\${NodeJsServerExeName}"  "DumpType" "1"
    ${If} ${RunningX64}
      SetRegView lastused
    ${EndIf}
  ${EndIf}

  ; Register App Path so that typing status in Win+R dialog starts Status Desktop
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\App Paths\${AppExeName}" "" "$INSTDIR\${AppExeName}"
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\App Paths\${AppExeName}" "Path" "$INSTDIR"

SectionEnd
 
;--------------------------------
; Functions
;--------------------------------

Function PostInstPage

  ; Don't advance automatically if details expanded
  FindWindow $R0 "#32770" "" $HWNDPARENT
  GetDlgItem $R0 $R0 1016
  System::Call user32::IsWindowVisible(i$R0)i.s
  Pop $R0

  ${If} $R0 != 0
    SetAutoClose false
  ${EndIf}

FunctionEnd

Function GetInstalledSize
  Push $0
  Push $1
  StrCpy $GetInstalledSize.total 0
  ${ForEach} $1 0 256 + 1
    ${if} ${SectionIsSelected} $1
      SectionGetSize $1 $0
      IntOp $GetInstalledSize.total $GetInstalledSize.total + $0
    ${Endif}
  ${Next}
  Pop $1
  Pop $0
  IntFmt $GetInstalledSize.total "0x%08X" $GetInstalledSize.total
  Push $GetInstalledSize.total
FunctionEnd

;--------------------------------
; Descriptions
;--------------------------------

  ; Language strings
  LangString DESC_SecMain ${LANG_ENGLISH} "Required program files."
  ;LangString DESC_SecDesktop ${LANG_ENGLISH} "Create desktop icon for FileZilla"

  ; Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMain} $(DESC_SecMain)
    ;!insertmacro MUI_DESCRIPTION_TEXT ${SecDesktop} $(DESC_SecDesktop)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
; Uninstaller Variables
;--------------------------------
 
Var un.REMOVE_ALL_USERS
Var un.REMOVE_CURRENT_USER
 
;--------------------------------
;Uninstaller Functions
;--------------------------------
 
Function un.GetUserInfo
  ClearErrors
  UserInfo::GetName
  ${If} ${Errors}
    StrCpy $IS_ADMIN 1
    Return
  ${EndIf}
  Pop $USERNAME

  ${If} ${UAC_IsAdmin}
    StrCpy $IS_ADMIN 1
  ${Else}
    StrCpy $IS_ADMIN 0
  ${EndIf}
 
FunctionEnd
 
Function un.ReadPreviousVersion
 
  ReadRegStr $R0 HKLM "${AppRegistryKeyPath}" ""
 
  ${If} $R0 != ""
    ;Detect version
    ReadRegStr $R2 HKLM "${AppRegistryKeyPath}" "Version"
    ${If} $R2 == ""
      StrCpy $R0 ""
    ${EndIf}
  ${EndIf}
 
  ReadRegStr $R1 HKCU "${AppRegistryKeyPath}" ""
 
  ${If} $R1 != ""
    ;Detect version
    ReadRegStr $R2 HKCU "${AppRegistryKeyPath}" "Version"
    ${If} $R2 == ""
      StrCpy $R1 ""
    ${EndIf}
  ${EndIf}
 
  ${If} $R1 == $INSTDIR
    Strcpy $un.REMOVE_CURRENT_USER 1
  ${EndIf}
  ${If} $R0 == $INSTDIR
    Strcpy $un.REMOVE_ALL_USERS 1
  ${EndIf}
  ${If} $un.REMOVE_CURRENT_USER != 1
  ${AndIf} $un.REMOVE_ALL_USERS != 1
    ${If} $R1 != ""
      Strcpy $un.REMOVE_CURRENT_USER 1
      ${If} $R0 == $R1
        Strcpy $un.REMOVE_ALL_USERS 1
      ${EndIf}
    ${Else}
      StrCpy $un.REMOVE_ALL_USERS = 1
    ${EndIf}
  ${EndIf}
 
FunctionEnd
 
Function un.onInit
 
  ${un.GetParameters} $CommandLine

  ${un.GetOptions} $CommandLine "/quiet" $R1
  ${If} ${Errors}
    StrCpy $Quiet 0
  ${Else}
    StrCpy $Quiet 1
    SetSilent silent
  ${EndIf}

  Call un.GetUserInfo
  Call un.ReadPreviousVersion
 
  ${If} $un.REMOVE_ALL_USERS == 1
  ${AndIf} $IS_ADMIN == 0
uac_tryagain:
    !insertmacro UAC_RunElevated

    ${Switch} $0
    ${Case} 0
      ${IfThen} $1 = 1 ${|} Quit ${|} ;we are the outer process, the inner process has done its work, we are done.
      ${IfThen} $3 <> 0 ${|} ${Break} ${|} ;we are admin, let the show go on
      ${If} $2 = 3 ;RunAs completed successfully, but with a non-admin user
        MessageBox MB_YESNO|MB_ICONEXCLAMATION|MB_TOPMOST|MB_SETFOREGROUND "Status Desktop setup requires admin privileges, try again" /SD IDNO IDYES uac_tryagain IDNO 0
        Abort
      ${EndIf}
      ;fall-through and die
    ${Default}
      ${MessageBox} MB_ICONSTOP "Status Desktop has been installed for all users.$\nPlease restart the uninstaller with Administrator privileges to remove it." IDOK ''
      Abort
    ${EndSwitch}

    ; The UAC plugin changes the error level even in the inner process, reset it.
    SetErrorLevel -1
  ${EndIf}
 
FunctionEnd
 
Function un.RemoveStartmenu
 
  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP
 
  Delete "$SMPROGRAMS\$MUI_TEMP\${UninstallLinkName}"
  Delete "$SMPROGRAMS\$MUI_TEMP\${AppLinkFileName}"
 
  ${un.GetOptions} $CommandLine "/keepstartmenudir" $R1
  ${If} ${Errors}

    ; Delete empty start menu parent diretories
    StrCpy $MUI_TEMP "$SMPROGRAMS\$MUI_TEMP"

    startMenuDeleteLoop:
      RMDir $MUI_TEMP
      GetFullPathName $MUI_TEMP "$MUI_TEMP\.."

      IfErrors startMenuDeleteLoopDone

      StrCmp $MUI_TEMP $SMPROGRAMS startMenuDeleteLoopDone startMenuDeleteLoop
    startMenuDeleteLoopDone:

  ${EndUnless}

FunctionEnd

Function un.ConfirmPagePre

  ${un.GetOptions} $CommandLine "/frominstall" $R1
  ${Unless} ${Errors}
    Abort
  ${EndUnless}

FunctionEnd

Function un.FinishPagePre

  ${un.GetOptions} $CommandLine "/frominstall" $R1
  ${Unless} ${Errors}
    SetRebootFlag false
    Abort
  ${EndUnless}

FunctionEnd

;--------------------------------
; Uninstaller Section
;--------------------------------

Section "Uninstall"
  
  SetShellVarContext all

  SetDetailsPrint lastused

  ${nsProcess::FindProcess} "${AppExeName}" $R0

  ${If} $R0 == 0
    DetailPrint "${AppExeName} is running. Closing it down"
    ${nsProcess::CloseProcess} "${AppExeName}" $R0
    DetailPrint "Waiting for ${AppExeName} to close"
    Sleep 2000  
  ${Else}
    DetailPrint "${AppExeName} was not found to be running"        
  ${EndIf}    

  ${nsProcess::FindProcess} "${NodeJsServerExeName}" $R0

  ${If} $R0 == 0
    DetailPrint "${NodeJsServerExeName} is running. Closing it down"
    ${nsProcess::CloseProcess} "${NodeJsServerExeName}" $R0
    DetailPrint "Waiting for ${NodeJsServerExeName} to close"
    Sleep 2000  
  ${Else}
    DetailPrint "${NodeJsServerExeName} was not found to be running"        
  ${EndIf}    

  ${nsProcess::Unload}

  SimpleFC::RemoveApplication "$INSTDIR\${AppExeName}"
  SimpleFC::RemoveApplication "$INSTDIR\${NodeJsServerExeName}"

  Delete "$INSTDIR\.env"
  Delete "$INSTDIR\*.dll"
  Delete "$INSTDIR\${AppExeName}"
  Delete "$INSTDIR\reportApp.exe"
  Delete "$INSTDIR\vc_redist.x64.exe"
  Delete "$INSTDIR\${NodeJsServerExeName}"
  Delete "$INSTDIR\${UninstallExeName}"
  RMDir /r "$INSTDIR\assets"
  Delete "$INSTDIR\bearer\*.dll"
  RMDir "$INSTDIR\bearer"
  Delete "$INSTDIR\iconengines\*.dll"
  RMDir "$INSTDIR\iconengines"
  Delete "$INSTDIR\imageformats\*.dll"
  RMDir "$INSTDIR\imageformats"
  RMDir /r "$INSTDIR\node_modules"
  Delete "$INSTDIR\notifier\*.exe"
  RMDir "$INSTDIR\notifier"
  Delete "$INSTDIR\platforms\*.dll"
  RMDir "$INSTDIR\platforms"
  RMDir /r "$INSTDIR\QtGraphicalEffects"
  RMDir /r "$INSTDIR\QtQml"
  RMDir /r "$INSTDIR\QtQuick"
  RMDir /r "$INSTDIR\QtQuick.2"
  RMDir /r "$INSTDIR\QtWebSockets"
  Delete "$INSTDIR\styles\*.dll"
  RMDir "$INSTDIR\styles"
  Delete "$INSTDIR\translations\*.qm"
  RMDir "$INSTDIR\translations"

  Delete "$INSTDIR"
  
  ${un.GetOptions} $CommandLine "/frominstall" $R1
  ${If} ${Errors}
    RMDir /r /REBOOTOK "$INSTDIR"

    DeleteRegValue SHCTX "${AppRegistryKeyPath}" "Package"
    DeleteRegValue SHCTX "${AppRegistryKeyPath}" "Updated"
    DeleteRegValue SHCTX "${AppRegistryKeyPath}" "Channel"
  ${EndIf}

  ${If} $un.REMOVE_ALL_USERS == 1
    SetShellVarContext all
    Call un.RemoveStartmenu
  
    DeleteRegKey /ifempty HKLM "${AppRegistryKeyPath}"
    DeleteRegKey /ifempty HKLM "${OrgRegistryKeyPath}"
    DeleteRegKey HKLM "${UninstallRegKeyPath}"
  
    Delete "$DESKTOP\${AppLinkFileName}"

    ; Remove dump key
    ${If} ${RunningX64}
      SetRegView 64
    ${EndIf}
    DeleteRegValue HKLM "${DUMP_KEY}\${AppExeName}"            "DumpType"
    DeleteRegValue HKLM "${DUMP_KEY}\${NodeJsServerExeName}"   "DumpType"
    ${If} ${RunningX64}
      SetRegView lastused
    ${EndIf}
  ${EndIf}

  ${If} $un.REMOVE_CURRENT_USER == 1
    SetShellVarContext current
    Call un.RemoveStartmenu

    DeleteRegKey /ifempty HKCU "${AppRegistryKeyPath}"
    DeleteRegKey /ifempty HKCU "${OrgRegistryKeyPath}"
    DeleteRegKey HKCU "${UninstallRegKeyPath}"

    Delete "$DESKTOP\${AppLinkFileName}"
  ${EndIf}
  
  DeleteRegKey SHCTX "Software\Microsoft\Windows\CurrentVersion\App Paths\${AppExeName}"

SectionEnd
