/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.dcc.kde;

import com.google.common.io.CharStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Hold a Process and stream readers for a KDialog Process.
 */
public class KDialogProcess {

    /** Is kdialog in /bin? */
    private static final boolean IS_BIN = new File("/bin/kdialog").exists();
    /** Does KDialog exist? */
    private static final boolean HAS_KDIALOG = IS_BIN || new File("/usr/bin/kdialog").exists();
    /** Output from the output stream. */
    private final List<String> stdOutput;
    /** Output from the error stream. */
    private final List<String> stdError;
    /** The actual process for this process. */
    private final Process process;

    /**
     * Execute kdialog with the Parameters in params.
     *
     * @param params Parameters to pass to kdialog
     *
     * @throws IOException if an I/O error occurs
     */
    public KDialogProcess(final String[] params) throws IOException {
        final String[] exec = new String[params.length + 1];
        System.arraycopy(params, 0, exec, 1, params.length);
        exec[0] = IS_BIN ? "/bin/kdialog" : "/usr/bin/kdialog";
        process = Runtime.getRuntime().exec(exec);
        stdOutput = CharStreams.readLines(new InputStreamReader(process.getInputStream()));
        stdError = CharStreams.readLines(new InputStreamReader(process.getErrorStream()));
    }

    /**
     * Does this system have kdialog?
     *
     * @return True if kdialog was found, else false
     */
    public static boolean hasKDialog() {
        return HAS_KDIALOG;
    }

    /**
     * Get the process object for this KDialogProcess.
     *
     * @return The process object for this KDialogProcess
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get the StreamReader for this KDialogProcess's stdout stream.
     *
     * @return The StreamReader for this KDialogProcess's stdout stream
     */
    public List<String> getStdOut() {
        return stdOutput;
    }

    /**
     * Get the StreamReader for this KDialogProcess's stderr stream.
     *
     * @return The StreamReader for this KDialogProcess's stderr stream
     */
    public List<String> getStdErr() {
        return stdError;
    }

    /**
     * Wait for the process to finish.
     *
     * @throws InterruptedException if the current thread is interrupted by another thread while it
     *                              is waiting
     */
    public void waitFor() throws InterruptedException {
        process.waitFor();
    }

}
