env.LANG="en_US.UTF-8"
env.LANGUAGE="en_US.UTF-8"
env.LC_ALL="en_US.UTF-8"

def installJSDeps() {
    def attempt = 1
    def maxAttempts = 10
    def installed = false
    while (!installed && attempt <= maxAttempts) {
        println "#${attempt} attempt to install npm deps"
        sh 'npm install'
        installed = fileExists('node_modules/web3/index.js')
        attemp = attempt + 1
    }
}

node ('macos1') {
  def apkUrl = ''
  def ipaUrl = ''
  def testPassed = true
  def branch;

  load "$HOME/env.groovy"

  try {

    stage('Git & Dependencies') {
      slackSend color: 'good', message: BRANCH_NAME + '(' + env.CHANGE_BRANCH + ') build started. ' + env.BUILD_URL

      checkout scm
      sh 'git rebase origin/develop'

      sh 'rm -rf node_modules'
      sh 'cp .env.jenkins .env'
      sh 'lein deps'

      installJSDeps()

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
        def job = sh(returnStdout: true, script: 'curl https://upload.diawi.com/ -F token='+token+' -F file=@android/app/build/outputs/apk/release/app-release.apk -F find_by_udid=0 -F wall_of_apps=0 | jq -r ".job"').trim()
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

    stage('Upload apk for e2e tests') {
      if (env.CHANGE_ID != null){
      withCredentials([string(credentialsId: 'SAUCE_ACCESS_KEY', variable: 'key'), string(credentialsId: 'SAUCE_USERNAME', variable: 'username')]){
        def apk_name = env.CHANGE_ID + '.apk'
        sh('curl -u ' + username+ ':' + key + ' -X POST -H "Content-Type: application/octet-stream" https://saucelabs.com/rest/v1/storage/' + username + '/' + apk_name + '?overwrite=true --data-binary @android/app/build/outputs/apk/release/app-release.apk')
      }
     }
    }

  } catch (e) {
    slackSend color: 'bad', message: BRANCH_NAME + ' failed to build. ' + env.BUILD_URL
    throw e
  }
}
