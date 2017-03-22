node {
  sh 'source /etc/profile'
  stage('Git & Dependencies') {
    git([url: 'https://github.com/status-im/status-react.git', branch: env.BRANCH_NAME])
    // Checkout master because used for iOS Plist version information
    sh 'git checkout -- .'
    sh 'git checkout master' 
    sh 'git checkout ' + env.BRANCH_NAME
    sh 'rm -rf node_modules'
    sh 'lein deps && npm install && ./re-natal deps'
    sh 'lein generate-externs'
    sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
    sh 'cd ios && pod install && cd ..'
  }
  stage('Build') {
    sh 'lein prod-build'
  }
  stage('Build (Android)') {
    sh 'cd android && ./gradlew assembleRelease'
  }
  stage('Build (iOS)') {
    sh 'export RCT_NO_LAUNCH_PACKAGER=true && xcodebuild -workspace ios/StatusIm.xcworkspace -scheme StatusIm -configuration release -archivePath status clean archive'
    sh 'xcodebuild -exportArchive -exportPath status -archivePath status.xcarchive -exportOptionsPlist /Users/Xcloud/archive.plist'
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
    slackSend color: 'good', message: env.BRANCH_NAME + ' (Android) http://artifacts.status.im:8081/artifactory/pull-requests/' + filename
  }
  stage('Deploy (iOS)') {
    withCredentials([string(credentialsId: 'diawi-token', variable: 'token')]) {
      def job = sh(returnStdout: true, script: 'curl https://upload.diawi.com/ -F token='+token+' -F file=@status/StatusIm.ipa -F find_by_udid=0 -F wall_of_apps=0 | jq -r ".job"').trim()
      sh 'sleep 10'
      def hash = sh(returnStdout: true, script: "curl -vvv 'https://upload.diawi.com/status?token="+token+"&job="+job+"'|jq -r '.hash'").trim()  
      slackSend color: 'good', message: env.BRANCH_NAME + ' (iOS) https://i.diawi.com/' + hash
    }
  }
}
