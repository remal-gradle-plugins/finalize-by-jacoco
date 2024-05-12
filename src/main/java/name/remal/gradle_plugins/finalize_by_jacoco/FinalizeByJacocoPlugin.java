package name.remal.gradle_plugins.finalize_by_jacoco;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getOptionalExtension;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;

import java.io.File;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;
import org.gradle.testing.jacoco.tasks.JacocoBase;
import org.gradle.testing.jacoco.tasks.JacocoReportBase;

public abstract class FinalizeByJacocoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("jacoco", __ -> {
            configureFinalizedBy(project);
            configureDependsOn(project);
        });
    }


    private void configureFinalizedBy(Project project) {
        project.getTasks()
            .matching(task -> !(task instanceof JacocoBase))
            .configureEach(task ->
                task.finalizedBy(project.provider(() -> getFinalizedBy(task)))
            );
    }

    private List<Object> getFinalizedBy(Task task) {
        val taskExecutionDataFile = getOptionalExtension(task, JacocoTaskExtension.class)
            .map(JacocoTaskExtension::getDestinationFile)
            .map(File::getAbsoluteFile)
            .orElse(null);
        if (taskExecutionDataFile == null) {
            return emptyList();
        }

        return task.getProject().getTasks().stream()
            .filter(JacocoReportBase.class::isInstance)
            .map(JacocoReportBase.class::cast)
            .filter(reportTask -> {
                val reportExecutionDataFile = singleOrNullFile(reportTask.getExecutionData().getFiles());
                if (reportExecutionDataFile == null) {
                    return false;
                }
                return reportExecutionDataFile.equals(taskExecutionDataFile);
            })
            .collect(toList());
    }


    private void configureDependsOn(Project project) {
        project.getTasks()
            .withType(JacocoReportBase.class)
            .configureEach(reportTask ->
                reportTask.dependsOn(project.provider(() -> getDependsOn(reportTask)))
            );
    }

    private List<Object> getDependsOn(JacocoReportBase reportTask) {
        val reportExecutionDataFile = singleOrNullFile(reportTask.getExecutionData().getFiles());
        if (reportExecutionDataFile == null) {
            return emptyList();
        }

        return reportTask.getProject().getTasks().stream()
            .filter(not(JacocoBase.class::isInstance))
            .filter(task -> {
                val taskExecutionDataFile = getOptionalExtension(task, JacocoTaskExtension.class)
                    .map(JacocoTaskExtension::getDestinationFile)
                    .map(File::getAbsoluteFile)
                    .orElse(null);
                if (taskExecutionDataFile == null) {
                    return false;
                }

                return taskExecutionDataFile.equals(reportExecutionDataFile);
            })
            .collect(toList());
    }


    @Nullable
    private static File singleOrNullFile(Collection<? extends File> collection) {
        if (collection.size() == 1) {
            return collection.iterator().next().getAbsoluteFile();
        }

        return null;
    }

}
