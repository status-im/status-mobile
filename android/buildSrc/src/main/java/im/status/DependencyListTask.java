package im.status;

import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.diagnostics.AbstractDependencyReportTask;

import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.diagnostics.internal.DependencyReportRenderer;
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer;
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer;

//https://github.com/gradle/gradle/tree/master/subprojects/diagnostics/src/main/java/org/gradle/api/tasks/diagnostics/internal

/**
 * Displays the dependency tree for a project. An instance of this type is used when you
 * execute the {@code dependencies} task from the command-line.
 */
public abstract class DependencyListTask extends AbstractDependencyReportTask {

    @Override
    public ConfigurationContainer getTaskConfigurations() {
        return getProject().getConfigurations();
    }

    @TaskAction
    void action() {

    }
}