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

package org.sonatype.maven.shell.maven.internal;

import org.apache.maven.cli.AbstractMavenTransferListener;

import java.io.PrintStream;

/**
 * Batch console transfer listener.
 *
 * @since 0.9
 */
public class BatchModeMavenTransferListener
    extends AbstractMavenTransferListener
{
    public BatchModeMavenTransferListener(final PrintStream out) {
        super(out);
    }
}