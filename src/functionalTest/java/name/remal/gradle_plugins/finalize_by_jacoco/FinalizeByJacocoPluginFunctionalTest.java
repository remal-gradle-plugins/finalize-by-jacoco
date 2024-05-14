package name.remal.gradle_plugins.finalize_by_jacoco;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.testkit.GradleDependencyVersions.getExternalPluginToTestVersion;
import static name.remal.gradle_plugins.toolkit.testkit.GradleDependencyVersions.getJUnitVersion;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class FinalizeByJacocoPluginFunctionalTest {

    final GradleProject project;

    @BeforeEach
    void beforeEach() {
        project.forBuildFile(build -> {
            build.applyPlugin("name.remal.finalize-by-jacoco");

            build.applyPlugin("java");
            build.appendBlock("tasks.test", test -> {
                test.append("useJUnitPlatform()");
            });

            build.addMavenCentralRepository();
            build.appendBlock("dependencies", deps -> {
                deps.append("testImplementation platform('org.junit:junit-bom:" + getJUnitVersion() + "')");
                deps.append("testImplementation 'org.junit.jupiter:junit-jupiter-api'");
                deps.append("testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'");
                deps.append("testRuntimeOnly 'org.junit.platform:junit-platform-launcher'");
            });
        });

        project.writeTextFile("src/test/java/pkg/ClazzTest.java", join(
            "\n",
            "",
            "package pkg;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class ClazzTest {",
            "",
            "    @Test",
            "    void method() {",
            "        // test nothing",
            "    }",
            "",
            "}",
            ""
        ));
    }

    @Test
    void testTaskIsFinalizedByJacoco() {
        project.getBuildFile().registerDefaultTask("test");
        val buildResult = project.assertBuildSuccessfully();

        val successfullyExecutedTasks = buildResult.getTasks().stream()
            .map(BuildTask::getPath)
            .collect(toList());
        assertThat(successfullyExecutedTasks)
            .contains(":jacocoTestReport", ":jacocoTestCoverageVerification");
    }

    @Test
    void jacocoReportDependsOnTest() {
        project.getBuildFile().registerDefaultTask("jacocoTestReport");
        val buildResult = project.assertBuildSuccessfully();

        val successfullyExecutedTasks = buildResult.getTasks().stream()
            .map(BuildTask::getPath)
            .collect(toList());
        assertThat(successfullyExecutedTasks)
            .contains(":test");
    }

    @Test
    void jacocoCoverageVerificationDependsOnTest() {
        project.getBuildFile().registerDefaultTask("jacocoTestCoverageVerification");
        val buildResult = project.assertBuildSuccessfully();

        val successfullyExecutedTasks = buildResult.getTasks().stream()
            .map(BuildTask::getPath)
            .collect(toList());
        assertThat(successfullyExecutedTasks)
            .contains(":test");
    }


    @Nested
    class TestSourceSets {

        @BeforeEach
        void beforeEach() {
            project.getBuildFile().applyPlugin(
                "name.remal.test-source-sets",
                getExternalPluginToTestVersion("name.remal.test-source-sets")
            );
            project.getBuildFile().append("testSourceSets.create('integrationTest')");

            project.writeTextFile("src/integrationTest/java/pkg/ClazzIntegrationTest.java", join(
                "\n",
                "",
                "package pkg;",
                "",
                "import org.junit.jupiter.api.Test;",
                "",
                "class ClazzIntegrationTest {",
                "",
                "    @Test",
                "    void method() {",
                "        // test nothing",
                "    }",
                "",
                "}",
                ""
            ));
        }

        @Test
        void integrationTestTaskIsFinalizedByJacoco() {
            project.getBuildFile().registerDefaultTask("integrationTest");
            val buildResult = project.assertBuildSuccessfully();

            val successfullyExecutedTasks = buildResult.getTasks().stream()
                .map(BuildTask::getPath)
                .collect(toList());
            assertThat(successfullyExecutedTasks)
                .contains(":jacocoIntegrationTestReport", ":jacocoIntegrationTestCoverageVerification");
        }

        @Test
        void jacocoReportDependsOnIntegrationTest() {
            project.getBuildFile().registerDefaultTask("jacocoIntegrationTestReport");
            val buildResult = project.assertBuildSuccessfully();

            val successfullyExecutedTasks = buildResult.getTasks().stream()
                .map(BuildTask::getPath)
                .collect(toList());
            assertThat(successfullyExecutedTasks)
                .contains(":integrationTest");
        }

        @Test
        void jacocoCoverageVerificationDependsOnIntegrationTest() {
            project.getBuildFile().registerDefaultTask("jacocoIntegrationTestCoverageVerification");
            val buildResult = project.assertBuildSuccessfully();

            val successfullyExecutedTasks = buildResult.getTasks().stream()
                .map(BuildTask::getPath)
                .collect(toList());
            assertThat(successfullyExecutedTasks)
                .contains(":integrationTest");
        }

    }

}
