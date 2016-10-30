#!/usr/bin/env bash

HS_HOME=$(cd "$(dirname "$0")"; pwd)"/.."

${HS_HOME}/bin/hs-daemon.sh Master

for line in `cat ${HS_HOME}"/conf/slaves"`
do
    ssh ${line} ${HS_HOME}/bin/hs-daemon.sh Worker
done
