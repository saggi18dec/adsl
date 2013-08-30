job {
    name 'L_LPbackend'
    scm {
        git('git@github.com:agilemedialab/LPbackend.git', 'origin/master')
    }
    customWorkspace("/var/www/LPbackend")
    label('F_job_slave')
    steps {
        shell("sh bin/update-lp2conf.sh")
        shell("composer.phar update")
        shell('echo $BUILD_NUMBER>LPbackendBuildNumber')
        shell('echo codebase=LPbackend > param')
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
