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

package org.apache.eventmesh.client.http.util;

import org.apache.eventmesh.client.http.model.RequestParam;
import org.apache.eventmesh.common.Constants;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.netty.handler.codec.http.HttpMethod;

import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HttpUtils {

    public static String post(final CloseableHttpClient client,
        final String uri,
        final RequestParam requestParam) throws IOException {

        return post(client, null, uri, requestParam);
    }

    public static String post(final CloseableHttpClient client,
        final HttpHost forwardAgent,
        final String uri,
        final RequestParam requestParam) throws IOException {

        return post(client, forwardAgent, uri, requestParam, new EventMeshResponseHandler());

    }

    public static String post(final CloseableHttpClient client,
        final HttpHost forwardAgent,
        final String uri,
        final RequestParam requestParam,
        final HttpClientResponseHandler<String> responseHandler) throws IOException {

        Preconditions.checkState(client != null, "client can't be null");
        Preconditions.checkState(StringUtils.isNotBlank(uri), "uri can't be null");
        Preconditions.checkState(requestParam != null, "requestParam can't be null");
        Preconditions.checkState(responseHandler != null, "responseHandler can't be null");
        Preconditions.checkState(requestParam.getHttpMethod().equals(HttpMethod.POST), "invalid requestParam httpMethod");

        final HttpPost httpPost = new HttpPost(uri);

        // header
        if (MapUtils.isNotEmpty(requestParam.getHeaders())) {
            for (final Map.Entry<String, String> entry : requestParam.getHeaders().entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }

        // body
        if (MapUtils.isNotEmpty(requestParam.getBody())) {
            final List<NameValuePair> pairs = new ArrayList<>();
            for (final Map.Entry<String, String> entry : requestParam.getBody().entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, Constants.DEFAULT_CHARSET));
        }

        // ttl
        final RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setConnectionRequestTimeout(Timeout.of(requestParam.getTimeout(), TimeUnit.MILLISECONDS));

        if (forwardAgent != null) {
            configBuilder.setProxy(forwardAgent);
        }

        httpPost.setConfig(configBuilder.build());

        log.debug("{}", httpPost);

        return client.execute(httpPost, responseHandler);
    }

    public static String get(final CloseableHttpClient client,
        final String url,
        final RequestParam requestParam) throws IOException {

        return get(client, null, url, requestParam, new EventMeshResponseHandler());
    }

    public static String get(final CloseableHttpClient client,
        final HttpHost forwardAgent,
        final String url,
        final RequestParam requestParam) throws IOException {

        return get(client, forwardAgent, url, requestParam, new EventMeshResponseHandler());
    }

    public static String get(final CloseableHttpClient client,
        final HttpHost forwardAgent,
        final String uri,
        final RequestParam requestParam,
        final HttpClientResponseHandler<String> responseHandler) throws IOException {

        Preconditions.checkState(client != null, "client can't be null");
        Preconditions.checkState(StringUtils.isNotBlank(uri), "uri can't be null");
        Preconditions.checkState(requestParam != null, "requestParam can't be null");
        Preconditions.checkState(requestParam.getHttpMethod().equals(HttpMethod.GET), "invalid requestParam httpMethod");

        final HttpGet httpGet = new HttpGet(MapUtils.isNotEmpty(requestParam.getQueryParamsMap()) ? uri + "?" + requestParam.getQueryParams() : uri);

        // header
        if (MapUtils.isNotEmpty(requestParam.getHeaders())) {
            for (final Map.Entry<String, String> entry : requestParam.getHeaders().entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }

        // ttl
        final RequestConfig.Builder configBuilder = RequestConfig.custom();
        final ConnectionConfig.Builder connectionBuilder = ConnectionConfig.custom();
        connectionBuilder.setSocketTimeout(Timeout.of(requestParam.getTimeout(), TimeUnit.MILLISECONDS))
                .setConnectTimeout(Timeout.of(requestParam.getTimeout(), TimeUnit.MILLISECONDS));
        configBuilder.setConnectionRequestTimeout(Timeout.of(requestParam.getTimeout(), TimeUnit.MILLISECONDS));

        if (forwardAgent != null) {
            configBuilder.setProxy(forwardAgent);
        }

        httpGet.setConfig(configBuilder.build());

        log.debug("{}", httpGet);

        return client.execute(httpGet, responseHandler);
    }

    private static class EventMeshResponseHandler implements HttpClientResponseHandler<String> {

        /**
         * Processes an {@link HttpResponse} and returns some value corresponding to that response.
         *
         * @param response The response to process
         * @return A value determined by the response
         * @throws ClientProtocolException in case of an http protocol error
         * @throws IOException             in case of a problem or the connection was aborted
         */
        @Override
        public String handleResponse(ClassicHttpResponse response) throws IOException, ParseException {

            int statusCode = response.getCode();
            // Successful responses (200-299)
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, Constants.DEFAULT_CHARSET) : null;
            } else {
                throw new ClientProtocolException("Unexpected response statusCode: " + statusCode);
            }
        }
    }

}
