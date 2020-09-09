pipeline {
    agent {
        kubernetes {
            yamlFile 'build-agent.yaml'
            idleMinutes 1
        }
    }
    stages {
        stage('Build') {
            parallel {
                stage('Build app') {
                    steps {
                        sh '''./gradlew clean clean build -x test -x spotbugsMain'''
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
        stage('Deploy test env') {
            steps {
                echo 'Test env ready'
            }
        }
        stage('Functional tests | DAST') {
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
            steps {
                echo 'Staging ready'
            }
        }
    }
}

