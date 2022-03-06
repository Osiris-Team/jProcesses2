/*
 * Copyright 2016 Javier Garcia Alonso.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jutils.jprocesses;

public enum JProcessPriority {
    IDLE(64, 19),
    BELOW_NORMAL(16384, 10),
    NORMAL(32, 0),
    ABOVE_NORMAL(32768, -5),
    HIGH(128, -10),
    REAL_TIME(258, -20);

    public int windowsPriority = 0;
    public int unixPriority = 0; // TODO FIND OUT

    JProcessPriority(int windowsPriority, int unixPriority) {
        this.windowsPriority = windowsPriority;
        this.unixPriority = unixPriority;
    }
}
