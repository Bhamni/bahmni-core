#!/bin/bash -x -e

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $PATH_OF_CURRENT_SCRIPT/vagrant_functions.sh

#All config is here
MODULE_DEPLOYMENT_FOLDER=/tmp/deploy_bahmni_core
CWD=$1
VERSION=$2
SCRIPTS_DIR=$CWD/scripts/vagrant
PROJECT_BASE=$PATH_OF_CURRENT_SCRIPT/../../..

# Setup environment
run_in_vagrant -f "$SCRIPTS_DIR/setup_environment.sh"

# Kill tomcat
run_in_vagrant -f "$SCRIPTS_DIR/openmrs_stop.sh"

# Deploy Bhamni core
scp_to_vagrant $PROJECT_BASE/bahmnicore-omod/target/bahmnicore*-$VERSION.omod $MODULE_DEPLOYMENT_FOLDER/bahmnicore-$VERSION.omod

# Copy omod files to the vagrant box - in /tmp
scp_to_vagrant $PROJECT_BASE/openmrs-elis-atomfeed-client-omod/target/openelis-atomfeed-client*-$VERSION.omod $MODULE_DEPLOYMENT_FOLDER/openelis-atomfeed-client-$VERSION.omod
scp_to_vagrant $PROJECT_BASE/reference-data/omod/target/reference-data*-$VERSION.omod $MODULE_DEPLOYMENT_FOLDER/reference-data-$VERSION.omod

#Deploy them from Vagrant /tmp to appropriate location
run_in_vagrant -f "$SCRIPTS_DIR/deploy_omods.sh"

# Restart tomcat
run_in_vagrant -f "$SCRIPTS_DIR/openmrs_start.sh"
