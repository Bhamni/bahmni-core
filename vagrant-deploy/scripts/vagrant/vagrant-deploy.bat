@echo off 

REM !!!!IMPORTANT!!!!
REM Before using this script to deploy do the following
REM Add putty to your path
REM Use puttygen to generate win_insecure_private_key.ppk from your %USERPROFILE%\.vagrant.d\insecure_private_key that comes along with vagrant.
REM !!!End of IMPORTANT!!!

REM All config is here

set MACHINE_IP=192.168.33.10
set MODULE_DEPLOYMENT_FOLDER=/tmp/deploy_bahmni_core
set VERSION=%2
set CWD=%1
set SCRIPTS_DIR=%CWD%/scripts/vagrant
set KEY_FILE=%USERPROFILE%\.vagrant.d\win_insecure_private_key.ppk

if exist %KEY_FILE% (
    REM setup
    putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/setup_environment.sh
    REM Kill tomcat
    putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/tomcat_stop.sh
    REM Deploy Bhamni core
    pscp  -i %KEY_FILE% %CWD%/../../bahmnicore-omod/target/bahmnicore-omod-%VERSION%.omod vagrant@%MACHINE_IP%:%MODULE_DEPLOYMENT_FOLDER%/bahmnicore-%VERSION%.omod
    REM Deploy Open erp atom feed client
    pscp  -i %KEY_FILE% %CWD%/../../openerp-atomfeed-client-omod/target/openerp-atomfeed-client-omod-%VERSION%.omod vagrant@%MACHINE_IP%:%MODULE_DEPLOYMENT_FOLDER%/openerp-atomfeed-client-%VERSION%.omod
    REM Deploy Open elis
    pscp  -i %KEY_FILE% %CWD%/../../openmrs-elis-atomfeed-client-omod/target/openelis-atomfeed-client-omod-%VERSION%.omod vagrant@%MACHINE_IP%:%MODULE_DEPLOYMENT_FOLDER%/openelis-atomfeed-client-%VERSION%.omod
    REM Copy omods into module directories
    putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/deploy_omods.sh
    REM Start tomcat
    putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/tomcat_start.sh
) else (
    echo Use puttygen to generate win_insecure_private_key.ppk from your %USERPROFILE%\.vagrant.d\insecure_private_key that comes along with vagrant.
)

