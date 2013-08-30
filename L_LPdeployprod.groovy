def shellCommand =
'''
curl http://ci2.weblogssl.com/job/LPdeployprod/lastSuccessfulBuild/artifact/amazon-prod/lightpress.tgz > LPdeployprod.tgz
echo $BUILD_NUMBER > LPdeployprodBuildNumber
echo codebase=lp2 > param
'''

job {
    name 'L_LPdeployprod'
    customWorkspace("/var/www/lp2")
    label('F_job_slave')
    logRotator(-1,30)
    steps {
        shell(shellCommand)
    }
    publishers {
        archiveArtifacts("LPdeployprod.tgz,LPdeployprodBuildNumber")
        downstreamParameterized() {
            trigger('build_www') {
                propertiesFile('param')
            }
        }
    }
}
