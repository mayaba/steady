pipeline {
  agent {
    kubernetes {
      label 'my-agent-pod'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3-jdk-8-alpine
    command:
    - cat
    tty: true
"""
    }
  }
  stages {
    stage('Compile') {
      steps {
        container('maven') {
          sh 'whoami'
          sh 'ls ~'
          sh 'pwd'
          sh 'printenv'
          sh 'ls /home'
          sh 'ls /var/maven'
          sh 'mvn -Duser.home=/var/maven -P gradle clean compile'
        }
      }
    }
  }
}
