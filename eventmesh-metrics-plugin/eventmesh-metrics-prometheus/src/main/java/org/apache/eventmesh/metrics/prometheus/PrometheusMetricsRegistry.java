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

package org.apache.eventmesh.metrics.prometheus;

import org.apache.eventmesh.common.config.Config;
import org.apache.eventmesh.metrics.api.MetricsRegistry;
import org.apache.eventmesh.metrics.api.model.GrpcSummaryMetrics;
import org.apache.eventmesh.metrics.api.model.HttpSummaryMetrics;
import org.apache.eventmesh.metrics.api.model.Metric;
import org.apache.eventmesh.metrics.api.model.TcpSummaryMetrics;
import org.apache.eventmesh.metrics.prometheus.config.PrometheusConfiguration;
import org.apache.eventmesh.metrics.prometheus.metrics.PrometheusGrpcExporter;
import org.apache.eventmesh.metrics.prometheus.metrics.PrometheusHttpExporter;
import org.apache.eventmesh.metrics.prometheus.metrics.PrometheusTcpExporter;

import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.internal.PrometheusMetricReaderProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Config(field = "prometheusConfiguration")
public class PrometheusMetricsRegistry implements MetricsRegistry {

    private volatile PrometheusHttpServer prometheusHttpServer;

    /**
     * Unified configuration class corresponding to prometheus.properties
     */
    private PrometheusConfiguration prometheusConfiguration;

    @Override
    public void start() {
        if (prometheusHttpServer == null) {
            synchronized (PrometheusMetricsRegistry.class) {
                if (prometheusHttpServer == null) {
                    PrometheusMetricReaderProvider readerProvider = new PrometheusMetricReaderProvider();
                    SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
                    int port = prometheusConfiguration.getEventMeshPrometheusPort();
                    // Use the daemon thread to start an HTTP server to serve the default Prometheus registry.
                    prometheusHttpServer = PrometheusHttpServer.builder().setPort(port).build();
                }
            }
        }

    }

    @Override
    public void showdown() {
        if (prometheusHttpServer != null) {
            prometheusHttpServer.shutdown();
        }
    }

    @Override
    public void register(Metric metric) {
        if (metric == null) {
            throw new IllegalArgumentException("Metric cannot be null");
        }
        if (metric instanceof HttpSummaryMetrics) {
            PrometheusHttpExporter.export("apache-eventmesh", (HttpSummaryMetrics) metric);
        }

        if (metric instanceof TcpSummaryMetrics) {
            PrometheusTcpExporter.export("apache-eventmesh", (TcpSummaryMetrics) metric);
        }

        if (metric instanceof GrpcSummaryMetrics) {
            PrometheusGrpcExporter.export("apache-eventmesh", (GrpcSummaryMetrics) metric);
        }
    }

    @Override
    public void unRegister(Metric metric) {
        // todo: need to split the current metrics
    }

    public PrometheusConfiguration getClientConfiguration() {
        return this.prometheusConfiguration;
    }
}
