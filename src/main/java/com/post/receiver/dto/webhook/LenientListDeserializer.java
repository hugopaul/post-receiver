package com.post.receiver.dto.webhook;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * WordPress às vezes envia {@code false} no lugar de uma lista vazia (ex.: {@code "tags": false}).
 */
public class LenientListDeserializer extends ValueDeserializer<List<?>> {

    private final JavaType elementType;

    public LenientListDeserializer() {
        this(null);
    }

    public LenientListDeserializer(JavaType elementType) {
        this.elementType = elementType;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType listType = property.getType();
        JavaType contentType = listType.containedTypeCount() > 0 ? listType.containedType(0) : ctxt.constructType(Object.class);
        return new LenientListDeserializer(contentType);
    }

    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonToken token = p.currentToken();
        if (token != JsonToken.START_ARRAY) {
            return List.of();
        }
        List<Object> values = new ArrayList<>();
        JavaType itemType = elementType == null ? ctxt.constructType(Object.class) : elementType;
        while (p.nextToken() != JsonToken.END_ARRAY) {
            values.add(ctxt.readValue(p, itemType));
        }
        return values;
    }

    @Override
    public List<?> getNullValue(DeserializationContext ctxt) {
        return List.of();
    }
}
