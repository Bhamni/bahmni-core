#!/bin/sh -x
PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $PATH_OF_CURRENT_SCRIPT/../vagrant-deploy/scripts/vagrant/vagrant_functions.sh

set -e
$PATH_OF_CURRENT_SCRIPT/vagrant-deploy.sh

#invoke migration of openmrs core
run_in_vagrant -c "sudo su - jss -c 'cd /bahmni_temp/ && ./run-liquibase-openmrs.sh'"
#invoke migrations of bahmni core omods
run_in_vagrant -c "sudo su - jss -c 'cd /bahmni_temp/ && ./run-core-bahmni-modules-liquibase.sh'"
run_in_vagrant -c "sudo su - jss -c 'cd /bahmni_temp/ && ./run-openelis-atomfeed-client-liquibase.sh'"
run_in_vagrant -c "sudo su - jss -c 'cd /bahmni_temp/ && ./run-openerp-atomfeed-client-liquibase.sh'"
