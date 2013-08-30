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
        git('git@github.com:agilemedialab/LPbackend.git', 'origin/master')
    }
    label('F_job_slave')
    logRotator(-1,30)
    parameters {
        stringParam('codebase', 'LPbackend')
    }
}