import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.JenkinsTriggerHelper
import com.offbytwo.jenkins.model.BuildWithDetails

JenkinsServer jenkinsServer = new JenkinsServer(new URI("https://jenkins.apgsga.ch"), "svcjenkinsclient","22136e2c863d782f50b568f2c2dfdac0");
JenkinsTriggerHelper jth = new JenkinsTriggerHelper(jenkinsServer, 2000L)
def jobParm = [token:"PATCHBUILDER_START",patchnumber:"9063"]
BuildWithDetails patchBuilderResult = jth.triggerJobAndWaitUntilFinished("PatchBuilder", jobParm,true);
if (!patchBuilderResult.getResult().equals(BuildResult.SUCCESS)) {
	println "PatchBuilder failed: " + patchBuilderResult.getResult().toString()
} else {
	println patchBuilderResult.getConsoleOutputText().toString()
}