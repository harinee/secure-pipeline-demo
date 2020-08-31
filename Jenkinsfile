pipeline {
  agent any
  stages {
    stage('Pre-deployment') {
      parallel {
        stage('Build tests'){
          stages{
            stage('Build') {
             steps {
                echo 'Building'
               }
             }
            stage('Unit tests') {
              steps {
               echo 'Unit testing'
              }
            }
           stage('Integration tests') {
             steps {
              echo 'integration testing'
              }
            }
          }
        }
      stage('SAST-C project') {
       post {
        always {
          archiveArtifacts 'flawfinderReport.html'
        }
      }
      steps {
        sh '''flawfinder -F --html --quiet --error-level=1 myhtml>flawfinderReport.html'''
      }
     }
     stage('SAST-Java project') {
       post {
        always {
          archiveArtifacts 'campr-injection-workshop/build/reports/spotbugs/main.html'
        }
      }
      steps {
        sh '''cd campr-injection-workshop/
./gradlew check'''
       }
      }
    stage('Dependency check') {
       post {
        always {
          archiveArtifacts 'campr-injection-workshop/build/reports/dependency-check-report.html'
        }
      }
      steps {
        sh '''cd campr-injection-workshop
./gradlew dependencyCheckAnalyze'''
      }
    }
    stage('Secret scan') {
      steps {
        sh 'echo "Secret scan"'
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
