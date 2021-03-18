#!/bin/bash

for myDiff in {1..3}; do
  myTag=Ms_patch_$( expr ${myDiff} + $( ls -al /var/opt/apg-patch-service-server/db/Patch*.json | grep -iv log | cut -d '/' -f6 | cut -d 'h' -f2 | cut -d '.' -f1 | tail -n1 ))

  echo "`date +%Y/%m/%d-%H:%M:%S` - Going to create Branch \"${myTag}\""

  if [ -e /tmp/doneBranchWorkaround${myTag} ]; then
    echo "`date +%Y/%m/%d-%H:%M:%S` - won't do it twice for Branch \"${myTag}\""
  else
    touch /tmp/doneBranchWorkaround${myTag}
    for myDbModule in $( cat /var/opt/apg-patch-service-server/metaInfoDb/DbModules.json | grep -v '{' | grep -v '}' | grep -v 'dbModules' | grep -v ']' | tr -d '",\t\r' | tr '\n' ' ' ); do
      echo "`date +%Y/%m/%d-%H:%M:%S` - Branching to \"${myTag}\" on Module \"${myDbModule}\"";
      ssh <%= $cvs_user %>@<%= piper_cvs_remote_host %> cvs -d /var/local/cvs/root rtag -r prod -b ${myTag} ${myDbModule}
    done
  fi
done
exit
