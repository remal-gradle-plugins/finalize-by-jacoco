package name.remal.gradle_plugins.finalize_by_jacoco;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class FinalizeByJacocoPluginAppliedViaSettingsFunctionalTest {

    final GradleProject project;

    @Test
    void appliedViaSettingsIsAppliedToProject() {
        project.forSettingsFile(settings -> settings.applyPlugin("name.remal.finalize-by-jacoco"));

        // The plugin must NOT be applied via the project's build file: it should reach the project
        // solely through the Settings-level application propagating via GradleLifecycle.beforeProject.
        // The assertion runs at configuration time (not inside doLast/doFirst), because reading
        // `project` at execution time is unsupported with the configuration cache, which this
        // plugin enables for its Jacoco report tasks.
        project.getBuildFile().line(
            "tasks.register('assertPluginApplied') { assert"
                + " project.pluginManager.hasPlugin('name.remal.finalize-by-jacoco') }"
        );

        project.assertBuildSuccessfully("assertPluginApplied");
    }

}
