package com.excella.gradle.cucumber

import com.excella.gradle.cucumber.tasks.CucumberTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 
 * 
 * @author Sam Brown
 * @since 7/18/12
 * @version 1.0
 *
 * Copyright 2012, Excella Consulting
 * 
 */
class CucumberPlugin  implements Plugin<Project> {
    Project project

    @Override
    void apply(Project project) {
        this.project = project
        project.apply plugin: 'java'

        project.extensions.cucumberRunner = new CucumberRunner()

        project.convention.plugins.cucumber = new CucumberConvention(project);

        if (!project.configurations.asMap['cucumberRuntime']) {
            project.configurations.add('cucumberRuntime') {
                extendsFrom project.configurations['testRuntime']
            }
        }

        project.tasks.withType(CucumberTask).whenTaskAdded {
            configureCucumberTask it
        }
        CucumberTask cucumberTask = project.tasks.add(name: 'cucumber', dependsOn: ['assemble'], type: CucumberTask)
        cucumberTask.description = "Run Cucumber Acceptance Test"
        cucumberTask.group = "Verification"

        project.dependencies.add('testRuntime',  "info.cukes:cucumber-junit:${project.cucumberJvmVersion}")
    }

    private def configureCucumberTask(CucumberTask cucumberTask) {
        cucumberTask.runner = project.cucumberRunner
        cucumberTask.conventionMapping.map('glueDirs') {
            project.glueDirs
        }
        cucumberTask.conventionMapping.map('tags') {
            project.tags
        }
        cucumberTask.conventionMapping.map('formats') {
            project.formats
        }
        cucumberTask.conventionMapping.map('strict') {
            project.strict
        }
        cucumberTask.conventionMapping.map('monochrome') {
            project.monochrome
        }
        cucumberTask.conventionMapping.map('dryRun') {
            project.dryRun
        }
        project.gradle.taskGraph.whenReady {
            project.dependencies.add('cucumberRuntime',  files("${jar.archivePath}"))
            cucumberTask.classpath = project.configurations['cucumberRuntime']

        }

    }
}
