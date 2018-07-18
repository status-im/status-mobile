def call() {
    withCredentials([[
    $class: 'UsernamePasswordMultiBinding',
    credentialsId: 'jenkins-status-im',
    usernameVariable: 'GIT_USER',
    passwordVariable: 'GIT_PASS'
    ]]) {
        sh ('git push --tags https://${GIT_USER}:${GIT_PASS}@github.com/status-im/status-react --tags')
    }
}
