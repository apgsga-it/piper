#!/usr/bin/env bash
java -Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener -Dspring.profiles.active=dbcli -DappPropertiesFile=file:/etc/opt/apg-patch-cli/app.properties -DopsPropertiesFile=file:/etc/opt/apg-patch-cli/ops.properties -jar /opt/apg-patch-cli/bin/apg-patch-cli.jar pliDb $@  