#!/bin/bash
# post-install.sh
#
echo "Post Install script"


if [ ! -d "/var/opt/apg-patch-cli" ]; then

    echo "Creating and setting permissions on /var/opt/apg-patch-cli"
	mkdir /var/opt/apg-patch-cli

else

	echo "/var/opt/apg-patch-cli already exist."

fi

chown -R jenkins:jenkins /var/opt/apg-patch-cli

exit 0