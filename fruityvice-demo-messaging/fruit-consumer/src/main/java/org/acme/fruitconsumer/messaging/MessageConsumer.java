package org.acme.fruitconsumer.messaging;

import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.fruitconsumer.entities.FruitEntity;
import org.acme.fruitconsumer.rest.dto.Fruit;
import org.acme.fruitconsumer.rest.dto.Vote;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

/**
 * Consumes all types of messages from Artemis.
 */
@ApplicationScoped
public class MessageConsumer {

    @Blocking
    @Transactional
    @Incoming("fruit-in")
    public void consumeFruits(Fruit fruit) {
        FruitEntity.upsert(fruit);
    }

    @Blocking
    @Transactional
    @Incoming("vote-in")
    @Outgoing("vote-sse")
    public Vote consumeVotes(Vote vote) {
        vote.toEntity().persist();
        Log.infof("%s persisted", vote);
        return vote;
    }
}
