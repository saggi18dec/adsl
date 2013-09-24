def shellAgilemedialab =
'''
rm -rf ${WORKSPACE}/vendor/agilemedialab/*
tar -zxf agilemedialab.gz -C ${WORKSPACE}/vendor/agilemedialab
'''

shellCommand =
'''
rm -rf /var/www/lp2
mkdir -p /var/www/lp2
tar -zxf LPdeployprod.tgz -C /var/www/lp2
rm LPdeployprod.tgz
'''

shellCommand2 =
'''
cd $WORKSPACE [ -e www.gz ] #&& rm www.gz
sh bin/update-lp2conf.sh
mkdir -p ${WORKSPACE}/app/logs
mysql -u root testdb < testdb.sql
ant build-test -lib /usr/share/java/ant-contrib-1.0b3.jar -listener net.sf.antcontrib.perf.AntPerformanceListener
#php /var/www/LPbackend/app/console doctrine:migrations:status --env=dev --em=default -a=xataka
echo $BUILD_NUMBER > WWWBuildnumber
[ -e /var/www/www.gz ] && rm /var/www/www.gz || exit 0
'''

shellCommand3 =
'''
[ -e /var/www/Agilemedialab/composer.json ] && rm -rf /var/www/Agilemedialab
cd /var/www
tar -zcf www.gz *
mv www.gz $WORKSPACE
'''

job {
    name 'L_build_www'
    scm {
        cloneWorkspace('L_LPbackend', 'Successful')
    }
    label('F_job_slave')
    logRotator(-1,30)
    parameters {
        stringParam('codebase', 'LPbackend')
    }
    
    configure {
        def nodeBuilder = it / 'builders'
        def attributes = [plugin:'conditional-buildstep@1.2.2']
        def buildConditionalStepSingleNode = nodeBuilder / 'org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder'(attributes)

        def failureAttributes = [class:'org.jenkins_ci.plugins.run_condition.BuildStepRunner$Fail', 'plugin':'run-condition@0.10']
        def failureNode = buildConditionalStepSingleNode / 'runner'(failureAttributes)

        def conditionAttributes = [class:"org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition", plugin:'run-condition@0.10']
        def conditionNode = buildConditionalStepSingleNode / 'runCondition'(conditionAttributes)
        def argFirst = conditionNode / arg1
        argFirst.setValue('${codebase}')

        def argSecond = conditionNode / arg2
        argSecond.setValue("Agilemedialab")

        def argIgnoreCase = conditionNode / ignoreCase
        argIgnoreCase.setValue("false")

        def conditionalBuildersNode = buildConditionalStepSingleNode / 'conditionalbuilders'

        def copyArtifactAttributes = [plugin: "copyartifact@1.25"]

        def selectorAttributes = [class: "hudson.plugins.copyartifact.StatusBuildSelector"]

        def artifactNode = conditionalBuildersNode / 'hudson.plugins.copyartifact.CopyArtifact'(copyArtifactAttributes) {
            projectName 'L_Agilemedialab'
            filter '*'
            target ''
        }

        def selectorNode = artifactNode / 'selector'(selectorAttributes)

        def shellNode = conditionalBuildersNode / 'hudson.tasks.Shell'() {
            command shellAgilemedialab
        }

    }
    steps {
        copyArtifacts('L_LPbackend', 'LPbackendBuildNumber') {
            latestSuccessful()
        }
        copyArtifacts('L_LPdeployprod', '*') {
            latestSuccessful()
        }

        shell(shellCommand)

        copyArtifacts('L_Migrations', 'testdb.sql') {
            latestSuccessful()
        }
        shell(shellCommand2)
        
        shell(shellCommand3)
    }
    
    publishers {
        archiveArtifacts("WWWBuildnumber,LPbackendBuildNumber,LPdeployprodBuildNumber,www.gz")

        configure {
            def nodeBuilderPublisher = it / 'publishers'

            def publisherAttributes = [plugin: "s3@0.3.2"]

            def publisherNode = nodeBuilderPublisher / 'hudson.plugins.s3.S3BucketPublisher'(publisherAttributes) {
                profileName 'wsl'
            }

            def entriesPublisher = publisherNode / 'entries' / 'hudson.plugins.s3.Entry'() {
                bucket 'eu-wsl-images/LPbackend/testing/v3/js'
                sourceFile 'tmp2/js/*.js'
            }
        }
        downstream('build_www')
    }
}
