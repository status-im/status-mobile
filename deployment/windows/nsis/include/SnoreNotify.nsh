!include LogicLib.nsh
!include WordFunc.nsh

Function SnoreWinVer
  ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows NT\CurrentVersion" CurrentVersion
  ${VersionCompare} "6.2" $R0 $R0
  ${If} $R0 == 1
    Push "NotWin8"
  ${Else}
    Push "AtLeastWin8"
  ${EndIf}
FunctionEnd

!macro SnoreShortcut path exe appID
  Call SnoreWinVer
  Pop $0
  ${If} $0 == "AtLeastWin8"
    nsExec::ExecToLog '"${SnoreToastExe}" -install "${path}" "${exe}" "${appID}"'
  ${Else}
    DetailPrint "Creating shortcut to ${exe}"
    CreateShortCut "${path}" "${exe}"
  ${EndIf}
!macroend