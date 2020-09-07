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
                       sh 'cd secure-pipeline-demo && trufflehog .'
                       sh 'rm -rf secure-pipeline-demo'
                     }
                   }
                }
            }
        }
        stage('Pre-deployment') {
          parallel {
           stage('Code Tests'){
              stages {
                stage('Unit tests') {
                  steps {
                   sh '''./gradlew test'''
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
                  archiveArtifacts '/build/reports/spotbugs/main.html'
                }
              }
              steps {
                sh '''./gradlew spotbugsMain'''
               }
            }
            stage('Dependency check') {
               post {
                always {
                  archiveArtifacts '/build/reports/dependency-check-report.html'
                }
              }
              steps {
                sh '''./gradlew dependencyCheckAnalyze'''
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
                echo 'ZAPing'
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

