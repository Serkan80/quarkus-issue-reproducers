package org.acme.fruitconsumer.rest.dto;

import org.acme.fruitconsumer.entities.VoteEntity.Channel;

public record VoteSummary(Long id, String fruit, Long count, Channel channel) {

    public VoteSummary(Long id, String fruit, Long count) {
        this(id, fruit, count, null);
    }
}
