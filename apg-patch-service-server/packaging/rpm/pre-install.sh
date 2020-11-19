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
  if id "apg-patch-service-server" &>/dev/null; then
    echo 'apg-patch-service-server already exists '
  else
	  echo "Creating group: apg-patch-service-server"
	  /usr/sbin/groupadd -f -r apg-patch-service-server 2> /dev/null || :
	  echo "Creating user: apg-patch-service-server"
	  /usr/sbin/useradd -r -m -c "apg-patch-service-server user" apg-patch-service-server -g apg-patch-service-server 2> /dev/null || :
	  echo "apg-patch-service-server:apg-patch-service-server" | sudo chpasswd
	  echo "Password has been set for apg-patch-service-server user, you might want to change it."
	fi
fi

sudoFile="/etc/sudoers.d/apg-patch-service-server"
if [ -e "$sudoFile" ]; then
  echo "$sudoFile already exists. It will be deleted and newly created."
  rm $sudoFile
fi
echo "Creating $sudoFile"
echo "Defaults:apg-patch-service-server !requiretty" >> $sudoFile
echo "apg-patch-service-server ALL= (root) NOPASSWD: /bin/rm -Rf /tmp/apg_patch_ui_temp*" >> $sudoFile

exit 0