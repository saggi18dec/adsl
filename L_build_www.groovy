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
    steps {
        copyArtifacts('L_Agilemedialab', '*') {
            latestSuccessful()
        }
    }
    parameters {
        stringParam('codebase', 'LPbackend')
    }
    configure {
        def nodeBuilder = it / 'builders'
        def attributes = [plugin:'conditional-buildstep@1.2.2']
        def buildConditionalStepSingleNode = nodeBuilder / 'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder'(attributes)
        def conditionAttributes = [class:"org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition", plugin:'run-condition@0.10']
        def conditionNode = buildConditionalStepSingleNode / 'condition'(conditionAttributes)
        def argFirst = conditionNode / arg1
        argFirst.setValue("")
        def argSecond = conditionNode / arg2
        argSecond.setValue("")
        def argIgnoreCase = conditionNode / ignoreCase
        argIgnoreCase.setValue("false")

        def successAttributes = [class:'hudson.tasks.Shell']
        def successNode = buildConditionalStepSingleNode / 'buildStep'(successAttributes) {
            command 'hello'
        }

        def failureAttributes = [class:'org.jenkins_ci.plugins.run_condition.BuildStepRunner$Fail', 'plugin':'run-condition@0.10']
        def failureNode = buildConditionalStepSingleNode / 'runner'(failureAttributes)
    }
}
