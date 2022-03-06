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

import com.profesorfalken.wmi4java.WMI4Java;
import com.profesorfalken.wmi4java.WMIClass;
import org.jutils.jprocesses.util.NativeResult;
import org.jutils.jprocesses.util.NativeUtils;

import java.util.*;

/**
 * Service implementation for Windows
 *
 * @author Javier Garcia Alonso
 */
class WindowsProcessesService extends AbstractProcessesService {
    //TODO: This Windows implementation works but it is not optimized by lack of time.
//For example, the filter by name or the search by pid is done retrieving 
//all the processes searching in the returning list.
//Moreover, the information is dispersed and I had to get it from different sources (WMI classes, VBS scripts...)
    private final NativeUtils nativeUtils = new NativeUtils();
    private final VBScriptHelper vbScriptHelper = new VBScriptHelper();

    private final Map<String, String> userData = new HashMap<String, String>();
    private final Map<String, String> cpuData = new HashMap<String, String>();

    private final String LINE_BREAK_REGEX = "\\r?\\n";

    private final Map<String, String> keyMap;
    private final String NAME = "Name";
    private final String PID = "ProcessId";
    private final String USERMODETIME = "UserModeTime";
    private final String PRIORITY = "Priority";
    private final String VIRTUALSIZE = "VirtualSize";
    private final String WORKINGSETSIZE = "WorkingSetSize";
    private final String COMMANDLINE = "CommandLine";
    private final String CREATIONDATE = "CreationDate";
    private final String CAPTION_PROPNAME = "Caption";
    private final String PARENT_PID = "ParentProcessId";
    private final WMI4Java wmi4Java;
    private Map<String, String> processMap;

    {
        Map<String, String> tmpMap = new HashMap<String, String>();
        tmpMap.put(NAME, "proc_name");
        tmpMap.put(PID, "pid");
        tmpMap.put(USERMODETIME, "proc_time");
        tmpMap.put(PRIORITY, "priority");
        tmpMap.put(VIRTUALSIZE, "virtual_memory");
        tmpMap.put(WORKINGSETSIZE, "physical_memory");
        tmpMap.put(COMMANDLINE, "command");
        tmpMap.put(CREATIONDATE, "start_time");
        tmpMap.put(PARENT_PID, "parent_pid");

        keyMap = Collections.unmodifiableMap(tmpMap);
    }

    public WindowsProcessesService() {
        this(null);
    }

    WindowsProcessesService(WMI4Java wmi4Java) {
        this.wmi4Java = wmi4Java;
    }

    public WMI4Java getWmi4Java() {
        if (wmi4Java == null) {
            return WMI4Java.get();
        }
        return wmi4Java;
    }

    @Override
    protected List<Map<String, String>> parseList(String rawData) {
        List<Map<String, String>> processesDataList = new ArrayList<Map<String, String>>();

        String[] dataStringLines = rawData.split(LINE_BREAK_REGEX);

        for (final String dataLine : dataStringLines) {
            if (dataLine.trim().length() > 0) {
                processLine(dataLine, processesDataList);
            }
        }

        return processesDataList;
    }

    private void processLine(String dataLine, List<Map<String, String>> processesDataList) {
        if (dataLine.startsWith(CAPTION_PROPNAME)) {
            processMap = new HashMap<String, String>();
            processesDataList.add(processMap);
        }

        if (processMap != null) {
            String[] dataStringInfo = dataLine.split(":", 2);
            processMap.put(normalizeKey(dataStringInfo[0].trim()),
                    normalizeValue(dataStringInfo[0].trim(), dataStringInfo[1].trim()));

            if (PID.equals(dataStringInfo[0].trim())) {
                processMap.put("user", userData.get(dataStringInfo[1].trim()));
                processMap.put("cpu_usage", cpuData.get(dataStringInfo[1].trim()));
            }

            if (CREATIONDATE.equals(dataStringInfo[0].trim())) {
                processMap.put("start_datetime",
                        nativeUtils.parseWindowsDateTimeToFullDate(dataStringInfo[1].trim()));
            }
        }
    }

    @Override
    protected String getProcessesData(String name) {
        if (!fastMode) {
            fillExtraProcessData();
        }

        if (name != null) {
            return getWmi4Java().VBSEngine()
                    .properties(Arrays.asList(CAPTION_PROPNAME, PID, NAME,
                            USERMODETIME, COMMANDLINE,
                            WORKINGSETSIZE, CREATIONDATE,
                            VIRTUALSIZE, PRIORITY, PARENT_PID))
                    .filters(Collections.singletonList("Name like '%" + name + "%'"))
                    .getRawWMIObjectOutput(WMIClass.WIN32_PROCESS);
        } else{
            return getWmi4Java().VBSEngine()
                    .properties(Arrays.asList(CAPTION_PROPNAME, PID, NAME,
                            USERMODETIME, COMMANDLINE,
                            WORKINGSETSIZE, CREATIONDATE,
                            VIRTUALSIZE, PRIORITY, PARENT_PID))
                    .getRawWMIObjectOutput(WMIClass.WIN32_PROCESS);
        }
    }

