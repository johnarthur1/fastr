/*
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.test;

import com.oracle.truffle.r.runtime.RSource;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.context.RContext.ContextKind;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.oracle.truffle.r.test.generate.FastRSession;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import java.util.concurrent.Callable;
import org.junit.internal.AssumptionViolatedException;

public class TestBase {

    static RContext testVMContext;

    // clear out warnings (which are stored in shared base env)
    private static final Source CLEAR_WARNINGS = FastRSession.createSource("assign('last.warning', NULL, envir = baseenv())", RSource.Internal.CLEAR_WARNINGS.string);
    private static Context context;

    @BeforeClass
    public static void setupClass() {
        FastRSession session = FastRSession.create();
        testVMContext = session.getContext();
        context = session.createContext(ContextKind.SHARE_PARENT_RW);
    }

    @AfterClass
    public static void finishClass() {
        context.eval(CLEAR_WARNINGS);
        context.close();
    }

    protected void execInContext(Callable<Object> c) {
        FastRSession.execInContext(context, c, AssumptionViolatedException.class);
    }
}
