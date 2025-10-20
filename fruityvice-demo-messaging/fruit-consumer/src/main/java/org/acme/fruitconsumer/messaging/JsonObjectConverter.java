package org.acme.fruitconsumer.messaging;

import io.smallrye.reactive.messaging.MessageConverter;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.lang.reflect.Type;

/**
 * Converts incoming {@link JsonObject} into the given Object type.
 */
@ApplicationScoped
public class JsonObjectConverter implements MessageConverter {

    @Override
    public boolean canConvert(Message<?> message, Type type) {
        return message.getPayload().getClass().equals(JsonObject.class)
               && !type.equals(JsonObject.class)
               && type instanceof Class<?>;
    }

    @Override
    public Message<?> convert(Message<?> message, Type type) {
        var json = (JsonObject) message.getPayload();
        if (type instanceof Class<?> clazz) {
            return message.withPayload(json.mapTo(clazz));
        }

        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}
