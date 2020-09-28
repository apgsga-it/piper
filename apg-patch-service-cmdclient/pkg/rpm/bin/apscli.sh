#!/usr/bin/env bash
java -Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener -jar /opt/apg-patch-cli/bin/apg-patch-cli-fat.jar $@