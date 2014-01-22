/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.griffon.core.compile.processor.editor;

import griffon.metadata.PropertyEditorFor;
import org.kordamp.jipsy.processor.*;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Andres Almiray
 */
@SupportedAnnotationTypes("*")
@SupportedOptions({Options.SPI_DIR_OPTION, Options.SPI_LOG_OPTION, Options.SPI_VERBOSE_OPTION, Options.SPI_DISABLED_OPTION})
public class PropertyEditorProcessor extends AbstractSpiProcessor {
    public static final String NAME = PropertyEditorProcessor.class.getName()
        + " (" + PropertyEditorProcessor.class.getPackage().getImplementationVersion() + ")";

    private static final int MAX_SUPPORTED_VERSION = 8;

    private Persistence persistence;
    private PropertyEditorCollector data;
    private TypeElement propertyEditorType;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SourceVersion[] svs = SourceVersion.values();
        for (int i = svs.length - 1; i >= 0; i--) {
            String name = svs[i].name();
            Matcher m = RELEASE_PATTERN.matcher(name);
            if (m.matches()) {
                int release = Integer.parseInt(m.group(1));
                if (release <= MAX_SUPPORTED_VERSION) return svs[i];
            }
        }

        return SourceVersion.RELEASE_6;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return PropertyEditorFor.class;
    }

    @Override
    protected void initialize() {
        super.initialize();

        persistence = new PropertyEditorPersistence(NAME, options.dir(), processingEnv.getFiler(), logger);
        propertyEditorType = processingEnv.getElementUtils().getTypeElement(PropertyEditor.class.getName());
        data = new PropertyEditorCollector(persistence.getInitializer(), logger);
        data.load();
    }

    @Override
    protected void handleElement(Element e) {
        if (!(e instanceof TypeElement)) {
            return;
        }

        TypeElement currentClass = (TypeElement) e;

        CheckResult checkResult = checkCurrentClass(currentClass);
        if (checkResult.isError()) {
            reportError(currentClass, checkResult);
            return;
        }

        for (TypeElement type : findTypes(currentClass)) {
            CheckResult implementationResult = isImplementation(currentClass, propertyEditorType);
            if (implementationResult.isError()) {
                reportError(currentClass, implementationResult);
            } else {
                register(createProperQualifiedName(type), currentClass);
            }
        }
    }

    @Override
    protected void removeStaleData(RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getRootElements()) {
            if (e instanceof TypeElement) {
                TypeElement currentClass = (TypeElement) e;
                data.removeEditor(createProperQualifiedName(currentClass));
            }
        }
    }

    @Override
    protected void writeData() {
        if (data.isModified()) {
            String content = data.toList();
            if (content.length() > 0) {
                logger.note(LogLocation.LOG_FILE, "Writing output");
                try {
                    persistence.write(PropertyEditor.class.getName(), content);
                } catch (IOException ioe) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ioe.getMessage());
                }
                persistence.writeLog();
            } else {
                logger.note(LogLocation.LOG_FILE, "Writing output");
                try {
                    persistence.delete();
                } catch (IOException e) {
                    logger.warning(LogLocation.LOG_FILE, "An error occurred while deleting data file");
                }
            }
        }
    }

    private CheckResult checkCurrentClass(TypeElement currentClass) {
        if (currentClass.getKind() != ElementKind.CLASS) {
            return CheckResult.valueOf("is not a class");
        }

        if (!currentClass.getModifiers().contains(Modifier.PUBLIC)) {
            return CheckResult.valueOf("is not a public class");
        }

        if (isAbstractClass(currentClass)) {
            return CheckResult.valueOf("is an abstract class");
        }

        if (!hasCorrectConstructor(currentClass)) {
            return CheckResult.valueOf("has no public no-args constructor");
        }

        return CheckResult.OK;
    }

    private List<TypeElement> findTypes(TypeElement classElement) {
        List<TypeElement> types = new ArrayList<>();

        for (AnnotationMirror annotation : findAnnotationMirrors(classElement, getAnnotationClass().getName())) {
            for (AnnotationValue value : findCollectionValueMember(annotation, "value")) {
                types.add(toElement(value));
            }
        }

        return types;
    }

    private void register(String type, TypeElement editorClass) {
        data.getPropertyEditor(type, createProperQualifiedName(editorClass));
    }
}
