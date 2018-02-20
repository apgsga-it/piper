#!/bin/bash
# add-user-group.sh
#
# adds the unprivileged apg-patch-service-cli user 

if [ "$1" = "1" ]; then
	echo "Creating group: apg-patch-service-cli"
	/usr/sbin/groupadd -f -r apg-patch-service-cli 2> /dev/null || :
	echo "Creating user: apg-patch-service-cli"
	/usr/sbin/useradd -r -m -c "apg-patch-service-cli user" apg-patch-service-cli -g apg-patch-service-cli 2> /dev/null || :
elif [ "$1" = "2" ]; then
	echo "Update Preconditions, nothing to do"
fi

exit 0