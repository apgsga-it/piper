#!/bin/bash
# post-uninstall.sh
#
echo "Post Uninstall script: $1"
if [ "$1" = "0" ]; then
	echo "Removing user apg-patch-service-server"
	/usr/sbin/userdel -r apg-patch-service-server 2> /dev/null || :
	echo "Removing group apg-patch-service-server"
	/usr/sbin/groupdel apg-patch-service-server 2> /dev/null || :
fi
exit 0