package org.acme.fruitconsumer.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.acme.fruitconsumer.rest.dto.Vote;
import org.acme.fruitconsumer.rest.dto.VoteSummary;

import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;

@Entity
@Table(name = "votes")
public class VoteEntity extends PanacheEntity {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fruit_id", nullable = false)
    public FruitEntity fruit;

    @NotBlank
    @Column(name = "voter_id", unique = true)
    public String voterId;

    @NotNull
    @Enumerated(STRING)
    public Channel channel;

    public enum Channel {
        REST, TWITTER, WEB
    }

    public static List<VoteSummary> summary(boolean byChannel) {
        var withChannel = byChannel ? ", channel" : "";

        return find("""
                select fruit.id, fruit.name, count(*) %s
                from VoteEntity v
                group by fruit.id, fruit.name %s
                """.formatted(withChannel, withChannel))
                .project(VoteSummary.class)
                .withHint(HINT_READONLY, true)
                .list();
    }

    public static List<Vote> allVotes() {
        return findAll(Sort.by("fruit"))
                .project(Vote.class)
                .withHint(HINT_READONLY, true)
                .list();
    }
}
