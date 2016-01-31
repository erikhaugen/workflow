#!/bin/sh
set -e
MVN=mvn
TWE_VERSION=5.3.1
#CMD="install:install-file"
CMD="-DrepositoryId=nexus-snapshots -Durl=http://devel.net.ilb.ru:28052/nexus/content/repositories/releases deploy:deploy-file"
#svn checkout http://svn.code.sf.net/p/jawe/code/tags/releases/twe-5.1-3 jawe-code
SRC=$HOME/work/jawe-code/twe
svn revert -R $SRC
svn up $SRC
sh $SRC/configure -jdkhome=$JDK_HOME
make -C $SRC debug
OUT=$SRC/output/twe*/lib
for jar in $OUT/*; do
    jarfile=`basename $jar`
    artifactId=${jarfile%.jar}
    $MVN -DartifactId=$artifactId -DgroupId=org.enhydra.jawe -Dversion=$TWE_VERSION -Dpackaging=jar -Dfile=$jar $SRCARTIFACT -DgeneratePom=true $CMD
done
