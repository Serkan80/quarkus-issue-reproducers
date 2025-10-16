package org.acme.fruitproducer.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record Vote(
        @NotNull @Min(1) Long fruitId,
        @NotBlank @Length(min = 3, max = 100) String voterId,
        @NotNull Channel channel
) {
    public enum Channel {
        REST, TWITTER, WEB
    }
}
