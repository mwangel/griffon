/*
 * Copyright 2008-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.core.compile;

import static org.codehaus.griffon.core.compile.MethodDescriptor.annotatedMethod;
import static org.codehaus.griffon.core.compile.MethodDescriptor.args;
import static org.codehaus.griffon.core.compile.MethodDescriptor.type;
import static org.codehaus.griffon.core.compile.MethodDescriptor.types;

/**
 * @author Andres Almiray
 */
public interface ResourcesAwareConstants extends BaseConstants {
    String RESOURCE_HANDLER_TYPE = "griffon.core.resources.ResourceHandler";
    String RESOURCE_HANDLER_PROPERTY = "resourceHandler";

    String METHOD_GET_RESOURCE_AS_URL = "getResourceAsURL";
    String METHOD_GET_RESOURCE_AS_STREAM = "getResourceAsStream";
    String METHOD_GET_RESOURCES = "getResources";
    String METHOD_CLASSLOADER = "classloader";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NULLABLE)),
            type(JAVA_NET_URL),
            METHOD_GET_RESOURCE_AS_URL,
            args(type(types(type(JAVAX_ANNOTATION_NONNULL)), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NULLABLE)),
            type("java.io.InputStream"),
            METHOD_GET_RESOURCE_AS_STREAM,
            args(type(types(type(JAVAX_ANNOTATION_NONNULL)), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NULLABLE)),
            type(JAVA_UTIL_LIST, JAVA_NET_URL),
            METHOD_GET_RESOURCES,
            args(type(types(type(JAVAX_ANNOTATION_NONNULL)), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NONNULL)),
            type("java.lang.ClassLoader"),
            METHOD_CLASSLOADER
        )
    };
}
