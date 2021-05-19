package com.akkaserverless.springboot.starter;

import com.google.protobuf.Descriptors;
import com.google.protobuf.EmptyProto;

import com.akkaserverless.javasdk.replicatedentity.ReplicatedEntity;

@ReplicatedEntity
public class FakeCrdtEntity {

    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor descriptor(){
        return EmptyProto.getDescriptor().getFile().findServiceByName("");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] additional(){
        return new Descriptors.FileDescriptor[]{ EmptyProto.getDescriptor().getFile()};
    }
}
