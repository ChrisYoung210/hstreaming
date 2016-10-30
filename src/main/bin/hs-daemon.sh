#!/usr/bin/env bash

DaemonType=$1
HS_HOME=$(cd "$(dirname "$0")"; pwd)"/.."
PACKAGE_PATH=${HS_HOME}"/hstreaeming-1.0.jar"
LOGS_PATH="${HS_HOME}/logs"
MASTER_CLASS="cn.ac.nci.ztb.hs.resource.master.HSMaster"
WORKER_CLASS="cn.ac.nci.ztb.hs.resource.worker.HSWorker"

#create logs directory if not exist.
if [ ! -d ${LOGS_PATH} ]; then
  mkdir ${LOGS_PATH}
fi

if [ $DaemonType = "Worker" ]; then
    nohup java -Djava.ext.dirs=${HS_HOME}/lib -cp ${PACKAGE_PATH} ${WORKER_CLASS} >> ${LOGS_PATH}/worker.log 2>&1 < /dev/null &
elif [ $DaemonType = "Master" ]; then
    nohup java -Djava.ext.dirs=${HS_HOME}/lib -cp ${PACKAGE_PATH} ${MASTER_CLASS} >> ${LOGS_PATH}/master.log 2>&1 < /dev/null &
fi

