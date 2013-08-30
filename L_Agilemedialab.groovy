job {
    name 'L_Agilemedialab'
    scm {
        git('git@github.com:agilemedialab/agilemedialab.git', 'origin/master')
    }
    customWorkspace("/var/www/Agilemedialab")
    label('F_job_slave')
    steps {
        copyArtifacts('F_Migrations', '*') {
            latestSuccessful()
        }
        shell("mysql -u root testbackend < testdb.sql")
        shell("/home/ec2-user/composer.phar self-update")
        shell("echo codebase=Agilemedialab > param")
        shell("rm -rf *.gz")
        shell("rm -rf vendor")
        shell("tar -zcf agilemedialab.gz *")
        shell('echo $BUILD_NUMBER > AgilemedialabBuildNumber')
    }
    publishers {
        archiveArtifacts("*.gz,AgilemedialabBuildNumber")
        downstreamParameterized() {
            trigger('build_www') {
                propertiesFile('param')
            }
        }
    }
}