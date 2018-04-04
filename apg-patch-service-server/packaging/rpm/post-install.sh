#!/bin/bash
# post-install.sh
#
echo "Post Install script: $1"
echo "Setting permissions on /var/opt/apg-patch-service-server"
chown -R apg-patch-service-server:jenkins /var/opt/apg-patch-service-server
if [ "$1" = "1" ]; then
	echo "Registering apg-patch-service-server for Boot time start"
	systemctl enable apg-patch-service-server
	echo "Starting apg-patch-service-server"
	systemctl start apg-patch-service-server
fi
if [ "$1" = "2" ]; then
	echo "Starting apg-patch-service-server"
	systemctl start apg-patch-service-server
fi

exit 0