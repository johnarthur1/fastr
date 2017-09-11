/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.ffi.impl.nfi;

import static com.oracle.truffle.r.ffi.impl.common.RFFIUtils.traceDownCall;
import static com.oracle.truffle.r.ffi.impl.common.RFFIUtils.traceDownCallReturn;
import static com.oracle.truffle.r.ffi.impl.common.RFFIUtils.traceEnabled;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.ffi.impl.common.RFFIUtils;
import com.oracle.truffle.r.ffi.impl.nfi.TruffleNFI_CallFactory.TruffleNFI_InvokeCallNodeGen;
import com.oracle.truffle.r.ffi.impl.upcalls.Callbacks;
import com.oracle.truffle.r.ffi.impl.upcalls.FFIUnwrapNode;
import com.oracle.truffle.r.ffi.impl.upcalls.FFIWrapNode;
import com.oracle.truffle.r.ffi.impl.upcalls.UpCallsRFFI;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.ffi.CallRFFI;
import com.oracle.truffle.r.runtime.ffi.DLL;
import com.oracle.truffle.r.runtime.ffi.DLL.SymbolHandle;
import com.oracle.truffle.r.runtime.ffi.NativeCallInfo;
import com.oracle.truffle.r.runtime.ffi.RFFIVariables;

public class TruffleNFI_Call implements CallRFFI {

    private enum INIT_VAR_FUN {
        OBJ("(env, sint32, pointer) : void"),
        DOUBLE("(sint32, double): void"),
        STRING("(sint32, string): void"),
        INT("(sint32, sint32) : void");

        private final String funName;
        private TruffleObject initFunction;
        private final String signature;

        INIT_VAR_FUN(String signature) {
            this.signature = signature;
            funName = "Call_initvar_" + name().toLowerCase();
        }
    }

    public TruffleNFI_Call() {
        initialize();
    }

    private static void initVariables() {
        Node bind = Message.createInvoke(1).createNode();
        for (INIT_VAR_FUN initVarFun : INIT_VAR_FUN.values()) {
            SymbolHandle symbolHandle = DLL.findSymbol(initVarFun.funName, null); // libR
            try {
                initVarFun.initFunction = (TruffleObject) ForeignAccess.sendInvoke(bind, symbolHandle.asTruffleObject(), "bind", initVarFun.signature);
            } catch (InteropException ex) {
                throw RInternalError.shouldNotReachHere(ex);
            }
        }
        Node executeNode = Message.createExecute(2).createNode();
        RFFIVariables[] variables = RFFIVariables.initialize();
        boolean isNullSetting = RContext.getRForeignAccessFactory().setIsNull(false);
        try {
            for (int i = 0; i < variables.length; i++) {
                RFFIVariables var = variables[i];
                Object value = var.getValue();
                if (value == null || var.alwaysUpCall) {
                    continue;
                }
                try {
                    if (value instanceof Double) {
                        ForeignAccess.sendExecute(executeNode, INIT_VAR_FUN.DOUBLE.initFunction, i, value);
                    } else if (value instanceof Integer) {
                        ForeignAccess.sendExecute(executeNode, INIT_VAR_FUN.INT.initFunction, i, value);
                    } else if (value instanceof String) {
                        ForeignAccess.sendExecute(executeNode, INIT_VAR_FUN.STRING.initFunction, i, value);
                    } else {
                        ForeignAccess.sendExecute(executeNode, INIT_VAR_FUN.OBJ.initFunction, i, value);
                    }
                } catch (InteropException ex) {
                    throw RInternalError.shouldNotReachHere(ex);
                }
            }
        } finally {
            RContext.getRForeignAccessFactory().setIsNull(isNullSetting);
        }
    }

