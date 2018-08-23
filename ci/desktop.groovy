common = load 'ci/common.groovy'

qtBin = '/opt/qt59/bin'
packageFolder = './StatusImPackage'
external_modules_dir = [
  'node_modules/react-native-i18n/desktop',
  'node_modules/react-native-config/desktop',
  'node_modules/react-native-fs/desktop',
  'node_modules/react-native-http-bridge/desktop',
  'node_modules/react-native-webview-bridge/desktop',
  'node_modules/react-native-keychain/desktop',
  'node_modules/react-native-securerandom/desktop',
  'modules/react-native-status/desktop',
]

external_fonts = [
  '../../../../../resources/fonts/SF-Pro-Text-Regular.otf',
  '../../../../../resources/fonts/SF-Pro-Text-Medium.otf',
  '../../../../../resources/fonts/SF-Pro-Text-Light.otf',
]

def cleanupBuild() {
  sh """
    rm -rf \\
      node_modules ${packageFolder} \\
      desktop/modules desktop/node_modules
  """
}

def cleanupAndDeps() {
  cleanupBuild()
  sh 'cp .env.jenkins .env'
  sh 'lein deps'
  common.installJSDeps('desktop')
}

def slackNotify(message, color = 'good') {
  slackSend(
    color: color,
    channel: '#jenkins-desktop',
    message: "develop (${env.CHANGE_BRANCH}) ${message} ${env.BUILD_URL}"
  )
}

def buildClojureScript() {
  sh 'rm -f index.desktop.js'
  sh 'lein prod-build-desktop'
  sh "mkdir ${packageFolder}"
  sh """
    react-native bundle \\
      --entry-file index.desktop.js \\
      --dev false --platform desktop \\
      --bundle-output ${packageFolder}/StatusIm.jsbundle \\
      --assets-dest ${packageFolder}/assets
  """
}

def uploadArtifact(filename) {
  def domain = 'ams3.digitaloceanspaces.com'
  def bucket = 'status-im-desktop'
  withCredentials([usernamePassword(
    credentialsId: 'digital-ocean-access-keys',
    usernameVariable: 'DO_ACCESS_KEY',
    passwordVariable: 'DO_SECRET_KEY'
  )]) {
    sh """
      s3cmd \\
        --acl-public \\
        --host='${domain}' \\
        --host-bucket='%(bucket)s.${domain}' \\
        --access_key=${DO_ACCESS_KEY} \\
        --secret_key=${DO_SECRET_KEY} \\
        put ${filename} s3://${bucket}/
    """

  }
  def url = "https://${bucket}.${domain}/${filename}"
  return url
}

/* MAIN --------------------------------------------------*/

def prepDeps() {
  common.doGitRebase()
  cleanupAndDeps()
}
  
def compileLinux() {
  /* add path for QT installation binaries */
  env.PATH = "${qtBin}:${env.PATH}"
  dir('desktop') {
    sh 'rm -rf CMakeFiles CMakeCache.txt cmake_install.cmake Makefile'
    sh """
      cmake -Wno-dev \\
        -DCMAKE_BUILD_TYPE=Release \\
        -DEXTERNAL_MODULES_DIR='${external_modules_dir.join(";")}' \\
        -DDESKTOP_FONTS='${external_fonts.join(";")}' \\
        -DJS_BUNDLE_PATH='${workspace}/${packageFolder}/StatusIm.jsbundle' \\
        -DCMAKE_CXX_FLAGS:='-DBUILD_FOR_BUNDLE=1'
    """
    sh 'make'
  }
}
  