    @Override
    protected NativeResult kill(int pid) {
        NativeResult response = new NativeResult();
        if (nativeUtils.executeCommandAndGetCode("taskkill", "/PID", String.valueOf(pid), "/F") == 0) {
            response.setSuccess(true);
        }

        return response;
    }

    @Override
    protected NativeResult killGracefully(int pid) {
        NativeResult response = new NativeResult();
        if (nativeUtils.executeCommandAndGetCode("taskkill", "/PID", String.valueOf(pid)) == 0) {
            response.setSuccess(true);
        }

        return response;
    }

    private String normalizeKey(String origKey) {
        return keyMap.get(origKey);
    }

    private String normalizeValue(String origKey, String origValue) {
        if (USERMODETIME.equals(origKey)) {
            //100 nano to second - https://msdn.microsoft.com/en-us/library/windows/desktop/aa394372(v=vs.85).aspx
            long seconds = Long.parseLong(origValue) * 100 / 1000000 / 1000;
            return nomalizeTime(seconds);
        }
        if (VIRTUALSIZE.equals(origKey) || WORKINGSETSIZE.equals(origKey)) {
            if (!(origValue.isEmpty())) {
                return String.valueOf(Long.parseLong(origValue) / 1024);
            }
        }
        if (CREATIONDATE.equals(origKey)) {
            return nativeUtils.parseWindowsDateTimeToSimpleTime(origValue);
        }

        return origValue;
    }

    private String nomalizeTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void fillExtraProcessData() {
        String perfData = getWmi4Java().VBSEngine().getRawWMIObjectOutput(WMIClass.WIN32_PERFFORMATTEDDATA_PERFPROC_PROCESS);

        String[] dataStringLines = perfData.split(LINE_BREAK_REGEX);
        String pid = null;
        String cpuUsage = null;
        for (final String dataLine : dataStringLines) {

            if (dataLine.trim().length() > 0) {
                if (dataLine.startsWith(CAPTION_PROPNAME)) {
                    if (pid != null && cpuUsage != null) {
                        cpuData.put(pid, cpuUsage);
                        pid = null;
                        cpuUsage = null;
                    }
                    continue;
                }

                if (pid == null) {
                    pid = checkAndGetDataInLine("IDProcess", dataLine);
                }
                if (cpuUsage == null) {
                    cpuUsage = checkAndGetDataInLine("PercentProcessorTime", dataLine);
                }
            }
        }

        String processesData = vbScriptHelper.getProcessesOwner();

        if (processesData != null) {
            dataStringLines = processesData.split(LINE_BREAK_REGEX);
            for (final String dataLine : dataStringLines) {
                String[] dataStringInfo = dataLine.split(":", 2);
                if (dataStringInfo.length == 2) {
                    userData.put(dataStringInfo[0].trim(), dataStringInfo[1].trim());
                }
            }
        }
    }

    private String checkAndGetDataInLine(String dataName, String dataLine) {
        if (dataLine.startsWith(dataName)) {
            String[] dataStringInfo = dataLine.split(":");
            if (dataStringInfo.length == 2) {
                return dataStringInfo[1].trim();
            }
        }
        return null;
    }

    public NativeResult changePriority(int pid, int priority) {
        NativeResult response = new NativeResult();
        String message = vbScriptHelper.changePriority(pid, priority);
        if (message == null || message.length() == 0) {
            response.setSuccess(true);
        } else {
            response.setMessage(message);
        }
        return response;
    }

    public JProcess getProcess(int pid) {
        return getProcess(pid, false);
    }

    public JProcess getProcess(int pid, boolean fastMode) {
        this.fastMode = fastMode;
        List<Map<String, String>> allProcesses = parseList(getProcessesData(null));

        for (final Map<String, String> process : allProcesses) {
            if (String.valueOf(pid).equals(process.get("pid"))) {
                JProcess info = new JProcess();
                info.pid = (process.get("pid"));
                info.name = (process.get("proc_name"));
                info.time = (process.get("proc_time"));
                info.command = (process.get("command"));
                info.cpuUsage = (process.get("cpu_usage"));
                info.kbWorkingSet = (process.get("physical_memory"));
                info.startTime = (process.get("start_time"));
                info.user = (process.get("user"));
                info.kbVirtualMemory = (process.get("virtual_memory"));
                info.priority = (process.get("priority"));
                info.parentPid = (process.get("parent_pid"));

                return info;
            }
        }
        return null;
    }
}
