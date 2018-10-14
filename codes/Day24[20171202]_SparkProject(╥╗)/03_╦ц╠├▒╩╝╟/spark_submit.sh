#!/bin/sh

SPARK_HOME=/opt/cdh-5.3.6/spark

# submit application
${SPARK_HOME}/bin/spark-submit \
--master yarn \
--deploy-mode cluster \
--config "xxx=yyy" \
--config "yyy=zzz" \
--class $1 \
${SPARK_HOME}/apps/usertrack-0.0.1.jar \
# taskid
$2