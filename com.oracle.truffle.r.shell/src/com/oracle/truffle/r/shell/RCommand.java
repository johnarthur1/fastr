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
package com.oracle.truffle.r.shell;

import static com.oracle.truffle.r.runtime.RCmdOptions.*;

import java.io.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.runtime.*;

import jline.console.*;

/**
 * Emulates the (Gnu)R command as precisely as possible.
 */
public class RCommand {
    // CheckStyle: stop system..print check

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        String[] commandArgs = RCmdOptionsParser.parseArguments(args);
        if (HELP.getValue()) {
            RCmdOptionsParser.printHelp(0);
        } else if (VERSION.getValue()) {
            printVersionAndExit();
        } else if (RHOME.getValue()) {
            printRHomeAndExit();
        }

        if (VANILLA.getValue()) {
            NO_SAVE.setValue(true);
            NO_ENVIRON.setValue(true);
            NO_INIT_FILE.setValue(true);
            NO_RESTORE.setValue(true);
        }

        String fileArg = FILE.getValue();
        if (fileArg != null) {
            if (!SAVE.getValue()) {
                NO_SAVE.setValue(true);
            }
            if (fileArg.equals("-")) {
                // means stdin, but still implies NO_SAVE
                fileArg = null;
            }
        }

        // Whether the input is from stdin, a file or an expression on the command line (-e)
        // it goes through the console. However, we cannot (yet) do incremental parsing, so file
        // input has to be treated specially.
        if (fileArg != null) {
            evalFileInput(fileArg, commandArgs);
        } else {
            InputStream consoleInput = System.in;
            OutputStream consoleOutput = System.out;
            if (EXPR.getValue() != null) {
                consoleInput = new StringBufferInputStream(EXPR.getValue());
            }
            ConsoleReader console = null;
            try {
                console = new RJLineConsoleReader(consoleInput, consoleOutput);
            } catch (IOException ex) {
                Utils.fail("unexpected error opening console reader");
            }
            readEvalPrint(consoleInput == System.in, console, commandArgs);
        }
        // TODO exit code
    }

    private static void printVersionAndExit() {
        System.out.print("FastR version ");
        System.out.println(RVersionNumber.FULL);
        System.out.println(RRuntime.LICENSE);
        System.exit(0);
    }

    private static void printRHomeAndExit() {
        System.out.println(REnvVars.rHome());
        System.exit(0);
    }

    private static void evalFileInput(String filePath, String[] commandArgs) {
        File file = new File(filePath);
        if (!file.exists()) {
            Utils.fail("Fatal error: cannot open file '" + filePath + "': No such file or directory");
        }
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytes = new byte[(int) file.length()];
            is.read(bytes);
            String content = new String(bytes);
            JLineConsoleHandler consoleHandler = new JLineConsoleHandler(false, new ConsoleReader(null, System.out));
            REngine.setRuntimeState(commandArgs, consoleHandler);
            REngine.parseAndEval(content, REngine.createVirtualFrame(), true);
        } catch (IOException ex) {
            Utils.fail("unexpected error reading file input");
        }

    }

    private static void readEvalPrint(boolean isInteractive, ConsoleReader console, String[] commandArgs) {
        System.out.println(RRuntime.WELCOME_MESSAGE);
        try {
            REngine.setRuntimeState(commandArgs, new JLineConsoleHandler(isInteractive, console));
            VirtualFrame globalFrame = REngine.createVirtualFrame();
            for (;;) {
                String line = console.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.equals("") || line.charAt(0) == '#') {
                    continue;
                }

                REngine.parseAndEval(line, globalFrame, true);
            }
        } catch (UserInterruptException e) {
            // interrupted
        } catch (IOException ex) {
            Utils.fail("unexpected error reading console input");
        }

    }

}
