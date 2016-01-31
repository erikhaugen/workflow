#!/bin/sh
set -e
MVN=mvn
#CMD="install:install-file"
CMD="-DrepositoryId=nexus-snapshots -Durl=http://devel.net.ilb.ru:28052/nexus/content/repositories/releases deploy:deploy-file"
#CMD="-DrepositoryId=nexus-snapshots -Durl=http://devel.net.ilb.ru:28052/nexus/content/repositories/snapshots deploy:deploy-file"
REPO=http://svn.code.sf.net/p/sharkwf/code/tags/releases/tws-6.1-3
SRC=$HOME/work/sharkwf-code/tws
svn revert -R $SRC
svn up $SRC
TWS_VERSION=6.3.1
echo building $TWS_VERSION
cat SharkAPI.diff | patch -d $SRC -p0 # -N -r -
sh $SRC/configure -jdkhome=$JDK_HOME
make -C $SRC buildNoDoc
OUT=$SRC/Shark/output/lib
for jar in $OUT/sharkadminapi.jar $OUT/sharkclientapi.jar $OUT/sharkcommonapi.jar $OUT/sharkcorbaclientapi.jar $OUT/sharkinternalapi.jar $OUT/sharkwebservice-asapapi.jar  $OUT/sharkwebservice-wfxmlapi.jar; do
    jarfile=`basename $jar`
    artifactId=${jarfile%.jar}
    SRCARTIFACT=""
    if [ -f $OUT/$artifactId-sources.jar ]; then
        SRCARTIFACT="-Dsources=$OUT/$artifactId-sources.jar"
    fi
    $MVN -DartifactId=$artifactId -DgroupId=org.enhydra.shark.api -Dversion=$TWS_VERSION -Dpackaging=jar -Dfile=$jar $SRCARTIFACT -DgeneratePom=true $CMD
done
for jar in $OUT/client/*.jar; do
    jarfile=`basename $jar`
    artifactId=${jarfile%.jar}
    SRCARTIFACT=""
    if [ -f $OUT/client/$artifactId-sources.jar ]; then
        SRCARTIFACT="-Dsources=$OUT/client/$artifactId-sources.jar"
    fi
    $MVN -DartifactId=$artifactId -DgroupId=org.enhydra.shark.client -Dversion=$TWS_VERSION -Dpackaging=jar -Dfile=$jar $SRCARTIFACT -DgeneratePom=true $CMD
done
for jar in $OUT/engine/*.jar; do
    jarfile=`basename $jar`
    artifactId=${jarfile%.jar}
    SRCARTIFACT=""
    if [ -f $OUT/engine/$artifactId-sources.jar ]; then
        SRCARTIFACT="-Dsources=$OUT/engine/$artifactId-sources.jar"
    fi
    $MVN -DartifactId=$artifactId -DgroupId=org.enhydra.shark.engine -Dversion=$TWS_VERSION -Dpackaging=jar -Dfile=$jar $SRCARTIFACT -DgeneratePom=true $CMD
done
for jar in $OUT/contrib/jxpdl*.jar $OUT/contrib/tro* $OUT/contrib/ttt* $OUT/contrib/twe* $OUT/contrib/teu* $OUT/contrib/tll*; do
    jarfile=`basename $jar`
    artifactId=${jarfile%.jar}
    $MVN -DartifactId=$artifactId -DgroupId=org.enhydra.shark.contrib -Dversion=$TWS_VERSION -Dpackaging=jar -Dfile=$jar $SRCARTIFACT -DgeneratePom=true $CMD
done
for jar in $SRC/tools/taf/taf*.jar ; do
    jarfile=`basename $jar`
    artifactId=${jarfile%.jar}
    $MVN -DartifactId=$artifactId -DgroupId=org.enhydra.shark.taf -Dversion=$TWS_VERSION -Dpackaging=jar -Dfile=$jar $SRCARTIFACT -DgeneratePom=true $CMD
done
