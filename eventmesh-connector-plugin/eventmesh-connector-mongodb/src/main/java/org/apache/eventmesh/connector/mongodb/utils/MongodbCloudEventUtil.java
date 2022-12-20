package org.apache.eventmesh.connector.mongodb.utils;

import org.apache.eventmesh.connector.mongodb.exception.MongodbConnectorException;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.bson.Document;

import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;

public class MongodbCloudEventUtil {
    public static CloudEvent convertToCloudEvent(Document document) {
        document.remove("_id");
        String versionStr = document.getString("version");
        SpecVersion version = SpecVersion.valueOf(versionStr);
        CloudEventBuilder builder;
        switch (version) {
            case V03:
                builder = CloudEventBuilder.v03();
                break;
            case V1:
                builder = CloudEventBuilder.v1();
                break;
            default:
                throw new MongodbConnectorException(String.format("CloudEvent version %s does not support.", version));
        }
        builder.withData(document.remove("data").toString().getBytes())
                .withId(document.remove("id").toString())
                .withSource(URI.create(document.remove("source").toString()))
                .withType(document.remove("type").toString())
                .withDataContentType(document.remove("datacontenttype").toString())
                .withSubject(document.remove("subject").toString());
        document.forEach((key, value) -> builder.withExtension(key, value.toString()));

        return builder.build();
    }

    public static Document convertToDocument(CloudEvent cloudEvent) {
        Document document = new Document();
        document.put("version", cloudEvent.getSpecVersion().name());
        document.put("data", cloudEvent.getData() == null
                ? null : new String(cloudEvent.getData().toBytes(), StandardCharsets.UTF_8));
        document.put("id", cloudEvent.getId());
        document.put("source", cloudEvent.getSource().toString());
        document.put("type", cloudEvent.getType());
        document.put("datacontenttype", cloudEvent.getDataContentType());
        document.put("subject", cloudEvent.getSubject());
        cloudEvent.getExtensionNames().forEach(key -> document.put(key, cloudEvent.getExtension(key)));

        return document;
    }
}
