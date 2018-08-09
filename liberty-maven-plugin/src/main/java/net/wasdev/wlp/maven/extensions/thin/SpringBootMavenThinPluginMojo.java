/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.wasdev.wlp.maven.extensions.thin;

import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.codehaus.mojo.pluginsupport.ant.AntHelper;

import net.wasdev.wlp.ant.SpringBootUtilTask;
import net.wasdev.wlp.maven.plugins.BasicSupport;

//@Mojo(name = "thin", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class SpringBootMavenThinPluginMojo extends BasicSupport {

	
    protected static final ResourceBundle messages = ResourceBundle
            .getBundle("net.wasdev.wlp.maven.plugins.MvnMessages");

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    /**
     * Directory containing the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * Name of the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;

    /**
     *  Liberty profile install directory.
     
    @Parameter(defaultValue = "${liberty.install.dir}", required = true)
    private File installDirectory;
    */
    
    /**
     * Classifier to add to the artifact generated. If given, the artifact will be
     * attached with that classifier and the main artifact will be deployed as the
     * main artifact. If this is not given (default), it will replace the main
     * artifact and only the repackaged artifact will be deployed. Attaching the
     * artifact allows to deploy it alongside to the original one, see <a href=
     * "http://maven.apache.org/plugins/maven-deploy-plugin/examples/deploying-with-classifiers.html"
     * > the maven documentation for more details</a>.
     * 
     * @since 1.0
     */
    @Parameter
    private String classifier;

    @Component(role = AntHelper.class)
    protected AntHelper ant;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	init();
        try {
            File sourceFatJar = getTargetFile();
            invokeSpringBootUtilCommand(sourceFatJar);
        } catch (BuildException e) {
            throw new MojoExecutionException("Plugin execution failed while runnning springBootUtil utility", e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /*
    private void thin(File sourceFatJar)
            throws ZipException, IOException, NoSuchAlgorithmException, SpringBootThinException {
        File targetThinJar = new File(outputDirectory, "thin-" + sourceFatJar.getName());
        File libIndexCache = new File(outputDirectory, "lib.index.cache");
        getLog().info("Thinning " + getArtifactExtension() + ": " + targetThinJar.getAbsolutePath());
        getLog().info("Lib index cache: " + libIndexCache.getAbsolutePath());
        SpringBootThinUtil thinUtil = new SpringBootThinUtil(sourceFatJar, targetThinJar, libIndexCache, null);
        thinUtil.execute();
    }
    */

    
    private void invokeSpringBootUtilCommand(File sourceFatJar) {
    	System.out.println("\n\n**********INVOKING SPRINGBOOTUTIL***********\n\n");
    	System.out.println("install directory: " + installDirectory);
    	SpringBootUtilTask springBootUtilTask = (SpringBootUtilTask) ant.createTask("antlib:net/wasdev/wlp/ant:springBootUtil");

        if (springBootUtilTask == null) {
            throw new IllegalStateException(MessageFormat.format(messages.getString("error.dependencies.not.found"), "springBootUtil"));
        }

        springBootUtilTask.setInstallDir(installDirectory);
        springBootUtilTask.setTargetThinAppPath(new File(outputDirectory, "thin-" + sourceFatJar.getName()).getAbsolutePath());
        springBootUtilTask.setSourceAppPath(sourceFatJar.getAbsolutePath());
        springBootUtilTask.setTargetLibCachePath(new File(outputDirectory, "lib.index.cache").getAbsolutePath());
        springBootUtilTask.execute();
    }

    private File getTargetFile() {
        String classifier = (this.classifier == null ? "" : this.classifier.trim());
        if (!classifier.isEmpty() && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }
        return new File(this.outputDirectory, this.finalName + classifier + "." + getArtifactExtension());
    }

    private String getArtifactExtension() {
        return this.project.getArtifact().getArtifactHandler().getExtension();
    }

}
