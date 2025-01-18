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
            build.block("tasks.test", test -> {
                test.line("useJUnitPlatform()");
            });

            build.addMavenCentralRepository();
            build.block("dependencies", deps -> {
                deps.line("testImplementation platform('org.junit:junit-bom:" + getJUnitVersion() + "')");
                deps.line("testImplementation 'org.junit.jupiter:junit-jupiter-api'");
                deps.line("testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'");
                deps.line("testRuntimeOnly 'org.junit.platform:junit-platform-launcher'");
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
        val buildResult = project.assertBuildSuccessfully("test");

        val successfullyExecutedTasks = buildResult.getTasks().stream()
            .map(BuildTask::getPath)
            .collect(toList());
        assertThat(successfullyExecutedTasks)
            .contains(":jacocoTestReport", ":jacocoTestCoverageVerification");
    }

    @Test
    void jacocoReportDependsOnTest() {
        val buildResult = project.assertBuildSuccessfully("jacocoTestReport");

        val successfullyExecutedTasks = buildResult.getTasks().stream()
            .map(BuildTask::getPath)
            .collect(toList());
        assertThat(successfullyExecutedTasks)
            .contains(":test");
    }

    @Test
    void jacocoCoverageVerificationDependsOnTest() {
        val buildResult = project.assertBuildSuccessfully("jacocoTestCoverageVerification");

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
            project.getBuildFile().line("testSourceSets.create('integrationTest')");

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
            val buildResult = project.assertBuildSuccessfully("integrationTest");

            val successfullyExecutedTasks = buildResult.getTasks().stream()
                .map(BuildTask::getPath)
                .collect(toList());
            assertThat(successfullyExecutedTasks)
                .contains(":jacocoIntegrationTestReport", ":jacocoIntegrationTestCoverageVerification");
        }

        @Test
        void jacocoReportDependsOnIntegrationTest() {
            val buildResult = project.assertBuildSuccessfully("jacocoIntegrationTestReport");

            val successfullyExecutedTasks = buildResult.getTasks().stream()
                .map(BuildTask::getPath)
                .collect(toList());
            assertThat(successfullyExecutedTasks)
                .contains(":integrationTest");
        }

        @Test
        void jacocoCoverageVerificationDependsOnIntegrationTest() {
            val buildResult = project.assertBuildSuccessfully("jacocoIntegrationTestCoverageVerification");

            val successfullyExecutedTasks = buildResult.getTasks().stream()
                .map(BuildTask::getPath)
                .collect(toList());
            assertThat(successfullyExecutedTasks)
                .contains(":integrationTest");
        }

    }

}
