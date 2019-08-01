/*
This node works with workflow to cancel all other running builds of the same job
Use case: many build may go to QA and wait in queue, only latest build is important,
the other builds in the workflow are going to be aborted.
*/
slaveNode = 'flutteronly'

node('master') {
    def jobname = env.JOB_NAME
    def buildnum = env.BUILD_NUMBER.toInteger()

    def job = Jenkins.instance.getItemByFullName(jobname)
    def list = job.builds
    println("***jobname = env.JOB_NAME: " + jobname)
    println("***buildnum = env.BUILD_NUMBER.toInteger(): " + buildnum)
    println("***Jenkins.instance.getItemByFullName(jobname): " + job)
    println("***job.builds.first(): " + job.builds.first())
    for (build in job.builds) {
        if (build.isBuilding() && build != job.builds.first()) {
            println '*' * 30
            println("***build: " + build)
            println 'These builds for this job have been aborted!'
            println '*' * 30
            build.doStop()
        }
    }
}

/*
Pipeline will get latest code and build stage will perform the build
*/

pipeline {

    agent { label slaveNode }
    environment {
          LC_CTYPE = 'en_US.UTF-8'
           PATH = "$PATH:/Users/builder/homebrew/bin:/Users/builder/flutter/bin"
       }

    stages {
        stage('Build') {
            steps {
                script {
                    sh '''
                        flutter packages get

                        cd example
                        flutter packages get
                        cd ..

                        flutter clean
                    '''
                }
            }
        }

        stage('Testing') {
            steps {
                sh '''
                 flutter test
                 '''
            }

        } 

        stage('Analyzing') {
            steps {
                sh '''
                 flutter analyze
                 '''
            }
        }
    }
}