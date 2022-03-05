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
package org.jutils.jprocesses.info;

import org.jutils.jprocesses.model.JProcessesResponse;
import org.jutils.jprocesses.model.ProcessInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Info related with processes
 *
 * @author Javier Garcia Alonso
 */
abstract class AbstractProcessesService implements ProcessesService {

    protected boolean fastMode = false;

    public List<ProcessInfo> getList() {
        return getList(null);
    }

    public List<ProcessInfo> getList(boolean fastMode) {
        return getList(null, fastMode);
    }

    public List<ProcessInfo> getList(String name) {
        return getList(name, false);
    }

    public List<ProcessInfo> getList(String name, boolean fastMode) {
        this.fastMode = fastMode;
        String rawData = getProcessesData(name);

        List<Map<String, String>> mapList = parseList(rawData);

        return buildInfoFromMap(mapList);
    }

    public JProcessesResponse killProcess(int pid) {
        return kill(pid);
    }

    public JProcessesResponse killProcessGracefully(int pid) {
        return killGracefully(pid);
    }

    protected abstract List<Map<String, String>> parseList(String rawData);

    protected abstract String getProcessesData(String name);

    protected abstract JProcessesResponse kill(int pid);

    protected abstract JProcessesResponse killGracefully(int pid);

    private List<ProcessInfo> buildInfoFromMap(List<Map<String, String>> mapList) {
        List<ProcessInfo> infoList = new ArrayList<ProcessInfo>();

        for (final Map<String, String> map : mapList) {
            ProcessInfo info = new ProcessInfo();
            info.pid = (map.get("pid"));
            info.name = (map.get("proc_name"));
            info.time = (map.get("proc_time"));
            info.command = ((map.get("command") != null) ? map.get("command") : "");
            info.cpuUsage = (map.get("cpu_usage"));
            info.physicalMemory = (map.get("physical_memory"));
            info.startTime = (map.get("start_time"));
            info.user = (map.get("user"));
            info.virtualMemory = (map.get("virtual_memory"));
            info.priority = (map.get("priority"));

            //Adds extra data
            info.extraData = (map);

            infoList.add(info);
        }

        return infoList;
    }
}
