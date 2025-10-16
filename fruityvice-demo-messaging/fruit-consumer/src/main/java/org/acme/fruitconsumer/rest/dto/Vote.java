package org.acme.fruitconsumer.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.acme.fruitconsumer.entities.FruitEntity;
import org.acme.fruitconsumer.entities.VoteEntity;
import org.acme.fruitconsumer.entities.VoteEntity.Channel;

public record Vote(
        @NotNull @Min(1) Long fruitId,
        @NotBlank String voterId,
        @NotNull Channel channel
) {

    public VoteEntity toEntity() {
        var result = new VoteEntity();
        result.fruit = FruitEntity.getEntityManager().getReference(FruitEntity.class, this.fruitId);
        result.voterId = this.voterId;
        result.channel = this.channel;
        return result;
    }
}
