job {
    name 'L_LPbackend'
    scm {
        git('git@github.com:agilemedialab/LPbackend.git', 'origin/master')
    }
    customWorkspace("/var/www/LPbackend")
    label('F_job_slave')
    logRotator(-1,30)
    steps {
        shell(
'''
sh bin/update-lp2conf.sh
composer.phar update
echo $BUILD_NUMBER>LPbackendBuildNumber
echo codebase=LPbackend > param
'''
        )
    }
    publishers {
        publishCloneWorkspace('*')
        archiveArtifacts("LPbackendBuildNumber")
        downstreamParameterized() {
            trigger('build_www') {
                propertiesFile('param')
            }
        }
    }
}