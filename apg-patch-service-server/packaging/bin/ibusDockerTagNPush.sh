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

if [ $# -ne 2 -o "${1}" == ""  -o  "${2}" == "" ]; then
  myMsg "ERROR: need the following arguments..."
  myMsg "       * arg1: list of service names e.g. \"helloworld-service,vp-digital\""
  myMsg "       * arg2: version of service, patch name respectively e.g. 6547"
  exit 111
fi

SERVICE_NAMES="${1}"
SERVICE_TAG="Cm_patch_${2}"

myMsg "removing eventually existing tags"
for DOCKER_NAME in $( ssh dockerbuild-dev@dockerregistry.apgsga.ch -o "StrictHostKeyChecking no" docker images | grep "${SERVICE_TAG}" | awk '{ print $1; }' ); do
  SERVICE_NAME="$( basename ${DOCKER_NAME} )"
  myMsg "... untagging ${SERVICE_NAME}:${SERVICE_TAG}"
  ssh dockerbuild-dev@dockerregistry.apgsga.ch -o "StrictHostKeyChecking no" /opt/apgops/untag_docker_image.sh ${SERVICE_NAME} ${SERVICE_TAG}
done

myMsg "setting new tags"

for SERVICE_NAME in $( echo ${SERVICE_NAMES} | tr ',; ' ' ' ); do
  myMsg "... tagging ${SERVICE_NAME}:${SERVICE_TAG}"
  ssh dockerbuild-dev@dockerregistry.apgsga.ch -o "StrictHostKeyChecking no" /opt/apgops/tag_docker_image.sh ${SERVICE_NAME} ${SERVICE_TAG}
done

myMsg "<<<<END<<<< `whoami`@`hostname`:`type -p ${0}` ${*}"
