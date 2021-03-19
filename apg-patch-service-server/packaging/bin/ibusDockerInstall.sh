#!/bin/bash

myMsg() {
  echo "`date +%Y/%m/%d-%H:%M:%S` - ${*}"
}

mySignalHandler() {
  myMsg "Signal ${1} received - exiting"
  exit 144
}


trap "mySignalHandler 1"   1      # HUP
trap "mySignalHandler 2"   2      # INT
trap "mySignalHandler 3"   3      # QUIT
trap "mySignalHandler 15" 15      # TERM

myMsg ">>>START>>> `whoami`@`hostname`:`type -p ${0}` ${*}"

if [ "${1}" == ""  -o  "${2}" == "" ]; then
  myMsg "ERROR: need arguments..."
  myMsg "       * arg1: name of target e.g. chei212"
  myMsg "       * arg2: version of service, patch number respectively e.g. 6547 or 0900.6547"
  exit 111
fi

TARGET_ENVIRONMENT="${1}"
TARGET_ENVIRONMENT="`echo ${TARGET_ENVIRONMENT}|sed -r 's/\.apgsga\.ch+$//i'`"
TARGET_SERVER="service-${TARGET_ENVIRONMENT}.apgsga.ch"

if [ $( echo ${TARGET_ENVIRONMENT} | tr '[:upper:]' '[:lower:]' ) == *"light"* ]; then
  myMsg "sorry, no patching of docker for light environment yet..."
elif [ $( echo ${TARGET_ENVIRONMENT} | tr '[:upper:]' '[:lower:]' ) == *"dev-"* ]; then
  myMsg "sorry, no patching of docker for light environment yet..."
else

  DOCKER_SERVICE=""
  DOCKER_SERVICE_VERSIONS="$( echo ${2} | tr ',' ' ' )"

  for DOCKER_SERVICE_VERSION in ${DOCKER_SERVICE_VERSIONS}; do
    myMsg "looking for Docker Services with Version \"${DOCKER_SERVICE_VERSION}\""

    for i in $( ssh -o "StrictHostKeyChecking no" dockerbuild-dev@dockerregistry.apgsga.ch docker images | grep "Cm_patch_${DOCKER_SERVICE_VERSION}" | awk '{ print $1; }' ); do
      DOCKER_SERVICE="`basename ${i}`"
      myMsg "Patching Docker Service \"${DOCKER_SERVICE}\" to Version \"${DOCKER_SERVICE_VERSION}\" in Environment \"${TARGET_ENVIRONMENT}\" on Server \"${TARGET_SERVER}\""
      myMsg "... ssh root@${TARGET_SERVER} /opt/apgops/update_docker_service.sh ${TARGET_ENVIRONMENT} ${DOCKER_SERVICE} ${DOCKER_SERVICE_VERSION}"
      ssh -o "StrictHostKeyChecking no" root@${TARGET_SERVER} /opt/apgops/update_docker_service.sh ${TARGET_ENVIRONMENT} ${DOCKER_SERVICE} ${DOCKER_SERVICE_VERSION}
    done

  done

  myMsg "<<<<END<<<< `whoami`@`hostname`:`type -p ${0}` ${*}"
fi
