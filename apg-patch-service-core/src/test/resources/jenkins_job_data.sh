#!/bin/bash
USERNAME=$1
PASSWD_TOKEN=$2
JENKINS_URL=$3
JOB_NAME=$4
OUTPUT_FILE=$5
CRUMB=`curl -s -u "$USERNAME:$PASSWD_TOKEN" "$JENKINS_URL/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"`
echo "Crumb : $CRUMB"
curl -u "$USERNAME:$PASSWD_TOKEN"  -H "$CRUMB" "http://$JENKINS_URL/job/$JOB_NAME/api/json" > $OUTPUT_FILE
echo "Done."