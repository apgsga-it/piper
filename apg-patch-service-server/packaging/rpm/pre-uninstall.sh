#!/bin/bash
# pre-uninstall.sh
#
# Stopping Service for Uninstall 
echo "Pre Uninstall script: $1"
if [ "$1" = "0" ]; then
	echo "Stopping apg-patch-service-server"
	systemctl stop apg-patch-service-server
fi
exit 0