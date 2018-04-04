#!/bin/bash
# pre-install.sh
#
# Stopping Service for update 
echo "Pre Install script: $1"
if [ "$1" = "2" ]; then
	echo "Stopping apg-patch-service-server"
	systemctl stop apg-patch-service-server
fi
if [ "$1" = "1" ]; then
	#echo "Creating group: apg-patch-service-server"
	#/usr/sbin/groupadd -f -r apg-patch-service-server 2> /dev/null || :
	echo "Creating user: apg-patch-service-server"
	# TODO (che, 4.4.2018 ) : Temp fix for cvs right , resp AD domain user 
	/usr/sbin/useradd -r -m -c "apg-patch-service-server user" apg-patch-service-server -g jenkins 2> /dev/null || :
fi
exit 0