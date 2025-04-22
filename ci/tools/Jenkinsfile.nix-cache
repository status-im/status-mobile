#!/usr/bin/env groovy
library 'status-jenkins-lib@v1.9.21'

pipeline {
  agent { label params.AGENT_LABEL }

  parameters {
    string(
      name: 'NIX_CACHE_HOST',
      description: 'FQDN of Nix binary cache host.',
      defaultValue: params.NIX_CACHE_HOST ?: 'cache-01.he-eu-hel1.ci.nix.status.im'
    )
    string(
      name: 'NIX_CACHE_USER',
      description: 'Username for Nix binary cache host.',
      defaultValue: params.NIX_CACHE_USER ?: 'nix-cache'
    )
    booleanParam(
      name: 'BUILD_COMPONENTS',
      description: 'Whether to build Mobile components first.',
      defaultValue: params.BUILD_COMPONENTS ?: true,
    )
  }

  environment {
    NIX_SSHOPTS = "-oStrictHostKeyChecking=accept-new"
    NIX_CONF_DIR = "${env.WORKSPACE}/nix"
    NIX_STORE_CMD = '/nix/var/nix/profiles/default/bin/nix-store'
    NIX_SSH_REMOTE = "ssh://${params.NIX_CACHE_USER}@${params.NIX_CACHE_HOST}?remote-program=${env.NIX_STORE_CMD}"
  }

  options {
    timestamps()
    disableConcurrentBuilds()
    /* Prevent Jenkins jobs from running forever */
    timeout(time: 120, unit: 'MINUTES')
    /* Limit builds retained */
    buildDiscarder(logRotator(
      numToKeepStr: '20',
      daysToKeepStr: '30',
      artifactNumToKeepStr: '1',
    ))
  }

  stages {
    stage('Setup') {
      steps { script {
        nix.shell('nix-env -i openssh', sandbox: false, pure: false)
        /* some build targets don't build on MacOS */
        os = sh(script: 'uname', returnStdout: true).trim()
        arch = sh(script: 'arch', returnStdout: true).trim()
      } }
    }

    stage('Build status-go') {
      when { expression { params.BUILD_COMPONENTS } }
      steps { script {
        def platforms = ['mobile.android', 'mobile.ios', 'library']
        if (os != 'Darwin')  { platforms.removeAll { it == 'mobile.ios' } }
        /* FIXME: "'x86_64-darwin' with features {} is required to build" */
        if (arch == 'arm64') { platforms.removeAll { it == 'mobile.android' } }
        platforms.each { platform ->
          /* Allow for Android builds on Apple ARM. */
          env.NIXPKGS_SYSTEM_OVERRIDE = nixSysOverride(os, arch, platform)
          nix.build(
            attr: "targets.status-go.${platform}",
            sandbox: false,
            link: false
          )
        }
      } }
    }

    stage('Build android jsbundle') {
      when { expression { params.BUILD_COMPONENTS } }
      steps { script {
        /* Build/fetch deps required for jsbundle build. */
        nix.build(
          attr: 'targets.mobile.jsbundle',
          sandbox: false,
          pure: false,
          link: false
        )
      } }
    }

    stage('Build android deps') {
      when { expression { params.BUILD_COMPONENTS } }
      steps { script {
        /* Allow for Android builds on Apple ARM. */
        env.NIXPKGS_SYSTEM_OVERRIDE = nixSysOverride(os, arch, 'android')
        /* Build/fetch deps required to build android release. */
        nix.build(
          attr: 'targets.mobile.android.build.buildInputs',
          sandbox: false,
          pure: false,
          link: false
        )
      } }
    }

    stage('Build nix shell deps') {
      when { expression { params.BUILD_COMPONENTS } }
      steps { script {
        def shells = ['android', 'ios', 'fastlane', 'keytool', 'clojure', 'gradle']
        if (os != 'Darwin') { shells.removeAll { it == 'ios' } }
        /* FIXME: "'x86_64-darwin' with features {} is required to build" */
        if (arch == 'arm64') { shells.removeAll { it == 'android' } }
        /* Build/fetch deps required to start default Nix shell. */
        shells.each { shell ->
          /* Allow for Android builds on Apple ARM. */
          env.NIXPKGS_SYSTEM_OVERRIDE = nixSysOverride(os, arch, shell)
          nix.build(
            attr: "shells.${shell}.buildInputs",
            sandbox: false,
            link: false
          )
        }
      } }
    }

    stage('Collect') {
      steps { script {
        exitCode = nix.shell(
          """
            find /nix/store/ -mindepth 1 -maxdepth 1 \
              | grep -v -e '.*.links\$' -e '.*.lock\$' -e '.*.drv\$' -e '.*.drv\$' -e '.*-status-mobile-.*' \
              | xargs nix path-info --recursive \
              > "${WORKSPACE_TMP}/store-paths.txt"
          """,
          pure: false,
          returnStatus: true
        )
        /* Some paths can throw 'path is not valid' errors, but it's not fatal. */
        if (exitCode != 0) {
          currentBuild.result = 'UNSTABLE'
        }
        if (lineCount("${WORKSPACE_TMP}/store-paths.txt") == 0) {
          throw new Exception("No Nix store paths found to copy.")
        }
      } }
    }

    stage('Upload') {
      steps { script {
        sshagent(credentials: ['nix-cache-ssh']) {
          nix.shell(
            """
              cat "${WORKSPACE_TMP}/store-paths.txt" \
                | nix copy --stdin --to "${NIX_SSH_REMOTE}"
            """,
            pure: false
          )
        }
      } }
    }
  }
  post {
    always { script {
      nix.shell('nix-store --optimize', pure: false)
    } }
  }
}

def nixSysOverride(os, arch, target='android') {
    return (
        os == 'Darwin' &&
        arch == 'arm64' &&
        target =~ /.*android$/
    ) ? 'x86_64-darwin' : ''
}

def lineCount(filePath) {
    return sh(
        script: "wc -l ${filePath}",
        returnStdout: true
    ).trim()
}
