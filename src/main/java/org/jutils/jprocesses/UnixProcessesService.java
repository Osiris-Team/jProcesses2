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

import org.jutils.jprocesses.util.OS;
import org.jutils.jprocesses.util.NativeUtils;

import java.text.ParseException;
import java.util.*;

/**
 * Service implementation for Unix/BSD systems
 *
 * @author Javier Garcia Alonso
 */
class UnixProcessesService extends AbstractProcessesService {
    private final NativeUtils nativeUtils = new NativeUtils();

    //Use BSD sytle to get data in order to be compatible with Mac Systems(thanks to jkuharev for this tip)
    private final String PS_COLUMNS = "pid,ruser,vsize,rss,%cpu,lstart,cputime,nice,ucomm";
    private final String PS_FULL_COMMAND = "pid,command";

    private final int PS_COLUMNS_SIZE = PS_COLUMNS.split(",").length;
    private final int PS_FULL_COMMAND_SIZE = PS_FULL_COMMAND.split(",").length;

    private String nameFilter = null;

    @Override
    protected List<Map<String, String>> parseList(String rawData) {
        List<Map<String, String>> processesDataList = new ArrayList<Map<String, String>>();
        String[] dataStringLines = rawData.split("\\r?\\n");

        int index;
        for (final String dataLine : dataStringLines) {
            String line = dataLine.trim();
            if (line.startsWith("PID")) {
                // skip header
            } else {
                // LinkedHashMap keeps the insertion order, thus easier to debug
                Map<String, String> element = new LinkedHashMap<String, String>();
                String[] elements = line.split("\\s+", PS_COLUMNS_SIZE + 5);
                index = 0;
                element.put("pid", elements[index++]);
                element.put("user", elements[index++]);
                element.put("virtual_memory", elements[index++]);
                element.put("physical_memory", elements[index++]);
                element.put("cpu_usage", elements[index++]);
                index++; // Skip weekday
                String longDate = elements[index++] + " "
                        + elements[index++] + " "
                        + elements[index++] + " "
                        + elements[index++];
                element.put("start_time", elements[index - 2]);
                try {
                    element.put("start_datetime",
                            nativeUtils.parseUnixLongTimeToFullDate(longDate));
                } catch (ParseException e) {
                    element.put("start_datetime", "01/01/2000 00:00:00");
                    System.err.println("Failed formatting date from ps: " + longDate + ", using \"01/01/2000 00:00:00\"");
                }
                element.put("proc_time", elements[index++]);
                element.put("priority", elements[index++]);
                element.put("proc_name", elements[index++]);
                // first init full command by content of proc_name
                element.put("command", elements[index - 1]);

                processesDataList.add(element);
            }
        }
        loadFullCommandData(processesDataList);

        if (nameFilter != null) {
            filterByName(processesDataList);
        }

        return processesDataList;
    }

    @Override
    protected String getProcessesData(String name) {
        if (name != null) {
            if (OS.isLinux) {
                return nativeUtils.executeCommand("ps",
                        "-o", PS_COLUMNS, "-C", name);
            } else {
                this.nameFilter = name;
            }
        }
        return nativeUtils.executeCommand("ps",
                "-e", "-o", PS_COLUMNS);
    }

    public JProcess getProcess(int pid) {
        return getProcess(pid, false);
    }

    public JProcess getProcess(int pid, boolean fastMode) {
        this.fastMode = fastMode;
        List<Map<String, String>> processList
                = parseList(nativeUtils.executeCommand("ps",
                "-o", PS_COLUMNS, "-p", String.valueOf(pid)));

        if (processList != null && !processList.isEmpty()) {
            Map<String, String> processData = processList.get(0);
            JProcess info = new JProcess();
            info.pid = (processData.get("pid"));
            info.name = (processData.get("proc_name"));
            info.time = (processData.get("proc_time"));
            info.command = (processData.get("command"));
            info.cpuUsage = (processData.get("cpu_usage"));
            info.usedMemoryInKB = (processData.get("physical_memory"));
            info.timestampStart = (processData.get("start_time"));
            info.username = (processData.get("user"));
            info.usedVirtualMemoryInKB = (processData.get("virtual_memory"));
            info.priority = (processData.get("priority"));

            return info;
        }
        return null;
    }

    private void loadFullCommandData(List<Map<String, String>> processesDataList) {
        Map<String, String> commandsMap = new HashMap<String, String>();
        String data = nativeUtils.executeCommand("ps",
                "-e", "-o", PS_FULL_COMMAND);
        String[] dataStringLines = data.split("\\r?\\n");

        for (final String dataLine : dataStringLines) {
            if (!(dataLine.trim().startsWith("PID"))) {
                String[] elements = dataLine.trim().split("\\s+", PS_FULL_COMMAND_SIZE);
                if (elements.length == PS_FULL_COMMAND_SIZE) {
                    commandsMap.put(elements[0], elements[1]);
                }
            }
        }

        for (final Map<String, String> process : processesDataList) {
            if (commandsMap.containsKey(process.get("pid"))) {
                process.put("command", commandsMap.get(process.get("pid")));
            }
        }
    }


    private void filterByName(List<Map<String, String>> processesDataList) {
        List<Map<String, String>> processesToRemove = new ArrayList<Map<String, String>>();
        for (final Map<String, String> process : processesDataList) {
            if (!nameFilter.equals(process.get("proc_name"))) {
                processesToRemove.add(process);
            }
        }
        processesDataList.removeAll(processesToRemove);
    }
}
