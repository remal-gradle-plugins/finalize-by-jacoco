package name.remal.gradle_plugins.finalize_by_jacoco;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getOptionalExtension;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static name.remal.gradle_plugins.toolkit.TaskUtils.onlyIfWithReason;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;
import org.gradle.testing.jacoco.tasks.JacocoBase;
import org.gradle.testing.jacoco.tasks.JacocoReportBase;

public abstract class FinalizeByJacocoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("jacoco");
        configureOnlyIf(project);
        configureFinalizedBy(project);
        configureDependsOn(project);
    }


    private static void configureOnlyIf(Project project) {
        project.getTasks()
            .withType(JacocoReportBase.class)
            .configureEach(it ->
                onlyIfWithReason(it, "Any of the execution data files exists", reportTask ->
                    reportTask.getExecutionData().getFiles().stream()
                        .anyMatch(File::exists)
                )
            );
    }


    private static void configureFinalizedBy(Project project) {
        project.getTasks()
            .matching(task -> !(task instanceof JacocoBase))
            .configureEach(task ->
                task.finalizedBy(project.provider(() -> getFinalizedBy(task)))
            );
    }

    private static List<Object> getFinalizedBy(Task task) {
        var taskExecutionDataFile = getTaskExecutionDataFile(task);
        if (taskExecutionDataFile == null) {
            return emptyList();
        }

        return task.getProject().getTasks().stream()
            .filter(JacocoReportBase.class::isInstance)
            .map(JacocoReportBase.class::cast)
            .filter(reportTask -> {
                var reportExecutionDataFile = getReportExecutionDataFile(reportTask);
                return taskExecutionDataFile.equals(reportExecutionDataFile);
            })
            .collect(toUnmodifiableList());
    }


    private static void configureDependsOn(Project project) {
        project.getTasks()
            .withType(JacocoReportBase.class)
            .configureEach(reportTask ->
                reportTask.dependsOn(project.provider(() -> getDependsOn(reportTask)))
            );
    }

    private static List<Object> getDependsOn(JacocoReportBase reportTask) {
        var reportExecutionDataFile = getReportExecutionDataFile(reportTask);
        if (reportExecutionDataFile == null) {
            return emptyList();
        }

        return reportTask.getProject().getTasks().stream()
            .filter(not(JacocoBase.class::isInstance))
            .filter(task -> {
                var taskExecutionDataFile = getTaskExecutionDataFile(task);
                return reportExecutionDataFile.equals(taskExecutionDataFile);
            })
            .collect(toUnmodifiableList());
    }


    @Nullable
    private static File getTaskExecutionDataFile(Task task) {
        return getOptionalExtension(task, JacocoTaskExtension.class)
            .map(JacocoTaskExtension::getDestinationFile)
            .map(File::getAbsoluteFile)
            .orElse(null);
    }

    @Nullable
    private static File getReportExecutionDataFile(JacocoReportBase reportTask) {
        var files = reportTask.getExecutionData().getFiles();
        if (files.size() != 1) {
            return null;
        }

        return files.iterator().next().getAbsoluteFile();
    }

}
