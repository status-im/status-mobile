{ qt5 }:

# Custom collection of QT libraries for Status desktop App
qt5.env "qt-status-${qt5.qtbase.version}" (with qt5; [
  qtbase
  qtsvg
  qtwebengine
  qtwebview
  qtdeclarative
  qtquickcontrols2
])