    private static void initCallbacks(UpCallsRFFI upCallsImpl) {
        Node bind = Message.createInvoke(1).createNode();
        Node executeNode = Message.createExecute(1).createNode();
        SymbolHandle symbolHandle = DLL.findSymbol("Rinternals_addCallback", null);

        try {
            Callbacks.createCalls(upCallsImpl);
            for (Callbacks callback : Callbacks.values()) {
                String addCallbackSignature = String.format("(env, sint32, %s): void", callback.nfiSignature);
                TruffleObject addCallbackFunction = (TruffleObject) ForeignAccess.sendInvoke(bind, symbolHandle.asTruffleObject(), "bind", addCallbackSignature);
                ForeignAccess.sendExecute(executeNode, addCallbackFunction, callback.ordinal(), callback.call);
            }
        } catch (InteropException ex) {
            throw RInternalError.shouldNotReachHere(ex);
        }
    }

    private static void initialize() {
        RFFIUtils.initializeTracing();
        if (traceEnabled()) {
            traceDownCall("initialize");
        }
        try {
            initCallbacks(new TruffleNFI_UpCallsRFFIImpl());
            initVariables();
        } finally {
            if (traceEnabled()) {
                traceDownCallReturn("initialize", null);
            }
        }
    }

    private static final String[] SIGNATURES = new String[32];

    private static String getSignatureForArity(int arity) {
        CompilerAsserts.neverPartOfCompilation();
        if (arity >= SIGNATURES.length || SIGNATURES[arity] == null) {
            StringBuilder str = new StringBuilder().append('(');
            for (int i = 0; i < arity; i++) {
                str.append(i > 0 ? ", " : "");
                str.append("pointer");
            }
            String signature = str.append("): pointer").toString();
            if (arity < SIGNATURES.length) {
                SIGNATURES[arity] = signature;
            }
            return signature;
        } else {
            return SIGNATURES[arity];
        }
    }

    @ImportStatic(FFIWrapNode.class)
    public abstract static class TruffleNFI_InvokeCallNode extends Node implements InvokeCallNode {

        @Child private Node bindNode = Message.createInvoke(1).createNode();

        @TruffleBoundary
        protected TruffleObject getFunction(TruffleObject address, int arity) {
            // cache signatures
            try {
                return (TruffleObject) ForeignAccess.sendInvoke(bindNode, address, "bind", getSignatureForArity(arity));
            } catch (InteropException ex) {
                throw RInternalError.shouldNotReachHere(ex);
            }
        }

        @Specialization(guards = {"args.length == cachedArgsLength", "nativeCallInfo.address.asTruffleObject() == cachedAddress"})
        protected Object invokeCallCached(NativeCallInfo nativeCallInfo, Object[] args,
                        @SuppressWarnings("unused") @Cached("args.length") int cachedArgsLength,
                        @Cached("create(cachedArgsLength)") FFIWrapNode[] ffiWrapNodes,
                        @Cached("create()") FFIUnwrapNode unwrap,
                        @Cached("createExecute(cachedArgsLength)") Node executeNode,
                        @SuppressWarnings("unused") @Cached("nativeCallInfo.address.asTruffleObject()") TruffleObject cachedAddress,
                        @Cached("getFunction(cachedAddress, cachedArgsLength)") TruffleObject cachedFunction) {
            synchronized (TruffleNFI_Call.class) {
                Object result = null;
                boolean isNullSetting = prepareCall(nativeCallInfo.name, args, ffiWrapNodes);
                try {
                    result = ForeignAccess.sendExecute(executeNode, cachedFunction, args);
                    return unwrap.execute(result);
                } catch (InteropException ex) {
                    throw RInternalError.shouldNotReachHere(ex);
                } finally {
                    prepareReturn(nativeCallInfo.name, result, isNullSetting);
                }
            }
        }