def bundleLinux(type = 'nightly') {
  def pkg

  dir(packageFolder) {
    sh 'rm -rf StatusImAppImage'
    /* TODO this needs to be fixed: status-react/issues/5378 */
    sh 'cp /opt/StatusImAppImage.zip ./'
    sh 'unzip ./StatusImAppImage.zip'
    sh 'rm -rf AppDir'
    sh 'mkdir AppDir'
  }
  sh "cp -r ./deployment/linux/usr  ${packageFolder}/AppDir"
  sh "cp ./deployment/linux/.env  ${packageFolder}/AppDir"
  sh "cp ./desktop/bin/StatusIm ${packageFolder}/AppDir/usr/bin"
  sh 'wget https://github.com/probonopd/linuxdeployqt/releases/download/continuous/linuxdeployqt-continuous-x86_64.AppImage'
  sh 'chmod a+x ./linuxdeployqt-continuous-x86_64.AppImage'

  sh 'rm -f Application-x86_64.AppImage'
  sh 'rm -f StatusIm-x86_64.AppImage'

  sh "ldd ${packageFolder}/AppDir/usr/bin/StatusIm"
  sh """
    ./linuxdeployqt-continuous-x86_64.AppImage \\
      ${packageFolder}/AppDir/usr/share/applications/StatusIm.desktop \\
      -verbose=3 -always-overwrite -no-strip \\
      -no-translations -bundle-non-qt-libs \\
      -qmake=${qtBin}/qmake \\
      -extra-plugins=imageformats/libqsvg.so \\
      -qmldir='${workspace}/node_modules/react-native'
  """
  dir(packageFolder) {
    sh 'ldd AppDir/usr/bin/StatusIm'
    sh 'cp -r assets/share/assets AppDir/usr/bin'
    sh 'cp -rf StatusImAppImage/* AppDir/usr/bin'
    sh 'rm -f AppDir/usr/bin/StatusIm.AppImage'
  }
  sh """
    ./linuxdeployqt-continuous-x86_64.AppImage \\
    ${packageFolder}/AppDir/usr/share/applications/StatusIm.desktop \\
    -verbose=3 -appimage -qmake=${qtBin}/qmake
  """
  dir(packageFolder) {
    sh 'ldd AppDir/usr/bin/StatusIm'
    sh 'rm -rf StatusIm.AppImage'
    pkg = common.pkgFilename(type, 'AppImage')
    sh "mv ../StatusIm-x86_64.AppImage ${pkg}"
  }
  return "${packageFolder}/${pkg}".drop(2)
}

def compileMacOS() {
  /* add path for QT installation binaries */
  env.PATH = "/Users/administrator/qt/5.9.1/clang_64/bin:${env.PATH}"
  dir('desktop') {
    sh 'rm -rf CMakeFiles CMakeCache.txt cmake_install.cmake Makefile'
    sh """
      cmake -Wno-dev \\
        -DCMAKE_BUILD_TYPE=Release \\
        -DEXTERNAL_MODULES_DIR='${external_modules_dir.join(";")}' \\
        -DDESKTOP_FONTS='${external_fonts.join(";")}' \\
        -DJS_BUNDLE_PATH='${workspace}/${packageFolder}/StatusIm.jsbundle' \\
        -DCMAKE_CXX_FLAGS:='-DBUILD_FOR_BUNDLE=1'
    """
    sh 'make'
  }
}

def bundleMacOS(type = 'nightly') {
  def pkg = common.pkgFilename(type, 'dmg')
  dir(packageFolder) {
    sh 'git clone https://github.com/vkjr/StatusAppFiles.git'
    sh 'unzip StatusAppFiles/StatusIm.app.zip'
    sh 'cp -r assets/share/assets StatusIm.app/Contents/MacOs'
    sh 'chmod +x StatusIm.app/Contents/MacOs/ubuntu-server'
    sh 'cp ../desktop/bin/StatusIm StatusIm.app/Contents/MacOs'
    sh """
      macdeployqt StatusIm.app -verbose=1 -dmg \\
        -qmldir='${workspace}/node_modules/react-native/ReactQt/runtime/src/qml/'
    """
    sh 'rm -fr StatusAppFiles'
    sh "mv StatusIm.dmg ${pkg}"
  }
  return "${packageFolder}/${pkg}".drop(2)
}

return this
