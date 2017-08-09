env.LANG="en_US.UTF-8"
env.LANGUAGE="en_US.UTF-8"
env.LC_ALL="en_US.UTF-8"
node {
  def apkUrl = ''
  def ipaUrl = ''
  def testPassed = true

  sh 'source /etc/profile'

  try {

    stage('Git & Dependencies') {
      git([url: 'https://github.com/status-im/status-react.git', branch: env.BRANCH_NAME])
      // Checkout master because used for iOS Plist version information
      sh 'git checkout -- .'
      sh 'git checkout master' 
      sh 'git checkout ' + env.BRANCH_NAME
      sh 'rm -rf node_modules'
      sh 'cp .env.jenkins .env'
      sh 'lein deps && npm install && ./re-natal deps'
      sh 'sed -i "" "s/301000/601000/g" node_modules/react-native/packager/src/JSTransformer/index.js'
      sh 'lein generate-externs'
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
      def artifact_dir = pwd() + '/android/app/build/outputs/apk/'
      def artifact = new File(artifact_dir + 'app-release.apk')
      assert artifact.exists()
      def server = Artifactory.server('artifacts')
      def shortCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim().take(6)
      def filename = 'im.status.ethereum-' + shortCommit + '.apk'
      artifact.renameTo artifact_dir + filename
      def uploadSpec = '{ "files": [ { "pattern": "*apk/' + filename + '", "target": "pull-requests" }]}'
      def buildInfo = server.upload(uploadSpec)
      apkUrl = 'http://artifacts.status.im:8081/artifactory/pull-requests/' + filename
    }

    // try {
    //   stage('Test (Android)') {
    //     sauce('b9aded57-5cc1-4f6b-b5ea-42d989987852') {
    //         sh 'cd test/appium && mvn -DapkUrl=' + apkUrl + ' test'
    //         saucePublisher()
    //     }
    //   }
    // } catch(e) {
    //   testPassed = false
    // }

    // iOS
    stage('Build (iOS)') {
      sh 'export RCT_NO_LAUNCH_PACKAGER=true && xcodebuild -workspace ios/StatusIm.xcworkspace -scheme StatusIm -configuration release -archivePath status clean archive'
      sh 'xcodebuild -exportArchive -exportPath status -archivePath status.xcarchive -exportOptionsPlist /Users/Xcloud/archive.plist'
    }
    stage('Deploy (iOS)') {
      withCredentials([string(credentialsId: 'diawi-token', variable: 'token')]) {
        def job = sh(returnStdout: true, script: 'curl https://upload.diawi.com/ -F token='+token+' -F file=@status/StatusIm.ipa -F find_by_udid=0 -F wall_of_apps=0 | jq -r ".job"').trim()
        sh 'sleep 10'
        def hash = sh(returnStdout: true, script: "curl -vvv 'https://upload.diawi.com/status?token="+token+"&job="+job+"'|jq -r '.hash'").trim()  
        ipaUrl = 'https://i.diawi.com/' + hash
      }
    }

  } catch (e) {
    slackSend color: 'bad', message: env.BRANCH_NAME + ' failed to build. ' + env.BUILD_URL
    throw e
  }

  stage('Slack Notification') {
    def c = (testPassed ? 'good' : 'warning' )
    slackSend color: c, message: 'Branch: ' + env.BRANCH_NAME + '\nTests: ' + (testPassed ? ':+1:' : ':-1:') + ')\nAndroid: ' + apkUrl + '\n iOS: ' + ipaUrl
  }
}
