job {
    name 'L_LPdeployprod'
    customWorkspace("/var/www/lp2")
  label('F_job_slave')
 steps {
    shell("curl http://ci2.weblogssl.com/job/LPdeployprod/lastSuccessfulBuild/artifact/amazon-prod/lightpress.tgz > LPdeployprod.tgz")
    shell('echo $BUILD_NUMBER > LPdeployprodBuildNumber')
    shell('echo codebase=lp2 > param')
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
