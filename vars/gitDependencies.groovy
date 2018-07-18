def call(){
    if (!BRANCH_NAME.startsWith("release/")){
        error "Wrong branch name format: " + BRANCH_NAME + ", but it should be `release/version`"
    }

    checkout scm

    version = readFile("${env.WORKSPACE}/VERSION").trim()

    sh 'git fetch --tags'

    sh 'rm -rf node_modules'
    sh 'cp .env.prod .env'
    sh 'lein deps'
    installJSDeps()

    sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
    sh 'cd ios && pod install && cd ..'
}

def installJSDeps(){
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