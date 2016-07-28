package com.github.dockerjava.core.command;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.client.AbstractDockerClientTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;


@Test(groups = "integration")
public class KillContainerCmdImplTest extends AbstractDockerClientTest {

    public static final Logger LOG = LoggerFactory.getLogger(KillContainerCmdImplTest.class);

    @BeforeTest
    public void beforeTest() throws Exception {
        super.beforeTest();
    }

    @AfterTest
    public void afterTest() {
        super.afterTest();
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        super.beforeMethod(method);
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
    }

    @Test(groups = "ignoreInCircleCi")
    public void killContainer() throws DockerException {

        CreateContainerResponse container = dockerClient.createContainerCmd(BUSYBOX_IMAGE).withCmd("sleep", "9999").exec();
        LOG.info("Created container: {}", container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainerCmd(container.getId()).exec();

        LOG.info("Killing container: {}", container.getId());
        dockerClient.killContainerCmd(container.getId()).exec();

        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
        LOG.info("Container Inspect: {}", inspectContainerResponse.toString());

        assertThat(inspectContainerResponse.getState().getRunning(), is(equalTo(false)));
        assertThat(inspectContainerResponse.getState().getExitCode(), not(equalTo(0)));

    }

    @Test(expectedExceptions = NotFoundException.class)
    public void killNonExistingContainer() throws DockerException {
        dockerClient.killContainerCmd("non-existing").exec();
    }

    @Test(groups = "ignoreInCircleCi", expectedExceptions = InternalServerErrorException.class)
    public void killStoppedContainer() {
        CreateContainerResponse container = dockerClient.createContainerCmd(BUSYBOX_IMAGE).withCmd("sleep", "9999").exec();
        LOG.info("Created container: {}", container.toString());
        assertThat(container.getId(), not(isEmptyString()));

        dockerClient.startContainerCmd(container.getId()).exec();

        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
        assertThat(inspectContainerResponse.getState().getRunning(), is(true));

        dockerClient.stopContainerCmd(container.getId()).exec();

        InspectContainerResponse inspectContainerResponse1 = dockerClient.inspectContainerCmd(container.getId()).exec();
        assertThat(inspectContainerResponse1.getState().getRunning(), is(false));

        dockerClient.killContainerCmd(container.getId()).exec();

    }

    @Test(groups = "ignoreInCircleCi", expectedExceptions = InternalServerErrorException.class)
    public void killPausedContainer() {
        CreateContainerResponse container = dockerClient.createContainerCmd(BUSYBOX_IMAGE).withCmd("sleep", "9999").exec();
        LOG.info("Created container: {}", container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainerCmd(container.getId()).exec();

        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
        assertThat(inspectContainerResponse.getState().getRunning(), is(true));

        dockerClient.pauseContainerCmd(container.getId()).exec();

        InspectContainerResponse inspectContainerResponse1 = dockerClient.inspectContainerCmd(container.getId()).exec();
        assertThat(inspectContainerResponse1.getState().getPaused(), is(true));

        dockerClient.killContainerCmd(container.getId()).exec();
    }

    @Test(groups = "ignoreInCircleCi", expectedExceptions = InternalServerErrorException.class)
    public void killJustCreatedContainer() {
        CreateContainerResponse container = dockerClient.createContainerCmd(BUSYBOX_IMAGE).withCmd("sleep", "9999").exec();
        LOG.info("Created container: {}", container.toString());
        assertThat(container.getId(), not(isEmptyString()));

        dockerClient.killContainerCmd(container.getId()).exec();
    }

}
