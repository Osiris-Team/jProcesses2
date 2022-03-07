/*
 * Copyright 2016 Javier Garcia Alonso.
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
package org.jutils.jprocesses;

import org.jutils.jprocesses.util.NativeResult;
import org.jutils.jprocesses.util.NativeUtils;
import org.jutils.jprocesses.util.OS;

import java.text.ParseException;
import java.util.*;

/**
 * Holds raw process information,
 * and provides various methods to work with its data,
 * or change its state.
 *
 * @author Javier Garcia Alonso
 * @author Osiris-Team
 */
public class JProcess {
    /**
     * The name by which this process is known.
     */
    public String name;
    /**
     * Similar (or even the same as) to {@link #name}, but in most cases shorter.
     */
    public String caption;
    /**
     * The process identifier.
     */
    public String pid;
    /**
     * The name of the user running this process.
     */
    public String username;
    /**
     * The virtual memory used by this process in kilobytes.
     */
    public String usedVirtualMemoryInKB;
    /**
     * The total amount of memory used by this process, in kilobytes. <br>
     * Also known under the processes' working set.
     */
    public String usedMemoryInKB;
    /**
     * The CPU usage of this process. <br>
     * A value between 0.0 (0%) and 100.0 (100%).<br>
     * Note that this is currently not supported for Windows. TODO find a way of making this available for Windows.
     */
    public String cpuUsage;
    /**
     * The timestamp of when this process was created. <br>
     * Note that this is a raw and system-dependent value, <br>
     * thus it's recommended to use {@link #getTimestampStart()} instead. <br>
     */
    public String timestampStart;
    /**
     * The priority of this process. <br>
     * Note that this is a raw and system-dependent value, <br>
     * thus it's recommended to use {@link #getPriority()} instead. <br>
     */
    public String priority;
    /**
     * The complete command that was used to start this process.
     */
    public String command;
    /**
     * The process identifier of the process that created this process.
     */
    public String parentPid;
    public JProcess parentProcess;
    public List<JProcess> childProcesses = new ArrayList<>(1);

    //Used to store system specific data
    public Map<String, String> extraData = new HashMap<String, String>();

    public JProcess() {
    }

    public JProcess(String pid, String name, String username, String usedVirtualMemoryInKB, String usedMemoryInKB, String cpuUsage, String timestampStart, String priority, String command) {
        this.pid = pid;
        this.name = name;
        this.username = username;
        this.usedVirtualMemoryInKB = usedVirtualMemoryInKB;
        this.usedMemoryInKB = usedMemoryInKB;
        this.cpuUsage = cpuUsage;
        this.timestampStart = timestampStart;
        this.priority = priority;
        this.command = command;
    }

    public NativeResult stop() {
        NativeUtils nativeUtils = new NativeUtils();
        NativeResult response = new NativeResult();
        if (OS.isWindows) {
            if (nativeUtils.executeCommandAndGetCode("taskkill", "/PID", String.valueOf(pid)) == 0) {
                response.setSuccess(true);
            }
        } else {
            if (nativeUtils.executeCommandAndGetCode("kill", "-15", String.valueOf(pid)) == 0) {
                response.setSuccess(true);
            }
        }
        return response;
    }

    public NativeResult kill() {
        NativeUtils nativeUtils = new NativeUtils();
        NativeResult response = new NativeResult();
        if (OS.isWindows) {
            if (nativeUtils.executeCommandAndGetCode("taskkill", "/PID", String.valueOf(pid), "/F") == 0) {
                response.setSuccess(true);
            }
        } else {
            if (nativeUtils.executeCommandAndGetCode("kill", "-9", String.valueOf(pid)) == 0) {
                response.setSuccess(true);
            }
        }
        return response;
    }

    public NativeResult changePriority(JProcessPriority priority) {
        if (OS.isWindows)
            return changePriority(priority.windowsPriority);
        else
            return changePriority(priority.unixPriority);
    }

    public NativeResult changePriority(int priority) {
        if (OS.isWindows) {
            VBScriptHelper vbScriptHelper = new VBScriptHelper();
            NativeResult response = new NativeResult();
            String message = vbScriptHelper.changePriority(Integer.parseInt(pid), priority);
            if (message == null || message.length() == 0) {
                response.setSuccess(true);
                this.priority = "" + priority;
            } else {
                response.setMessage(message);
            }
            return response;
        } else {
            NativeUtils nativeUtils = new NativeUtils();
            NativeResult result = new NativeResult();
            if (nativeUtils.executeCommandAndGetCode("renice", "" + priority,
                    "-p", pid) == 0) {
                result.setSuccess(true);
                this.priority = "" + priority;
            }
            return result;
        }
    }

    public JProcessPriority getPriority() {
        int prio = Integer.parseInt(priority);
        if (OS.isWindows) {
            switch (prio) {
                case 64:
                    return JProcessPriority.IDLE;
                case 16384:
                    return JProcessPriority.BELOW_NORMAL;
                case 32:
                    return JProcessPriority.NORMAL;
                case 32768:
                    return JProcessPriority.ABOVE_NORMAL;
                case 128:
                    return JProcessPriority.HIGH;
                case 258:
                    return JProcessPriority.REAL_TIME;
                default:
                    return JProcessPriority.NORMAL;
            }
        } else {
            switch (prio) {
                case 19:
                    return JProcessPriority.IDLE;
                case 10:
                    return JProcessPriority.BELOW_NORMAL;
                case 0:
                    return JProcessPriority.NORMAL;
                case -5:
                    return JProcessPriority.ABOVE_NORMAL;
                case -10:
                    return JProcessPriority.HIGH;
                case -20:
                    return JProcessPriority.REAL_TIME;
                default:
                    return JProcessPriority.NORMAL;
            }
        }
    }

    public Date getTimestampStart() throws ParseException {
        NativeUtils nativeUtils = new NativeUtils();
        if (OS.isWindows)
            return nativeUtils.parseWindowsDateTimeToFullDate(timestampStart);
        else
            return nativeUtils.parseUnixLongTimeToFullDate(timestampStart);
    }

    public String toPrintString() {
        return "NAME:" + name + " PID:" + pid + " CPU:" + cpuUsage + " MEM:" + usedMemoryInKB
                + "	PRIORITY:" + priority + " CMD:" + command;
    }

    public String toMinimalPrintString() {
        return "NAME:" + name + " PID:" + pid;
    }
}
