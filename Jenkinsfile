env.LANG="en_US.UTF-8"
env.LANGUAGE="en_US.UTF-8"
env.LC_ALL="en_US.UTF-8"
env.FASTLANE_DISABLE_COLORS=1

def installJSDeps() {
    def attempt = 1
    def maxAttempts = 10
    def installed = false
    while (!installed && attempt <= maxAttempts) {
        println "#${attempt} attempt to install npm deps"
        sh 'scripts/prepare-for-platform.sh mobile'
        sh 'npm install'
        installed = fileExists('node_modules/web3/index.js')
        attemp = attempt + 1
    }
}

timeout(90) {
    node ('macos') {
      def apkUrl = ''
      def ipaUrl = ''
      def testPassed = true
      def branch;

      load "$HOME/env.groovy"

      try {
        stage('Git & Dependencies') {
          slackSend color: 'good', message: BRANCH_NAME + '(' + env.CHANGE_BRANCH + ') build started. ' + env.BUILD_URL

          checkout scm

          try {
            sh 'git rebase origin/develop'
          } catch (e) {
            sh 'git rebase --abort'
            throw e
          }

          sh 'rm -rf node_modules'
          sh 'cp .env.jenkins .env'

          installJSDeps()

          sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
          sh 'cd ios && pod install && cd ..'
        }

        stage('Code style checks') {
            sh 'lein cljfmt check'
        }

        stage('Tests') {
          sh 'lein test-cljs'
        }

        stage('Build') {
          sh 'lein prod-build'
        }

        // Android
        stage('Build (Android)') {
          sh 'cd android && ./gradlew react-native-android:installArchives && ./gradlew assembleRelease'
        }

        stage('Deploy (Android)') {
          withCredentials([string(credentialsId: 'diawi-token', variable: 'token')] ) {
            def job = sh(returnStdout: true, script: 'curl https://upload.diawi.com/ -F token='+token+' -F file=@android/app/build/outputs/apk/release/app-release.apk -F find_by_udid=0 -F wall_of_apps=0 | jq -r ".job"').trim()
            def hash = 'null'
            for (int r = 0; r < 6 && hash == 'null'; r++) {
              sh 'sleep 10'
              hash = sh(returnStdout: true, script: "curl -vvv 'https://upload.diawi.com/status?token="+token+"&job="+job+"'|jq -r '.hash'").trim()
            }
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
            def hash = 'null'
            for (int r = 0; r < 6 && hash == 'null'; r++) {
              sh 'sleep 10'
              hash = sh(returnStdout: true, script: "curl -vvv 'https://upload.diawi.com/status?token="+token+"&job="+job+"'|jq -r '.hash'").trim()
            }
            ipaUrl = 'https://i.diawi.com/' + hash

            sh ('echo ARTIFACT iOS: ' + ipaUrl)
          }
        }

        stage('Slack/GH Notifications') {
          def c = (testPassed ? 'good' : 'warning' )

          slackSend color: c, message: ('Branch: ' + BRANCH_NAME + '\nAndroid: ' + apkUrl + '\niOS: ' + ipaUrl);

          withCredentials([string(credentialsId: 'GIT_HUB_TOKEN', variable: 'githubToken')]) {
            def commentMsg = ("branch " + BRANCH_NAME + ":\\napk uploaded to " + apkUrl + "\\nipa uploaded to " + ipaUrl)
            def ghOutput = sh(returnStdout: true, script: "curl -u status-im:" + githubToken + " -H 'Content-Type: application/json'  --data '{\"body\": \"" + commentMsg + "\"}' https://api.github.com/repos/status-im/status-react/issues/" + CHANGE_ID + "/comments")
            println("Result of github comment curl: " + ghOutput);
          }
        }

        // Android for e2e
        stage('Build (Android) for e2e tests') {
          sh 'cd android && mv app/build/outputs/apk/release/app-release.apk app/build/outputs/apk/release/app-release.original.apk && ENVFILE=.env.e2e ./gradlew assembleRelease'
        }

        stage('Upload apk for e2e tests') {
          if (env.CHANGE_ID != null){
          withCredentials([string(credentialsId: 'SAUCE_ACCESS_KEY', variable: 'key'), string(credentialsId: 'SAUCE_USERNAME', variable: 'username')]){
            def apk_name = env.CHANGE_ID + '.apk'
            sh('curl -u ' + username+ ':' + key + ' -X POST -H "Content-Type: application/octet-stream" https://saucelabs.com/rest/v1/storage/' + username + '/' + apk_name + '?overwrite=true --data-binary @android/app/build/outputs/apk/release/app-release.apk')
          }
          withCredentials([string(credentialsId: 'diawi-token', variable: 'token')]) {
            def job = sh(returnStdout: true, script: 'curl https://upload.diawi.com/ -F token='+token+' -F file=@android/app/build/outputs/apk/release/app-release.apk -F find_by_udid=0 -F wall_of_apps=0 | jq -r ".job"').trim()
            sh 'sleep 10'
            def hash = sh(returnStdout: true, script: "curl -vvv 'https://upload.diawi.com/status?token="+token+"&job="+job+"'|jq -r '.hash'").trim()
            apkUrl = 'https://i.diawi.com/' + hash

            sh ('echo ARTIFACT Android for e2e tests: ' + apkUrl)
          }
         }
        }

      } catch (e) {
        slackSend color: 'bad', message: BRANCH_NAME + ' failed to build. ' + env.BUILD_URL
        throw e
      }
    }
}
