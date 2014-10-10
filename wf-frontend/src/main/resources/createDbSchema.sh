#!/bin/sh
PATH=`pwd`

echo “start to create db schema”

CLASSPATH=${JAVA_HOME}:$PATH:${CLASSPATH}
## application libs
for jar in `find $PATH/WEB-INF/lib -print`; do
	CLASSPATH=${CLASSPATH}:${jar}
done

java -classpath ${CLASSPATH} \
	org.wf.service.utils.CreateDbTables

echo “create db schema finished.”

exit 0