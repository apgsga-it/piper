package com.apgsga.microservice.patch.core.commands.jenkins.ssh.test;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.core.impl.jenkins.TaskCreatePatchPipeline;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TaskCreatePatchPipelineTest extends JenkinsCliBaseTest {

    @Test
    public void testCreatePatchPipelineTest() {
        Patch p = new Patch();
        p.setPatchNummer("3333");
        TaskCreatePatchPipeline.create(JENKINS_HOST, JENKINS_SSH_PORT, JENKINS_SSH_USER,p).run();
    }

    @Test(expected = PatchServiceRuntimeException.class)
    public void testCreatePatchPipelineThrowErrorTest() {
        Patch p = new Patch();
        p.setPatchNummer("3333");
        TaskCreatePatchPipeline.create(JENKINS_HOST, JENKINS_SSH_PORT, "dummy" + JENKINS_SSH_USER, p).run();
    }

}
