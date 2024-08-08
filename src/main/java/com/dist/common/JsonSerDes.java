package com.dist.common;

import com.dist.net.InetAddressAndPort;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;
import java.util.List;


public class JsonSerDes {

    public static String toJson(Object obj) {
        var objectMapper = new ObjectMapper(new JsonFactory());
        try {
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return new String(objectMapper.writeValueAsBytes(obj));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(byte[] json, Class<T> clazz) {
        try {
            var objectMapper = new ObjectMapper(new JsonFactory());
            return readValue(json, clazz, objectMapper);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(byte[] json, TypeReference<T> typeRef) {
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            // Handle exceptions appropriately, e.g., throw a custom exception or log the error
            throw new RuntimeException("Error deserializing object", e);
        }
    }

    private static <T> T readValue(byte[] json, Class<T> clazz, ObjectMapper objectMapper) throws IOException {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT));
        var module = new SimpleModule();
        module.addKeyDeserializer(InetAddressAndPort.class, new InetAddressAndPortKeyDeserializer());
        objectMapper.registerModule(module);
        return objectMapper.readValue(json, clazz);
    }

    public static byte[] serialize(Object obj) {
        var objectMapper = new ObjectMapper(new CBORFactory());
        try {
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return objectMapper.writeValueAsBytes(obj);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(byte[] json, Class<T> clazz) {
        try {
            var objectMapper = new ObjectMapper(new CBORFactory());
            return readValue(json, clazz, objectMapper);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(String json, TypeReference<T> typeRef) {
        return deserialize(json.getBytes(), typeRef);
    }

    public static <T> T deserialize(byte[] json, TypeReference<T> typeRef) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            // Handle exceptions appropriately, e.g., throw a custom exception or log the error
            throw new RuntimeException("Error deserializing object", e);
        }
    }

    static class InetAddressAndPortKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext deserializationContext) throws IOException {
            if (key.startsWith("[") && key.endsWith("]")) {
                return InetAddressAndPort.parse(key);
            }

            throw new IllegalArgumentException(key + "is not valid InetAddressAndPort");
        }
    }
}
