package org.acme.fruitproducer.rest;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.acme.fruitproducer.rest.dto.Fruit;
import org.acme.fruitproducer.rest.dto.Vote;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.acme.fruitproducer.RestAssuredUtil.post;
import static org.acme.fruitproducer.rest.dto.Vote.Channel.REST;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@ConnectWireMock
@TestHTTPEndpoint(FruitController.class)
class FruitControllerTest {

    @Any
    @Inject
    InMemoryConnector connector;

    @BeforeAll
    static void switchMyChannels() {
        InMemoryConnector.switchOutgoingChannelsToInMemory("fruit-out");
        InMemoryConnector.switchOutgoingChannelsToInMemory("vote-out");
    }

    @AfterAll
    static void revertMyChannels() {
        InMemoryConnector.clear();
    }

    @AfterEach
    void clearChannels() {
        this.connector.sink("fruit-out").clear();
        this.connector.sink("vote-out").clear();
    }

    @Test
    void wrongAuthentication() {
        post("/kiwi").statusCode(401);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            citrus, 200
            pineapple, 500
            """)
    @TestSecurity(user = "alice", roles = "admin")
    void sendFruit(String name, int expectedStatus) {
        // given
        var result = this.connector.sink("fruit-out");

        // when
        post(name).statusCode(expectedStatus);

        // then
        WireMock.verify(getRequestedFor(urlPathMatching("/api/fruit/%s".formatted(name))));
        if (expectedStatus == 200) {
            assertThat(result.received()).hasSize(1).extracting(msg -> (Fruit) msg.getPayload()).isNotNull();
        } else {
            assertThat(result.received()).isEmpty();
        }
    }

    @Test
    void sendVote() {
        // given
        var result = this.connector.sink("vote-out");

        // when
        post("/votes", new Vote(1L, "tester", REST)).statusCode(204);

        // then
        assertThat(result.received()).hasSize(1).extracting(msg -> (Vote) msg.getPayload()).isNotNull();
    }
}