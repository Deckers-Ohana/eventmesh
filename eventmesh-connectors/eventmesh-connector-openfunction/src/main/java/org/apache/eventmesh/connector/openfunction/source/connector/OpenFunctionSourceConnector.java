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

package org.apache.eventmesh.connector.openfunction.source.connector;

import org.apache.eventmesh.common.config.connector.Config;
import org.apache.eventmesh.common.config.connector.openfunction.OpenFunctionSourceConfig;
import org.apache.eventmesh.openconnect.api.connector.ConnectorContext;
import org.apache.eventmesh.openconnect.api.connector.SourceConnectorContext;
import org.apache.eventmesh.openconnect.api.source.Source;
import org.apache.eventmesh.openconnect.offsetmgmt.api.data.ConnectRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenFunctionSourceConnector implements Source {

    private OpenFunctionSourceConfig sourceConfig;

    private BlockingQueue<ConnectRecord> queue;

    private int maxBatchSize;

    private long maxPollWaitTime;

    @Override
    public Class<? extends Config> configClass() {
        return OpenFunctionSourceConfig.class;
    }

    @Override
    public void init(Config config) throws Exception {
        // init config for openfunction source connector
        this.sourceConfig = (OpenFunctionSourceConfig) config;
        doInit();
    }

    @Override
    public void init(ConnectorContext connectorContext) throws Exception {
        SourceConnectorContext sourceConnectorContext = (SourceConnectorContext) connectorContext;
        // init config for openfunction source connector
        this.sourceConfig = (OpenFunctionSourceConfig) sourceConnectorContext.getSourceConfig();
        doInit();
    }

    private void doInit() {
        // init config for openfunction source connector
        this.queue = new LinkedBlockingQueue<>(sourceConfig.getPollConfig().getCapacity());
        this.maxBatchSize = sourceConfig.getPollConfig().getMaxBatchSize();
        this.maxPollWaitTime = sourceConfig.getPollConfig().getMaxWaitTime();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void commit(ConnectRecord record) {

    }

    @Override
    public String name() {
        return this.sourceConfig.getSourceConnectorConfig().getConnectorName();
    }

    @Override
    public void onException(ConnectRecord record) {

    }

    @Override
    public void stop() {

    }

    public BlockingQueue<ConnectRecord> queue() {
        return queue;
    }

    @Override
    public List<ConnectRecord> poll() {
        long startTime = System.currentTimeMillis();
        long remainingTime = maxPollWaitTime;

        List<ConnectRecord> connectRecords = new ArrayList<>(maxBatchSize);
        for (int count = 0; count < maxBatchSize; ++count) {
            try {
                ConnectRecord connectRecord = queue.poll(remainingTime, TimeUnit.MILLISECONDS);
                if (connectRecord == null) {
                    break;
                }
                connectRecords.add(connectRecord);

                // calculate elapsed time and update remaining time for next poll
                long elapsedTime = System.currentTimeMillis() - startTime;
                remainingTime = maxPollWaitTime > elapsedTime ? maxPollWaitTime - elapsedTime : 0;
            } catch (InterruptedException e) {
                Thread currentThread = Thread.currentThread();
                log.warn("[OpenFunctionSourceConnector] Interrupting thread {} due to exception {}",
                    currentThread.getName(), e.getMessage());
                currentThread.interrupt();
            }
        }
        return connectRecords;
    }
}
