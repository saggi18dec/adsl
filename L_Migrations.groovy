def shellCommand =
'''
git diff `git log -1 | grep Merge: | cut -f 2 -d ':'` | grep 'app/DoctrineMigration' || exit 0
sh bin/update-lp2conf.sh
composer.phar self-update
composer.phar update
mysql -u root testdb < testdb.sql
rm -rf  *.sql
ant schema-update
mysqldump -u root testdb > testdb.sql
'''

job {
    name 'L_Migrations'
    customWorkspace("/var/www/LPbackend")
    scm {
        git('git@github.com:agilemedialab/LPbackend.git', 'origin/master')
    }
    label('F_job_slave')
    logRotator(-1,30)
    steps {
        copyArtifacts('F_Migrations', '*') {
            latestSuccessful()
        }
        shell(shellCommand)
    }
    publishers {
        archiveArtifacts("testdb.sql")
    }
}
