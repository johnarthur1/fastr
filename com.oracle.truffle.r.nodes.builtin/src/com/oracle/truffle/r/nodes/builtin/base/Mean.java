/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.runtime.RBuiltinKind.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.data.model.*;
import com.oracle.truffle.r.runtime.ops.*;

@RBuiltin(name = "mean.default", kind = SUBSTITUTE, parameterNames = {"x", "trim", "na.rm", "..."})
public abstract class Mean extends RBuiltinNode {

    @Override
    public RNode[] getParameterValues() {
        return new RNode[]{ConstantNode.create(RMissing.instance), ConstantNode.create(0), ConstantNode.create(RRuntime.LOGICAL_FALSE), ConstantNode.create(RMissing.instance)};
    }

    public abstract Object executeDouble(VirtualFrame frame, RDoubleVector x);

    @Child protected BinaryArithmetic add = BinaryArithmetic.ADD.create();
    @Child protected BinaryArithmetic div = BinaryArithmetic.DIV.create();

    @Specialization
    public double mean(RAbstractDoubleVector x) {
        controlVisibility();
        double sum = x.getDataAt(0);
        for (int k = 1; k < x.getLength(); ++k) {
            sum = add.op(sum, x.getDataAt(k));
        }
        return div.op(sum, x.getLength());
    }

    @Specialization
    public double mean(RAbstractIntVector x) {
        controlVisibility();
        double sum = x.getDataAt(0);
        for (int k = 1; k < x.getLength(); ++k) {
            sum = add.op(sum, x.getDataAt(k));
        }
        return div.op(sum, x.getLength());
    }

    @Specialization
    public double mean(RAbstractLogicalVector x) {
        controlVisibility();
        double sum = x.getDataAt(0);
        for (int k = 1; k < x.getLength(); ++k) {
            sum = add.op(sum, x.getDataAt(k));
        }
        return div.op(sum, x.getLength());
    }

    @Specialization
    public RComplex mean(RAbstractComplexVector x) {
        controlVisibility();
        RComplex sum = x.getDataAt(0);
        RComplex comp;
        for (int k = 1; k < x.getLength(); ++k) {
            comp = x.getDataAt(k);
            sum = add.op(sum.getRealPart(), sum.getImaginaryPart(), comp.getRealPart(), comp.getImaginaryPart());
        }
        return div.op(sum.getRealPart(), sum.getImaginaryPart(), x.getLength(), 0);
    }

}
