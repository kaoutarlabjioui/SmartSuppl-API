// Declarative Jenkins pipeline for Digital Logistics
// - Builds, runs unit tests, generates JaCoCo report
// - Optionally runs SonarQube analysis when SONAR_HOST and SONAR_TOKEN are provided

pipeline {
  // Run on any available node (avoid Docker agent because controller/node may not have Docker installed)
  agent any

  environment {
    MVN_CMD = './mvnw'
    MAVEN_OPTS = '-Xmx1g'
    // Optional environment variables to provide in Jenkins credentials or job config:
    // SONAR_HOST, SONAR_TOKEN
  }

  options {
    timestamps()
    ansiColor('xterm')
    timeout(time: 60, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '25'))
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        // Ensure the Maven wrapper is executable on Linux agents
        bat 'chmod +x mvnw || true'
        bat 'ls -la mvnw || true'
      }
    }

    stage('Build (compile)') {
      steps {
        bat "${MVN_CMD} -B -DskipTests=true clean package"
      }
    }

    stage('Unit Tests') {
      steps {
        bat "${MVN_CMD} -B test"
      }
    }

    stage('JaCoCo Report') {
      steps {
        // Generate HTML coverage report
        bat "${MVN_CMD} -B jacoco:report"
      }
    }

    stage('SonarQube Analysis') {
      when {
        expression { env.SONAR_HOST?.trim() && env.SONAR_TOKEN?.trim() }
      }
      steps {
        bat "${MVN_CMD} -B sonar:sonar -Dsonar.host.url=${env.SONAR_HOST} -Dsonar.login=${env.SONAR_TOKEN}"
      }
    }

    stage('Package') {
      steps {
        bat "${MVN_CMD} -B -DskipTests=true package"
      }
    }
  }

  post {
    always {
      // Publish JUnit results
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

      // Archive JaCoCo HTML report and built artifacts
      archiveArtifacts artifacts: 'target/*.jar, target/site/jacoco/**', allowEmptyArchive: true

      // Clean workspace to avoid filling Jenkins disk
      cleanWs()
    }

    success {
      echo "Build succeeded: ${env.BUILD_URL}"
    }

    failure {
      echo "Build failed: ${env.BUILD_URL}"
    }
  }
}