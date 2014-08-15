/*
 * Copyright (c) 2014, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.nodes.builtin.processor;

import java.io.*;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

import com.oracle.truffle.r.runtime.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.oracle.truffle.r.runtime.RBuiltin")
public class BuiltinProcessor extends AbstractProcessor {

    /**
     * Set true to trace processor.
     */
    private static boolean trace = false;

    private Map<PackageElement, PackageBuiltins> map;

    private static class PackageBuiltins {
        PackageElement packageElement;
        ArrayList<TypeElement> builtinClassElements = new ArrayList<>();

        PackageBuiltins(PackageElement packageElement) {
            this.packageElement = packageElement;
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment pe) {
        super.init(pe);
        map = new HashMap<>();
        note("BuiltinProcessor.init");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            note("BuiltinProcessor.process");
            if (roundEnv.processingOver()) {
                checkRBuiltin();
                note("writing RBUILTINS");
                writeBuiltinsFiles();
                return true;
            }
            boolean added = false;
            note("BuiltinProcessor: analyzing RBuiltins");
            for (Element element : roundEnv.getElementsAnnotatedWith(RBuiltin.class)) {
                TypeElement classElement = (TypeElement) element;
                PackageElement packageElement = getPackage(classElement);
                PackageBuiltins packageBuiltins = map.get(packageElement);
                if (packageBuiltins == null) {
                    packageBuiltins = new PackageBuiltins(packageElement);
                    map.put(packageElement, packageBuiltins);
                }
                packageBuiltins.builtinClassElements.add(classElement);
                added = true;
            }
            note("BuiltinProcessor.process added=" + added);
        } catch (Exception ex) {
            error("error generating RBUILTINS: " + ex);
        }
        return true;
    }

    private void writeBuiltinsFiles() throws IOException {
        for (PackageBuiltins packageBuiltins : map.values()) {
            String packageName = packageBuiltins.packageElement.getQualifiedName().toString();
            SortedSet<String> classNames = readBuiltinsClass(packageName);
            JavaFileObject srcLocator = processingEnv.getFiler().createSourceFile(packageName + ".RBuiltinClasses");
            try (PrintWriter wr = new PrintWriter(new BufferedWriter(new FileWriter(srcLocator.toUri().getPath())))) {
                wr.printf("package %s;%n", packageName);
                wr.println("public final class RBuiltinClasses {");
                wr.println("    public static final Class<?>[] RBUILTIN_CLASSES = {");
                for (String className : classNames) {
                    wr.printf("        %s.class,%n", className);
                }
                wr.println("    };");
                wr.println("}");
            }

        }
    }

    private SortedSet<String> readBuiltinsClass(String packageName) throws IOException {
        FileObject locator = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, packageName, "RBuiltinClasses.java");
        File file = new File(locator.toUri().getPath());
        SortedSet<String> classNames = new TreeSet<>();
        if (file.exists()) {
            try (BufferedReader rd = new BufferedReader(new FileReader(file))) {
                String line = null;
                boolean started = false;
                while ((line = rd.readLine()) != null) {
                    if (!started) {
                        if (line.contains("RBUILTIN_CLASSES")) {
                            started = true;
                        }
                    } else {
                        if (line.contains("}")) {
                            break;
                        }
                        classNames.add(line.replace(".class,", "").trim());
                    }
                }
            }
        }
        return classNames;
    }

    private static boolean checkRBuiltin() {
        // TODO implement checks
        return true;
    }

    private static PackageElement getPackage(Element element) {
        Element enclosing = element.getEnclosingElement();
        while (!(enclosing instanceof PackageElement)) {
            enclosing = enclosing.getEnclosingElement();
        }
        return (PackageElement) enclosing;
    }

    private void diagnostic(Diagnostic.Kind kind, String msg) {
        processingEnv.getMessager().printMessage(kind, msg);
    }

    @SuppressWarnings("unused")
    private void warning(String msg) {
        diagnostic(Diagnostic.Kind.WARNING, msg);
    }

    private void error(String msg) {
        diagnostic(Diagnostic.Kind.ERROR, msg);
    }

    public void note(String msg) {
        if (trace) {
            diagnostic(Diagnostic.Kind.NOTE, msg);
        }
    }

}
