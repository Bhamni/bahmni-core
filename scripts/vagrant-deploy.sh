#!/bin/sh -x

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $PATH_OF_CURRENT_SCRIPT/../

mvn clean install -DskipTests -Pvagrant-deploy
