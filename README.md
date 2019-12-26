# OpenMRS module bahmnicore

This module provides necessary services for running Bahmni

## Build

[![Build Status](https://travis-ci.org/Bahmni/bahmni-core.svg?branch=master)](https://travis-ci.org/Bahmni/bahmni-core)

### Prerequisite
    JDK 1.8
    ruby 2.2+
    RubyGems
    Compass 1.0.3 (gem install compass)
### Clone the repository and build the omod
   
    git clone https://github.com/bahmni/bahmni-core
    cd bahmni-core
    mvn clean install
    
## Deploy

Copy ```bahmni-core/bahmnicore-omod/target/bahmnicore-omod-VERSION-SNAPSHOT.omod``` into OpenMRS modules directory and restart OpenMRS
