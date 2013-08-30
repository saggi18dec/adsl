def shellCommand = '''
mysql -u root testbackend < testdb.sql
/home/ec2-user/composer.phar self-update
echo codebase=Agilemedialab > param
rm -rf *.gz
rm -rf vendor
tar -zcf agilemedialab.gz *
echo $BUILD_NUMBER > AgilemedialabBuildNumber
'''

job {
    name 'L_Agilemedialab'
    scm {
        git('git@github.com:agilemedialab/agilemedialab.git', 'origin/master')
    }
    customWorkspace("/var/www/Agilemedialab")
    label('F_job_slave')
    logRotator(-1,30)
    steps {
        copyArtifacts('F_Migrations', '*') {
            latestSuccessful()
        }
        shell(shellCommand)
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