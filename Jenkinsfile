env.LANG="en_US.UTF-8"
env.LANGUAGE="en_US.UTF-8"
env.LC_ALL="en_US.UTF-8"

node ('macos1') {
  def apkUrl = ''
  def ipaUrl = ''
  def testPassed = true
  def branch;

  load "$HOME/env.groovy"

  try {

    stage('Git & Dependencies') {
      slackSend color: 'good', message: BRANCH_NAME + ' build started. ' + env.BUILD_URL

      checkout scm
      sh 'git checkout -- .'
      sh 'git checkout develop'
      sh 'git checkout ' + BRANCH_NAME
      
      sh 'rm -rf node_modules'
      sh 'cp .env.jenkins .env'
      sh 'lein deps && npm install'
      sh '[ -f node_modules/react-native/packager/src/JSTransformer/index.js ] && sed -i "" "s/301000/1201000/g" node_modules/react-native/packager/src/JSTransformer/index.js || echo "New packager"'

      // Fix silly RN upgrade weird env issue
      sh 'cp findSymlinkedModules.js.patch node_modules/react-native/local-cli/util/findSymlinkedModules.js'

      sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
      sh 'cd ios && pod install && cd ..'
    }

    stage('Tests') {
      sh 'lein test-cljs'
    }

    stage('Build') {
      sh 'lein prod-build'
    }

    // Android
    stage('Build (Android)') {
      sh 'cd android && ./gradlew assembleRelease'
    }

    stage('Deploy (Android)') {
      withCredentials([string(credentialsId: 'diawi-token', variable: 'token')]) {
        def job = sh(returnStdout: true, script: 'curl https://upload.diawi.com/ -F token='+token+' -F file=@android/app/build/outputs/apk/app-release.apk -F find_by_udid=0 -F wall_of_apps=0 | jq -r ".job"').trim()
        sh 'sleep 10'
        def hash = sh(returnStdout: true, script: "curl -vvv 'https://upload.diawi.com/status?token="+token+"&job="+job+"'|jq -r '.hash'").trim()
        apkUrl = 'https://i.diawi.com/' + hash

        sh ('echo ARTIFACT Android: ' + apkUrl)
      }
    }

    // iOS
    stage('Build (iOS)') {
      sh 'export RCT_NO_LAUNCH_PACKAGER=true && xcodebuild -workspace ios/StatusIm.xcworkspace -scheme StatusIm -configuration release -archivePath status clean archive'
      sh 'xcodebuild -exportArchive -exportPath status -archivePath status.xcarchive -exportOptionsPlist ~/archive.plist'
    }

    stage('Deploy (iOS)') {
      withCredentials([string(credentialsId: 'diawi-token', variable: 'token')]) {
        def job = sh(returnStdout: true, script: 'curl https://upload.diawi.com/ -F token='+token+' -F file=@status/StatusIm.ipa -F find_by_udid=0 -F wall_of_apps=0 | jq -r ".job"').trim()
        sh 'sleep 10'
        def hash = sh(returnStdout: true, script: "curl -vvv 'https://upload.diawi.com/status?token="+token+"&job="+job+"'|jq -r '.hash'").trim()
        ipaUrl = 'https://i.diawi.com/' + hash

        sh ('echo ARTIFACT iOS: ' + ipaUrl)
      }
    }

    stage('Slack Notification') {
      def c = (testPassed ? 'good' : 'warning' )

      slackSend color: c, message: 'Branch: ' + BRANCH_NAME +
        '\nAndroid: ' + apkUrl +
        '\niOS: ' + ipaUrl
    }

  } catch (e) {
    slackSend color: 'bad', message: BRANCH_NAME + ' failed to build. ' + env.BUILD_URL
    throw e
  }
}
