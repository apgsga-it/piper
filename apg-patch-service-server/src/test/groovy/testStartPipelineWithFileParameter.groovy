import java.net.URI
import java.util.Map

import com.apgsga.microservice.patch.exceptions.ExceptionFactory
import com.google.common.collect.Maps
import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.JenkinsTriggerHelper
import com.offbytwo.jenkins.model.Build
import com.offbytwo.jenkins.model.BuildWithDetails
import com.offbytwo.jenkins.model.PipelineBuild
import com.offbytwo.jenkins.model.QueueReference

def JenkinsServer jenkinsServer = new JenkinsServer(new URI("https://jenkins-t.apgsga.ch"), "che",
		"c0db2852e2204b72c0ba345446ec2dd0");
JenkinsTriggerHelper jth = new JenkinsTriggerHelper(jenkinsServer, 2000L);
def jobParm = ['token':'TEST', 'PARAMETER':'Someting or other']
def fileParameter = ['test.txt': new File('src/test/resources/test.txt')]
BuildWithDetails result = jth.triggerPipelineJobAndWaitUntilFinished("testPipelineWithFileParameters", jobParm,fileParameter, true)
println result.getConsoleOutputText()

