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
package org.jutils.jprocesses.model;

import java.util.*;

/**
 * Model that encapsulates process information
 *
 * @author Javier Garcia Alonso
 */
public class ProcessInfo {

    public String name;
    public String caption;
    public String pid;
    public String time; // TODO Remove
    public String user; // TODO remove
    public String kbVirtualMemory;
    public String kbWorkingSet;
    public String cpuUsage;
    /**
     * The creation date of this process. <br>
     * // TODO find out the Windows/Unix formats for this. <br>
     */
    public String startTime;
    /**
     * The priority of this process as int. <br>
     * Note that Windows and Unix priorities are different. <br>
     * For Windows see the {@link WindowsPriority} enum, for Unix value range from //TODO find this out. <br>
     */
    public String priority;
    /**
     * The complete command that was used to start this process.
     */
    public String command;

    public String parentPid;
    public ProcessInfo parentProcess;
    public List<ProcessInfo> childProcesses = new ArrayList<>(1);

    //Used to store system specific data
    public Map<String, String> extraData = new HashMap<String, String>();

    public ProcessInfo(){
    }

    public ProcessInfo(String pid, String time, String name, String user, String kbVirtualMemory, String kbWorkingSet, String cpuUsage, String startTime, String priority, String command) {
        this.pid = pid;
        this.time = time;
        this.name = name;
        this.user = user;
        this.kbVirtualMemory = kbVirtualMemory;
        this.kbWorkingSet = kbWorkingSet;
        this.cpuUsage = cpuUsage;
        this.startTime = startTime;
        this.priority = priority;
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessInfo that = (ProcessInfo) o;

        if (!Objects.equals(pid, that.pid)) return false;
        if (!Objects.equals(time, that.time)) return false;
        if (!Objects.equals(name, that.name)) return false;
//        if (user != null ? !user.equals(that.user) : that.user != null) return false;
//        if (virtualMemory != null ? !virtualMemory.equals(that.virtualMemory) : that.virtualMemory != null)
//            return false;
//        if (physicalMemory != null ? !physicalMemory.equals(that.physicalMemory) : that.physicalMemory != null)
//            return false;
//        if (cpuUsage != null ? !cpuUsage.equals(that.cpuUsage) : that.cpuUsage != null) return false;
        if (!Objects.equals(startTime, that.startTime)) return false;
        if (!Objects.equals(priority, that.priority)) return false;
        return Objects.equals(command, that.command);

    }

    @Override
    public int hashCode() {
        int result = pid != null ? pid.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
//        result = 31 * result + (user != null ? user.hashCode() : 0)   //TODO: return this to equals and hashcode after getProcessesOwner refactoring
//        result = 31 * result + (virtualMemory != null ? virtualMemory.hashCode() : 0);
//        result = 31 * result + (physicalMemory != null ? physicalMemory.hashCode() : 0);
//        result = 31 * result + (cpuUsage != null ? cpuUsage.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (command != null ? command.hashCode() : 0);
        return result;
    }

    public String toPrintString() {
        return "NAME:" +name+ " PID:" + pid + " CPU:" + cpuUsage + " MEM:" + kbWorkingSet
                + "	PRIORITY:" + priority + " CMD:" + command;
    }
}
