#!/bin/bash

myMsg() {
  echo "`date +%Y/%m/%d-%H:%M:%S` - ${*}"
}

mySignalHandler() {
  myMsg "Signal ${1} received - exiting"
  if [ -f "/var/jenkins/branchWorkaround/done${myTag}" ]; then
    rm -f /var/jenkins/branchWorkaround/done${myTag}
  fi
  exit 144
}

trap "mySignalHandler 1"   1      # HUP
trap "mySignalHandler 2"   2      # INT
trap "mySignalHandler 3"   3      # QUIT
trap "mySignalHandler 15" 15      # TERM

if [ "$#" -ne 3 ]; then
  myMsg "Wrong number of parameter: CVS_RSH, CVS_ROOT and processing modus (\"production\" or any other value) expected as arguments"
  exit 111
fi

myMsg ">>>START>>> `whoami`@`hostname`:`type -p ${0}` ${*}"

export CVS_RSH=$1
export CVSROOT=$2
PROCESSING_MODE=$3

myMsg "Running with Parameters CVS_RSH:\"${CVS_RSH}\", CVSROOT:\"$CVSROOT\", PROCESSING_MODE:\"${PROCESSING_MODE}\""

CVS_USER=$( echo "${CVSROOT}" | cut -d '@' -f1 | cut -d ':' -f2- | cut -d ':' -f2- )
CVS_HOST=$( echo "${CVSROOT}" | cut -d '@' -f2- | cut -d ':' -f1 )

if [ "${CVS_USER}" != "" -a "${CVS_HOST}" != "" ]; then
  myMsg "Testing connection to CVS-Host: \"ssh ${CVS_USER}@${CVS_HOST}\""
  ssh -o "StrictHostKeyChecking no" ${CVS_USER}@${CVS_HOST} hostname
  if [ $? -ne 0 ]; then
    myMsg "WARNING: potential problems with connection to CVS-Host"
  fi
fi

if [ ! -d "/var/jenkins/branchWorkaround" ]; then
  mkdir -p /var/jenkins/branchWorkaround
fi

if [ -d "/var/jenkins/branchWorkaround" ]; then
  myMsg "Going for pre-emptive Branching"
else
  myMsg "ERROR: Status-Directory \"/var/jenkins/branchWorkaround\" missing - no processing possible"
  exit 118
fi

for myDiff in {1..3}; do
  myTag=Ms_patch_$( expr ${myDiff} + $( ls -al /var/opt/apg-patch-service-server/db/Patch*.json | grep -iv null | grep -iv log | cut -d '/' -f6 | cut -d 'h' -f2 | cut -d '.' -f1 | tail -n1 ))

  myMsg "Going to create Branch \"${myTag}\""

  if [ -f "/var/jenkins/branchWorkaround/done${myTag}" ]; then
    myMsg "... already processed earlier - not going to process \"${myTag}\" twice"
  else
    touch /var/jenkins/branchWorkaround/done${myTag}
    for myDbModule in $( cat /var/opt/apg-patch-service-server/metaInfoDb/DbModules.json | grep -v '{' | grep -v '}' | grep -v 'dbModules' | grep -v ']' | tr -d '",\t\r' | tr '\n' ' ' ); do
      myMsg "Branching to \"${myTag}\" on Module \"${myDbModule}\"";
      if [ "$PROCESSING_MODE" == "production" -o "$PROCESSING_MODE" == "integration" ]; then
        cvs rtag -r prod -b ${myTag} ${myDbModule}
        rtag_rc=$?
        if [ ${rtag_rc} -ne 0 ]; then
          myMsg "... ERROR: something went wrong (rtag rc=${rtag_rc}) - \"${myTag}\" will have to be reprocessed later"
          rm -f /var/jenkins/branchWorkaround/done${myTag}
          exit 117
        fi
      else
        myMsg "Dry run, would run : cvs rtag -r prod -b ${myTag} ${myDbModule}"
      fi
    done
  fi
done

myMsg "<<<<END<<<< `whoami`@`hostname`:`type -p ${0}` ${*}"

exit
