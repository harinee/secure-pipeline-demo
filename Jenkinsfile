pipeline {
    agent none
    stages {
        stage('Build') {
            parallel {
                stage('Build app') {
                    agent any
                    steps {
                        sh '''./gradlew clean clean build -x test -x spotbugsMain'''
                    }
                }
                stage('Secret scan') {
                    agent {
                        kubernetes {
                            yamlFile 'build-agent-trufflehog.yaml'
                            idleMinutes 1
                        }
                    }
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
                stage('Code Tests') {
                    agent any
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
                stage('SAST') {
                    agent any
                    post {
                        always {
                            archiveArtifacts allowEmptyArchive: true, artifacts: '/build/reports/spotbugs/main.html', fingerprint: true, onlyIfSuccessful: false
                        }
                    }
                    steps {
                        echo '''./gradlew spotbugsMain'''
                    }
                }
                stage('Dependency check') {
                    agent any
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
        stage('Package') {
            agent any
            steps {
                container('docker-cmds') {
                    sh 'ls -al'
                    sh 'docker build . -t sample-app'
                }
            }
        }
        stage('Artefact Analysis') {
            parallel {
                    stage('Container Dependency Scan') {
                        agent {
                            kubernetes {
                                yamlFile 'build-agent-trivy.yaml'
                                idleMinutes 1
                            }
                        }
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
                stage('Image Hardening') {
                    agent {
                        kubernetes {
                            yamlFile 'build-agent-dockle.yaml'
                            idleMinutes 1
                        }
                    }
                    steps {
                        container('dockle') {
                            sh 'dockle sample-app:latest'
                        }
                    }
                }
            }
        }
        stage('Deploy test env') {
            agent any
            steps {
                echo 'Test env ready'
            }
        }
        stage('Functional tests | DAST') {
            agent any
            parallel {
                stage('Functional tests') {
                    steps {
                        echo 'Functional tests'
                    }
                }
                stage('Dynamic Security Analysis') {
                    steps {
                        container('docker-cmds') {
                            sh 'docker run -t owasp/zap2docker-stable zap-baseline.py -t https://www.zaproxy.org/'
                        }
                    }
                }
            }
        }
        stage('Deploy staging') {
            agent any
            steps {
                echo 'Staging ready'
            }
        }
    }
}

