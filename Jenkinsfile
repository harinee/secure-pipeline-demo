pipeline {
    stages {
        stage('Build') {
            parallel {
                agent any
                stage('Build app') {
                    steps {
                        sh '''./gradlew clean clean build -x test -x spotbugsMain'''
                    }
                }
                agent {
                    kubernetes {
                        yamlFile 'build-agent-trufflehog.yaml'
                        idleMinutes 1
                    }
                }
                stage('Secret scan') {
                    steps {
                        container('trufflehog') {
                            sh 'git clone ${GIT_URL}'
                            sh 'cd secure-pipeline-demo && ls -al'
                            echo 'cd secure-pipeline-demo && trufflehog .'
                            echo 'rm -rf secure-pipeline-demo'
                        }
                    }
                }
            }
        }
        stage('Pre-deployment') {
            parallel {
                agent any
                stage('Code Tests') {
                    stages {
                        stage('Unit tests') {
                            steps {
                                echo '''./gradlew test'''
                            }
                        }
                        stage('Integration tests') {
                            steps {
                                echo 'integration testing'
                            }
                        }
                    }
                }
                agent any
                stage('SAST') {
                    post {
                        always {
                            archiveArtifacts allowEmptyArchive: true, artifacts: '/build/reports/spotbugs/main.html', fingerprint: true, onlyIfSuccessful: false
                        }
                    }
                    steps {
                        echo '''./gradlew spotbugsMain'''
                    }
                }
                agent any
                stage('Dependency check') {
                    post {
                        always {
                            archiveArtifacts allowEmptyArchive: true, artifacts: '/build/reports/dependency-check-report.html', fingerprint: true, onlyIfSuccessful: false
                        }
                    }
                    steps {
                        echo '''./gradlew dependencyCheckAnalyze'''
                    }
                }
            }
        }
        agent any
        stage('Package') {
            steps {
                container('docker-cmds') {
                    sh 'ls -al'
                    sh 'docker build . -t sample-app'
                }
            }
        }
        stage('Artefact Analysis') {
            parallel {
                agent {
                    kubernetes {
                        yamlFile 'build-agent-trivy.yaml'
                        idleMinutes 1
                    }
                }
                    stage('Image Scan') {
                    steps {
                        container('docker-cmds') {
                            sh '''#!/bin/sh
                    apk add --update-cache --upgrade curl rpm
                    export TRIVY_VERSION="0.8.0"
                    echo $TRIVY_VERSION
                    wget https://github.com/aquasecurity/trivy/releases/download/v${TRIVY_VERSION}/trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz
                    tar zxvf trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz
                    mv trivy /usr/local/bin
                    trivy --cache-dir /tmp/trivycache/ sample-app:latest
                  '''
                        }
                    }
                }
                agent {
                    kubernetes {
                        yamlFile 'build-agent-dockle.yaml'
                        idleMinutes 1
                    }
                }
                stage('Image Hardening') {
                    steps {
                        container('dockle') {
                            sh 'dockle sample-app:latest'
                        }
                    }
                }
            }
        }
        agent any
        stage('Deploy test env') {
            steps {
                echo 'Test env ready'
            }
        }
         agent any
        stage('Functional tests | DAST') {
            parallel {
                stage('Functional tests') {
                    steps {
                        echo 'Functional tests'
                    }
                }
                agent any
                stage('Dynamic Security Analysis') {
                    steps {
                        container('docker-cmds') {
                            sh 'docker run -t owasp/zap2docker-stable zap-baseline.py -t https://www.zaproxy.org/'
                        }
                    }
                }
            }
        }
        agent any
        stage('Deploy staging') {
            steps {
                echo 'Staging ready'
            }
        }
    }
}

