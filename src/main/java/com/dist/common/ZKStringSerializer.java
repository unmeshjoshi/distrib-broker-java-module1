package com.dist.common;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.io.UnsupportedEncodingException;

public class ZKStringSerializer implements ZkSerializer {
    @Override
    public byte[] serialize(Object data) {
        if (!(data instanceof String)) {
            throw new ZkMarshallingError("Expected a String object");
        }
        try {
            return ((String) data).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ZkMarshallingError("Error serializing data", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ZkMarshallingError("Error deserializing data", e);
        }
    }
}
