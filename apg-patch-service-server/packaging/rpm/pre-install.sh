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
	echo "Creating group: apg-patch-service-server"
	/usr/sbin/groupadd -f -r apg-patch-service-server 2> /dev/null || :
	echo "Creating user: apg-patch-service-server"
	/usr/sbin/useradd -r -m -c "apg-patch-service-server user" apg-patch-service-server -g apg-patch-service-server 2> /dev/null || :
#	echo "Adding user apg-patch-service-server to *domain users*"
#	/usr/sbin/usermod -G domain\ users apg-patch-service-server 2> /dev/null || :
#	# See JIRA JAVA8MIG-324
#	setfacl -R --modify g:apg-patch-service-server:rwX /var/local/cvs/root
#	setfacl -R --default --modify g:"domain users":rwX,g:jenkins:rwX,g:apg-patch-service-server:rwX /var/local/cvs/root
fi

exit 0