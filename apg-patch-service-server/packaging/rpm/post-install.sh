#!/bin/bash
# post-install.sh
#
echo "Post Install script: $1"
echo "Setting permissions on /var/opt/apg-patch-service-server"
chown -R apg-patch-service-server:apg-patch-service-server /var/opt/apg-patch-service-server
echo "Setting permissions on /etc/opt/apg-patch-service-server"
chown -R apg-patch-service-server:apg-patch-service-server /etc/opt/apg-patch-service-server
if [ "$1" = "1" ]; then
	echo "Registering apg-patch-service-server for Boot time start"
	systemctl enable apg-patch-service-server
	## On initial  install of Rpm the server is  not startet, puppet plan will do this
	## echo "Starting apg-patch-service-server"
	## systemctl start apg-patch-service-server
fi
if [ "$1" = "2" ]; then
	echo "Starting apg-patch-service-server"
	systemctl start apg-patch-service-server
fi

exit 0