import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.JenkinsTriggerHelper
import com.offbytwo.jenkins.model.BuildWithDetails

JenkinsServer jenkinsServer = new JenkinsServer(new URI('https://jenkins-t.apgsga.ch/'), 'che',
		'c0db2852e2204b72c0ba345446ec2dd0');
JenkinsTriggerHelper jth = new JenkinsTriggerHelper(jenkinsServer, 2000L);
def jobParm = [token:'TEST', PARAMETER:'/var/opt/apg-patch-service-server/db/Patch5695.json']
BuildWithDetails patchBuilderResult = jth.triggerJobAndWaitUntilFinished("testPipelineWithParameters", jobParm,
		true);
println patchBuilderResult.getConsoleOutputText().toString()