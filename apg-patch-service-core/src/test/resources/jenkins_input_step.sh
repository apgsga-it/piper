#!/bin/bash
## TODO (che, jhe) : Which scripting host
## TODO (che, jhe) : Parameters
USERNAME="che"
APITOKEN="110e4477bc6617996960b87c9841383ec1"
JSONPARAMETER='{"parameter": [{"name": "testParameter", "value": "Something else Whatever"},
  {"name":"patchFile.json", "file":"file0"}]}'
JENKINSURL="http://172.16.92.140:8080/"
JENKINSJOB="job/TestDeclarativeWithTargetSystemMappings/build"
# shellcheck disable=SC2006
CRUMB=`curl -s -u "$USERNAME:$APITOKEN" '172.16.92.140:8080/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)'`
echo "Crumb : $CRUMB"
curl  -v $JENKINSURL$JENKINSJOB  -s -X POST -u "$USERNAME:$APITOKEN"  -H "$CRUMB" -F file0=@/Users/chhex/git/apg-jenkins-pipelines/src/test/resources/Patch5401.json -F json="$JSONPARAMETER"
