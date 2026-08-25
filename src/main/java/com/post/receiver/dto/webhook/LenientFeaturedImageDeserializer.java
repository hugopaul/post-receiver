package com.post.receiver.dto.webhook;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * WordPress pode enviar {@code false} quando o post não tem imagem destacada.
 */
public class LenientFeaturedImageDeserializer extends ValueDeserializer<FeaturedImage> {

    @Override
    public FeaturedImage deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NULL
                || token == JsonToken.VALUE_FALSE
                || token == JsonToken.VALUE_TRUE
                || token == JsonToken.VALUE_STRING) {
            return null;
        }
        if (token == JsonToken.START_OBJECT) {
            JsonNode node = ctxt.readTree(p);
            return ctxt.readTreeAsValue(node, FeaturedImage.class);
        }
        p.skipChildren();
        return null;
    }
}
