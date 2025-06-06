/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.storage.rabbitmq.common;

public class EventMeshConstants {

    public static final String REQ_C2EVENTMESH_TIMESTAMP = "reqc2eventmeshtimestamp";
    public static final String REQ_EVENTMESH2MQ_TIMESTAMP = "reqeventmesh2mqtimestamp";
    public static final String REQ_MQ2EVENTMESH_TIMESTAMP = "reqmq2eventmeshtimestamp";
    public static final String REQ_EVENTMESH2C_TIMESTAMP = "reqeventmesh2ctimestamp";
    public static final String RSP_C2EVENTMESH_TIMESTAMP = "rspc2eventmeshtimestamp";
    public static final String RSP_EVENTMESH2MQ_TIMESTAMP = "rspeventmesh2mqtimestamp";
    public static final String RSP_MQ2EVENTMESH_TIMESTAMP = "rspmq2eventmeshtimestamp";
    public static final String RSP_EVENTMESH2C_TIMESTAMP = "rspeventmesh2ctimestamp";

    public static final String REQ_SEND_EVENTMESH_IP = "reqsendeventmeship";
    public static final String REQ_RECEIVE_EVENTMESH_IP = "reqreceiveeventmeship";
    public static final String RSP_SEND_EVENTMESH_IP = "rspsendeventmeship";
    public static final String RSP_RECEIVE_EVENTMESH_IP = "rspreceiveeventmeship";

    public static final String RSP_SYS = "rsp0sys";
    public static final String RSP_IP = "rsp0ip";
    public static final String RSP_IDC = "rsp0idc";
    public static final String RSP_GROUP = "rsp0group";
    public static final String RSP_URL = "rsp0url";
    public static final String RSP_RETRY = "rsp0retry";

    public static final String REQ_SYS = "req0sys";
    public static final String REQ_IP = "req0ip";
    public static final String REQ_IDC = "req0idc";
    public static final String REQ_GROUP = "req0group";
}
