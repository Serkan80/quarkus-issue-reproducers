package org.acme.fruitconsumer.messaging;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.fruitconsumer.entities.FruitEntity;
import org.acme.fruitconsumer.rest.dto.Fruit;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class FruitConsumer {

    @Blocking
    @Transactional
    @Incoming("fruit-in")
    public void consume(Fruit fruit) {
        FruitEntity.upsert(fruit);
    }
}
