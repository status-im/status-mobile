def call(){
    withCredentials([[
        $class: 'UsernamePasswordMultiBinding',
        credentialsId: 'jenkins-status-im',
        usernameVariable: 'GIT_USER',
        passwordVariable: 'GIT_PASS'
    ]]) {
        build_no = sh(
            returnStdout: true,
            script: './scripts/build_no.sh --increment'
        ).trim()
    }
}