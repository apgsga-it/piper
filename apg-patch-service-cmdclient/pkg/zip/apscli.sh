#!/usr/bin/env bash
java -Dspring.profiles.active=live,remotecvs,groovyactions -cp "lib/apg-patch-cli-fat.jar:." -Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener -DappPropertiesFile=conf/application.properties pliStarter "$@"
