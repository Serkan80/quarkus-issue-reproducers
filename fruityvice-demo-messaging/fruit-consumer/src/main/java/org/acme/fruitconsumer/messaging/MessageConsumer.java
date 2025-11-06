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
import org.hibernate.exception.ConstraintViolationException;

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
        try {
            vote.toEntity().persistAndFlush();
            Log.infof("%s persisted", vote);
            return vote;
        } catch (ConstraintViolationException e) {
            // skip duplicate votes (or non-existing fruit) without retrying
            return switch (e.getSQLState()) {
                case "23503", "23505" -> {
                    Log.warnf("Skipping duplicate vote or vote on non-existing fruit %s", vote);
                    yield null;
                }
                default -> e;
            };                      
        }
    }
}
