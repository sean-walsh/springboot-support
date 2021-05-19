package com.akkaserverless.springboot.starter;

import com.google.protobuf.Descriptors;
import com.google.protobuf.EmptyProto;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity;

@EventSourcedEntity(entityType = "fake-eventsourced-entity")
public class FakeEventSourcedEntity {

    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor descriptor(){
        return EmptyProto.getDescriptor().getFile().findServiceByName("");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] additional(){
        return new Descriptors.FileDescriptor[]{ EmptyProto.getDescriptor().getFile()};
    }
}
