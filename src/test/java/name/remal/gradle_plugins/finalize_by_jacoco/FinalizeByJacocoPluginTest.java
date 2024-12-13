package name.remal.gradle_plugins.finalize_by_jacoco;

import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.packageNameOf;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static name.remal.gradle_plugins.toolkit.testkit.ProjectValidations.executeAfterEvaluateActions;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.TaskValidations;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class FinalizeByJacocoPluginTest {

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply(FinalizeByJacocoPlugin.class);
    }

    @Test
    void pluginTasksDoNotHavePropertyProblems() {
        project.getPluginManager().apply("java");
        project.getPluginManager().apply("jacoco");

        executeAfterEvaluateActions(project);

        val taskClassNamePrefix = packageNameOf(FinalizeByJacocoPlugin.class) + '.';
        project.getTasks().stream()
            .filter(task -> {
                val taskClass = unwrapGeneratedSubclass(task.getClass());
                return taskClass.getName().startsWith(taskClassNamePrefix);
            })
            .forEach(TaskValidations::assertNoTaskPropertiesProblems);
    }

}