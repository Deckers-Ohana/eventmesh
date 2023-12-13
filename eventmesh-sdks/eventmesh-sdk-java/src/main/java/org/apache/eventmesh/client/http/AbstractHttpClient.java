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

package org.apache.eventmesh.client.http;

import org.apache.eventmesh.client.http.conf.EventMeshHttpClientConfig;
import org.apache.eventmesh.client.http.ssl.MyX509TrustManager;
import org.apache.eventmesh.client.http.util.HttpLoadBalanceUtils;
import org.apache.eventmesh.common.Constants;
import org.apache.eventmesh.common.config.ConfigService;
import org.apache.eventmesh.common.exception.EventMeshException;
import org.apache.eventmesh.common.loadbalance.LoadBalanceSelector;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractHttpClient implements AutoCloseable {

    protected EventMeshHttpClientConfig eventMeshHttpClientConfig;

    protected LoadBalanceSelector<String> eventMeshServerSelector;

    protected final CloseableHttpClient httpClient;

    public AbstractHttpClient(final EventMeshHttpClientConfig eventMeshHttpClientConfig) throws EventMeshException {
        Objects.requireNonNull(eventMeshHttpClientConfig, "liteClientConfig can't be null");
        Objects.requireNonNull(eventMeshHttpClientConfig.getLiteEventMeshAddr(), "liteServerAddr can't be null");

        this.eventMeshHttpClientConfig = eventMeshHttpClientConfig;
        this.eventMeshServerSelector = HttpLoadBalanceUtils.createEventMeshServerLoadBalanceSelector(
                eventMeshHttpClientConfig);
        this.httpClient = setHttpClient();
    }

    @Override
    public void close() throws EventMeshException {
        if (this.httpClient != null) {
            try {
                this.httpClient.close();
            } catch (IOException e) {
                throw new EventMeshException(e);
            }
        }

    }

    private CloseableHttpClient setHttpClient() throws EventMeshException {
        if (!eventMeshHttpClientConfig.isUseTls()) {
            return HttpClients.createDefault();
        }
        SSLContext sslContext;
        try {
            final String protocol = eventMeshHttpClientConfig.getSslClientProtocol();
            final TrustManager[] tm = new TrustManager[]{new MyX509TrustManager()};
            sslContext = SSLContext.getInstance(protocol);
            sslContext.init(null, tm, new SecureRandom());

            HttpClientBuilder builder = HttpClients.custom();
            if (ConfigService.getInstance().buildConfigInstance(HttpRequestInterceptor.class) != null) {
                builder.addRequestInterceptorFirst(ConfigService.getInstance().buildConfigInstance(HttpRequestInterceptor.class));
            }
            return builder
                    .setConnectionManager(
                            getHttpPoolManager(sslContext, eventMeshHttpClientConfig.getMaxConnectionPoolSize()))
                    .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                    .evictIdleConnections(
                            TimeValue.of(eventMeshHttpClientConfig.getConnectionIdleTimeSeconds(), TimeUnit.SECONDS))
                    .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                    .build();
        } catch (Exception e) {
            log.error("Error in creating HttpClient.", e);
            throw new EventMeshException(e);
        }
    }

    private HttpClientConnectionManager getHttpPoolManager(final SSLContext sslContext, final int poolSize) {
        final SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext,
                new DefaultHostnameVerifier());
        final Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslFactory)
                .build();
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        final ConnectionConfig.Builder connectionBuilder = ConnectionConfig.custom();
        connectionBuilder.setSocketTimeout(
                        Timeout.of(eventMeshHttpClientConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS))
                .setConnectTimeout(Timeout.of(eventMeshHttpClientConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS));
        connectionManager.setDefaultConnectionConfig(connectionBuilder.build());
        connectionManager.setMaxTotal(poolSize);
        return connectionManager;
    }

    protected String selectEventMesh() {
        // todo: target endpoint maybe destroy, should remove the bad endpoint
        if (eventMeshHttpClientConfig.isUseTls()) {
            return Constants.HTTPS_PROTOCOL_PREFIX + eventMeshServerSelector.select();
        } else {
            return Constants.HTTP_PROTOCOL_PREFIX + eventMeshServerSelector.select();
        }
    }
}
