/*
 * Copyright (C) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonatype.maven.shell.commands.maven;

import com.google.inject.Inject;
import jline.Terminal;
import org.apache.maven.execution.MavenExecutionRequest;
import org.sonatype.grrrowl.Growler;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.io.StreamJack;
import org.sonatype.gshell.io.StreamSet;
import org.sonatype.gshell.util.NameValue;
import org.sonatype.gshell.util.Strings;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.util.pref.Preference;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.gshell.vars.Variables;
import org.sonatype.maven.shell.maven.MavenRuntime;
import org.sonatype.maven.shell.maven.MavenRuntimeConfiguration;
import org.sonatype.maven.shell.maven.MavenSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.sonatype.gshell.vars.VariableNames.SHELL_HOME;
import static org.sonatype.gshell.vars.VariableNames.SHELL_USER_DIR;

/**
 * Execute Maven.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
@Command(name = "mvn")
@Preferences(path = "commands/mvn")
public class MavenCommand
    extends CommandActionSupport
{
    @Option(name="f", longName="file", optionalArg=false)
    private File file;

    private Properties props = new Properties();

    @Option(name="D", longName="define", optionalArg=false)
    protected void setProperty(final String input) {
        NameValue nv = NameValue.parse(input);
        props.setProperty(nv.name, nv.value);
    }

    @Preference
    @Option(name="o", longName="offline")
    private boolean offline;

    @Option(name="v", longName="version")
    private boolean version;

    @Preference
    @Option(name="q", longName="quiet")
    private boolean quiet;

    @Preference
    @Option(name="X", longName="debug")
    private boolean debug;

    @Preference
    @Option(name="e", longName="errors")
    private boolean showErrors;

    @Option(name="N", longName="non-recursive")
    private boolean nonRecursive;

    @Option(name="U", longName="update-snapshots")
    private boolean updateSnapshots;

    private List<String> profiles = new ArrayList<String>();

    @Option(name="P", longName="activate-profiles", optionalArg=false)
    private void addProfile(final String profile) {
        assert profile != null;

        for (String p : profile.split(",")) {
            profiles.add(p.trim());
        }
    }

    @Preference
    @Option(name="B", longName="batch-mode")
    private boolean batch;

    @Option(name="cpu", longName="check-plugin-updates")
    private boolean checkPluginUpdates;

    @Option(name="up", longName="update-plugins")
    private boolean updatePlugins;

    @Option(name="npu", longName="no-plugin-updates")
    private boolean noPluginUpdates;

    @Option(name="nsu", longName="no-shapshot-updates")
    private boolean noSnapshotUpdates;

    @Option(name="C", longName="strict-checksums")
    private boolean strictChecksums;

    @Option(name="c", longName="lax-checksums")
    private boolean laxChecksums;

    @Preference
    @Option(name="s", longName="settings", optionalArg=false)
    private File settingsFile;

    @Preference
    @Option(name="gs", longName="global-settings", optionalArg=false)
    private File globalSettingsFile;

    @Preference
    @Option(name="t", longName="toolchains", optionalArg=false)
    private File toolChainsFile;

    @Option(name="ff", longName="fail-fast")
    private boolean failFast;

    @Option(name="fae", longName="fail-at-end")
    private boolean failAtEnd;

    @Option(name="fn", longName="fail-never")
    private boolean failNever;

    @Option(name="rf", longName="resume-from", optionalArg=false)
    private String resumeFrom;

    private List<String> selectedProjects = new ArrayList<String>();

    @Option(name="pl", longName="projects", optionalArg=false)
    private void addSelectedProject(final String project) {
        assert project != null;

        for (String p : project.split(",")) {
            profiles.add(p.trim());
        }
    }

    @Option(name="am", longName="also-make")
    private boolean alsoMake;

    @Option(name="amd", longName="also-make-dependents")
    private boolean alsoMakeDependents;

    @Option(name="l", longName="log-file", optionalArg=false)
    private File logFile;

    @Preference
    @Option(name="V", longName="show-version")
    private boolean showVersion;

    @Argument
    private List<String> goals;

    private static enum Notifications
    {
        BUILD_PASSED, BUILD_FAILED
    }

    private final MavenSystem maven;

    @Preference
    private boolean growl = true;

    private Growler growler;

    @Inject
    public MavenCommand(final MavenSystem maven) {
        assert maven != null;
        this.maven = maven;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        Variables vars = context.getVariables();
        
        if (version) {
            io.info(maven.getVersion());
            return Result.SUCCESS;
        }

        File homeDir = vars.get(SHELL_HOME, File.class);
        System.setProperty("maven.home", homeDir.getAbsolutePath());

        MavenRuntimeConfiguration config = new MavenRuntimeConfiguration();

        config.setBaseDirectory(vars.get(SHELL_USER_DIR, File.class));

        StreamSet current = StreamJack.current();
        StreamSet streams = new StreamSet(current.in, new ColorizingStream(current.out), new ColorizingStream(current.err));
        config.setStreams(streams);

        config.setPomFile(file);
        config.getProfiles().addAll(profiles);
        config.setQuiet(quiet);
        config.setDebug(debug);
        config.setShowVersion(showVersion);
        config.getProperties().putAll(props);
        config.setSettingsFile(settingsFile);
        config.setGlobalSettingsFile(globalSettingsFile);
        config.setLogFile(logFile);

        MavenRuntime runtime = maven.create(config);

        MavenExecutionRequest request = runtime.create();
        request.setOffline(offline);
        request.setGoals(goals);
        request.setInteractiveMode(!batch);
        request.setShowErrors(showErrors);
        request.setRecursive(!nonRecursive);
        request.setUpdateSnapshots(updateSnapshots);
        request.setNoSnapshotUpdates(noSnapshotUpdates);

        if (checkPluginUpdates || updatePlugins) {
            request.setUsePluginUpdateOverride(true);
        }
        else if (noPluginUpdates) {
            request.setUsePluginUpdateOverride(false);
        }

        if (strictChecksums) {
            request.setGlobalChecksumPolicy(MavenExecutionRequest.CHECKSUM_POLICY_FAIL);
        }
        if (laxChecksums) {
            request.setGlobalChecksumPolicy(MavenExecutionRequest.CHECKSUM_POLICY_WARN);
        }

        if (failFast) {
            request.setReactorFailureBehavior(MavenExecutionRequest.REACTOR_FAIL_FAST);
        }
        else if (failAtEnd) {
            request.setReactorFailureBehavior(MavenExecutionRequest.REACTOR_FAIL_AT_END);
        }
        else if (failNever) {
            request.setReactorFailureBehavior(MavenExecutionRequest.REACTOR_FAIL_NEVER);
        }

        request.setSelectedProjects(selectedProjects);

        if (alsoMake && !alsoMakeDependents) {
            request.setMakeBehavior(MavenExecutionRequest.REACTOR_MAKE_UPSTREAM);
        }
        else if (!alsoMake && alsoMakeDependents) {
            request.setMakeBehavior(MavenExecutionRequest.REACTOR_MAKE_DOWNSTREAM);
        }
        else if (alsoMake && alsoMakeDependents) {
            request.setMakeBehavior(MavenExecutionRequest.REACTOR_MAKE_BOTH);
        }

        request.setResumeFrom(resumeFrom);

        StreamJack.register(streams);

        // Execute Maven
        int result = 0;
        try {
            result = runtime.execute(request);
        }
        finally {
            StreamJack.deregister();

            // HACK: Not sure why, but we need to reset the terminal after some mvn builds
            Terminal term = io.getTerminal();
            term.reset();

            // HACK: Attempt to let the VM clean up, no clue if this helps or not
            Thread.yield();
            System.runFinalization();
            Thread.yield();
            System.gc();
            Thread.yield();
        }

        if (growl) {
            if (growler == null) {
                growler = new Growler(getName(), Notifications.class);
                growler.register();
            }

            String cl = String.format("%s %s", getName(), Strings.join(context.getArguments(), " "));

            if (result == 0) {
                growler.growl(
                    Notifications.BUILD_PASSED,
                    "BUILD SUCCESS", // TODO: i18n
                    cl);
            }
            else {
                growler.growl(
                    Notifications.BUILD_FAILED,
                    "BUILD FAILURE", // TODO: i18n
                    cl);
            }
        }

        return result;
    }
}