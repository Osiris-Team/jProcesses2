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

    public List<JProcess> getList() {
        return getList(null);
    }

    public List<JProcess> getList(boolean fastMode) {
        return getList(null, fastMode);
    }

    public List<JProcess> getList(String name) {
        return getList(name, false);
    }

    public List<JProcess> getList(String name, boolean fastMode) {
        this.fastMode = fastMode;
        String rawData = getProcessesData(name);

        List<Map<String, String>> mapList = parseList(rawData);

        return buildInfoFromMap(mapList);
    }

    public NativeResult killProcess(int pid) {
        return kill(pid);
    }

    public NativeResult killProcessGracefully(int pid) {
        return killGracefully(pid);
    }

    protected abstract List<Map<String, String>> parseList(String rawData);

    protected abstract String getProcessesData(String name);

    protected abstract NativeResult kill(int pid);

    protected abstract NativeResult killGracefully(int pid);

    private List<JProcess> buildInfoFromMap(List<Map<String, String>> mapList) {
        List<JProcess> infoList = new ArrayList<JProcess>();

        for (final Map<String, String> map : mapList) {
            JProcess info = new JProcess();
            info.pid = (map.get("pid"));
            info.name = (map.get("proc_name"));
            info.time = (map.get("proc_time"));
            info.command = ((map.get("command") != null) ? map.get("command") : "");
            info.cpuUsage = (map.get("cpu_usage"));
            info.usedMemoryInKB = (map.get("physical_memory"));
            info.timestampStart = (map.get("start_time"));
            info.username = (map.get("user"));
            info.usedVirtualMemoryInKB = (map.get("virtual_memory"));
            info.priority = (map.get("priority"));

            //Adds extra data
            info.extraData = (map);

            infoList.add(info);
        }

        return infoList;
    }
}