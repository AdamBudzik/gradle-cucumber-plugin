package com.excella.gradle.cucumber.tasks

import org.apache.tools.ant.AntClassLoader
import org.gradle.api.DefaultTask
import org.gradle.api.UncheckedIOException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

/**
 *
 * User: samuelbrown
 * Date: 7/18/12
 * Time: 11:36 PM
 *
 */


class CucumberTask extends DefaultTask  {

    def runner
    List<String> glueDirs
    List<String> tags
    List<String> formats
    boolean strict
    boolean monochrome
    boolean dryRun
    FileCollection buildscriptClasspath
    FileCollection cucumberClasspath

    @TaskAction
    def cucumber() {
        LOGGER.info "Configuring Cucumber for ${getProject()}"

        ClassLoader originalClassLoader = getClass().classLoader
        URLClassLoader cucumberClassloader = createCucumberClassLoader()

        try {
            Thread.currentThread().contextClassLoader = cucumberClassloader
            executeCucumberRunner()
        }
        finally {
            Thread.currentThread().contextClassLoader = originalClassLoader
        }

    }

    /**
     * Creates Cucumber ClassLoader which consists of the Gradle runtime, Cucumber runtime and plugin classpath. The ClassLoader
     * is using a parent last strategy to make sure that the provided Gradle libraries get loaded only if they can't be
     * found in the application classpath. Borrowed from Ben Muschko and the gradle tomcat plugin:
     * https://github.com/bmuschko/gradle-tomcat-plugin
     *
     * @return Cucumber ClassLoader
     */
    private URLClassLoader createCucumberClassLoader() {
        ClassLoader rootClassLoader = new AntClassLoader(getClass().classLoader, false)
        URLClassLoader pluginClassloader = new URLClassLoader(toURLArray(getBuildscriptClasspath().files), rootClassLoader)
        new URLClassLoader(toURLArray(getCucumberClasspath().files), pluginClassloader)
    }

    private URL[] toURLArray(Collection<File> files) {
        List<URL> urls = new ArrayList<URL>(files.size())

        for(File file : files) {
            try {
                urls.add(file.toURI().toURL())
            }
            catch(MalformedURLException e) {
                throw new UncheckedIOException(e)
            }
        }

        urls.toArray(new URL[urls.size()]);
    }

    private void executeCucumberRunner(){
        runner.runCucumberTests getGlueDirs(), getTags(), getFormats(), getStrict(), getMonochrome(), getDryRun()
    }
}
