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

import java.util.List;

/**
 * Interface for service retrieving processes information
 *
 * @author Javier Garcia Alonso
 */
public interface ProcessesService {
    List<JProcess> getList();

    List<JProcess> getList(boolean fastMode);

    List<JProcess> getList(String name);

    List<JProcess> getList(String name, boolean fastMode);

    JProcess getProcess(int pid);

    JProcess getProcess(int pid, boolean fastMode);

    NativeResult killProcess(int pid);

    NativeResult killProcessGracefully(int pid);

    NativeResult changePriority(int pid, int priority);
}
