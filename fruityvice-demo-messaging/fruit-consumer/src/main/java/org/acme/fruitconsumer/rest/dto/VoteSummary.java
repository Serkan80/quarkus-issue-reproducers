package org.acme.fruitconsumer.rest.dto;

import org.acme.fruitconsumer.entities.VoteEntity.Channel;

public record VoteSummary(String fruit, Long count, Channel channel) {

    public VoteSummary(String fruit, Long count) {
        this(fruit, count, null);
    }
}
