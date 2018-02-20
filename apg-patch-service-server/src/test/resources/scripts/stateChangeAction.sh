#!/bin/bash
usage()
{
    echo "Example "
    echo "./stateChangeAction.sh -p=5555 -t=EntwicklungInstallationsbereit|Informatiktestinstallation|Produktionsinstallation|Entwicklung"
    echo ""
}	
for i in "$@"
do
case $i in
	-h|--help*)
   		usage
        exit 1
    ;;
    -t=*|--toState=*)
    TOSTATE="${i#*=}"
    shift # past argument=value
    ;;
    -p=*|--patchnumber=*)
    PATCHNUMBER="${i#*=}"
    shift # past argument=value
    ;;
    *)
          # unknown option
    ;;
esac
done
echo "To State = ${TOSTATE}"
echo "Patchnumber  = ${PATCHNUMBER}"
if [ -z "$TOSTATE" ] ; then
    echo "TOSTATE must be set"; ERROR=true; 
fi
if [ -z "$PATCHNUMBER" ] ; then
    echo "PATCHNUMBER must be set"; ERROR=true; 
fi
if [ ! -z "$ERROR" ] ; then
   usage;exit;
fi
wget -O- --post-data="" --header=Content-Type:application/json "http://localhost:9001/patch/executeStateChangeAction/${PATCHNUMBER}/${TOSTATE}"; 




