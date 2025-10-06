package org.acme.fruitivy.rest;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.acme.fruitivy.entities.FruitEntity;
import org.acme.fruitivy.rest.dto.Fruit;
import org.acme.fruitivy.rest.dto.FruitPOST;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.ThreadLocalRandom;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static jakarta.ws.rs.core.HttpHeaders.LOCATION;
import static org.acme.fruitivy.rest.RestAssuredUtil.get;
import static org.acme.fruitivy.rest.RestAssuredUtil.patch;
import static org.acme.fruitivy.rest.RestAssuredUtil.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesRegex;

@QuarkusTest
@ConnectWireMock
@TestHTTPEndpoint(FruitController.class)
class FruitControllerTest {

    @Test
    @TestTransaction
    @TestSecurity(user = "bob", roles = "admin")
    void saveFruit() {
        post(randomFruit())
                .assertThat()
                .statusCode(201)
                .header(LOCATION, matchesRegex(".*\\/fruits\\/\\d+"));

        var fruits = get(Fruit[].class);
        assertThat(fruits).isNotNull().hasSize(1).singleElement().hasNoNullFieldsOrPropertiesExcept("nutrition");
    }

    @ParameterizedTest
    @TestTransaction
    @TestSecurity(user = "bob", roles = "admin")
    @CsvSource(textBlock = """
            citrus, orange, 200
            pineapple, orange, 500
            """)
    void updateNutrition(String name, String family, int expectedStatus) {
        var fruit = new FruitPOST(name, family);
        post(fruit);
        var result = patch(fruit.name()).statusCode(expectedStatus);

        WireMock.verify(getRequestedFor(urlPathMatching(".*/api/fruit/%s.*".formatted(name))));
        if (expectedStatus == 200) {
            assertThat(result.extract().as(Fruit.class)).isNotNull().hasNoNullFieldsOrProperties();
        }
    }

    @Test
    void wrongAuthentication() {
        post(new FruitPOST("orange", "citrus")).statusCode(401);
    }

    @Test
    @TestTransaction
    void crudFromEntities() {
        var entity = new FruitEntity();
        entity.name = "orange";
        entity.family = "citrus";
        entity.persist();
        assertThat(entity.id).isPositive();
    }

    private static FruitPOST randomFruit() {
        var random = ThreadLocalRandom.current();
        var fruit = new FruitPOST("apple-%d".formatted(random.nextInt(1000)), "appolino");
        return fruit;
    }
}