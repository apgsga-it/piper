#!/bin/bash

if [ "$#" -ne 3 ]; then
  echo "Wrong number of parameter: CVS_RSH, CVS_ROOT and processing modus as parameters expected "
  exit
fi

export CVS_RSH=$1
export CVSROOT=$2
PROCESSING_MODUS=$3
echo "Running with CVS_RSH: \"${CVS_RSH}\" CVSROOT: \"$CVSROOT\" and PROCESSING_MODUS: \"${PROCESSING_MODUS}\""

for myDiff in {1..3}; do
  myTag=Ms_patch_$( expr ${myDiff} + $( ls -al /var/opt/apg-patch-service-server/db/Patch*.json | grep -iv log | cut -d '/' -f6 | cut -d 'h' -f2 | cut -d '.' -f1 | tail -n1 ))

  echo "`date +%Y/%m/%d-%H:%M:%S` - Going to create Branch \"${myTag}\""

  if [ -e /tmp/doneBranchWorkaround${myTag} ]; then
    echo "`date +%Y/%m/%d-%H:%M:%S` - won't do it twice for Branch \"${myTag}\""
  else
    touch /tmp/doneBranchWorkaround${myTag}
    for myDbModule in $( cat /var/opt/apg-patch-service-server/metaInfoDb/DbModules.json | grep -v '{' | grep -v '}' | grep -v 'dbModules' | grep -v ']' | tr -d '",\t\r' | tr '\n' ' ' ); do
      echo "`date +%Y/%m/%d-%H:%M:%S` - Branching to \"${myTag}\" on Module \"${myDbModule}\"";
      if [ "$PROCESSING_MODUS" == "production" ]; then
        cvs rtag -r prod -b ${myTag} ${myDbModule}
      else
        echo "Dry run, would run : cvs rtag -r prod -b ${myTag} ${myDbModule}"
      fi
    done
  fi
done
exit
