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

package org.apache.eventmesh.admin.server.web;

import org.apache.eventmesh.admin.server.ComponentLifeCycle;
import org.apache.eventmesh.common.remote.payload.PayloadFactory;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
public abstract class BaseServer implements ComponentLifeCycle {

    static {
        PayloadFactory.getInstance().init();
    }

    @PostConstruct
    public void init() throws Exception {
        log.info("[{}] server starting at port [{}]", this.getClass().getSimpleName(), getPort());
        start();
        log.info("[{}] server started at port [{}]", this.getClass().getSimpleName(), getPort());
    }

    @PreDestroy
    public void shutdown() {
        log.info("[{}] server will destroy", this.getClass().getSimpleName());
        destroy();
        log.info("[{}] server has be destroy", this.getClass().getSimpleName());
    }

    public abstract int getPort();
}
