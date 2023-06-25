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

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: callback-service.proto

package org.apache.eventmesh.connector.openfunction.client;

public final class EventMeshGrpcService {

    private EventMeshGrpcService() {
    }

    public static void registerAllExtensions(
        com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
        com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
            (com.google.protobuf.ExtensionRegistryLite) registry);
    }

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor
        descriptor;

    static {
        String[] descriptorData = {
            "\n\026callback-service.proto\022#org.apache.eve" +
                "ntmesh.cloudevents.v1\032\033eventmesh-cloudev" +
                "ents.proto\032\031google/protobuf/any.proto\032\037g" +
                "oogle/protobuf/timestamp.proto2\203\001\n\017Callb" +
                "ackService\022p\n\014OnTopicEvent\022/.org.apache." +
                "eventmesh.cloudevents.v1.CloudEvent\032/.or" +
                "g.apache.eventmesh.cloudevents.v1.CloudE" +
                "ventBA\n)org.apache.eventmesh.connect.ope" +
                "nfunctionB\024EventMeshGrpcServiceb\006proto3"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
            new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                public com.google.protobuf.ExtensionRegistry assignDescriptors(
                    com.google.protobuf.Descriptors.FileDescriptor root) {
                    descriptor = root;
                    return null;
                }
            };
        com.google.protobuf.Descriptors.FileDescriptor
            .internalBuildGeneratedFileFrom(descriptorData,
                new com.google.protobuf.Descriptors.FileDescriptor[] {
                    org.apache.eventmesh.common.protocol.grpc.cloudevents.EventMeshCloudEvents.getDescriptor(),
                    com.google.protobuf.AnyProto.getDescriptor(),
                    com.google.protobuf.TimestampProto.getDescriptor(),
                }, assigner);
        org.apache.eventmesh.common.protocol.grpc.cloudevents.EventMeshCloudEvents.getDescriptor();
        com.google.protobuf.AnyProto.getDescriptor();
        com.google.protobuf.TimestampProto.getDescriptor();
    }

    // @@protoc_insertion_point(outer_class_scope)
}
