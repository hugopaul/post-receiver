package com.post.receiver.dto.webhook;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Aceita número ou string ("1") para IDs do WordPress.
 */
public class FlexibleLongDeserializer extends ValueDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NULL || token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getLongValue();
        }
        if (token == JsonToken.VALUE_STRING) {
            String raw = p.getValueAsString();
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return Long.valueOf(raw.trim());
        }
        return null;
    }
}
