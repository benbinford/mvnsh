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

package com.sonatype.maven.shell.nexus.internal.wink;

import java.util.Map;

/**
 * {@link javax.ws.rs.core.MultivaluedMap} implementation.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class MultivaluedMapImpl<K,V>
    extends org.apache.wink.common.internal.MultivaluedMapImpl<K,V>
{
    public MultivaluedMapImpl() {
        super();
    }

    public MultivaluedMapImpl(final Map<K,V> map) {
        super(map);
    }
}