        @Specialization(limit = "99", guards = "args.length == cachedArgsLength")
        protected Object invokeCallCachedLength(NativeCallInfo nativeCallInfo, Object[] args,
                        @Cached("args.length") int cachedArgsLength,
                        @Cached("create(cachedArgsLength)") FFIWrapNode[] ffiWrapNodes,
                        @Cached("create()") FFIUnwrapNode unwrap,
                        @Cached("createExecute(cachedArgsLength)") Node executeNode) {
            synchronized (TruffleNFI_Call.class) {
                Object result = null;
                boolean isNullSetting = prepareCall(nativeCallInfo.name, args, ffiWrapNodes);
                try {
                    result = ForeignAccess.sendExecute(executeNode, getFunction(nativeCallInfo.address.asTruffleObject(), cachedArgsLength), args);
                    return unwrap.execute(result);
                } catch (InteropException ex) {
                    throw RInternalError.shouldNotReachHere(ex);
                } finally {
                    prepareReturn(nativeCallInfo.name, result, isNullSetting);
                }
            }
        }

        public static Node createExecute(int n) {
            return Message.createExecute(n).createNode();
        }
    }

    private static class TruffleNFI_InvokeVoidCallNode extends Node implements InvokeVoidCallNode {
        private static final String CallVoid1Sig = "(pointer): void";
        private static final String CallVoid0Sig = "(): void";
        @Child private Node bindNode = Message.createInvoke(1).createNode();
        @Child private Node execute0Node = Message.createExecute(0).createNode();
        @Child private Node execute1Node = Message.createExecute(1).createNode();
        @Children private final FFIWrapNode[] ffiWrapNodes0 = FFIWrapNode.create(0);
        @Children private final FFIWrapNode[] ffiWrapNodes1 = FFIWrapNode.create(1);

        @Override
        public void execute(NativeCallInfo nativeCallInfo, Object[] args) {
            synchronized (TruffleNFI_Call.class) {
                boolean isNullSetting = true;
                try {
                    switch (args.length) {
                        case 0:
                            isNullSetting = prepareCall(nativeCallInfo.name, args, ffiWrapNodes0);
                            TruffleObject callVoid0Function = (TruffleObject) ForeignAccess.sendInvoke(bindNode, nativeCallInfo.address.asTruffleObject(), "bind", CallVoid0Sig);
                            ForeignAccess.sendExecute(execute0Node, callVoid0Function);
                            break;
                        case 1:
                            isNullSetting = prepareCall(nativeCallInfo.name, args, ffiWrapNodes1);
                            TruffleObject callVoid1Function = (TruffleObject) ForeignAccess.sendInvoke(bindNode, nativeCallInfo.address.asTruffleObject(), "bind", CallVoid1Sig);
                            ForeignAccess.sendExecute(execute1Node, callVoid1Function, args[0]);
                            break;
                        default:
                            throw RInternalError.shouldNotReachHere();
                    }
                } catch (InteropException ex) {
                    throw RInternalError.shouldNotReachHere(ex);
                } finally {
                    prepareReturn(nativeCallInfo.name, null, isNullSetting);
                }
            }
        }
    }

    @ExplodeLoop
    private static boolean prepareCall(String name, Object[] args, FFIWrapNode[] ffiWrapNodes) {
        CompilerAsserts.compilationConstant(ffiWrapNodes.length);
        if (traceEnabled()) {
            traceDownCall(name, args);
        }
        for (int i = 0; i < ffiWrapNodes.length; i++) {
            args[i] = ffiWrapNodes[i].execute(args[i]);
        }
        boolean isNullSetting = RContext.getRForeignAccessFactory().setIsNull(false);
        return isNullSetting;
    }

    private static void prepareReturn(String name, Object result, boolean isNullSetting) {
        if (traceEnabled()) {
            traceDownCallReturn(name, result);
        }
        RContext.getRForeignAccessFactory().setIsNull(isNullSetting);
    }

    @Override
    public InvokeCallNode createInvokeCallNode() {
        return TruffleNFI_InvokeCallNodeGen.create();
    }

    @Override
    public InvokeVoidCallNode createInvokeVoidCallNode() {
        return new TruffleNFI_InvokeVoidCallNode();
    }
}
