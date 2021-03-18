#!/bin/bash

myMsg() {
  echo "`date +%Y/%m/%d-%H:%M:%S` - ${*}"
}

mySignalHandler() {
  myMsg "Signal ${1} received - exiting"
  if [ -f "/tmp/doneBranchWorkaround${myTag}" ]; then
    rm -f /tmp/doneBranchWorkaround${myTag}
  fi
  exit 144
}


if [ "$#" -ne 3 ]; then
  myMsg "Wrong number of parameter: CVS_RSH, CVS_ROOT and processing modus (\"production\" or any other value) expected as arguments"
  exit 111
fi

export CVS_RSH=$1
export CVSROOT=$2
PROCESSING_MODUS=$3

myMsg "Running with CVS_RSH: \"${CVS_RSH}\" CVSROOT: \"$CVSROOT\" and PROCESSING_MODUS: \"${PROCESSING_MODUS}\""

trap "mySignalHandler 1"   1      # HUP
trap "mySignalHandler 2"   2      # INT
trap "mySignalHandler 3"   3      # QUIT
trap "mySignalHandler 15" 15      # TERM

for myDiff in {1..3}; do
  myTag=Ms_patch_$( expr ${myDiff} + $( ls -al /var/opt/apg-patch-service-server/db/Patch*.json | grep -iv log | cut -d '/' -f6 | cut -d 'h' -f2 | cut -d '.' -f1 | tail -n1 ))

  myMsg "Going to create Branch \"${myTag}\""

  if [ -f "/tmp/doneBranchWorkaround${myTag}" ]; then
    myMsg "not going to process \"${myTag}\" twice"
  else
    touch /tmp/doneBranchWorkaround${myTag}
    for myDbModule in $( cat /var/opt/apg-patch-service-server/metaInfoDb/DbModules.json | grep -v '{' | grep -v '}' | grep -v 'dbModules' | grep -v ']' | tr -d '",\t\r' | tr '\n' ' ' ); do
      myMsg "Branching to \"${myTag}\" on Module \"${myDbModule}\"";
      if [ "$PROCESSING_MODUS" == "production" ]; then
        cvs rtag -r prod -b ${myTag} ${myDbModule}
        if [ $? -ne 0 ]; then
          rm -f /tmp/doneBranchWorkaround${myTag}
        fi
      else
        myMsg "Dry run, would run : cvs rtag -r prod -b ${myTag} ${myDbModule}"
      fi
    done
  fi
done

exit
