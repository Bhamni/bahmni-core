#!/bin/sh -x
set +e
sudo fuser -k 8080/tcp
set -e
