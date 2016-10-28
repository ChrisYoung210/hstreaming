#!/usr/bin/env bash

HS_HOME=$(cd "$(dirname "$0")"; pwd)"/.."
PACKAGE_PATH=${HS_HOME}"/hstreaeming-1.0.jar"
LOGS_PATH="${HS_HOME}/logs"
MASTER_CLASS="cn.ac.nci.ztb.hs.resource.master.HSMaster"
WORKER_CLASS="cn.ac.nci.ztb.hs.resource.worker.HSWorker"
MASTER_CMD="nohup java -Djava.ext.dirs="${HS_HOME}"/lib -cp "${PACKAGE_PATH}" "${MASTER_CLASS}
WORKER_CMD="nohup java -Djava.ext.dirs="${HS_HOME}"/lib -cp "${PACKAGE_PATH}" "${WORKER_CLASS}
#" >> "${HS_HOME}/logs/worker.log" 2>&1 < /dev/null &"

#create logs directory if not exist.
if [ ! -d ${LOGS_PATH} ]; then
  mkdir ${LOGS_PATH}
fi

nohup ${MASTER_CMD} >> ${HS_HOME}/logs/master.log 2>&1 < /dev/null &

for line in `cat ${HS_HOME}"/conf/slaves"`
do
    ssh ${line} ${WORKER_CMD} >> ${HS_HOME}/logs/worker.log 2>&1 < /dev/null &
done
